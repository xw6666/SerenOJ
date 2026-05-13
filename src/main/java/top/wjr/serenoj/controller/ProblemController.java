package top.wjr.serenoj.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.manager.ProblemManager;
import top.wjr.serenoj.manager.TagManager;
import top.wjr.serenoj.pojo.entity.Language;
import top.wjr.serenoj.pojo.vo.TagGroupVO;

import java.util.List;

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
}
