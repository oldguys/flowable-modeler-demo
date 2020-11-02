package com.example.oldguy.modules.examples.controllers.handles;

import com.example.oldguy.common.dto.CommonRsp;
import com.example.oldguy.modules.flow.exceptions.FlowRuntimeException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @ClassName: ControllerExceptionHandle
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/11/2 0002 下午 2:04
 * @Version：
 **/
@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(FlowRuntimeException.class)
    public CommonRsp flowException(FlowRuntimeException e) {
        return new CommonRsp(e.getErrorCode(), e.getMessage());
    }
}
