package com.example.oldguy.modules.app.controllers;

import com.example.oldguy.common.dto.CommonRsp;
import com.example.oldguy.modules.app.dto.rsp.ModelRsp;
import com.example.oldguy.modules.app.dto.rsp.ProcessDefinitionRsp;
import com.example.oldguy.modules.app.services.ProcessDefinitionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @ClassName: ProcessDefinitionController
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/10 0010 下午 10:03
 * @Version：
 **/
@Api(tags = "流程定义管理")
@RequestMapping("process/definition")
@RestController
public class ProcessDefinitionController {

    @Autowired
    private ProcessDefinitionService processDefinitionService;


    @ApiOperation("已部署流程定义")
    @GetMapping("active/list")
    public CommonRsp<List<ProcessDefinitionRsp>> getActiveProcessList() {
        List<ProcessDefinitionRsp> records = processDefinitionService.getProcessDefinitions();
        return new CommonRsp<>(records);
    }

    @ApiOperation("流程设计器中保存的模型列表")
    @GetMapping("modeler/model/list")
    public CommonRsp<List<ModelRsp>> getModelList(String key, Integer modelType) {

        if (StringUtils.isEmpty(key)) {
            key = null;
        }
        if (null == modelType) {
            modelType = null;
        }
        List<ModelRsp> records = processDefinitionService.getModelList(key, modelType);
        return new CommonRsp<>(records);
    }

    @ApiOperation("从流程设计器获取bpmn20.xml")
    @GetMapping("/modeler/{modelId}/bpmn20")
    public void getBpmn20Xml(@PathVariable String modelId, HttpServletResponse response) throws IOException {
        String xml = processDefinitionService.getBPMNXmlFromModelId(modelId);
        if (!StringUtils.isEmpty(xml)) {
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(xml);
            response.getWriter().close();
        }
    }

    @ApiOperation("部署流程设计器中流程")
    @PostMapping("modeler/develop/")
    public CommonRsp developFromModeler(
            @RequestParam String modelerId,
            @RequestParam String processDefinitionName) {

        processDefinitionService.developFromModeler(modelerId, processDefinitionName);
        return CommonRsp.SUCCESS;
    }

}
