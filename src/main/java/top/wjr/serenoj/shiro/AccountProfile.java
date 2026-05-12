package top.wjr.serenoj.shiro;

import lombok.Data;
import java.io.Serializable;

@Data
public class AccountProfile implements Serializable {

    private String uid;

    private String username;

    private String nickname;

    private String realname;

    private String titleName;

    private String titleColor;

    private String avatar;

    private int status;

    public String getId() {
        return uid;
    }
}