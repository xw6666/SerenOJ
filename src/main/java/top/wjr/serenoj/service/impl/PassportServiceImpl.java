package top.wjr.serenoj.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.manager.PassportManager;
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

    @Override
    public CommonResult<CheckUsernameOrEmailVO> checkUsernameOrEmail(CheckUsernameOrEmailDTO dto) {
        return passportManager.checkUsernameOrEmail(dto);
    }

    @Override
    public CommonResult<RegisterCodeVO> getRegisterCode(String email) {
        return passportManager.getRegisterCode(email);
    }

    @Override
    public CommonResult<UserInfoVO> changeUserInfo(EditUserInfoDTO dto) {
        return passportManager.changeUserInfo(dto);
    }

    @Override
    public CommonResult<Void> changePassword(ChangePasswordDTO dto) {
        return passportManager.changePassword(dto);
    }

    @Override
    public CommonResult<Void> applyResetPassword(ApplyResetPasswordDTO dto) {
        return passportManager.applyResetPassword(dto);
    }

    @Override
    public CommonResult<Void> resetPassword(ResetPasswordDTO dto) {
        return passportManager.resetPassword(dto);
    }
}
