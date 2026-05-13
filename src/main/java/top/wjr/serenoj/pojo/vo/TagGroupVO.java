package top.wjr.serenoj.pojo.vo;

import lombok.Data;
import top.wjr.serenoj.pojo.entity.Tag;
import top.wjr.serenoj.pojo.entity.TagClassification;

import java.util.List;

@Data
public class TagGroupVO {
    private TagClassification classification;
    private List<Tag> tagList;
}
