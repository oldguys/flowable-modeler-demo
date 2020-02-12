package com.example.oldguy.common.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: CommonRsp
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/11 0011 下午 12:57
 * @Version：
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonRsp<T> {

    public static CommonRsp SUCCESS = new CommonRsp();

    @ApiModelProperty("返回值状态")
    private Integer status = 0;

    @ApiModelProperty("返回数据")
    private T data;

    public CommonRsp(T data) {
        this.data = data;
    }
}
