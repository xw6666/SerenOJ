package top.wjr.serenoj.pojo.dto;

import lombok.Data;
import top.wjr.serenoj.pojo.entity.CodeTemplate;
import top.wjr.serenoj.pojo.entity.Language;
import top.wjr.serenoj.pojo.entity.Problem;
import top.wjr.serenoj.pojo.entity.ProblemCase;
import top.wjr.serenoj.pojo.entity.Tag;

import java.util.List;

@Data
public class ProblemUpsertDTO {
    private Problem problem;
    private List<Tag> tags;
    private List<Language> languages;
    private List<CodeTemplate> codeTemplates;
    private Boolean isUploadTestCase;
    private String uploadTestcaseDir;
    private String judgeMode;
    private List<ProblemCase> samples;
    private Boolean changeJudgeCaseMode;
    private Boolean changeModeCode;
}
