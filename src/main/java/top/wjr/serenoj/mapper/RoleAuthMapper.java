package top.wjr.serenoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wjr.serenoj.pojo.entity.Auth;
import top.wjr.serenoj.pojo.entity.RoleAuth;
import java.util.List;

@Mapper
public interface RoleAuthMapper extends BaseMapper<RoleAuth> {
    @Select("SELECT a.* FROM role_auth ra JOIN auth a ON ra.auth_id = a.id WHERE ra.role_id = #{roleId}")
    List<Auth> getAuthsByRoleId(@Param("roleId") Long roleId);
}
