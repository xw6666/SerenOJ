package top.wjr.serenoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import top.wjr.serenoj.mapper.JudgeCaseMapper;
import top.wjr.serenoj.pojo.entity.JudgeCase;
import top.wjr.serenoj.service.JudgeCaseService;

@Service
public class JudgeCaseServiceImpl extends ServiceImpl<JudgeCaseMapper, JudgeCase> implements JudgeCaseService {
}
