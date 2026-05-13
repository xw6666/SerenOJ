package top.wjr.serenoj.manager;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.wjr.serenoj.pojo.entity.Tag;
import top.wjr.serenoj.pojo.entity.TagClassification;
import top.wjr.serenoj.pojo.vo.TagGroupVO;
import top.wjr.serenoj.service.TagClassificationService;
import top.wjr.serenoj.service.TagService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TagManager {

    @Autowired
    private TagService tagService;

    @Autowired
    private TagClassificationService tagClassificationService;

    public List<TagGroupVO> getTagList(String oj) {
        // Query Classifications
        QueryWrapper<TagClassification> tcw = new QueryWrapper<>();
        if (oj != null && !oj.isEmpty() && !"All".equalsIgnoreCase(oj)) {
            tcw.eq("oj", oj);
        }
        tcw.orderByAsc("`rank`", "id");
        List<TagClassification> classifications = tagClassificationService.list(tcw);

        // Query Tags
        QueryWrapper<Tag> tw = new QueryWrapper<>();
        if (oj != null && !oj.isEmpty() && !"All".equalsIgnoreCase(oj)) {
            tw.eq("oj", oj);
        }
        tw.orderByAsc("id");
        List<Tag> tags = tagService.list(tw);

        // Group tags by tcid
        Map<Long, List<Tag>> tagsByTcid = tags.stream()
                .filter(t -> t.getTcid() != null)
                .collect(Collectors.groupingBy(Tag::getTcid));

        List<Tag> unclassifiedTags = tags.stream()
                .filter(t -> t.getTcid() == null)
                .collect(Collectors.toList());

        List<TagGroupVO> result = new ArrayList<>();

        for (TagClassification classification : classifications) {
            TagGroupVO vo = new TagGroupVO();
            vo.setClassification(classification);
            vo.setTagList(tagsByTcid.getOrDefault(classification.getId(), new ArrayList<>()));
            result.add(vo);
        }

        if (!unclassifiedTags.isEmpty()) {
            TagGroupVO unclassifiedVo = new TagGroupVO();
            unclassifiedVo.setClassification(null);
            unclassifiedVo.setTagList(unclassifiedTags);
            result.add(unclassifiedVo);
        }

        return result;
    }

    public List<Tag> getAllProblemTagList(String oj) {
        QueryWrapper<Tag> tw = new QueryWrapper<>();
        if (oj != null && !oj.isEmpty() && !"ALL".equalsIgnoreCase(oj) && !"All".equalsIgnoreCase(oj)) {
            tw.eq("oj", oj);
        }
        tw.orderByAsc("id");
        return tagService.list(tw);
    }

    public List<TagClassification> getTagClassification(String oj) {
        QueryWrapper<TagClassification> tcw = new QueryWrapper<>();
        if (oj != null && !oj.isEmpty() && !"ALL".equalsIgnoreCase(oj) && !"All".equalsIgnoreCase(oj)) {
            tcw.eq("oj", oj);
        }
        tcw.orderByAsc("`rank`", "id");
        return tagClassificationService.list(tcw);
    }
}
