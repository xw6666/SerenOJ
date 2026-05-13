package top.wjr.serenoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.wjr.serenoj.mapper.LanguageMapper;
import top.wjr.serenoj.pojo.entity.Language;
import top.wjr.serenoj.service.LanguageService;

@Service
public class LanguageServiceImpl extends ServiceImpl<LanguageMapper, Language> implements LanguageService {
}
