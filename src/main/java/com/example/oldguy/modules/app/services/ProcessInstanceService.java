package com.example.oldguy.modules.app.services;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
