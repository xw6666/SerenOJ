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
import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.common.result.ResultStatus;
import top.wjr.serenoj.utils.JwtUtils;
import top.wjr.serenoj.utils.RedisUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j(topic = "serenoj")
public class JwtFilter extends AuthenticatingFilter {

    private static final String DEFAULT_AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final Set<String> ANON_API_PATHS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "/api/login",
            "/api/register"
    )));

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        WebUtils.saveRequest(httpRequest);
        if (!isAnonApi(httpRequest)) {
            return false;
        }

        String jwt = resolveToken(httpRequest);
        if (StrUtil.isBlank(jwt)) {
            return true;
        }

        try {
            Claims claim = jwtUtils.getClaimByToken(jwt);
            if (claim == null || jwtUtils.isTokenExpired(claim.getExpiration())) {
                return true;
            }
            String userId = claim.getSubject();
            if (!jwtUtils.hasToken(userId, jwt)) {
                return true;
            }
            if (SecurityUtils.getSubject().getPrincipal() == null) {
                SecurityUtils.getSubject().login(new JwtToken(jwt));
            }
        } catch (Exception e) {
            log.debug("Skip optional jwt login for anon api: {}", httpRequest.getRequestURI(), e);
        }
        return true;
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = resolveToken(request);
        if (StrUtil.isBlank(jwt)) {
            return null;
        }
        return new JwtToken(jwt);
    }


    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String token = resolveToken(request);
        if (StrUtil.isBlank(token)) {
            return this.onLoginFailure(null,
                    new AuthenticationException("请先登录"), servletRequest, servletResponse);
        } else {
            Claims claim = jwtUtils.getClaimByToken(token);
            if (claim == null || jwtUtils.isTokenExpired(claim.getExpiration())) {
                return this.onLoginFailure(null,
                        new AuthenticationException("登录状态已失效，请重新登录！"), servletRequest, servletResponse);
            }
            String userId = claim.getSubject();

            boolean hasToken = jwtUtils.hasToken(userId, token);
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

    private boolean isAnonApi(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (StrUtil.isNotBlank(contextPath) && requestUri.startsWith(contextPath)) {
            requestUri = requestUri.substring(contextPath.length());
        }
        return ANON_API_PATHS.contains(requestUri);
    }

    private String resolveToken(HttpServletRequest request) {
        String headerName = StrUtil.blankToDefault(jwtUtils.getHeader(), DEFAULT_AUTHORIZATION_HEADER);
        String token = request.getHeader(headerName);
        if (StrUtil.isBlank(token) && !DEFAULT_AUTHORIZATION_HEADER.equalsIgnoreCase(headerName)) {
            token = request.getHeader(DEFAULT_AUTHORIZATION_HEADER);
        }
        if (StrUtil.isBlank(token)) {
            return null;
        }

        token = token.trim();
        if (StrUtil.startWithIgnoreCase(token, BEARER_PREFIX)) {
            token = token.substring(BEARER_PREFIX.length()).trim();
        }
        return StrUtil.isBlank(token) ? null : token;
    }

    private void refreshToken(HttpServletRequest request, HttpServletResponse response, String userId) throws IOException {
        String requestId = UUID.randomUUID().toString();
        boolean locked = redisUtils.getLock(ShiroConstant.SHIRO_TOKEN_LOCK + userId, 20, requestId);
        try {
            if (locked) {
                String newToken = jwtUtils.generateToken(userId);
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Authorization", newToken);
                response.setHeader("Access-Control-Expose-Headers", "Refresh-Token,Authorization,Url-Type");
                response.setHeader("Url-Type", request.getHeader("Url-Type"));
                response.setHeader("Refresh-Token", "true");
            }
        } finally {
            if (locked) {
                redisUtils.releaseLock(ShiroConstant.SHIRO_TOKEN_LOCK + userId, requestId);
            }
        }
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
