package top.wjr.serenoj.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Problem implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String problemId;
    private String title;
    private String author;
    private Integer type;
    private Integer timeLimit;
    private Integer memoryLimit;
    private Integer stackLimit;
    private String description;
    private String input;
    private String output;
    private String examples;
    private String source;
    private Integer difficulty;
    private String hint;
    private Integer auth;
    private Integer ioScore;
    private Boolean codeShare;
    private String judgeMode;
    private String judgeCaseMode;
    private String userExtraFile;
    private String judgeExtraFile;
    private String spjCode;
    private String spjLanguage;
    private Boolean isRemoveEndBlank;
    private Boolean openCaseResult;
    private Boolean isUploadCase;
    private String caseVersion;
    private String modifiedUser;
    private Boolean isFileIo;
    private String ioReadFileName;
    private String ioWriteFileName;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;
}
