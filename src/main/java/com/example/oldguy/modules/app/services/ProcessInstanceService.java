package com.example.oldguy.modules.app.services;

import com.example.oldguy.modules.app.dto.rsp.ProcessInstanceRsp;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: ProcessInstanceService
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/2/11 0011 上午 11:50
 * @Version：
 **/
@Service
public class ProcessInstanceService {


    @Autowired
    private RuntimeService runtimeService;

    public List<ProcessInstanceRsp> getRuntimeProcessInstanceList() {

        List<ProcessInstance> list = runtimeService.createProcessInstanceQuery().list();
        List<ProcessInstanceRsp> records = new ArrayList<>();

        list.stream().forEach(obj->{

            ProcessInstanceRsp rsp = new ProcessInstanceRsp();
            rsp.setProcessInstanceId(obj.getId());
            rsp.setProcessDefinitionId(obj.getProcessDefinitionId());
            rsp.setProcessDefinitionName(obj.getProcessDefinitionName());
            records.add(rsp);

        });
        return records;
    }

    /**
     *  开启流程实例
     * @param processDefinitionKey
     * @return
     */
    public ProcessInstance startProcessInstance(String processDefinitionKey){
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey);
        return processInstance;
    }


}
