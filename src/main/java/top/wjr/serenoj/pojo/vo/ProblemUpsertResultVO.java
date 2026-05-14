package top.wjr.serenoj.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemUpsertResultVO {
    private Long pid;
    private Long id;
    private String problemId;
}
