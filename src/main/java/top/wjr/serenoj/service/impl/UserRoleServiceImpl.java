package top.wjr.serenoj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.wjr.serenoj.mapper.UserRoleMapper;
import top.wjr.serenoj.pojo.entity.Role;
import top.wjr.serenoj.pojo.entity.UserRole;
import top.wjr.serenoj.service.UserRoleService;
import java.util.List;

@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {
    
    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public List<Role> getRolesByUid(String uid) {
        return userRoleMapper.getRolesByUid(uid);
    }
}
