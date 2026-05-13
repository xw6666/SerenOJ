package top.wjr.serenoj.pojo.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class TagDTO {
    private Long id;

    @NotBlank(message = "标签名称不能为空")
    private String name;

    private String color;

    private String oj;

    private Long tcid;
}
