package com.example.oldguy.modules.modeler.security;

import org.flowable.ui.common.model.RemoteUser;
import org.flowable.ui.common.security.FlowableAppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import static com.example.oldguy.modules.modeler.constants.FlowableConstants.FLOW_ABLE_MODELER_ROLES;

/**
 * @ClassName: MyUserDetailsService
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/1/17 0017 下午 4:37
 * @Version：
 **/
@Service
public class MyUserDetailsService implements UserDetailsService {

    private Logger LOGGER = LoggerFactory.getLogger(MyUserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        LOGGER.debug("MyUserDetailsService:loadUserByUsername:认证权限.....");
        ArrayList<GrantedAuthority> authorities = new ArrayList<>();

        // 配置 flowable-modeler 权限
        FLOW_ABLE_MODELER_ROLES.parallelStream().forEach(obj -> {
            authorities.add(new SimpleGrantedAuthority(obj));
        });

        RemoteUser sourceUser = new RemoteUser();
        sourceUser.setFirstName("admin");
        sourceUser.setDisplayName("测试中文");
        sourceUser.setPassword("123456");
        sourceUser.setPrivileges(new ArrayList<>(FLOW_ABLE_MODELER_ROLES));
        sourceUser.setId("123456");
        FlowableAppUser user = new FlowableAppUser(sourceUser, "admin", authorities);
        return user;
    }
}
