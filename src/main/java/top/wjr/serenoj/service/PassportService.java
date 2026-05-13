package top.wjr.serenoj.service;

import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.pojo.dto.*;
import top.wjr.serenoj.pojo.vo.CheckUsernameOrEmailVO;
import top.wjr.serenoj.pojo.vo.RegisterCodeVO;
import top.wjr.serenoj.pojo.vo.UserInfoVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface PassportService {
    CommonResult<UserInfoVO> login(LoginDTO loginDto, HttpServletResponse response, HttpServletRequest request);
    CommonResult<Void> register(RegisterDTO registerDto);
    CommonResult<UserInfoVO> getUserInfo();
    CommonResult<Void> logout();
    CommonResult<CheckUsernameOrEmailVO> checkUsernameOrEmail(CheckUsernameOrEmailDTO dto);
    CommonResult<RegisterCodeVO> getRegisterCode(String email);
    CommonResult<UserInfoVO> changeUserInfo(EditUserInfoDTO dto);
    CommonResult<Void> changePassword(ChangePasswordDTO dto);
    CommonResult<Void> applyResetPassword(ApplyResetPasswordDTO dto);
    CommonResult<Void> resetPassword(ResetPasswordDTO dto);
}
