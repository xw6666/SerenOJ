package top.wjr.serenoj.pojo.dto;

import lombok.Data;

@Data
public class AdminProblemListQueryDTO {
    private Integer currentPage;
    private Integer page;
    private Integer limit;
    private String keyword;
    private Integer auth;
    private String oj;
    private Long cid;

    public Integer getPage() {
        if (page != null) {
            return page;
        }
        return currentPage != null ? currentPage : 1;
    }
}
