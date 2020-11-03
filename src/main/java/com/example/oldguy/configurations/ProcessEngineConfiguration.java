package com.example.oldguy.configurations;

import com.example.oldguy.modules.flow.listeners.GlobalTaskListener;
import com.example.oldguy.modules.flow.services.functions.VariableMethod01ExpressionFunction;
import com.example.oldguy.modules.flow.services.functions.VariableMethod02ExpressionFunction;
import com.example.oldguy.modules.flow.services.functions.VariableQueryExpressionFunction;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huangrenhao
 * @date 2018/8/13
 * @Descripton 配置字符集
 */
@Slf4j
@Configuration
public class ProcessEngineConfiguration implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {

    @Autowired
    private GlobalTaskListener globalTaskListener;

    @Override
    public void configure(SpringProcessEngineConfiguration processEngineConfiguration) {

        processEngineConfiguration.setActivityFontName("宋体");
        processEngineConfiguration.setLabelFontName("宋体");
        processEngineConfiguration.setAnnotationFontName("宋体");
        log.info("配置字体:" + processEngineConfiguration.getActivityFontName());

        // 配置表达式
        initExpressFunction(processEngineConfiguration);
        // 配置全局监听器
        initGlobalListeners(processEngineConfiguration);
    }

    private void initGlobalListeners(SpringProcessEngineConfiguration processEngineConfiguration) {
        log.info("配置全局监听器");
        Map<String, List<FlowableEventListener>> typedListeners = new HashMap<>(1);
        typedListeners.put(FlowableEngineEventType.TASK_CREATED.name(), Arrays.asList(globalTaskListener));

        processEngineConfiguration.setTypedEventListeners(typedListeners);
    }

    /**
     * 配置扩展表达式解析方法
     *
     * @param springProcessEngineConfiguration
     */
    private void initExpressFunction(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
        log.info("配置扩展表达式解析方法");
        String variableScopeName = "execution";
        springProcessEngineConfiguration.initShortHandExpressionFunctions();
        springProcessEngineConfiguration.getShortHandExpressionFunctions().add(new VariableQueryExpressionFunction(variableScopeName));
        springProcessEngineConfiguration.getShortHandExpressionFunctions().add(new VariableMethod01ExpressionFunction(variableScopeName));
        springProcessEngineConfiguration.getShortHandExpressionFunctions().add(new VariableMethod02ExpressionFunction(variableScopeName));
    }
}
