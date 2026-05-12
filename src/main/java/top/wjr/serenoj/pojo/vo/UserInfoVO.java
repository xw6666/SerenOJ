package top.wjr.serenoj.pojo.vo;

import lombok.Data;
import java.util.List;

@Data
public class UserInfoVO {
    private String uid;
    private String username;
    private String nickname;
    private String avatar;
    private String titleName;
    private String titleColor;
    private String email;
    private String signature;
    private String github;
    private String blog;
    private String cfUsername;
    private List<String> roleList;
}
