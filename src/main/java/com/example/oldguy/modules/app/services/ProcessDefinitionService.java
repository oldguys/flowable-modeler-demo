package com.example.oldguy.modules.app.services;

import com.example.oldguy.modules.app.dto.rsp.ModelRsp;
import com.example.oldguy.modules.app.dto.rsp.ProcessDefinitionRsp;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.repository.ModelRepository;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: ProcessDefinitionService
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/10 0010 下午 10:05
 * @Version：
 **/
@Service
public class ProcessDefinitionService {

    private static Logger LOGGER = LoggerFactory.getLogger(ProcessDefinitionService.class);

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    protected ModelService modelService;
    @Autowired
    protected ModelRepository modelRepository;

    /**
     *  当前有效的 流程定义 列表
     * @return
     */
    public List<ProcessDefinitionRsp> getProcessDefinitions() {

        List<ProcessDefinitionRsp> records = new ArrayList<>();
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().active().list();

        list.stream().forEach(obj -> {
            records.add(new ProcessDefinitionRsp(obj.getId(), obj.getName(), obj.getDeploymentId()));
        });
        return records;
    }

    /**
     * 获取流程设计器中存在的 模型列表
     *
     * @param key
     * @param modelType
     * @return
     */
    public List<ModelRsp> getModelList(String key, Integer modelType) {

        List<ModelRsp> records = new ArrayList<>();

        List<Model> modelList = modelRepository.findByKeyAndType(key, modelType);
        modelList.stream().forEach(obj -> {
            records.add(new ModelRsp(obj.getId(), obj.getKey(), obj.getName(), obj.getModelType()));
        });
        return records;
    }

    /**
     * 从流程库中获取bpmn20xml 文件
     *
     * @param modelProcessId
     * @return
     */
    public String getBPMNXmlFromModelId(String modelProcessId) {

        Model model = modelService.getModel(modelProcessId);
        if (null == model) {
            LOGGER.warn("没有找到 [ modelProcessId = " + modelProcessId + " ] 的流程定义");
            return "";
        }
        BpmnModel bpmnModel = modelService.getBpmnModel(model);
        byte[] xmlBytes = modelService.getBpmnXML(bpmnModel);
        return new String(xmlBytes);
    }

    /**
     * 从 流程设计器 中获取 流程表单进行部署
     *
     * @param modelProcessId        流程设计器保存的流程定义ID
     * @param processDefinitionName 流程定义名称
     */
    public void developFromModeler(String modelProcessId, String processDefinitionName) {

        Model model = modelService.getModel(modelProcessId);
        BpmnModel bpmnModel = modelService.getBpmnModel(model);
        if (StringUtils.isEmpty(processDefinitionName)) {
            processDefinitionName = model.getName();
            System.out.println("model:" + model.getName());
        }
        repositoryService
                .createDeployment()
                .name(processDefinitionName)
                .addBpmnModel(processDefinitionName + ".bpmn", bpmnModel)
                .deploy();
    }


}
