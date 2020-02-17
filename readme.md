> **业务场景**：先去文章讲了如何从github官方代码中抽离流程设计器[《flowable 流程设计器 抽离，及源码改造原理分析》](https://www.jianshu.com/p/13093a1cd6a8)
> 本章主要在上一篇章的基础上，整合 **flowable引擎** 和 **mybatis-plus**，以及流程图部署
>
> **环境**：
>   springboot：2.2.0.RELEASE
>   flowable：6.4.2
>
> git地址：[https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_engine_and_mybatis_plus](https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_engine_and_mybatis_plus)
>
> 上一篇：[《flowable 流程设计器抽离，及源码改造原理分析》](https://www.jianshu.com/p/13093a1cd6a8)
>
>
>

**启动后地址：**
流程设计器地址：http://127.0.0.1:8081/flowable-modeler-demo/#/processes
swagger-api：http://127.0.0.1:8081/flowable-modeler-demo/doc.html

##### 1. maven配置引入

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

        <!-- flowable 核心 -->

        <dependency>
            <groupId>org.flowable</groupId>
            <artifactId>flowable-spring-boot-starter</artifactId>
            <version>${flowable-version}</version>
        </dependency>

        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.2.0</version>
        </dependency>

```
##### 2. 重写mybatis-plus 自动配置类
> PS: 由于 flowable-modeler 引入时候，会初始化 mybatis的Template和SqlFactory，这导致 mybatis-plus 本身的autoconfig 无法生效，所以需要重写。
>
从 **com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration** 中拆出代码进行改造 **com.example.oldguy.configurations.AbstractMybatisPlusConfiguration**
```
package com.example.oldguy.configurations;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.autoconfigure.SpringBootVFS;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * @ClassName: AbstractMybatisPlusConfiguration
 * @Author: ren
 * @Description:
 * @CreateTIme: 2019/6/18 0018 上午 11:02
 **/
public class AbstractMybatisPlusConfiguration {

    protected SqlSessionFactory getSqlSessionFactory(
            DataSource dataSource,
            MybatisPlusProperties properties,
            ResourceLoader resourceLoader,
            Interceptor[] interceptors,
            DatabaseIdProvider databaseIdProvider,
            ApplicationContext applicationContext
    ) throws Exception {
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        if (StringUtils.hasText(properties.getConfigLocation())) {
            factory.setConfigLocation(resourceLoader.getResource(properties.getConfigLocation()));
        }
        applyConfiguration(factory, properties);
        if (properties.getConfigurationProperties() != null) {
            factory.setConfigurationProperties(properties.getConfigurationProperties());
        }
        if (!ObjectUtils.isEmpty(interceptors)) {
            factory.setPlugins(interceptors);
        }
        if (databaseIdProvider != null) {
            factory.setDatabaseIdProvider(databaseIdProvider);
        }
        if (StringUtils.hasLength(properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(properties.getTypeAliasesPackage());
        }
        // TODO 自定义枚举包
        if (StringUtils.hasLength(properties.getTypeEnumsPackage())) {
            factory.setTypeEnumsPackage(properties.getTypeEnumsPackage());
        }
        if (properties.getTypeAliasesSuperType() != null) {
            factory.setTypeAliasesSuperType(properties.getTypeAliasesSuperType());
        }
        if (StringUtils.hasLength(properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(properties.getTypeHandlersPackage());
        }
        if (!ObjectUtils.isEmpty(properties.resolveMapperLocations())) {
            factory.setMapperLocations(properties.resolveMapperLocations());
        }
        // TODO 此处必为非 NULL
        GlobalConfig globalConfig = properties.getGlobalConfig();
        //注入填充器
        if (applicationContext.getBeanNamesForType(MetaObjectHandler.class,
                false, false).length > 0) {
            MetaObjectHandler metaObjectHandler = applicationContext.getBean(MetaObjectHandler.class);
            globalConfig.setMetaObjectHandler(metaObjectHandler);
        }
        //注入主键生成器
        if (applicationContext.getBeanNamesForType(IKeyGenerator.class, false,
                false).length > 0) {
            IKeyGenerator keyGenerator = applicationContext.getBean(IKeyGenerator.class);
            globalConfig.getDbConfig().setKeyGenerator(keyGenerator);
        }
        //注入sql注入器
        if (applicationContext.getBeanNamesForType(ISqlInjector.class, false,
                false).length > 0) {
            ISqlInjector iSqlInjector = applicationContext.getBean(ISqlInjector.class);
            globalConfig.setSqlInjector(iSqlInjector);
        }
        factory.setGlobalConfig(globalConfig);
        return factory.getObject();
    }

    private void applyConfiguration(MybatisSqlSessionFactoryBean factory, MybatisPlusProperties properties) {
        MybatisConfiguration configuration = properties.getConfiguration();
        if (configuration == null && !StringUtils.hasText(properties.getConfigLocation())) {
            configuration = new MybatisConfiguration();
        }
//        if (configuration != null && !CollectionUtils.isEmpty(this.configurationCustomizers)) {
//            for (ConfigurationCustomizer customizer : this.configurationCustomizers) {
//                customizer.customize(configuration);
//            }
//        }
        factory.setConfiguration(configuration);
    }


    public SqlSessionTemplate getSqlSessionTemplate(SqlSessionFactory sqlSessionFactory, MybatisPlusProperties properties) {
        ExecutorType executorType = properties.getExecutorType();
        if (executorType != null) {
            return new SqlSessionTemplate(sqlSessionFactory, executorType);
        } else {
            return new SqlSessionTemplate(sqlSessionFactory);
        }
    }

}

```

实现 模板类，并配置包扫描 **com.example.oldguy.configurations.AppMybatisPlusConfiguration**
```
package com.example.oldguy.configurations;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;

/**
 * @ClassName: Test1DataSource
 * @Author: ren
 * @Description:
 * @CreateTIme: 2019/7/17 0017 下午 1:55
 **/
@MapperScan(basePackages = {
        "com.example.oldguy.modules.app.dao.jpas"
},
        sqlSessionTemplateRef = "appSqlSessionTemplate",
        sqlSessionFactoryRef = "appSqlSessionFactory"
)
@EnableConfigurationProperties(MybatisPlusProperties.class)
@Configuration
public class AppMybatisPlusConfiguration extends AbstractMybatisPlusConfiguration {

    @Bean(name = "appSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactoryBean(DataSource dataSource,
                                                   MybatisPlusProperties properties,
                                                   ResourceLoader resourceLoader,
                                                   ApplicationContext applicationContext) throws Exception {
        return getSqlSessionFactory(dataSource,
                properties,
                resourceLoader,
                null,
                null,
                applicationContext);
    }

    @Bean(name = "appSqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(MybatisPlusProperties properties,
                                                 @Qualifier("appSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return getSqlSessionTemplate(sqlSessionFactory, properties);
    }
}

```
3. 重写 flowable-modeler 中 **DatabaseConfiguration** 
> PS: 之前抽出的时候，只是直接使用 @Import（DatabaseConfiguration.class）。此处 多配置了一套mybatis-plus，会导致 出现两个 **appSqlSessionFactory **，**appSqlSessionTemplate**。所以需要 用 @Primary 指定框架内部的mybatis 作为默认的 
>
```
/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.oldguy.modules.modeler.configurations;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.ui.common.service.exception.InternalServerErrorException;
import org.flowable.ui.modeler.properties.FlowableModelerAppProperties;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
public class DatabaseConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(org.flowable.ui.modeler.conf.DatabaseConfiguration.class);

    protected static final String LIQUIBASE_CHANGELOG_PREFIX = "ACT_DE_";

    @Autowired
    protected FlowableModelerAppProperties modelerAppProperties;

    @Autowired
    protected ResourceLoader resourceLoader;

    protected static Properties databaseTypeMappings = getDefaultDatabaseTypeMappings();

    public static final String DATABASE_TYPE_H2 = "h2";
    public static final String DATABASE_TYPE_HSQL = "hsql";
    public static final String DATABASE_TYPE_MYSQL = "mysql";
    public static final String DATABASE_TYPE_ORACLE = "oracle";
    public static final String DATABASE_TYPE_POSTGRES = "postgres";
    public static final String DATABASE_TYPE_MSSQL = "mssql";
    public static final String DATABASE_TYPE_DB2 = "db2";

    public static Properties getDefaultDatabaseTypeMappings() {
        Properties databaseTypeMappings = new Properties();
        databaseTypeMappings.setProperty("H2", DATABASE_TYPE_H2);
        databaseTypeMappings.setProperty("HSQL Database Engine", DATABASE_TYPE_HSQL);
        databaseTypeMappings.setProperty("MySQL", DATABASE_TYPE_MYSQL);
        databaseTypeMappings.setProperty("Oracle", DATABASE_TYPE_ORACLE);
        databaseTypeMappings.setProperty("PostgreSQL", DATABASE_TYPE_POSTGRES);
        databaseTypeMappings.setProperty("Microsoft SQL Server", DATABASE_TYPE_MSSQL);
        databaseTypeMappings.setProperty(DATABASE_TYPE_DB2, DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/NT", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/NT64", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2 UDP", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/LINUX", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/LINUX390", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/LINUXX8664", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/LINUXZ64", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/LINUXPPC64", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/400 SQL", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/6000", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2 UDB iSeries", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/AIX64", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/HPUX", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/HP64", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/SUN", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/SUN64", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/PTX", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2/2", DATABASE_TYPE_DB2);
        databaseTypeMappings.setProperty("DB2 UDB AS400", DATABASE_TYPE_DB2);
        return databaseTypeMappings;
    }

    @Bean
    @Primary
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        String databaseType = initDatabaseType(dataSource);
        if (databaseType == null) {
            throw new FlowableException("couldn't deduct database type");
        }

        try {
            Properties properties = new Properties();
            properties.put("prefix", modelerAppProperties.getDataSourcePrefix());
            properties.put("blobType", "BLOB");
            properties.put("boolValue", "TRUE");

            properties.load(this.getClass().getClassLoader().getResourceAsStream("org/flowable/db/properties/" + databaseType + ".properties"));

            sqlSessionFactoryBean.setConfigurationProperties(properties);
            sqlSessionFactoryBean
                    .setMapperLocations(ResourcePatternUtils.getResourcePatternResolver(resourceLoader).getResources("classpath:/META-INF/modeler-mybatis-mappings/*.xml"));
            sqlSessionFactoryBean.afterPropertiesSet();
            return sqlSessionFactoryBean.getObject();
        } catch (Exception e) {
            throw new FlowableException("Could not create sqlSessionFactory", e);
        }

    }

    @Primary
    @Bean(destroyMethod = "clearCache") // destroyMethod: see https://github.com/mybatis/old-google-code-issues/issues/778
    public SqlSessionTemplate SqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    public Liquibase liquibase(DataSource dataSource) {
        LOGGER.info("Configuring Liquibase");

        Liquibase liquibase = null;
        try {
            DatabaseConnection connection = new JdbcConnection(dataSource.getConnection());
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
            database.setDatabaseChangeLogTableName(LIQUIBASE_CHANGELOG_PREFIX + database.getDatabaseChangeLogTableName());
            database.setDatabaseChangeLogLockTableName(LIQUIBASE_CHANGELOG_PREFIX + database.getDatabaseChangeLogLockTableName());

            liquibase = new Liquibase("META-INF/liquibase/flowable-modeler-app-db-changelog.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update("flowable");
            return liquibase;

        } catch (Exception e) {
            throw new InternalServerErrorException("Error creating liquibase database", e);
        } finally {
            closeDatabase(liquibase);
        }
    }

    protected String initDatabaseType(DataSource dataSource) {
        String databaseType = null;
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            String databaseProductName = databaseMetaData.getDatabaseProductName();
            LOGGER.info("database product name: '{}'", databaseProductName);
            databaseType = databaseTypeMappings.getProperty(databaseProductName);
            if (databaseType == null) {
                throw new FlowableException("couldn't deduct database type from database product name '" + databaseProductName + "'");
            }
            LOGGER.info("using database type: {}", databaseType);

        } catch (SQLException e) {
            LOGGER.error("Exception while initializing Database connection", e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOGGER.error("Exception while closing the Database connection", e);
            }
        }

        return databaseType;
    }

    private void closeDatabase(Liquibase liquibase) {
        if (liquibase != null) {
            Database database = liquibase.getDatabase();
            if (database != null) {
                try {
                    database.close();
                } catch (DatabaseException e) {
                    LOGGER.warn("Error closing database", e);
                }
            }
        }
    }

}

```
以上就完成了 mybatis-plus ， flowable-engine ，flowable-modeler 的整合。

--- 

##### 流程图部署：

###### 1. 获取流程模型 
集成 flowable-model 之后，可以调用 原本 bpmn 的操作 实体Model 的相关接口对 bpmn图进行操作 。 **org.flowable.ui.modeler.repository.ModelRepository**


```

    @Autowired
    protected ModelRepository modelRepository;

    /**
     * 获取流程设计器中存在的 模型列表
     *
     * @param key
     * @param modelType
     * @return
     */
    public List<ModelRsp> getModelList(String key, Integer modelType) {

        List<ModelRsp> records = new ArrayList<>();

        List<Model> modelList = modelRepository.findByKeyAndType(key, modelType);
        modelList.stream().forEach(obj -> {
            records.add(new ModelRsp(obj.getId(), obj.getKey(), obj.getName(), obj.getModelType()));
        });
        return records;
    }

```

###### 2. 将 Model 转换成为 Bpmn.xml 或者 InputStream

**org.flowable.ui.modeler.serviceapi.ModelService**

```
    @Autowired
    protected ModelService modelService;

    /**
     * 从流程库中获取bpmn20xml 文件
     *
     * @param modelProcessId
     * @return
     */
    public String getBPMNXmlFromModelId(String modelProcessId) {

        Model model = modelService.getModel(modelProcessId);
        if (null == model) {
            LOGGER.warn("没有找到 [ modelProcessId = " + modelProcessId + " ] 的流程定义");
            return "";
        }
        BpmnModel bpmnModel = modelService.getBpmnModel(model);
        byte[] xmlBytes = modelService.getBpmnXML(bpmnModel);
        return new String(xmlBytes);
    }


```

###### 3. 将 Model 部署到 flowable-engine 中

**org.flowable.engine.RepositoryService**

```

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    protected ModelService modelService;


   /**
     * 从 流程设计器 中获取 流程表单进行部署
     *
     * @param modelProcessId        流程设计器保存的流程定义ID
     * @param processDefinitionName 流程定义名称
     */
    public void developFromModeler(String modelProcessId, String processDefinitionName) {

        Model model = modelService.getModel(modelProcessId);
        BpmnModel bpmnModel = modelService.getBpmnModel(model);
        if (StringUtils.isEmpty(processDefinitionName)) {
            processDefinitionName = model.getName();
            System.out.println("model:" + model.getName());
        }
        repositoryService
                .createDeployment()
                .name(processDefinitionName)
                .addBpmnModel(processDefinitionName + ".bpmn", bpmnModel)
                .deploy();
    }

```

