package top.wjr.serenoj.shiro;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.support.RequestContextUtils;
import top.wjr.serenoj.annotation.AnonApi;
import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.common.result.ResultStatus;
import top.wjr.serenoj.utils.JwtUtils;
import top.wjr.serenoj.utils.RedisUtils;
import top.wjr.serenoj.utils.ServiceContextUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j(topic = "serenoj")
public class JwtFilter extends AuthenticatingFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        WebUtils.saveRequest(httpRequest);
        WebApplicationContext ctx = RequestContextUtils.findWebApplicationContext(httpRequest);
        RequestMappingHandlerMapping mapping = ctx.getBean(
                "requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        try {
            HandlerExecutionChain handler = mapping.getHandler(httpRequest);
            HandlerMethod handlerClazz = (HandlerMethod) handler.getHandler();
            AnonApi anonApi = ServiceContextUtils.getAnnotation(handlerClazz.getMethod(),
                    handlerClazz.getBeanType(),
                    AnonApi.class);
            if (anonApi != null) {
                String jwt = httpRequest.getHeader("Authorization");
                if (StrUtil.isNotBlank(jwt)) {
                    try {
                        Claims claim = jwtUtils.getClaimByToken(jwt);
                        if (claim == null || jwtUtils.isTokenExpired(claim.getExpiration())) {
                            return true;
                        }
                        String userId = claim.getSubject();
                        boolean hasToken = jwtUtils.hasToken(userId);
                        if (!hasToken) {
                            return true;
                        }
                        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
                        if (userRolesVo == null) {
                            JwtToken jwtToken = new JwtToken(jwt);
                            SecurityUtils.getSubject().login(jwtToken);
                        }
                    } catch (Exception ignored) {
                    }
                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = request.getHeader("Authorization");
        if (StrUtil.isBlank(jwt)) {
            return null;
        }
        return new JwtToken(jwt);
    }


    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = request.getHeader("Authorization");
        if (StrUtil.isBlank(token)) {
            return true;
        } else {
            Claims claim = jwtUtils.getClaimByToken(token);
            if (claim == null || jwtUtils.isTokenExpired(claim.getExpiration())) {
                return this.onLoginFailure(null,
                        new AuthenticationException("登录状态已失效，请重新登录！"), servletRequest, servletResponse);
            }
            String userId = claim.getSubject();

            boolean hasToken = jwtUtils.hasToken(userId);
            if (!hasToken) {
                return this.onLoginFailure(null,
                        new AuthenticationException("登录状态已失效，请重新登录！"), servletRequest, servletResponse);
            }
            if (!redisUtils.hasKey(ShiroConstant.SHIRO_TOKEN_REFRESH + userId)) {
                HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
                HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
                this.refreshToken(httpRequest, httpResponse, userId);
            }
        }
        return executeLogin(servletRequest, servletResponse);
    }

    private void refreshToken(HttpServletRequest request, HttpServletResponse response, String userId) throws IOException {
        String requestId = UUID.randomUUID().toString();
        boolean locked = redisUtils.getLock(ShiroConstant.SHIRO_TOKEN_LOCK + userId, 20, requestId);
        if (locked) {
            String newToken = jwtUtils.generateToken(userId);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Authorization", newToken); 
            response.setHeader("Access-Control-Expose-Headers", "Refresh-Token,Authorization,Url-Type"); 
            response.setHeader("Url-Type", request.getHeader("Url-Type")); 
            response.setHeader("Refresh-Token", "true"); 
        }
        redisUtils.releaseLock(ShiroConstant.SHIRO_TOKEN_LOCK + userId, requestId);
    }


    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        returnErrorResponse(request, response, e, ResultStatus.ACCESS_DENIED);
        return false;
    }

    private void returnErrorResponse(ServletRequest request, ServletResponse response, Exception e, ResultStatus resultStatus) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        try {
            Throwable throwable = e.getCause() == null ? e : e.getCause();
            CommonResult<Void> result = CommonResult.errorResponse(throwable.getMessage(), resultStatus);
            String json = JSONUtil.toJsonStr(result);
            httpResponse.setContentType("application/json;charset=utf-8");
            httpResponse.setHeader("Access-Control-Expose-Headers", "Refresh-Token,Authorization,Url-Type"); 
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
            httpResponse.setHeader("Url-Type", httpRequest.getHeader("Url-Type")); 
            httpResponse.setStatus(resultStatus.getStatus());
            httpResponse.getWriter().print(json);
        } catch (IOException e1) {
        }
    }

    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        httpServletResponse.setHeader("Access-Control-Expose-Headers",
                "Refresh-Token,Authorization,Url-Type,Content-disposition,Content-Type"); 
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(org.springframework.http.HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }
}