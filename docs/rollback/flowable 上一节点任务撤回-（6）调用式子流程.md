> **场景**：本章主要描述 下一节点为调用子流程 如何进行回退操作。
>
> ![调用式子流程](https://upload-images.jianshu.io/upload_images/14387783-ffc40b1d7c866da4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
> 
>
> **上一章：**[flowable 上一节点任务撤回-（4）多重网关](https://www.jianshu.com/p/9801acf01ceb)
> **下一章：**flowable 上一节点任务撤回-（2）会签任务（已完成）
>  
> **环境**：
>   springboot：2.2.0.RELEASE
>   flowable：6.4.2
>
> git地址：[https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_examples](https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_examples)
>
> 所有章节：
> 1. [flowable 上一节点任务撤回-（1）普通节点撤回](https://www.jianshu.com/p/ee42924ed029)
> 2. [flowable 上一节点任务撤回-（2）会签任务（已完成）](https://www.jianshu.com/p/93fc02bb31d7)
> 2. [flowable 上一节点任务撤回-（3）会签任务（正在执行中）](https://www.jianshu.com/p/6daf767b1084)
> 2. [标题](链接地址)
> 2. [标题](链接地址)
> 2. [标题](链接地址)

**主流程：**
![调用式子流程](https://upload-images.jianshu.io/upload_images/14387783-ffc40b1d7c866da4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

**被调用流程：**
![被调用流程](https://upload-images.jianshu.io/upload_images/14387783-401e59c944bd7c75.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![ACT_RU_EXECUTION](https://upload-images.jianshu.io/upload_images/14387783-a46a47ed5ea1f6a9.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


![ACT_RU_TASK](https://upload-images.jianshu.io/upload_images/14387783-f6f4537325bddb64.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

>  调用式 子流程实质流程 2个独立流程 ，只不过一个流程的 SuperExecution是另一个节点的 executionId，在子流程完成之后，触发主流程的execution完成
> 
> 

##### NextCallActivityRollbackOperateStrategy：调用子流程 撤回策略

> 步骤：
> 1. 判断下一节点的流程任务是否已经完成，可以利用接口：(原理是：顶级Execution的 ID 实质就行 ProcessInstance Id 的特性进行处理)
> ~~~
>         callActivityExecutionList = > CommandContextUtil.getExecutionEntityManager(commandContext)
>                .findExecutionsByParentExecutionAndActivityIds(hisTask.getProcessInstanceId(), Collections.singletonList(callActivity.getId()));
>
>        // callActivity 在 父级流程的 executionId = 子流程的 processInstanceId
>        ExecutionEntity executionEntity = callActivityExecutionList.get(0);
> ~~~
> 找到子流程的流程实例之后，对获取已完成任务列表就轻松多了。然后采取之前的逻辑，如果未进行任务操作，则可以进行回退的前置条件。
>
> 2. 创建任务 execution，与普通节点一致，注意像SubProcess从相邻节点获取Execution，毕竟原来任务executio已经消失。
> 3. 删除正在执行任务，注意需要把 顶级节点 Execution和 主流程的 execution删除。
> 4. 处理相关数据


##### 基于模板方法模式，构建编写通用 RollbackOperateStrategy.process() 操作
~~~
    @Override
    public void process(CommandContext commandContext, String assignee, Map<String, Object> variables) {

        this.commandContext = commandContext;
        this.assignee = assignee;
        this.variables = variables;

        log.info("处理 existNextFinishedTask");
        existNextFinishedTask();
        log.info("配置任务执行人 setAssignee");
        setAssignee();
        log.info("处理 createExecution");
        createExecution();
        log.info("处理 deleteRuntimeTasks");
        deleteRuntimeTasks();
        log.info("处理 deleteHisActInstance");
        deleteHisActInstance();
    }
~~~

##### 判断已完成任务策略 existNextFinishedTask()
~~~
 @Override
    public void existNextFinishedTask() {

        HistoricTaskInstance hisTask = paramsTemplate.getHisTask();

        Map<String, CallActivity> callActivityMap = paramsTemplate.getCallActivityMap();
        String key = callActivityMap.keySet().iterator().next();
        this.callActivity = callActivityMap.get(key);

        // 下一节点callActivity的 flowId
        callActivityExecutionList = CommandContextUtil.getExecutionEntityManager(commandContext)
                .findExecutionsByParentExecutionAndActivityIds(hisTask.getProcessInstanceId(), Collections.singletonList(callActivity.getId()));

        // callActivity 在 父级流程的 executionId = 子流程的 processInstanceId
        ExecutionEntity executionEntity = callActivityExecutionList.get(0);

        // 子流程
        callActivityProcess = CommandContextUtil.getExecutionEntityManager(commandContext)
                .findSubProcessInstanceBySuperExecutionId(executionEntity.getId());

        List<HistoricTaskInstance> hisTaskList = CommandContextUtil.getHistoricTaskService(commandContext)
                .findHistoricTaskInstancesByQueryCriteria(
                        (HistoricTaskInstanceQueryImpl) new HistoricTaskInstanceQueryImpl()
                                .finished()
                                .processInstanceId(callActivityProcess.getId())
                );

        if (!hisTaskList.isEmpty()) {
            throw new FlowableRuntimeException("子流程已经具有完成的任务,流程无法回退");
        }
    }
~~~

##### createExecution()
~~~

    @Override
    public void createExecution() {
        HistoricTaskInstance hisTask = paramsTemplate.getHisTask();
        ExecutionEntity executionEntity = CommandContextUtil.getExecutionEntityManager(commandContext)
                .findById(hisTask.getExecutionId());

        if (null == executionEntity) {
            log.info("没有找到execution");
            executionEntity = callActivityExecutionList.get(0);
        }

        ExecutionEntity newExecution = CommandContextUtil.getExecutionEntityManager(commandContext)
                .createChildExecution(executionEntity.getParent());

        // 创建新任务
        createExecution(newExecution);
        // 移除历史任务
        removeHisTask(hisTask);
    }
~~~

清除已经生成任务
~~~
    @Override
    public void deleteRuntimeTasks() {

        ExecutionEntity parentExecution = callActivityExecutionList.get(0);


        // 清理子流程
        cleanCallActivityProcessInstance(callActivityProcess);
        // 清理主流程记录
        CommandContextUtil.getExecutionEntityManager(commandContext)
                .delete(parentExecution);

    }

    /**
     * // 无效操作
     * CommandContextUtil.getExecutionEntityManager(commandContext)
     * .deleteProcessInstance(callActivityProcess.getId(), "进行流程撤回", false);
     * 清理 调用子流程 相关数据
     *
     * @param processInstance
     */
    private void cleanCallActivityProcessInstance(ExecutionEntity processInstance) {
        // 移除正在运行任务信息
        List<Task> list = CommandContextUtil.getTaskService(commandContext)
                .createTaskQuery()
                .processInstanceId(processInstance.getId())
                .list();
        list.forEach(obj->removeRuntimeTaskOperate((TaskEntity) obj));

        // 移除历史任务信息
        List<HistoricTaskInstanceEntity> historicTaskInstanceList = CommandContextUtil.getHistoricTaskService(commandContext)
                .findHistoricTasksByProcessInstanceId(processInstance.getId());
        historicTaskInstanceList.forEach(obj->CommandContextUtil.getHistoricTaskService(commandContext).deleteHistoricTask(obj));

        // 移除 子流程实例
        CommandContextUtil.getIdentityLinkService(commandContext).deleteIdentityLinksByProcessInstanceId(processInstance.getId());
        CommandContextUtil.getVariableService(commandContext).deleteVariablesByExecutionId(processInstance.getId());
        CommandContextUtil.getExecutionEntityManager(commandContext).delete(processInstance.getId());
    }
~~~

以上 完成对 上一节点任务撤回 架构设计 及 第 6 种情况 解决方案讲述。
git地址：[https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_examples](https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_examples)

总结：
基本上流程撤回的实现思路就是围绕着：模拟数据流，难点在于需要分析比较几种主要节点的特性，当节点特性本质熟悉之后。基于这5大类进行组合重写策略就能解决更加复杂的场景，见招拆招，以数据流本质应万变就行。