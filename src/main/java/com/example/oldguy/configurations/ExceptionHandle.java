package com.example.oldguy.configurations;

import com.example.oldguy.common.dto.CommonRsp;
import com.example.oldguy.modules.examples.exceptions.FlowableRuntimeException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @ClassName: ExceptionHandle
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/20 0020 下午 9:13
 * @Version：
 **/
@RestControllerAdvice
public class ExceptionHandle {

    @ExceptionHandler(FlowableRuntimeException.class)
    public CommonRsp flowableRuntimeException(FlowableRuntimeException e) {
        return new CommonRsp(500, e.getMessage());
    }

}
