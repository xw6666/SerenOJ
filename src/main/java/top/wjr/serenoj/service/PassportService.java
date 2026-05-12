package top.wjr.serenoj.service;

import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.pojo.dto.LoginDTO;
import top.wjr.serenoj.pojo.dto.RegisterDTO;
import top.wjr.serenoj.pojo.vo.UserInfoVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface PassportService {
    CommonResult<UserInfoVO> login(LoginDTO loginDto, HttpServletResponse response, HttpServletRequest request);
    CommonResult<Void> register(RegisterDTO registerDto);
    CommonResult<UserInfoVO> getUserInfo();
    CommonResult<Void> logout();
}
