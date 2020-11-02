package com.example.oldguy.modules.flow.services.batchs.commons;

import com.example.oldguy.modules.flow.services.batchs.CommonThreadExecutionService;
import com.example.oldguy.modules.flow.services.batchs.ThreadExecution;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ClassName: DefaultCommonThreadExecutionServiceImpl
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/11/2 0002 下午 1:50
 * @Version：
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class DefaultCommonThreadExecutionServiceBean implements CommonThreadExecutionService {
    @Override
    public int executeBatch(ThreadExecution threadExecution, List sequence) {
        threadExecution.threadExecute(sequence);
        return 0;
    }
}
