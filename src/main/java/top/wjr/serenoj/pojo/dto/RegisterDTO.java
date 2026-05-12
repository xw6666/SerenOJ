package top.wjr.serenoj.pojo.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Email;
import org.hibernate.validator.constraints.Length;

@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Length(min = 4, max = 20, message = "用户名长度应该在4-20之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 20, message = "密码长度应该在6-20之间")
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    private String email;
}
