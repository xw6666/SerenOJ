package top.wjr.serenoj.controller;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.manager.TagManager;
import top.wjr.serenoj.pojo.dto.TagClassificationDTO;
import top.wjr.serenoj.pojo.dto.TagDTO;
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

    @PostMapping
    public CommonResult<Tag> createTag(@Validated @RequestBody TagDTO dto) {
        Tag tag = tagManager.addTag(dto);
        return CommonResult.successResponse(tag, "新增标签成功");
    }

    @PutMapping
    public CommonResult<Void> updateTag(@Validated @RequestBody TagDTO dto) {
        tagManager.updateTag(dto);
        return CommonResult.successResponse("修改标签成功");
    }

    @DeleteMapping
    public CommonResult<Void> deleteTag(@RequestParam Long tid) {
        tagManager.deleteTag(tid);
        return CommonResult.successResponse("删除标签成功");
    }

    @PostMapping("/classification")
    public CommonResult<TagClassification> createClassification(@Validated @RequestBody TagClassificationDTO dto) {
        TagClassification tc = tagManager.addClassification(dto);
        return CommonResult.successResponse(tc, "新增分类成功");
    }

    @PutMapping("/classification")
    public CommonResult<Void> updateClassification(@Validated @RequestBody TagClassificationDTO dto) {
        tagManager.updateClassification(dto);
        return CommonResult.successResponse("修改分类成功");
    }

    @DeleteMapping("/classification")
    public CommonResult<Void> deleteClassification(@RequestParam Long tcid) {
        tagManager.deleteClassification(tcid);
        return CommonResult.successResponse("删除分类成功");
    }
}
