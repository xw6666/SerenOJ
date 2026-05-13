package top.wjr.serenoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.wjr.serenoj.mapper.TagClassificationMapper;
import top.wjr.serenoj.pojo.entity.TagClassification;
import top.wjr.serenoj.service.TagClassificationService;

@Service
public class TagClassificationServiceImpl extends ServiceImpl<TagClassificationMapper, TagClassification> implements TagClassificationService {
}
