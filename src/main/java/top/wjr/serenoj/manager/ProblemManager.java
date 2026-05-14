package top.wjr.serenoj.manager;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import top.wjr.serenoj.pojo.dto.AdminProblemListQueryDTO;
import top.wjr.serenoj.pojo.dto.ProblemAuthDTO;
import top.wjr.serenoj.pojo.dto.ProblemListQueryDTO;
import top.wjr.serenoj.pojo.dto.ProblemUpsertDTO;
import top.wjr.serenoj.pojo.entity.*;
import top.wjr.serenoj.pojo.vo.PageVO;
import top.wjr.serenoj.pojo.vo.ProblemDetailVO;
import top.wjr.serenoj.pojo.vo.ProblemListVO;
import top.wjr.serenoj.pojo.vo.ProblemUpsertResultVO;
import top.wjr.serenoj.pojo.vo.TagVO;
import top.wjr.serenoj.shiro.AccountProfile;
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
        if (all == null || !all) {
            wrapper.eq("oj", "ME");
        }
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
            vo.setGmtModified(problem.getGmtModified());
            vo.setModifiedUser(problem.getModifiedUser());

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

    public PageVO<ProblemListVO> getAdminProblemList(AdminProblemListQueryDTO dto) {
        int page = dto.getPage();
        int limit = dto.getLimit() != null && dto.getLimit() > 0 ? dto.getLimit() : 10;

        QueryWrapper<Problem> wrapper = new QueryWrapper<>();
        if (dto.getAuth() != null && dto.getAuth() != 0) {
            wrapper.eq("auth", dto.getAuth());
        }

        String oj = dto.getOj();
        if (StringUtils.hasText(oj)
                && !"All".equalsIgnoreCase(oj)
                && !"Mine".equalsIgnoreCase(oj)
                && !"ME".equalsIgnoreCase(oj)) {
            // SerenOJ Phase 3 only keeps local ME problems. Remote OJ filtering is intentionally ignored.
            wrapper.eq("problem_id", "__NO_REMOTE_OJ_PROBLEM__");
        }

        String keyword = dto.getKeyword();
        if (StringUtils.hasText(keyword)) {
            String key = keyword.trim();
            wrapper.and(w -> w.like("problem_id", key)
                    .or().like("title", key)
                    .or().like("author", key));
        }
        wrapper.orderByDesc("id");

        Page<Problem> mpPage = problemService.page(new Page<>(page, limit), wrapper);
        List<ProblemListVO> records = mpPage.getRecords().stream()
                .map(this::buildAdminProblemListVO)
                .collect(Collectors.toList());
        return new PageVO<>(mpPage.getTotal(), records);
    }

    public Problem getAdminProblem(Long pid) {
        if (pid == null) {
            return null;
        }
        Problem problem = problemService.getById(pid);
        if (problem != null) {
            fillProblemDefaults(problem);
        }
        return problem;
    }

    @Transactional(rollbackFor = Exception.class)
    public ProblemUpsertResultVO createProblem(ProblemUpsertDTO dto) {
        Problem problem = validateAndNormalize(dto, false);
        if (problemService.count(new QueryWrapper<Problem>().eq("problem_id", problem.getProblemId())) > 0) {
            throw new IllegalArgumentException("该题目的 Problem ID 已存在，请更换");
        }

        String username = getCurrentUsername();
        if (!StringUtils.hasText(problem.getAuthor())) {
            problem.setAuthor(username);
        }
        problem.setModifiedUser(username);
        problem.setCaseVersion(String.valueOf(System.currentTimeMillis()));
        updateIoScore(problem, dto.getSamples());

        problemService.save(problem);
        createProblemCount(problem.getId());
        rebuildProblemRelations(problem.getId(), dto);
        return new ProblemUpsertResultVO(problem.getId(), problem.getId(), problem.getProblemId());
    }

    @Transactional(rollbackFor = Exception.class)
    public ProblemUpsertResultVO updateProblem(ProblemUpsertDTO dto) {
        Problem problem = validateAndNormalize(dto, true);
        Problem oldProblem = problemService.getById(problem.getId());
        if (oldProblem == null) {
            throw new IllegalArgumentException("题目不存在");
        }

        Problem sameProblemId = problemService.getOne(
                new QueryWrapper<Problem>().eq("problem_id", problem.getProblemId()), false);
        if (sameProblemId != null && !sameProblemId.getId().equals(problem.getId())) {
            throw new IllegalArgumentException("当前 Problem ID 已被使用，请更换");
        }

        problem.setModifiedUser(getCurrentUsername());
        problem.setCaseVersion(String.valueOf(System.currentTimeMillis()));
        updateIoScore(problem, dto.getSamples());

        problemService.updateById(problem);
        rebuildProblemRelations(problem.getId(), dto);
        return new ProblemUpsertResultVO(problem.getId(), problem.getId(), problem.getProblemId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteProblem(Long pid) {
        if (pid == null) {
            throw new IllegalArgumentException("题目 ID 不能为空");
        }
        if (problemService.getById(pid) == null) {
            throw new IllegalArgumentException("题目不存在");
        }
        removeProblemRelations(pid);
        problemCountService.removeById(pid);
        problemService.removeById(pid);
    }

    public void changeProblemAuth(ProblemAuthDTO dto) {
        if (dto == null || dto.getId() == null) {
            throw new IllegalArgumentException("题目 ID 不能为空");
        }
        if (dto.getAuth() == null || (dto.getAuth() != 1 && dto.getAuth() != 2 && dto.getAuth() != 3)) {
            throw new IllegalArgumentException("题目权限只能是 1、2、3");
        }
        Problem problem = problemService.getById(dto.getId());
        if (problem == null) {
            throw new IllegalArgumentException("题目不存在");
        }
        problem.setAuth(dto.getAuth());
        problem.setModifiedUser(getCurrentUsername());
        problemService.updateById(problem);
    }

    public List<ProblemCase> getProblemCases(Long pid, Boolean isUpload) {
        QueryWrapper<ProblemCase> wrapper = new QueryWrapper<>();
        wrapper.eq("pid", pid).eq("status", 0);
        if (Boolean.TRUE.equals(isUpload)) {
            wrapper.last("order by length(input) asc, input asc");
        } else {
            wrapper.orderByAsc("group_num", "id");
        }
        return problemCaseService.list(wrapper);
    }

    public List<Tag> getProblemTags(Long pid) {
        List<Long> tagIds = problemTagService.list(new QueryWrapper<ProblemTag>().eq("pid", pid))
                .stream().map(ProblemTag::getTid).distinct().collect(Collectors.toList());
        if (tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        return tagService.listByIds(tagIds);
    }

    public List<Language> getProblemLanguages(Long pid) {
        List<Long> languageIds = problemLanguageService.list(new QueryWrapper<ProblemLanguage>().eq("pid", pid))
                .stream().map(ProblemLanguage::getLid).distinct().collect(Collectors.toList());
        if (languageIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Language> languages = languageService.listByIds(languageIds);
        languages.sort(Comparator.comparing(Language::getSeq, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(Language::getId));
        return languages;
    }

    public List<CodeTemplate> getProblemCodeTemplate(Long pid) {
        return codeTemplateService.list(new QueryWrapper<CodeTemplate>().eq("pid", pid).orderByAsc("lid", "id"));
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
        problemData.put("ioScore", problem.getIoScore());
        problemData.put("codeShare", problem.getCodeShare());
        problemData.put("judgeMode", problem.getJudgeMode());
        problemData.put("judgeCaseMode", problem.getJudgeCaseMode());
        problemData.put("userExtraFile", problem.getUserExtraFile());
        problemData.put("isFileIO", problem.getIsFileIo());
        problemData.put("ioReadFileName", problem.getIoReadFileName());
        problemData.put("ioWriteFileName", problem.getIoWriteFileName());
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

    private ProblemListVO buildAdminProblemListVO(Problem problem) {
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
        vo.setGmtModified(problem.getGmtModified());
        vo.setModifiedUser(problem.getModifiedUser());
        return vo;
    }

    private Problem validateAndNormalize(ProblemUpsertDTO dto, boolean update) {
        if (dto == null || dto.getProblem() == null) {
            throw new IllegalArgumentException("题目信息不能为空");
        }
        Problem problem = dto.getProblem();
        if (update && problem.getId() == null) {
            throw new IllegalArgumentException("题目 ID 不能为空");
        }
        if (!StringUtils.hasText(problem.getProblemId())) {
            throw new IllegalArgumentException("Problem ID 不能为空");
        }
        if (!StringUtils.hasText(problem.getTitle())) {
            throw new IllegalArgumentException("题目标题不能为空");
        }

        String judgeMode = StringUtils.hasText(dto.getJudgeMode()) ? dto.getJudgeMode() : problem.getJudgeMode();
        if (!StringUtils.hasText(judgeMode)) {
            judgeMode = "default";
        }
        if (!"default".equals(judgeMode)) {
            throw new IllegalArgumentException("Phase 3 暂不支持 SPJ/交互题，请使用普通判题模式");
        }

        problem.setProblemId(problem.getProblemId().trim().toUpperCase());
        problem.setJudgeMode("default");
        problem.setSpjCode(null);
        problem.setSpjLanguage(null);
        if (dto.getIsUploadTestCase() != null) {
            problem.setIsUploadCase(dto.getIsUploadTestCase());
        }
        fillProblemDefaults(problem);
        return problem;
    }

    private void fillProblemDefaults(Problem problem) {
        if (problem.getType() == null) problem.setType(0);
        if (problem.getTimeLimit() == null) problem.setTimeLimit(1000);
        if (problem.getMemoryLimit() == null) problem.setMemoryLimit(256);
        if (problem.getStackLimit() == null) problem.setStackLimit(128);
        if (problem.getDifficulty() == null) problem.setDifficulty(0);
        if (problem.getAuth() == null) problem.setAuth(1);
        if (problem.getIoScore() == null) problem.setIoScore(100);
        if (problem.getCodeShare() == null) problem.setCodeShare(true);
        if (!StringUtils.hasText(problem.getJudgeMode())) problem.setJudgeMode("default");
        if (!StringUtils.hasText(problem.getJudgeCaseMode())) problem.setJudgeCaseMode("default");
        if (problem.getIsRemoveEndBlank() == null) problem.setIsRemoveEndBlank(false);
        if (problem.getOpenCaseResult() == null) problem.setOpenCaseResult(true);
        if (problem.getIsUploadCase() == null) problem.setIsUploadCase(true);
        if (!StringUtils.hasText(problem.getCaseVersion())) problem.setCaseVersion("0");
        if (problem.getIsFileIo() == null) problem.setIsFileIo(false);
    }

    private void createProblemCount(Long pid) {
        ProblemCount count = new ProblemCount();
        count.setPid(pid);
        count.setTotal(0);
        count.setAc(0);
        count.setWa(0);
        count.setTle(0);
        count.setMle(0);
        count.setRe(0);
        count.setPe(0);
        count.setCe(0);
        count.setSe(0);
        count.setPa(0);
        count.setVersion(0L);
        problemCountService.save(count);
    }

    private void updateIoScore(Problem problem, List<ProblemCase> samples) {
        if (problem.getType() == null || problem.getType() != 1 || samples == null || samples.isEmpty()) {
            return;
        }
        int score = 0;
        for (ProblemCase sample : samples) {
            if (sample != null && sample.getScore() != null) {
                score += sample.getScore();
            }
        }
        problem.setIoScore(score);
    }

    private void rebuildProblemRelations(Long pid, ProblemUpsertDTO dto) {
        removeProblemRelations(pid);
        saveProblemCases(pid, dto.getSamples());
        saveProblemTags(pid, dto.getTags());
        saveProblemLanguages(pid, dto.getLanguages());
        saveCodeTemplates(pid, dto.getCodeTemplates());
    }

    private void removeProblemRelations(Long pid) {
        problemCaseService.remove(new QueryWrapper<ProblemCase>().eq("pid", pid));
        problemTagService.remove(new QueryWrapper<ProblemTag>().eq("pid", pid));
        problemLanguageService.remove(new QueryWrapper<ProblemLanguage>().eq("pid", pid));
        codeTemplateService.remove(new QueryWrapper<CodeTemplate>().eq("pid", pid));
    }

    private void saveProblemCases(Long pid, List<ProblemCase> samples) {
        if (samples == null || samples.isEmpty()) {
            return;
        }
        List<ProblemCase> cases = new ArrayList<>();
        for (ProblemCase sample : samples) {
            if (sample == null) {
                continue;
            }
            ProblemCase problemCase = new ProblemCase();
            problemCase.setPid(pid);
            problemCase.setInput(sample.getInput());
            problemCase.setOutput(sample.getOutput());
            problemCase.setScore(sample.getScore());
            problemCase.setStatus(sample.getStatus() == null ? 0 : sample.getStatus());
            problemCase.setGroupNum(sample.getGroupNum());
            cases.add(problemCase);
        }
        if (!cases.isEmpty()) {
            problemCaseService.saveBatch(cases);
        }
    }

    private void saveProblemTags(Long pid, List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        Set<Long> tagIds = new LinkedHashSet<>();
        for (Tag tag : tags) {
            Long tagId = resolveTagId(tag);
            if (tagId != null) {
                tagIds.add(tagId);
            }
        }
        if (tagIds.isEmpty()) {
            return;
        }
        List<ProblemTag> relations = tagIds.stream()
                .map(tagId -> new ProblemTag().setPid(pid).setTid(tagId))
                .collect(Collectors.toList());
        problemTagService.saveBatch(relations);
    }

    private Long resolveTagId(Tag tag) {
        if (tag == null) {
            return null;
        }
        if (tag.getId() != null) {
            return tag.getId();
        }
        if (!StringUtils.hasText(tag.getName())) {
            return null;
        }
        String oj = StringUtils.hasText(tag.getOj()) ? tag.getOj() : "ME";
        Tag exists = tagService.getOne(new QueryWrapper<Tag>()
                .eq("name", tag.getName().trim())
                .eq("oj", oj), false);
        if (exists != null) {
            return exists.getId();
        }
        Tag newTag = new Tag();
        newTag.setName(tag.getName().trim());
        newTag.setOj(oj);
        newTag.setColor(StringUtils.hasText(tag.getColor()) ? tag.getColor() : "#409EFF");
        newTag.setTcid(tag.getTcid());
        tagService.save(newTag);
        return newTag.getId();
    }

    private void saveProblemLanguages(Long pid, List<Language> languages) {
        if (languages == null || languages.isEmpty()) {
            return;
        }
        Set<Long> languageIds = new LinkedHashSet<>();
        for (Language language : languages) {
            Long languageId = resolveLanguageId(language);
            if (languageId != null) {
                languageIds.add(languageId);
            }
        }
        if (languageIds.isEmpty()) {
            return;
        }
        List<ProblemLanguage> relations = languageIds.stream()
                .map(languageId -> new ProblemLanguage().setPid(pid).setLid(languageId))
                .collect(Collectors.toList());
        problemLanguageService.saveBatch(relations);
    }

    private Long resolveLanguageId(Language language) {
        if (language == null) {
            return null;
        }
        if (language.getId() != null) {
            return language.getId();
        }
        if (!StringUtils.hasText(language.getName())) {
            return null;
        }
        Language exists = languageService.getOne(new QueryWrapper<Language>()
                .eq("name", language.getName())
                .eq("oj", StringUtils.hasText(language.getOj()) ? language.getOj() : "ME"), false);
        return exists == null ? null : exists.getId();
    }

    private void saveCodeTemplates(Long pid, List<CodeTemplate> codeTemplates) {
        if (codeTemplates == null || codeTemplates.isEmpty()) {
            return;
        }
        List<CodeTemplate> templates = new ArrayList<>();
        for (CodeTemplate codeTemplate : codeTemplates) {
            if (codeTemplate == null || codeTemplate.getLid() == null
                    || !Boolean.TRUE.equals(codeTemplate.getStatus())
                    || !StringUtils.hasText(codeTemplate.getCode())) {
                continue;
            }
            CodeTemplate newTemplate = new CodeTemplate();
            newTemplate.setPid(pid);
            newTemplate.setLid(codeTemplate.getLid());
            newTemplate.setCode(codeTemplate.getCode());
            newTemplate.setStatus(true);
            templates.add(newTemplate);
        }
        if (!templates.isEmpty()) {
            codeTemplateService.saveBatch(templates);
        }
    }

    private String getCurrentUsername() {
        Object principal = SecurityUtils.getSubject().getPrincipal();
        if (principal instanceof AccountProfile) {
            String username = ((AccountProfile) principal).getUsername();
            if (StringUtils.hasText(username)) {
                return username;
            }
        }
        return "system";
    }
}
