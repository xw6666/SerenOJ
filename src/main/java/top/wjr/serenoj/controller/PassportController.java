package top.wjr.serenoj.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wjr.serenoj.annotation.AnonApi;
import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.pojo.dto.LoginDTO;
import top.wjr.serenoj.pojo.dto.RegisterDTO;
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
}
