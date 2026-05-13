package top.wjr.serenoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.wjr.serenoj.mapper.CodeTemplateMapper;
import top.wjr.serenoj.pojo.entity.CodeTemplate;
import top.wjr.serenoj.service.CodeTemplateService;

@Service
public class CodeTemplateServiceImpl extends ServiceImpl<CodeTemplateMapper, CodeTemplate> implements CodeTemplateService {
}
