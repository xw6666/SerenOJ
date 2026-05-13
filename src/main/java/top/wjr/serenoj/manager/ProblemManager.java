package top.wjr.serenoj.manager;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.wjr.serenoj.pojo.entity.Language;
import top.wjr.serenoj.service.LanguageService;

import java.util.List;

@Component
public class ProblemManager {

    @Autowired
    private LanguageService languageService;

    public List<Language> getLanguages(Long pid, Boolean all) {
        QueryWrapper<Language> wrapper = new QueryWrapper<>();
        // In Phase 3, we just return all ME languages unless otherwise required
        wrapper.eq("oj", "ME");
        wrapper.orderByAsc("seq", "id");
        return languageService.list(wrapper);
    }
}
