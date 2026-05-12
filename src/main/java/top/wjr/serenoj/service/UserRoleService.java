package top.wjr.serenoj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.wjr.serenoj.pojo.entity.Role;
import top.wjr.serenoj.pojo.entity.UserRole;
import java.util.List;

public interface UserRoleService extends IService<UserRole> {
    List<Role> getRolesByUid(String uid);
}
