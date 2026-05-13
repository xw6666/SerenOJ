package top.wjr.serenoj.pojo.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class TagClassificationDTO {
    private Long id;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String oj;

    private Integer rank;
}
