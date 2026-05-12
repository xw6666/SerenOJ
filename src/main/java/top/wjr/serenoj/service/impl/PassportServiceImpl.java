package top.wjr.serenoj.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.manager.PassportManager;
import top.wjr.serenoj.pojo.dto.LoginDTO;
import top.wjr.serenoj.pojo.dto.RegisterDTO;
import top.wjr.serenoj.pojo.vo.UserInfoVO;
import top.wjr.serenoj.service.PassportService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class PassportServiceImpl implements PassportService {

    @Autowired
    private PassportManager passportManager;

    @Override
    public CommonResult<UserInfoVO> login(LoginDTO loginDto, HttpServletResponse response, HttpServletRequest request) {
        return passportManager.login(loginDto, response, request);
    }

    @Override
    public CommonResult<Void> register(RegisterDTO registerDto) {
        return passportManager.register(registerDto);
    }

    @Override
    public CommonResult<UserInfoVO> getUserInfo() {
        return passportManager.getUserInfo();
    }

    @Override
    public CommonResult<Void> logout() {
        return passportManager.logout();
    }
}
