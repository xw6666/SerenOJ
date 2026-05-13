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
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "uuid", type = IdType.ASSIGN_UUID)
    private String uuid;

    private String username;

    private String password;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String nickname;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String school;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String course;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String number;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String gender;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String realname;

    @TableField(exist = false)
    private String cfUsername;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String github;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String blog;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String email;

    private String avatar;

    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private String signature;

    private String titleName;

    private String titleColor;

    private int status;

    @TableField(fill = FieldFill.INSERT)
    private Date gmtCreate;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date gmtModified;
}
