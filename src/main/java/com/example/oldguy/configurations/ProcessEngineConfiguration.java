package com.example.oldguy.configurations;

import com.example.oldguy.modules.flow.services.functions.VariableMethod01ExpressionFunction;
import com.example.oldguy.modules.flow.services.functions.VariableMethod02ExpressionFunction;
import com.example.oldguy.modules.flow.services.functions.VariableQueryExpressionFunction;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * @author huangrenhao
 * @date 2018/8/13
 * @Descripton 配置字符集
 */
@Configuration
public class ProcessEngineConfiguration  implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {

    private static Logger LOGGER = LoggerFactory.getLogger(ProcessEngineConfiguration.class);

    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {

        processEngineConfiguration.setActivityFontName("宋体");
        processEngineConfiguration.setLabelFontName("宋体");
        processEngineConfiguration.setAnnotationFontName("宋体");
        LOGGER.info("配置字体:" + processEngineConfiguration.getActivityFontName());

//         配置表达式
        initExpressFunction(processEngineConfiguration);
    }

    /**
     *
     *  配置扩展表达式解析方法
     * @param springProcessEngineConfiguration
     */
    private void initExpressFunction(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
        LOGGER.info("配置扩展表达式解析方法");
        String variableScopeName = "execution";
        springProcessEngineConfiguration.initShortHandExpressionFunctions();
        springProcessEngineConfiguration.getShortHandExpressionFunctions().add(new VariableQueryExpressionFunction(variableScopeName));
        springProcessEngineConfiguration.getShortHandExpressionFunctions().add(new VariableMethod01ExpressionFunction(variableScopeName));
        springProcessEngineConfiguration.getShortHandExpressionFunctions().add(new VariableMethod02ExpressionFunction(variableScopeName));
    }
}
