package top.wjr.serenoj.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class ProblemDetailVO {
    private Object problem;
    private Object problemCount;
    private List<?> tags;
    private List<String> languages;
    private Object codeTemplate;
}
