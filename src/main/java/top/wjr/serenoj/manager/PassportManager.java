package top.wjr.serenoj.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.pojo.dto.LoginDTO;
import top.wjr.serenoj.pojo.dto.RegisterDTO;
import top.wjr.serenoj.pojo.entity.Role;
import top.wjr.serenoj.pojo.entity.UserInfo;
import top.wjr.serenoj.pojo.entity.UserRole;
import top.wjr.serenoj.pojo.vo.UserInfoVO;
import top.wjr.serenoj.service.UserInfoService;
import top.wjr.serenoj.service.UserRoleService;
import top.wjr.serenoj.shiro.AccountProfile;
import top.wjr.serenoj.utils.JwtUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PassportManager {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private JwtUtils jwtUtils;

    public CommonResult<UserInfoVO> login(LoginDTO loginDto, HttpServletResponse response, HttpServletRequest request) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("username", loginDto.getUsername());
        UserInfo user = userInfoService.getOne(wrapper);
        if (user == null) {
            return CommonResult.errorResponse("用户名或密码错误");
        }
        if (!user.getPassword().equals(SecureUtil.md5(loginDto.getPassword()))) {
            return CommonResult.errorResponse("用户名或密码错误");
        }
        if (user.getStatus() == 1) {
            return CommonResult.errorResponse("该账户已被封禁，请联系管理员！");
        }
        
        String jwt = jwtUtils.generateToken(user.getUuid());
        response.setHeader("Authorization", jwt);
        response.setHeader("Access-Control-Expose-Headers", "Authorization");

        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtil.copyProperties(user, userInfoVO);
        userInfoVO.setUid(user.getUuid());
        
        List<Role> roles = userRoleService.getRolesByUid(user.getUuid());
        List<String> roleList = roles.stream().map(Role::getRole).collect(Collectors.toList());
        userInfoVO.setRoleList(roleList);
        
        return CommonResult.successResponse(userInfoVO, "登录成功");
    }

    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> register(RegisterDTO registerDto) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("username", registerDto.getUsername()).or().eq("email", registerDto.getEmail());
        long count = userInfoService.count(wrapper);
        if (count > 0) {
            return CommonResult.errorResponse("用户名或邮箱已被注册！");
        }
        
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(registerDto.getUsername());
        userInfo.setPassword(SecureUtil.md5(registerDto.getPassword()));
        userInfo.setEmail(registerDto.getEmail());
        userInfo.setNickname(registerDto.getUsername());
        userInfoService.save(userInfo);
        
        UserRole userRole = new UserRole();
        userRole.setUid(userInfo.getUuid());
        userRole.setRoleId(1002L); // 1002 is default_user
        userRoleService.save(userRole);
        
        return CommonResult.successResponse("注册成功");
    }

    public CommonResult<UserInfoVO> getUserInfo() {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        if (userRolesVo == null) {
            return CommonResult.errorResponse("请先登录");
        }
        
        UserInfo user = userInfoService.getById(userRolesVo.getUid());
        if (user == null) {
            return CommonResult.errorResponse("用户不存在");
        }

        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtil.copyProperties(user, userInfoVO);
        userInfoVO.setUid(user.getUuid());
        
        List<Role> roles = userRoleService.getRolesByUid(user.getUuid());
        List<String> roleList = roles.stream().map(Role::getRole).collect(Collectors.toList());
        userInfoVO.setRoleList(roleList);
        
        return CommonResult.successResponse(userInfoVO);
    }
    
    public CommonResult<Void> logout() {
        AccountProfile userRolesVo = (AccountProfile) SecurityUtils.getSubject().getPrincipal();
        if (userRolesVo != null) {
            jwtUtils.cleanToken(userRolesVo.getUid());
            SecurityUtils.getSubject().logout();
        }
        return CommonResult.successResponse("登出成功");
    }
}
