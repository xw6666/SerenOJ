package top.wjr.serenoj.pojo.dto;

import lombok.Data;
import javax.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

@Data
public class EditUserInfoDTO {
    @Length(max = 20, message = "昵称长度不能超过20")
    private String nickname;

    @Length(max = 100, message = "学校长度不能超过100")
    private String school;

    @Length(max = 100, message = "专业长度不能超过100")
    private String course;

    @Length(max = 20, message = "学号长度不能超过20")
    private String number;

    @Pattern(regexp = "^(male|female|secrecy)$", message = "性别参数错误")
    private String gender;

    @Length(max = 100, message = "真实姓名长度不能超过100")
    private String realname;

    @Length(max = 255, message = "GitHub 地址长度不能超过255")
    private String github;

    @Length(max = 255, message = "博客地址长度不能超过255")
    private String blog;

    @Length(max = 255, message = "头像地址长度不能超过255")
    private String avatar;

    @Length(max = 255, message = "个性签名长度不能超过255")
    private String signature;
}
