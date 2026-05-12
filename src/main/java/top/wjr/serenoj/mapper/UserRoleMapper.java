package top.wjr.serenoj.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.wjr.serenoj.pojo.entity.Role;
import top.wjr.serenoj.pojo.entity.UserRole;
import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    @Select("SELECT r.* FROM role r INNER JOIN user_role ur ON r.id = ur.role_id WHERE ur.uid = #{uid}")
    List<Role> getRolesByUid(@Param("uid") String uid);
}
