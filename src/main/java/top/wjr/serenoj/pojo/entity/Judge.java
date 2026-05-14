package top.wjr.serenoj.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Judge implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "submit_id", type = IdType.AUTO)
    private Long submitId;

    private Long pid;
    private String displayPid;
    private String uid;
    private String username;
    private Date submitTime;
    private Integer status;
    private Boolean share;
    private String errorMessage;
    private Integer time;
    private Integer memory;
    private Integer score;
    private Integer length;
    private String code;
    private String language;
    private Long cid;
    private Long cpid;
    private String judger;
    private String ip;
    private Integer oiRankScore;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;
}
