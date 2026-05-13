package top.wjr.serenoj.manager;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.wjr.serenoj.pojo.dto.ProblemListQueryDTO;
import top.wjr.serenoj.pojo.entity.*;
import top.wjr.serenoj.pojo.vo.PageVO;
import top.wjr.serenoj.pojo.vo.ProblemDetailVO;
import top.wjr.serenoj.pojo.vo.ProblemListVO;
import top.wjr.serenoj.pojo.vo.TagVO;
import top.wjr.serenoj.service.*;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProblemManager {

    @Autowired
    private LanguageService languageService;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private ProblemCountService problemCountService;

    @Autowired
    private ProblemTagService problemTagService;

    @Autowired
    private TagService tagService;

    @Autowired
    private ProblemLanguageService problemLanguageService;

    @Autowired
    private CodeTemplateService codeTemplateService;

    @Autowired
    private ProblemCaseService problemCaseService;

    public List<Language> getLanguages(Long pid, Boolean all) {
        QueryWrapper<Language> wrapper = new QueryWrapper<>();
        wrapper.eq("oj", "ME");
        wrapper.orderByAsc("seq", "id");
        return languageService.list(wrapper);
    }

    public PageVO<ProblemListVO> getProblemList(ProblemListQueryDTO dto) {
        int page = dto.getPage();
        int limit = dto.getLimit() != null ? dto.getLimit() : 20;

        QueryWrapper<Problem> wrapper = new QueryWrapper<>();
        wrapper.eq("auth", 1); // only public problems

        String keyword = dto.getKeyword();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("problem_id", keyword)
                    .or().like("title", keyword)
                    .or().like("author", keyword));
        }

        String difficulty = dto.getDifficulty();
        if (difficulty != null && !difficulty.isEmpty()) {
            wrapper.eq("difficulty", Integer.parseInt(difficulty));
        }

        String type = dto.getType();
        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", Integer.parseInt(type));
        }

        String tagId = dto.getTagId();
        if (tagId != null && !tagId.isEmpty()) {
            List<Long> tagIds = Arrays.stream(tagId.split(","))
                    .map(Long::parseLong).collect(Collectors.toList());
            List<Long> pids = problemTagService.list(new QueryWrapper<ProblemTag>().in("tid", tagIds))
                    .stream().map(ProblemTag::getPid).distinct().collect(Collectors.toList());
            if (pids.isEmpty()) {
                return new PageVO<>(0L, Collections.emptyList());
            }
            wrapper.in("id", pids);
        }

        wrapper.orderByDesc("id");

        Page<Problem> mpPage = problemService.page(new Page<>(page, limit), wrapper);

        List<ProblemListVO> records = mpPage.getRecords().stream().map(problem -> {
            ProblemListVO vo = new ProblemListVO();
            vo.setId(problem.getId());
            vo.setPid(problem.getId());
            vo.setProblemId(problem.getProblemId());
            vo.setTitle(problem.getTitle());
            vo.setAuthor(problem.getAuthor());
            vo.setDifficulty(problem.getDifficulty());
            vo.setType(problem.getType());
            vo.setAuth(problem.getAuth());
            vo.setGmtCreate(problem.getGmtCreate());

            ProblemCount count = problemCountService.getById(problem.getId());
            if (count != null) {
                vo.setTotal(count.getTotal());
                vo.setAc(count.getAc());
                vo.setWa(count.getWa());
                vo.setTle(count.getTle());
                vo.setMle(count.getMle());
                vo.setRe(count.getRe());
                vo.setPe(count.getPe());
                vo.setCe(count.getCe());
                vo.setSe(count.getSe());
                vo.setPa(count.getPa());
            }

            List<Long> tagIds = problemTagService.list(
                    new QueryWrapper<ProblemTag>().eq("pid", problem.getId()))
                    .stream().map(ProblemTag::getTid).collect(Collectors.toList());
            if (!tagIds.isEmpty()) {
                List<Tag> tags = tagService.listByIds(tagIds);
                vo.setTagList(tags.stream().map(t -> {
                    TagVO tv = new TagVO();
                    tv.setId(t.getId());
                    tv.setName(t.getName());
                    tv.setColor(t.getColor());
                    tv.setOj(t.getOj());
                    tv.setTcid(t.getTcid());
                    return tv;
                }).collect(Collectors.toList()));
            }

            return vo;
        }).collect(Collectors.toList());

        return new PageVO<>(mpPage.getTotal(), records);
    }

    public ProblemDetailVO getProblemDetail(String pid) {
        Problem problem;
        try {
            Long id = Long.parseLong(pid);
            problem = problemService.getOne(new QueryWrapper<Problem>().eq("id", id).eq("auth", 1));
        } catch (NumberFormatException e) {
            problem = problemService.getOne(new QueryWrapper<Problem>().eq("problem_id", pid).eq("auth", 1));
        }
        if (problem == null) {
            return null;
        }

        ProblemCount problemCount = problemCountService.getById(problem.getId());

        List<Long> tagIds = problemTagService.list(
                new QueryWrapper<ProblemTag>().eq("pid", problem.getId()))
                .stream().map(ProblemTag::getTid).collect(Collectors.toList());
        List<Tag> tags = tagIds.isEmpty() ? Collections.emptyList() : tagService.listByIds(tagIds);

        List<Language> languages = new ArrayList<>();
        List<Long> lidList = problemLanguageService.list(
                new QueryWrapper<ProblemLanguage>().eq("pid", problem.getId()))
                .stream().map(ProblemLanguage::getLid).collect(Collectors.toList());
        if (!lidList.isEmpty()) {
            languages = languageService.listByIds(lidList);
        }
        List<String> languageNames = languages.stream()
                .map(Language::getName).collect(Collectors.toList());

        Map<String, String> codeTemplate = new LinkedHashMap<>();
        List<CodeTemplate> templates = codeTemplateService.list(
                new QueryWrapper<CodeTemplate>().eq("pid", problem.getId()).eq("status", true));
        Map<Long, String> langIdToName = languages.stream()
                .collect(Collectors.toMap(Language::getId, Language::getName));
        for (CodeTemplate ct : templates) {
            String langName = langIdToName.get(ct.getLid());
            if (langName != null) {
                codeTemplate.put(langName, ct.getCode());
            }
        }

        // Build flattened problem object matching frontend expectations
        Map<String, Object> problemData = new LinkedHashMap<>();
        problemData.put("id", problem.getId());
        problemData.put("problemId", problem.getProblemId());
        problemData.put("title", problem.getTitle());
        problemData.put("author", problem.getAuthor());
        problemData.put("type", problem.getType());
        problemData.put("timeLimit", problem.getTimeLimit());
        problemData.put("memoryLimit", problem.getMemoryLimit());
        problemData.put("stackLimit", problem.getStackLimit());
        problemData.put("description", problem.getDescription());
        problemData.put("input", problem.getInput());
        problemData.put("output", problem.getOutput());
        problemData.put("examples", problem.getExamples());
        problemData.put("source", problem.getSource());
        problemData.put("difficulty", problem.getDifficulty());
        problemData.put("hint", problem.getHint());
        problemData.put("auth", problem.getAuth());
        problemData.put("codeShare", problem.getCodeShare());
        problemData.put("judgeMode", problem.getJudgeMode());
        problemData.put("judgeCaseMode", problem.getJudgeCaseMode());
        problemData.put("userExtraFile", problem.getUserExtraFile());
        problemData.put("isRemote", false);

        ProblemDetailVO vo = new ProblemDetailVO();
        vo.setProblem(problemData);
        vo.setProblemCount(problemCount);
        vo.setTags(tags);
        vo.setLanguages(languageNames);
        vo.setCodeTemplate(codeTemplate);

        return vo;
    }

    public String getRandomProblem() {
        long count = problemService.count(new QueryWrapper<Problem>().eq("auth", 1));
        if (count == 0) {
            return null;
        }
        int offset = new Random().nextInt((int) count);
        Page<Problem> page = problemService.page(new Page<>(offset, 1),
                new QueryWrapper<Problem>().eq("auth", 1).orderByAsc("id"));
        List<Problem> records = page.getRecords();
        return records.isEmpty() ? null : records.get(0).getProblemId();
    }
}
