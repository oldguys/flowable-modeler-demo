package com.example.oldguy.modules.examples.constants;



public interface AddTaskConstants {

    /**
     *  默认审核人
     */
    String ASSIGNEE_USER = "assignee";

    /**
     *  审核人集合
     */
    String ASSIGNEE_LIST = "assigneeList";

    /**
     *  历史变量集合
     */
    String HIS_FLAG = "his";

    /**
     *  会签任务总数
     */
    String NUMBER_OF_INSTANCES = "nrOfInstances";

    /**
     *  正在执行的会签总数
     */
    String NUMBER_OF_ACTIVE_INSTANCES = "nrOfActiveInstances";

    /**
     *  已完成的会签任务总数
     */
    String NUMBER_OF_COMPLETED_INSTANCES = "nrOfCompletedInstances";

    /**
     *  会签任务表示
     *  collectionElementIndexVariable
     */
    String LOOP_COUNTER = "loopCounter";

    String ASSIGNEE_LIST_EXPR = "${assigneeList}";

    String ASSIGNEE_FLAG = "assignee";

    String ASSIGNEE_EXPR = "${assignee}";
}
