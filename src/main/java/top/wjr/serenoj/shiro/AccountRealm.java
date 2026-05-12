package top.wjr.serenoj.shiro;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import top.wjr.serenoj.service.UserInfoService;
import top.wjr.serenoj.service.UserRoleService;
import top.wjr.serenoj.mapper.RoleAuthMapper;
import top.wjr.serenoj.pojo.entity.Auth;
import top.wjr.serenoj.pojo.entity.Role;
import top.wjr.serenoj.pojo.entity.UserInfo;
import top.wjr.serenoj.utils.JwtUtils;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class AccountRealm extends AuthorizingRealm {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private RoleAuthMapper roleAuthMapper;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        AccountProfile user = (AccountProfile) principals.getPrimaryPrincipal();
        List<String> permissionsNameList = new LinkedList<>();
        List<String> roleNameList = new LinkedList<>();
        
        List<Role> roles = userRoleService.getRolesByUid(user.getUid());
        for (Role role : roles) {
            roleNameList.add(role.getRole());
            for (Auth auth : roleAuthMapper.getAuthsByRoleId(role.getId())) {
                permissionsNameList.add(auth.getPermission());
            }
        }
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

        authorizationInfo.addRoles(roleNameList);
        authorizationInfo.addStringPermissions(permissionsNameList);
        return authorizationInfo;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        JwtToken jwt = (JwtToken) token;

        String userId = jwtUtils.getClaimByToken((String) jwt.getPrincipal()).getSubject();

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uuid", userId)
                .select("uuid", "username", "nickname", "realname", "title_name", "title_color", "avatar", "status");

        UserInfo userInfo = userInfoService.getOne(queryWrapper, false);
        if (userInfo == null) {
            throw new UnknownAccountException("账户不存在！");
        }
        if (userInfo.getStatus() == 1) {
            throw new LockedAccountException("该账户已被封禁，请联系管理员进行处理！");
        }
        AccountProfile profile = new AccountProfile();
        BeanUtil.copyProperties(userInfo, profile);
        profile.setUid(userInfo.getUuid());
        return new SimpleAuthenticationInfo(profile, jwt.getCredentials(), getName());
    }
}