package com.example.oldguy.modules.modeler.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.filter.CorsFilter;

/**
 * @ClassName: WebSecurityConfig
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/1/19 0019 下午 3:16
 * @Version：
 **/
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private MyFilter myFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .addFilterAfter(myFilter, SecurityContextPersistenceFilter.class)
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and().csrf().disable();
    }

}
