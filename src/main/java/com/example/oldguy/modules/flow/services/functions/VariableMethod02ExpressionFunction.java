package com.example.oldguy.modules.flow.services.functions;

import com.example.oldguy.common.utils.SpringContextUtils;
import com.example.oldguy.constants.MyFlowableConstants;
import com.example.oldguy.modules.examples.services.QueryService;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.impl.el.function.AbstractFlowableVariableExpressionFunction;
import org.flowable.variable.api.delegate.VariableScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName: VariableQueryExpressionFunction
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/3/19 0019 下午 9:37
 * @Version：
 **/
public class VariableMethod02ExpressionFunction extends AbstractFlowableVariableExpressionFunction {

    private static Logger LOGGER = LoggerFactory.getLogger(VariableMethod02ExpressionFunction.class);

    private QueryService queryService;

    public VariableMethod02ExpressionFunction(String variableScopeName) {
        super(variableScopeName, "method02");
        this.queryService = SpringContextUtils.getBean(QueryService.class);
    }

    @Override
    protected boolean isMultiParameterFunction() {
        return false;
    }

    public static boolean method02(VariableScope variableScope, String variableNames) {

        LOGGER.info("处理特殊表达式:method02");

        String[] variables = StringUtils.split(variableNames, MyFlowableConstants.SEPARATOR);
        Object variableValue = getVariableValue(variableScope, variables[0]);

        if (variableValue instanceof Number) {
            return ((Number) variableValue).intValue() > 10;
        }

        return false;
    }

}
