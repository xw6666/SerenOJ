package top.wjr.serenoj.pojo.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

@Data
public class ResetPasswordDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "验证码不能为空")
    @Length(min = 6, max = 6, message = "验证码长度应为6位")
    private String code;

    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度应该在6-20之间")
    private String password;
}
