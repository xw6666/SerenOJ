package top.wjr.serenoj.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.pojo.dto.ApplyResetPasswordDTO;
import top.wjr.serenoj.pojo.dto.ChangePasswordDTO;
import top.wjr.serenoj.pojo.dto.CheckUsernameOrEmailDTO;
import top.wjr.serenoj.pojo.dto.EditUserInfoDTO;
import top.wjr.serenoj.pojo.dto.LoginDTO;
import top.wjr.serenoj.pojo.dto.RegisterDTO;
import top.wjr.serenoj.pojo.dto.ResetPasswordDTO;
import top.wjr.serenoj.pojo.entity.Role;
import top.wjr.serenoj.pojo.entity.UserInfo;
import top.wjr.serenoj.pojo.entity.UserRole;
import top.wjr.serenoj.pojo.vo.CheckUsernameOrEmailVO;
import top.wjr.serenoj.pojo.vo.RegisterCodeVO;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${serenoj.web.register:true}")
    private boolean registerEnabled;

    public CommonResult<UserInfoVO> login(LoginDTO loginDto, HttpServletResponse response, HttpServletRequest request) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("username", loginDto.getUsername());
        UserInfo user = userInfoService.getOne(wrapper);
        if (user == null) {
            return CommonResult.errorResponse("用户名或密码错误");
        }
        if (!matchesPassword(loginDto.getPassword(), user)) {
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
        if (!registerEnabled) {
            return CommonResult.errorResponse("当前系统暂未开放注册！");
        }

        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("username", registerDto.getUsername()).or().eq("email", registerDto.getEmail());
        long count = userInfoService.count(wrapper);
        if (count > 0) {
            return CommonResult.errorResponse("用户名或邮箱已被注册！");
        }
        
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(registerDto.getUsername());
        userInfo.setPassword(passwordEncoder.encode(registerDto.getPassword()));
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

    private boolean matchesPassword(String rawPassword, UserInfo user) {
        String storedPassword = user.getPassword();
        if (storedPassword == null) {
            return false;
        }

        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        boolean legacyMd5Matched = storedPassword.equals(SecureUtil.md5(rawPassword));
        if (legacyMd5Matched) {
            UpdateWrapper<UserInfo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("uuid", user.getUuid()).set("password", passwordEncoder.encode(rawPassword));
            userInfoService.update(updateWrapper);
        }
        return legacyMd5Matched;
    }

    public CommonResult<CheckUsernameOrEmailVO> checkUsernameOrEmail(CheckUsernameOrEmailDTO dto) {
        return CommonResult.errorResponse("TODO: implement checkUsernameOrEmail");
    }

    public CommonResult<RegisterCodeVO> getRegisterCode(String email) {
        return CommonResult.errorResponse("TODO: implement getRegisterCode");
    }

    public CommonResult<UserInfoVO> changeUserInfo(EditUserInfoDTO dto) {
        return CommonResult.errorResponse("TODO: implement changeUserInfo");
    }

    public CommonResult<Void> changePassword(ChangePasswordDTO dto) {
        return CommonResult.errorResponse("TODO: implement changePassword");
    }

    public CommonResult<Void> applyResetPassword(ApplyResetPasswordDTO dto) {
        return CommonResult.errorResponse("TODO: implement applyResetPassword");
    }

    public CommonResult<Void> resetPassword(ResetPasswordDTO dto) {
        return CommonResult.errorResponse("TODO: implement resetPassword");
    }
}
