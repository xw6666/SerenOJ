package top.wjr.serenoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.wjr.serenoj.mapper.ProblemTagMapper;
import top.wjr.serenoj.pojo.entity.ProblemTag;
import top.wjr.serenoj.service.ProblemTagService;

@Service
public class ProblemTagServiceImpl extends ServiceImpl<ProblemTagMapper, ProblemTag> implements ProblemTagService {
}
