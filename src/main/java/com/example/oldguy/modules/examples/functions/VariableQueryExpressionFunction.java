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
public class VariableQueryExpressionFunction extends AbstractFlowableVariableExpressionFunction {

    private static Logger LOGGER = LoggerFactory.getLogger(VariableQueryExpressionFunction.class);


    public VariableQueryExpressionFunction(String variableScopeName) {
        super(variableScopeName, "query");
    }

    @Override
    protected boolean isMultiParameterFunction() {
        return false;
    }

    /**
     *  实现自定义函数
     *      与上面构造方法中的函数名相同
     * @param variableScope
     * @param variableNames
     * @return
     */
    public static String query(VariableScope variableScope, String variableNames) {

        LOGGER.info("处理特殊表达式:query");

        List<String> result = new ArrayList<>();
        String[] variables = StringUtils.split(variableNames, MyFlowableConstants.SEPARATOR);

        for (String var : variables) {
            Object variableValue = getVariableValue(variableScope, var);
            result.add(variableValue.toString());
        }
        QueryService queryService = SpringContextUtils.getBean(QueryService.class);
        return queryService.querySomeThing(result);
    }

}
