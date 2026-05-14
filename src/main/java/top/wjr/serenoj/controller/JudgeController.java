package top.wjr.serenoj.controller;

import cn.hutool.core.bean.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.common.result.ResultStatus;
import top.wjr.serenoj.pojo.entity.Judge;
import top.wjr.serenoj.pojo.vo.PageVO;
import top.wjr.serenoj.service.JudgeService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class JudgeController {

    @Autowired
    private JudgeService judgeService;

    @GetMapping("/get-submission-list")
    public CommonResult<PageVO<Map<String, Object>>> getSubmissionList() {
        return CommonResult.successResponse(new PageVO<>(0L, Collections.emptyList()));
    }

    @GetMapping("/get-submission-detail")
    public ResponseEntity<CommonResult<Map<String, Object>>> getSubmissionDetail(@RequestParam Long submitId) {
        Judge judge = judgeService.getById(submitId);
        if (judge == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(CommonResult.errorResponse("提交不存在", ResultStatus.NOT_FOUND));
        }

        Map<String, Object> submission = BeanUtil.beanToMap(judge);
        submission.put("cid", judge.getCid() == null ? 0L : judge.getCid());
        submission.put("displayPid", judge.getDisplayPid() == null
                ? (judge.getPid() == null ? "" : String.valueOf(judge.getPid()))
                : judge.getDisplayPid());
        submission.putIfAbsent("gid", 0L);

        Map<String, Object> result = new HashMap<>();
        result.put("submission", submission);
        result.put("codeShare", Boolean.TRUE.equals(judge.getShare()));
        return ResponseEntity.ok(CommonResult.successResponse(result));
    }
}
