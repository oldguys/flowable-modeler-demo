package com.example.oldguy.modules.flow.exceptions;

import com.example.oldguy.common.dto.ErrorCode;
import lombok.Getter;

/**
 * @ClassName: FlowRuntimeException
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/12 0012 上午 10:01
 * @Version：
 **/
@Getter
public class FlowRuntimeException extends RuntimeException {

    private ErrorCode errorCode;

    public FlowRuntimeException(String message) {
        super(message);
        this.errorCode = ErrorCode.FLOW_EXCEPTION;
    }

    public FlowRuntimeException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
