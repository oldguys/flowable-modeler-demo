package com.example.oldguy.configurations;

import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


/**
 * @ClassName: SwaggerConfiguration
 * @Author: ren
 * @Description:
 * @CreateTIme: 2019/6/17 0017 下午 12:30
 **/
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    private static Logger LOGGER = LoggerFactory.getLogger(SwaggerConfiguration.class);

    @Value("${spring.application.name}")
    private String appName;

    @Bean
    public Docket createRestApi() {

        LOGGER.info("初始化swagger");

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.oldguy"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(appName)
                .description("swagger-bootstrap-ui")
//                .termsOfServiceUrl("http://localhost:8999/")
//                .contact("developer@mail.com")
                .version("1.0")
                .build();
    }
}