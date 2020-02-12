package com.example.oldguy.configurations;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.ProcessEngineConfigurationConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author huangrenhao
 * @date 2018/8/13
 * @Descripton 配置字符集
 */
//@Component
public class ProcessEngineConfiguration implements ProcessEngineConfigurationConfigurer {

    private static Logger LOGGER = LoggerFactory.getLogger(ProcessEngineConfiguration.class);

    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {
        processEngineConfiguration.setActivityFontName("宋体");
        processEngineConfiguration.setLabelFontName("宋体");
        processEngineConfiguration.setAnnotationFontName("宋体");
        LOGGER.info("配置字体:" + processEngineConfiguration.getActivityFontName());
    }
}
