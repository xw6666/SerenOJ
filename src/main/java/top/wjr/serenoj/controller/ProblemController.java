package top.wjr.serenoj.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.manager.ProblemManager;
import top.wjr.serenoj.manager.TagManager;
import top.wjr.serenoj.pojo.dto.ProblemListQueryDTO;
import top.wjr.serenoj.pojo.entity.CodeTemplate;
import top.wjr.serenoj.pojo.entity.Language;
import top.wjr.serenoj.pojo.entity.Tag;
import top.wjr.serenoj.pojo.vo.PageVO;
import top.wjr.serenoj.pojo.vo.ProblemDetailVO;
import top.wjr.serenoj.pojo.vo.ProblemListVO;
import top.wjr.serenoj.pojo.vo.TagGroupVO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProblemController {

    @Autowired
    private ProblemManager problemManager;

    @Autowired
    private TagManager tagManager;

    @GetMapping("/get-languages")
    public CommonResult<List<Language>> getLanguages(
            @RequestParam(required = false) Long pid,
            @RequestParam(required = false) Boolean all) {
        return CommonResult.successResponse(problemManager.getLanguages(pid, all));
    }

    @GetMapping("/get-tag-list")
    public CommonResult<List<TagGroupVO>> getTagList(@RequestParam(required = false) String oj) {
        return CommonResult.successResponse(tagManager.getTagList(oj));
    }

    @GetMapping("/get-problem-list")
    public CommonResult<PageVO<ProblemListVO>> getProblemList(ProblemListQueryDTO dto) {
        return CommonResult.successResponse(problemManager.getProblemList(dto));
    }

    @GetMapping("/get-problem-detail")
    public CommonResult<ProblemDetailVO> getProblemDetail(@RequestParam String pid) {
        ProblemDetailVO vo = problemManager.getProblemDetail(pid);
        if (vo == null) {
            return CommonResult.errorResponse("题目不存在");
        }
        return CommonResult.successResponse(vo);
    }

    @GetMapping("/get-problem-tags")
    public CommonResult<List<Tag>> getProblemTags(@RequestParam Long pid) {
        return CommonResult.successResponse(problemManager.getProblemTags(pid));
    }

    @GetMapping("/get-problem-languages")
    public CommonResult<List<Language>> getProblemLanguages(@RequestParam Long pid) {
        return CommonResult.successResponse(problemManager.getProblemLanguages(pid));
    }

    @GetMapping("/get-problem-code-template")
    public CommonResult<List<CodeTemplate>> getProblemCodeTemplate(@RequestParam Long pid) {
        return CommonResult.successResponse(problemManager.getProblemCodeTemplate(pid));
    }

    @GetMapping("/get-random-problem")
    public CommonResult<Map<String, String>> getRandomProblem() {
        String problemId = problemManager.getRandomProblem();
        if (problemId == null) {
            return CommonResult.errorResponse("暂无公开题目");
        }
        Map<String, String> result = new HashMap<>();
        result.put("problemId", problemId);
        return CommonResult.successResponse(result);
    }
}
