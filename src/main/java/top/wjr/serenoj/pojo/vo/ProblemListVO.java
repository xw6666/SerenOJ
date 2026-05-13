package top.wjr.serenoj.pojo.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ProblemListVO {
    private Long id;
    private Long pid;
    private String problemId;
    private String title;
    private String author;
    private Integer difficulty;
    private Integer type;
    private Integer auth;
    private Date gmtCreate;
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
    private List<TagVO> tagList;
}
