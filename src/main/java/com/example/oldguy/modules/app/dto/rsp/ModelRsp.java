package com.example.oldguy.modules.app.dto.rsp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: ModelRsp
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/11 0011 下午 7:26
 * @Version：
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelRsp {

    @ApiModelProperty("模型ID")
    private String id;

    @ApiModelProperty("流程key")
    private String key;

    @ApiModelProperty("流程名称")
    private String name;

    @ApiModelProperty("类别")
    private Integer type;
}
