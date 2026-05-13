package top.wjr.serenoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.wjr.serenoj.mapper.ProblemMapper;
import top.wjr.serenoj.pojo.entity.Problem;
import top.wjr.serenoj.service.ProblemService;

@Service
public class ProblemServiceImpl extends ServiceImpl<ProblemMapper, Problem> implements ProblemService {
}
