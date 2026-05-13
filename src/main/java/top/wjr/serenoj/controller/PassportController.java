package top.wjr.serenoj.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wjr.serenoj.annotation.AnonApi;
import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.pojo.dto.ApplyResetPasswordDTO;
import top.wjr.serenoj.pojo.dto.ChangePasswordDTO;
import top.wjr.serenoj.pojo.dto.CheckUsernameOrEmailDTO;
import top.wjr.serenoj.pojo.dto.EditUserInfoDTO;
import top.wjr.serenoj.pojo.dto.LoginDTO;
import top.wjr.serenoj.pojo.dto.RegisterDTO;
import top.wjr.serenoj.pojo.dto.ResetPasswordDTO;
import top.wjr.serenoj.pojo.vo.CheckUsernameOrEmailVO;
import top.wjr.serenoj.pojo.vo.RegisterCodeVO;
import top.wjr.serenoj.pojo.vo.UserInfoVO;
import top.wjr.serenoj.service.PassportService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
public class PassportController {

    @Autowired
    private PassportService passportService;

    @AnonApi
    @PostMapping("/login")
    public CommonResult<UserInfoVO> login(@Validated @RequestBody LoginDTO loginDto, HttpServletResponse response, HttpServletRequest request) {
        return passportService.login(loginDto, response, request);
    }

    @AnonApi
    @PostMapping("/register")
    public CommonResult<Void> register(@Validated @RequestBody RegisterDTO registerDto) {
        return passportService.register(registerDto);
    }

    @GetMapping("/get-user-info")
    public CommonResult<UserInfoVO> getUserInfo() {
        return passportService.getUserInfo();
    }
    
    @GetMapping("/logout")
    public CommonResult<Void> logout() {
        return passportService.logout();
    }

    @AnonApi
    @PostMapping("/check-username-or-email")
    public CommonResult<CheckUsernameOrEmailVO> checkUsernameOrEmail(
            @RequestBody CheckUsernameOrEmailDTO dto) {
        return passportService.checkUsernameOrEmail(dto);
    }

    @AnonApi
    @GetMapping("/get-register-code")
    public CommonResult<RegisterCodeVO> getRegisterCode(@RequestParam String email) {
        return passportService.getRegisterCode(email);
    }

    @PostMapping("/change-userInfo")
    public CommonResult<UserInfoVO> changeUserInfo(@Validated @RequestBody EditUserInfoDTO dto) {
        return passportService.changeUserInfo(dto);
    }

    @PostMapping("/change-password")
    public CommonResult<Void> changePassword(@Validated @RequestBody ChangePasswordDTO dto) {
        return passportService.changePassword(dto);
    }

    @AnonApi
    @PostMapping("/apply-reset-password")
    public CommonResult<Void> applyResetPassword(@Validated @RequestBody ApplyResetPasswordDTO dto) {
        return passportService.applyResetPassword(dto);
    }

    @AnonApi
    @PostMapping("/reset-password")
    public CommonResult<Void> resetPassword(@Validated @RequestBody ResetPasswordDTO dto) {
        return passportService.resetPassword(dto);
    }
}
