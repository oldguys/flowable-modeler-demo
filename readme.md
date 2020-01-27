> **业务场景**：进行流程开发的时候，经常需要流程设计器进行Bpmn.xml设计，flowable官方也只是提供了war下载，并且需要基于本身的用户权限，这对于已有系统的集成是不方便的，所以对于流程设计器的权限模块剥离就很有必要，本文描述了如何对流程设计器的剥离以及核心模块的功能分析。
>
> **环境**：
>   springboot：2.2.0.RELEASE
>   flowable：6.4.2
>
> git地址：https://github.com/oldguys/flowable-modeler-demo.git
> flowable 官方git地址：[https://github.com/flowable/flowable-engine/releases](https://github.com/flowable/flowable-engine/releases)
>


![01.png](https://upload-images.jianshu.io/upload_images/14387783-99e75934db097e13.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

---

#### 流程设计器抽离demo

1. pom文件
2. 静态资源及配置文件
3. 重写 **flowable-modeler** 默认的类加载
4. 重写 **spring-security** 相关模块


#### Step 1：pom 文件

 **flowable-modeler 模块**：从官方源码可以分析得出，基本的modeler模块的相关引用 此处 版本是 **flowable 6.4.2**
**mysql数据源**：默认是h2数据库，此处引用的是mysql数据库。

```
        <!-- flowable-modeler 核心 -->
        <dependency>
            <groupId>org.flowable</groupId>
            <artifactId>flowable-ui-modeler-conf</artifactId>
            <version>${flowable-version}</version>
        </dependency>
        <dependency>
            <groupId>org.flowable</groupId>
            <artifactId>flowable-ui-modeler-rest</artifactId>
            <version>${flowable-version}</version>
        </dependency>
        <dependency>
            <groupId>org.flowable</groupId>
            <artifactId>flowable-ui-modeler-logic</artifactId>
            <version>${flowable-version}</version>
        </dependency>

        <!-- 替换数据源 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jdbc</artifactId>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.47</version>
        </dependency>
```

#### Step 2： 静态资源及配置文件

1. 复制相关的静态资源及配置文件

![静态原件及配置文件](https://upload-images.jianshu.io/upload_images/14387783-e9d874084718b485.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

在源码中可以看到，这些文件为前端页面的静态页面，需要完成转移到剥离出来的新项目之中。

2.编写项目的自定义配置文件，及springboot本身项目的 **application.yml** 文件。
```
logging:
  level:
    org.flowable.ui.modeler.rest.app: debug
#    root: debug
server:
  port: 8081
spring:
  datasource:
    username: root
    password: root
    url: jdbc:mysql://127.0.0.1:3306/flowable-modeler-demo?characterEncoding=UTF-8
    driver-class-name: com.mysql.jdbc.Driver

```
> PS: 由于springboot 项目的配置文件是逐层覆盖的，最外层项目的变量 可以直接覆盖掉原始的配置变量，所以可以直接在此处 配置数据源遍可以替换掉内部的默认数据源变量。

#### Step 3：重写 **flowable-modeler** 默认的类加载

![源码 中的默认类加载](https://upload-images.jianshu.io/upload_images/14387783-e6f61c9c34e59459.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


从源码中看到入口为2个类，本处需要对**org.flowable.ui.modeler.conf.ApplicationConfiguration** 进行相应的改造。
剔除不需要的相关依赖，添加缺少的依赖，改造结果如下:

1. 抽出自定义需要的全局配置类
```
package com.example.oldguy.modules.modeler.configurations;

import org.flowable.ui.modeler.conf.DatabaseConfiguration;
import org.flowable.ui.modeler.properties.FlowableModelerAppProperties;
import org.flowable.ui.modeler.servlet.ApiDispatcherServletConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;


@Import(
        value = {
                DatabaseConfiguration.class,
        }
)
@Configuration
@EnableConfigurationProperties(FlowableModelerAppProperties.class)
@ComponentScan(basePackages = {
        "org.flowable.ui.modeler.repository",
        "org.flowable.ui.modeler.service",
        "org.flowable.ui.common.repository",
        "org.flowable.ui.common.tenant",

        "org.flowable.ui.modeler.rest.app",
        "org.flowable.ui.modeler.rest.api"
    }
)
public class MyAppConfiguration {

    @Bean
    public ServletRegistrationBean modelerApiServlet(ApplicationContext applicationContext) {
        AnnotationConfigWebApplicationContext dispatcherServletConfiguration = new AnnotationConfigWebApplicationContext();
        dispatcherServletConfiguration.setParent(applicationContext);
        dispatcherServletConfiguration.register(ApiDispatcherServletConfiguration.class);
        DispatcherServlet servlet = new DispatcherServlet(dispatcherServletConfiguration);
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(servlet, "/api/*");
        registrationBean.setName("Flowable Modeler App API Servlet");
        registrationBean.setLoadOnStartup(1);
        registrationBean.setAsyncSupported(true);
        return registrationBean;
    }

}
```
> PS:  org.flowable.ui.modeler.conf.DatabaseConfiguration 为flowable源码中的mybatis配置，需要引入。
> 

2. 重写类 **org.flowable.ui.common.rest.idm.remote.RemoteAccountResource**，这个类是流程设计器获取用户相关信息的接口，此处的处理方式是，在新建项目中编写相同的restful接口覆盖，然后不加载原始的类。
```
package com.example.oldguy.modules.modeler.controllers;

import org.flowable.ui.common.model.RemoteUser;
import org.flowable.ui.common.model.UserRepresentation;
import org.flowable.ui.common.security.FlowableAppUser;
import org.flowable.ui.common.security.SecurityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName: RemoteAccountResource
 * @Author: ren
 * @Description:
 * @CreateTIme: 2020/1/25 0025 下午 10:37
 **/
@RestController
public class RemoteAccountResource {

    @GetMapping("app/rest/account")
    public UserRepresentation getAccount() {

        FlowableAppUser appUser = SecurityUtils.getCurrentFlowableAppUser();
        UserRepresentation userRepresentation = new UserRepresentation(appUser.getUserObject());
        if (appUser.getUserObject() instanceof RemoteUser) {
            RemoteUser temp = (RemoteUser) appUser.getUserObject();
            userRepresentation.setPrivileges(temp.getPrivileges());
        }
        return userRepresentation;
    }


}

```
3. 重写 **org.flowable.ui.common.service.idm.RemoteIdmService** ，此处在 **flowable-ui-modeler-rest** 模块中多次依赖注入，本处项目暂时没有使用到，所以这里采用重写空实现。

```
package com.example.oldguy.modules.modeler.services;

import org.flowable.ui.common.model.RemoteGroup;
import org.flowable.ui.common.model.RemoteToken;
import org.flowable.ui.common.model.RemoteUser;
import org.flowable.ui.common.service.idm.RemoteIdmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ClassName: MyRemoteServiceImpl
 * @Author: ren
 * @Description:
 * @CreateTIme: 2020/1/25 0025 下午 11:27
 **/
@Service
public class MyRemoteServiceImpl implements RemoteIdmService {

    private Logger LOGGER = LoggerFactory.getLogger(MyRemoteServiceImpl.class);

    @Override
    public RemoteUser authenticateUser(String username, String password) {
        LOGGER.debug("MyRemoteServiceImpl:authenticateUser");
        return null;
    }

    @Override
    public RemoteToken getToken(String tokenValue) {
        LOGGER.debug("MyRemoteServiceImpl:getToken");
        return null;
    }

    @Override
    public RemoteUser getUser(String userId) {
        LOGGER.debug("MyRemoteServiceImpl:getUser");
        return null;
    }

    @Override
    public List<RemoteUser> findUsersByNameFilter(String filter) {
        LOGGER.debug("MyRemoteServiceImpl:findUsersByNameFilter");
        return null;
    }

    @Override
    public List<RemoteUser> findUsersByGroup(String groupId) {
        LOGGER.debug("MyRemoteServiceImpl:findUsersByGroup");
        return null;
    }

    @Override
    public RemoteGroup getGroup(String groupId) {
        LOGGER.debug("MyRemoteServiceImpl:getGroup");
        return null;
    }

    @Override
    public List<RemoteGroup> findGroupsByNameFilter(String filter) {
        LOGGER.debug("MyRemoteServiceImpl:findGroupsByNameFilter");
        return null;
    }
}
```

#### Step 4：重写 **spring-security** 相关模块

在源码中 **org.flowable.ui.modeler.conf.SecurityConfiguration** 为流程设计器的权限核心控制，所以需要 进行改造。本处直接基于spring-security 的运行机制重写相关的实现类，不引用原始配置的相关信息

1. 自定义认证过滤器：**com.example.oldguy.modules.modeler.security.MyFilter**

```
package com.example.oldguy.modules.modeler.security;

import org.flowable.ui.common.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @ClassName: MyFilter
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/1/19 0019 下午 4:04
 * @Version：
 **/
@Component
@WebFilter(urlPatterns = {"/app/**", "/api/**"})
public class MyFilter extends OncePerRequestFilter {

    private Logger LOGGER = LoggerFactory.getLogger(MyFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (skipAuthenticationCheck(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        LOGGER.debug("MyFilter:doFilterInternal:" + request.getRequestURL());

        if (StringUtils.isEmpty(SecurityUtils.getCurrentUserId())) {

            LOGGER.debug("MyFilter:doFilterInternal:校验......");
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("admin", "");
            SecurityContextHolder.getContext().setAuthentication(token);

        } else {
            LOGGER.debug("MyFilter:doFilterInternal:校验通过.......");
        }

        filterChain.doFilter(request, response);
    }

    protected boolean skipAuthenticationCheck(HttpServletRequest request) {
        return request.getRequestURI().endsWith(".css") ||
                request.getRequestURI().endsWith(".js") ||
                request.getRequestURI().endsWith(".html") ||
                request.getRequestURI().endsWith(".map") ||
                request.getRequestURI().endsWith(".woff") ||
                request.getRequestURI().endsWith(".png") ||
                request.getRequestURI().endsWith(".jpg") ||
                request.getRequestURI().endsWith(".jpeg") ||
                request.getRequestURI().endsWith(".tif") ||
                request.getRequestURI().endsWith(".tiff");
    }

}

```
> PS: 经过试验，过滤器必须继承于 **org.springframework.web.filter.OncePerRequestFilter** 而不能直接实现 **javax.servlet.Filter** ，不然就算注入到 spring-security容器中，也不能触发本身的权限校验，具体原理还有待研究。此处参考源码中的 **org.flowable.ui.common.filter.FlowableCookieFilter**进行改造

2. 编写核心的校验类 **com.example.oldguy.modules.modeler.security.MyUserDetailsService**

```
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

```
3. 编写密码编译器 **com.example.oldguy.modules.modeler.security.MyPasswordEncoder**
```
package com.example.oldguy.modules.modeler.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * @ClassName: MyPasswordEncoder
 * @Author: ren
 * @Description:
 * @CreateTIme: 2020/1/25 0025 下午 7:11
 **/
@Component
public class MyPasswordEncoder implements PasswordEncoder {

    private Logger LOGGER = LoggerFactory.getLogger(MyPasswordEncoder.class);

    @Override
    public String encode(CharSequence charSequence) {
        LOGGER.debug("MyPasswordEncoder:encode:" + charSequence);
        return charSequence.toString();
    }

    @Override
    public boolean matches(CharSequence frontPsw, String sourcePsw) {
        LOGGER.debug("MyPasswordEncoder:matches:" + frontPsw + "\t sourcePsw:" + sourcePsw);
        return true;
    }
}
```

4. 编写spring-security 核心容器
```
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
```
> PS:  csrf 要关掉，不然前端调用会被拦截。出现 Forbidden 一闪而过的情况。


以上就完成流程设计器抽离。

--- 

下面是对于其中的部分模块及原理的解析：

> 1. **org.flowable.ui.common.security.SecurityUtils** 用户信息
> 2. 模型设计器配置相应模块
> 3. spring-security 过滤链 
> 


##### 1. **org.flowable.ui.common.security.SecurityUtils** 用户信息

```
// org.flowable.ui.common.security.SecurityUtils
    public static FlowableAppUser getCurrentFlowableAppUser() {
        FlowableAppUser user = null;
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext != null && securityContext.getAuthentication() != null) {
            Object principal = securityContext.getAuthentication().getPrincipal();
            if (principal instanceof FlowableAppUser) {
                user = (FlowableAppUser) principal;
            }
        }
        return user;
    }
```

```
// com.example.oldguy.modules.modeler.security.MyUserDetailsService
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
```

![spring-security 官网的 SecurityContextHolder 描述 ](https://upload-images.jianshu.io/upload_images/14387783-5c034272f0880707.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

>  PS: 从官网中可以看到 用户是基于 ThreadLocal 的，所以是线程安全的，并且在 SecurityUtils 调用的时候 FlowableAppUser 才会返回，所以在重写 org.springframework.security.core.userdetails.UserDetailsService 实现类的时候，需要使用 FlowableAppUser 作为用户类。 
>

另外，restful接口：http://localhost:8888/flowable-modeler/app/rest/account
默认返回值：
```
{
	"id": "admin",
	"firstName": "Test",
	"lastName": "Administrator",
	"email": "admin@flowable.org",
	"fullName": "Test Administrator",
	"tenantId": null,
	"groups": [],
	"privileges": ["access-idm", "access-rest-api", "access-task", "access-modeler", "access-admin"]
}
```
其中：["access-idm", "access-rest-api", "access-task", "access-modeler", "access-admin"]
分别对应流程设计器导航栏上面的各个功能，可以根据需要进行删减改造
![image.png](https://upload-images.jianshu.io/upload_images/14387783-6d3fae82ddedf874.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


#####  2. 模型设计器配置相应模块
模型设计器中功能特别多，并不是所有功能都需要用到的，可以通过修改配置文件进行修改
资源位置：
>**stencilset_bpmn.json**（流程设计器界面配置文件）：D:\workspace\demo\flowable\flowable-engine-flowable-6.4.2-old\modules\flowable-ui-modeler\flowable-ui-modeler-logic\src\main\resources\stencilset_bpmn.json
>
> **StencilSetResource**(流程设计器配置后端接口): org.flowable.ui.modeler.rest.app.StencilSetResource
>
> **zh-CN.json**(可参考的汉化文件)：static/i18n/zh-CN.json

![配置文件的位置](https://upload-images.jianshu.io/upload_images/14387783-6eb891f759e3af62.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![配置接口位置](https://upload-images.jianshu.io/upload_images/14387783-4ed44637acc973d9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



![修改完配置结果](https://upload-images.jianshu.io/upload_images/14387783-223fb808c47042a3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


##### 3. spring-security 过滤链 

spring-security的过滤链 基于 **org.springframework.security.config.annotation.web.builders.FilterComparator**
debug的时候可以拿到数据（如下），所以配置过滤器的时候，必须优先于指定的节点，如LogoutFilter，如：**CorsFilter**=600，如果后于此过滤器，会出现没有权限，然后界面一闪而过。很难调试，所以需要通过过滤链顺序对于指定的权限拦截进行处理。
```
{
org.springframework.security.openid.OpenIDAuthenticationFilter=1600, 
org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter=1300, 
org.springframework.security.web.authentication.ui.DefaultLogoutPageGeneratingFilter=1800, 
org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter=900, 
org.springframework.security.web.context.SecurityContextPersistenceFilter=400, 
org.springframework.security.web.access.intercept.FilterSecurityInterceptor=3100, 
org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter=1700, 
org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter=1000, 
org.springframework.security.web.session.SessionManagementFilter=2900, 
org.springframework.security.web.authentication.logout.LogoutFilter=800, 
org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter=1100, 
org.springframework.security.web.jaasapi.JaasApiIntegrationFilter=2500, 
org.springframework.security.web.authentication.switchuser.SwitchUserFilter=3200, 
org.springframework.security.web.access.channel.ChannelProcessingFilter=100, 
org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationFilter=2600, 
org.springframework.security.web.session.ConcurrentSessionFilter=1900, 
org.springframework.security.web.authentication.www.BasicAuthenticationFilter=2200, 
org.springframework.security.web.authentication.AnonymousAuthenticationFilter=2700, 
org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter=2100, 
org.springframework.security.web.csrf.CsrfFilter=700, 
org.springframework.security.cas.web.CasAuthenticationFilter=1200, 
org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter=2400, 
org.springframework.security.web.access.ExceptionTranslationFilter=3000, 
org.springframework.security.web.authentication.www.DigestAuthenticationFilter=2000,
 org.springframework.web.filter.CorsFilter=600, 
org.springframework.security.web.savedrequest.RequestCacheAwareFilter=2300, 
org.springframework.security.oauth2.client.web.OAuth2AuthorizationCodeGrantFilter=2800, 
org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter=1400,
org.springframework.security.web.header.HeaderWriterFilter=500, 
org.springframework.security.web.context.request.async.WebAsyncManagerIntegrationFilter=300
}
```







