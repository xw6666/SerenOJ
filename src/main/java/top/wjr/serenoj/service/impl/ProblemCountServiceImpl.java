package top.wjr.serenoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.wjr.serenoj.mapper.ProblemCountMapper;
import top.wjr.serenoj.pojo.entity.ProblemCount;
import top.wjr.serenoj.service.ProblemCountService;

@Service
public class ProblemCountServiceImpl extends ServiceImpl<ProblemCountMapper, ProblemCount> implements ProblemCountService {
}
