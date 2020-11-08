package com.example.oldguy.modules.flow.listeners;

import com.example.oldguy.common.utils.SpringContextUtils;
import com.example.oldguy.modules.app.services.TaskActionLogService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.common.engine.impl.event.FlowableEntityEventImpl;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.stereotype.Component;

/**
 * @ClassName: GlobalTaskListener
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/11/2 0002 下午 5:37
 * @Version：
 **/
@Component
@Slf4j
public class GlobalTaskListener implements FlowableEventListener {

    @Override
    public void onEvent(FlowableEvent event) {
//        log.info("进行全局任务");
        if (FlowableEngineEventType.TASK_CREATED.name().equals(event.getType().name())) {

        } else if (FlowableEngineEventType.TASK_COMPLETED.name().equals(event.getType().name())) {

        }
    }

    @Override
    public boolean isFailOnException() {
        return true;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }
}
