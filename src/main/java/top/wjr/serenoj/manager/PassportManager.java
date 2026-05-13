package top.wjr.serenoj.manager;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.common.result.ResultStatus;
import top.wjr.serenoj.constant.RedisKeyConstant;
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
import top.wjr.serenoj.utils.RedisUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
@Slf4j(topic = "serenoj")
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

    @Autowired
    private RedisUtils redisUtils;

    @Value("${serenoj.web.register-email-verify:true}")
    private boolean registerEmailVerify;

    @Value("${serenoj.mail.verify-code-expire:300}")
    private long verifyCodeExpire;

    @Value("${serenoj.mail.reset-code-expire:600}")
    private long resetCodeExpire;

    @Value("${serenoj.mail.send-interval:60}")
    private long sendInterval;

    @Value("${serenoj.mail.code-length:6}")
    private int codeLength;

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

        UserInfoVO userInfoVO = buildUserInfoVO(user);
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

        String codeKey = RedisKeyConstant.REGISTER_CODE_PREFIX + registerDto.getEmail();
        if (registerEmailVerify && !verifyCode(codeKey, registerDto.getCode())) {
            return CommonResult.errorResponse("验证码错误或已过期");
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

        if (registerEmailVerify) {
            redisUtils.del(codeKey);
        }
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

        return CommonResult.successResponse(buildUserInfoVO(user));
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
        String username = dto.getUsername();
        String email = dto.getEmail();

        if (StrUtil.isBlank(username) && StrUtil.isBlank(email)) {
            return CommonResult.errorResponse("用户名和邮箱至少填写一个");
        }

        if (StrUtil.isNotBlank(username) && (username.length() < 4 || username.length() > 20)) {
            return CommonResult.errorResponse("用户名长度应为4-20位");
        }
        if (StrUtil.isNotBlank(email) && !Validator.isEmail(email)) {
            return CommonResult.errorResponse("邮箱格式错误");
        }

        CheckUsernameOrEmailVO vo = new CheckUsernameOrEmailVO();
        if (StrUtil.isNotBlank(username)) {
            vo.setUsernameExists(userInfoService.count(
                    new QueryWrapper<UserInfo>().eq("username", username)) > 0);
        }
        if (StrUtil.isNotBlank(email)) {
            vo.setEmailExists(userInfoService.count(
                    new QueryWrapper<UserInfo>().eq("email", email)) > 0);
        }
        return CommonResult.successResponse(vo);
    }

    public CommonResult<RegisterCodeVO> getRegisterCode(String email) {
        if (StrUtil.isBlank(email) || !Validator.isEmail(email)) {
            return CommonResult.errorResponse("邮箱格式错误");
        }

        long count = userInfoService.count(new QueryWrapper<UserInfo>().eq("email", email));
        if (count > 0) {
            return CommonResult.errorResponse("邮箱已被注册");
        }

        String limitKey = RedisKeyConstant.REGISTER_CODE_LIMIT_PREFIX + email;
        if (redisUtils.hasKey(limitKey)) {
            return CommonResult.errorResponse("验证码发送过于频繁，请稍后再试");
        }

        String code = generateCode();
        String codeKey = RedisKeyConstant.REGISTER_CODE_PREFIX + email;
        redisUtils.set(codeKey, code, verifyCodeExpire);
        redisUtils.set(limitKey, "1", sendInterval);
        log.info("Register verification code generated. email={}, code={}, expire={}s", email, code, verifyCodeExpire);

        RegisterCodeVO vo = new RegisterCodeVO();
        vo.setEmail(email);
        vo.setExpire((int) verifyCodeExpire);
        return CommonResult.successResponse(vo, "验证码已生成，请查看服务器日志");
    }

    public CommonResult<UserInfoVO> changeUserInfo(EditUserInfoDTO dto) {
        AccountProfile profile = getCurrentProfile();
        if (profile == null) {
            return CommonResult.errorResponse("请先登录", ResultStatus.ACCESS_DENIED);
        }
        String uid = profile.getUid();

        UpdateWrapper<UserInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uuid", uid);
        boolean hasUpdate = false;

        if (dto.getNickname() != null) { updateWrapper.set("nickname", dto.getNickname()); hasUpdate = true; }
        if (dto.getSchool() != null) { updateWrapper.set("school", dto.getSchool()); hasUpdate = true; }
        if (dto.getCourse() != null) { updateWrapper.set("course", dto.getCourse()); hasUpdate = true; }
        if (dto.getNumber() != null) { updateWrapper.set("number", dto.getNumber()); hasUpdate = true; }
        if (dto.getGender() != null) { updateWrapper.set("gender", dto.getGender()); hasUpdate = true; }
        if (dto.getRealname() != null) { updateWrapper.set("realname", dto.getRealname()); hasUpdate = true; }
        if (dto.getGithub() != null) { updateWrapper.set("github", dto.getGithub()); hasUpdate = true; }
        if (dto.getBlog() != null) { updateWrapper.set("blog", dto.getBlog()); hasUpdate = true; }
        if (dto.getAvatar() != null) { updateWrapper.set("avatar", dto.getAvatar()); hasUpdate = true; }
        if (dto.getSignature() != null) { updateWrapper.set("signature", dto.getSignature()); hasUpdate = true; }

        if (!hasUpdate) {
            return CommonResult.errorResponse("没有需要修改的内容");
        }

        userInfoService.update(updateWrapper);

        UserInfo updatedUser = userInfoService.getById(uid);
        return CommonResult.successResponse(buildUserInfoVO(updatedUser), "修改成功");
    }

    public CommonResult<Void> changePassword(ChangePasswordDTO dto) {
        AccountProfile profile = getCurrentProfile();
        if (profile == null) {
            return CommonResult.errorResponse("请先登录", ResultStatus.ACCESS_DENIED);
        }
        String uid = profile.getUid();

        UserInfo user = userInfoService.getById(uid);
        if (user == null) {
            return CommonResult.errorResponse("用户不存在");
        }

        if (!matchesPassword(dto.getOldPassword(), user)) {
            return CommonResult.errorResponse("旧密码错误");
        }

        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            return CommonResult.errorResponse("新密码不能与旧密码相同");
        }

        UpdateWrapper<UserInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uuid", uid).set("password", passwordEncoder.encode(dto.getNewPassword()));
        userInfoService.update(updateWrapper);

        jwtUtils.cleanToken(uid);
        return CommonResult.successResponse("密码修改成功，请重新登录");
    }

    public CommonResult<Void> applyResetPassword(ApplyResetPasswordDTO dto) {
        String email = dto.getEmail();
        if (!Validator.isEmail(email)) {
            return CommonResult.errorResponse("邮箱格式错误");
        }

        UserInfo user = userInfoService.getOne(new QueryWrapper<UserInfo>().eq("email", email));
        if (user == null) {
            return CommonResult.errorResponse("邮箱未注册");
        }

        String limitKey = RedisKeyConstant.RESET_PASSWORD_LIMIT_PREFIX + email;
        if (redisUtils.hasKey(limitKey)) {
            return CommonResult.errorResponse("验证码发送过于频繁，请稍后再试");
        }

        String code = generateCode();

        String codeKey = RedisKeyConstant.RESET_PASSWORD_CODE_PREFIX + email;
        redisUtils.set(codeKey, code, resetCodeExpire);
        redisUtils.set(limitKey, "1", sendInterval);
        log.info("Reset password code generated. email={}, code={}, expire={}s", email, code, resetCodeExpire);

        return CommonResult.successResponse("重置验证码已生成，请查看服务器日志");
    }

    public CommonResult<Void> resetPassword(ResetPasswordDTO dto) {
        String username = dto.getUsername();
        if (StrUtil.isBlank(username)) {
            return CommonResult.errorResponse("用户名不能为空");
        }

        UserInfo user = userInfoService.getOne(new QueryWrapper<UserInfo>().eq("username", username));
        if (user == null) {
            return CommonResult.errorResponse("用户不存在");
        }

        String codeKey = RedisKeyConstant.RESET_PASSWORD_CODE_PREFIX + user.getEmail();
        if (!verifyCode(codeKey, dto.getCode())) {
            return CommonResult.errorResponse("验证码错误或已过期");
        }

        UpdateWrapper<UserInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("uuid", user.getUuid()).set("password", passwordEncoder.encode(dto.getPassword()));
        userInfoService.update(updateWrapper);

        redisUtils.del(codeKey);
        jwtUtils.cleanToken(user.getUuid());
        return CommonResult.successResponse("密码重置成功，请重新登录");
    }

    private AccountProfile getCurrentProfile() {
        return (AccountProfile) SecurityUtils.getSubject().getPrincipal();
    }

    private UserInfoVO buildUserInfoVO(UserInfo user) {
        UserInfoVO vo = new UserInfoVO();
        BeanUtil.copyProperties(user, vo);
        vo.setUid(user.getUuid());
        List<Role> roles = userRoleService.getRolesByUid(user.getUuid());
        vo.setRoleList(roles.stream().map(Role::getRole).collect(Collectors.toList()));
        return vo;
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, codeLength);
        int min = (int) Math.pow(10, codeLength - 1);
        return String.valueOf(ThreadLocalRandom.current().nextInt(min, bound));
    }

    private boolean verifyCode(String key, String code) {
        Object savedCode = redisUtils.get(key);
        return savedCode != null && savedCode.toString().equalsIgnoreCase(code);
    }
}
