package com.example.oldguy.modules.flow.services.cmd.rollback;

/**
 * @ClassName: RollbackConstants
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/3/31 0031 上午 10:52
 * @Version：
 **/
public interface RollbackConstants {



    /**
     *  配置 任务执行人变量
     */
    String ASSIGNEE_PREFIX_KEY = "ROLLBACK_ASSIGNEE_PREFIX_";

    /**
     *  配置 任务执行人变量
     */
    String TASK_TYPE_PREFIX_KEY = "ROLLBACK_TASK_TYPE_PREFIX_";


    /**
     *  会签任务变量
     */
    interface MultiInstanceConstants{

        String NR_OF_ACTIVE_INSTANCES = "nrOfActiveInstances";
        String NR_OF_COMPLETE_INSTANCES = "nrOfCompletedInstances";
        String NR_OF_INSTANCE = "nrOfInstances";

        String LOOP_COUNTER = "loopCounter";
    }
}
