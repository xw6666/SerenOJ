package top.wjr.serenoj.controller;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.manager.TagManager;
import top.wjr.serenoj.pojo.entity.Tag;
import top.wjr.serenoj.pojo.entity.TagClassification;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tag")
@RequiresPermissions("problem_admin")
public class AdminTagController {

    @Autowired
    private TagManager tagManager;

    @GetMapping("/list")
    public CommonResult<List<Tag>> getTagList(@RequestParam(required = false) String oj) {
        return CommonResult.successResponse(tagManager.getAllProblemTagList(oj));
    }

    @GetMapping("/classification")
    public CommonResult<List<TagClassification>> getTagClassification(@RequestParam(required = false) String oj) {
        return CommonResult.successResponse(tagManager.getTagClassification(oj));
    }
}
