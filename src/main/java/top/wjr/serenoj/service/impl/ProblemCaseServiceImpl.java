package top.wjr.serenoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.wjr.serenoj.mapper.ProblemCaseMapper;
import top.wjr.serenoj.pojo.entity.ProblemCase;
import top.wjr.serenoj.service.ProblemCaseService;

@Service
public class ProblemCaseServiceImpl extends ServiceImpl<ProblemCaseMapper, ProblemCase> implements ProblemCaseService {
}
