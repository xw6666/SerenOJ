package top.wjr.serenoj.pojo.vo;

import lombok.Data;

@Data
public class CheckUsernameOrEmailVO {
    private Boolean usernameExists;
    private Boolean emailExists;
}
