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
public class ProblemCount implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "pid", type = IdType.INPUT)
    private Long pid;
    private Integer total;
    private Integer ac;
    private Integer wa;
    private Integer tle;
    private Integer mle;
    private Integer re;
    private Integer pe;
    private Integer ce;
    private Integer se;
    private Integer pa;
    
    @Version
    private Long version;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;
}
