package com.example.oldguy.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @ClassName: ErrorCode
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/11/2 0002 下午 2:06
 * @Version：
 **/
@Getter
@AllArgsConstructor
public enum ErrorCode {

    /**
     * 批量接口异常
     */
    FLOW_BATCH_EXCEPTION(10001, "批处理异常"),
    /**
     * 功能异常
     */
    FLOW_EXCEPTION(10002, "功能异常"),

    /**
     * 流程条件表达式参数不完整
     */
    FLOW_EL_PROPERTY_NOT_FOUND_EXCEPTION(10003,"流程条件表达式参数不完整！"),

    /**
     *  线程池事务提交失败
     */
    THREAD_POOL_EXECUTOR_SPRING_TX_EXCEPTION(10004,"线程池事务提交失败！"),


    ;



    private Integer code;
    private String msg;
}
