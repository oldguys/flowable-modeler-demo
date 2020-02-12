package com.example.oldguy.services;

import com.example.oldguy.modules.app.services.ProcessDefinitionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @ClassName: ProcessDefinitionServiceTests
 * @Author: ren
 * @Description:
 * @CreateTIme: 2020/2/12 0012 下午 1:44
 **/
@SpringBootTest
public class ProcessDefinitionServiceTests {


    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Test
    public void test(){
        String modelId = "ff11aa57-4d58-11ea-a418-283a4d3b99a3";
        String xml = processDefinitionService.getBPMNXmlFromModelId(modelId);
        System.out.println(xml);
    }
}
