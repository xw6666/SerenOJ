package top.wjr.serenoj.controller;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import top.wjr.serenoj.common.result.CommonResult;
import top.wjr.serenoj.manager.ProblemManager;
import top.wjr.serenoj.pojo.dto.AdminProblemListQueryDTO;
import top.wjr.serenoj.pojo.dto.ProblemAuthDTO;
import top.wjr.serenoj.pojo.dto.ProblemUpsertDTO;
import top.wjr.serenoj.pojo.entity.Problem;
import top.wjr.serenoj.pojo.entity.ProblemCase;
import top.wjr.serenoj.pojo.vo.PageVO;
import top.wjr.serenoj.pojo.vo.ProblemListVO;
import top.wjr.serenoj.pojo.vo.ProblemUpsertResultVO;

import java.util.List;

@RestController
@RequestMapping("/api/admin/problem")
@RequiresPermissions("problem_admin")
public class AdminProblemController {

    @Autowired
    private ProblemManager problemManager;

    @GetMapping("/get-problem-list")
    public CommonResult<PageVO<ProblemListVO>> getProblemList(AdminProblemListQueryDTO dto) {
        return CommonResult.successResponse(problemManager.getAdminProblemList(dto));
    }

    @GetMapping
    public CommonResult<Problem> getProblem(@RequestParam Long pid) {
        Problem problem = problemManager.getAdminProblem(pid);
        if (problem == null) {
            return CommonResult.errorResponse("题目不存在");
        }
        return CommonResult.successResponse(problem);
    }

    @PostMapping
    public CommonResult<ProblemUpsertResultVO> createProblem(@RequestBody ProblemUpsertDTO dto) {
        return CommonResult.successResponse(problemManager.createProblem(dto), "新增题目成功");
    }

    @PutMapping
    public CommonResult<ProblemUpsertResultVO> updateProblem(@RequestBody ProblemUpsertDTO dto) {
        return CommonResult.successResponse(problemManager.updateProblem(dto), "修改题目成功");
    }

    @DeleteMapping
    public CommonResult<Void> deleteProblem(@RequestParam Long pid) {
        problemManager.deleteProblem(pid);
        return CommonResult.successResponse("删除成功");
    }

    @PutMapping("/change-problem-auth")
    public CommonResult<Void> changeProblemAuth(@RequestBody ProblemAuthDTO dto) {
        problemManager.changeProblemAuth(dto);
        return CommonResult.successResponse("修改题目权限成功");
    }

    @GetMapping("/get-problem-cases")
    public CommonResult<List<ProblemCase>> getProblemCases(@RequestParam Long pid,
                                                           @RequestParam(required = false) Boolean isUpload) {
        return CommonResult.successResponse(problemManager.getProblemCases(pid, isUpload));
    }
}
