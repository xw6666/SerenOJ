package top.wjr.serenoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import top.wjr.serenoj.pojo.entity.UserInfo;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
