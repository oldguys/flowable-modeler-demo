package com.example.oldguy.modules.examples.functions;

import com.example.oldguy.common.utils.SpringContextUtils;
import com.example.oldguy.constants.MyFlowableConstants;
import com.example.oldguy.modules.examples.services.QueryService;
import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.impl.el.function.AbstractFlowableVariableExpressionFunction;
import org.flowable.variable.api.delegate.VariableScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: VariableQueryExpressionFunction
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/3/19 0019 下午 9:37
 * @Version：
 **/
public class VariableMethod01ExpressionFunction extends AbstractFlowableVariableExpressionFunction {

    private static Logger LOGGER = LoggerFactory.getLogger(VariableMethod01ExpressionFunction.class);

    private QueryService queryService;

    public VariableMethod01ExpressionFunction(String variableScopeName) {
        super(variableScopeName, "method01");
        this.queryService = SpringContextUtils.getBean(QueryService.class);
    }

    @Override
    protected boolean isMultiParameterFunction() {
        return false;
    }

    public static double method01(VariableScope variableScope, String variableNames) {

        LOGGER.info("处理特殊表达式:method01");

        String[] variables = StringUtils.split(variableNames, MyFlowableConstants.SEPARATOR);

        double total = 0;

        for (String var : variables) {
            Object variableValue = getVariableValue(variableScope, var);
            if (variableValue instanceof Number) {
                total += ((Number) variableValue).doubleValue();
            }
        }

        return total / variables.length;
    }

}
