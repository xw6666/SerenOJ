package top.wjr.serenoj.pojo.dto;

import lombok.Data;

@Data
public class ProblemListQueryDTO {
    private Integer currentPage;
    private Integer page;
    private Integer limit;
    private String keyword;
    private String difficulty;
    private String tagId;
    private String oj;
    private String type;

    public Integer getPage() {
        if (page != null) return page;
        return currentPage != null ? currentPage : 1;
    }
}
