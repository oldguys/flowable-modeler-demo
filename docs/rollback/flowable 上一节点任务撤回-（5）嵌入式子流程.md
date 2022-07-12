###flowable 上一节点任务撤回-（5）嵌入式子流程

> **场景**：本章主要描述 下一节点 嵌入式子流程 如何进行回退操作。
>
> ![嵌入式子流程](https://upload-images.jianshu.io/upload_images/14387783-53554c6a77e10108.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
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

 ![嵌入式子流程](https://upload-images.jianshu.io/upload_images/14387783-53554c6a77e10108.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

嵌入式子流程具有一定的特殊性，操作撤回的时候，比如 ：
1. 节点 A 进入子流程这种边界事件
2. 子流程中的 各节点的跳转 b-1 b-2 b-3

与之前一样，对系统的各种数据进行分析

![BpmnModel](https://upload-images.jianshu.io/upload_images/14387783-6edcba4578049f32.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![Process 与 SubProcess 共性](https://upload-images.jianshu.io/upload_images/14387783-fe8984d7fe88d1fa.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


![ACT_RU_EXECUTION](https://upload-images.jianshu.io/upload_images/14387783-72bee26dd010fef2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


1. 从debug数据中可以看到，SubProcess 中的节点 存在于 节点 SubProcess 中，不能直接通过 getElement 拿到

2. 然后 Process和 SubProcess 都实现于接口 org.flowable.bpmn.model.FlowElementsContainer，并且调用的接口 是 FlowElementsContainer.getFlowElement(String id);

所以 可以从这两处入手，进行功能编写


##### NextSubProcessRollbackOperateStrategy: 从嵌入式子流程撤回到普通节点

> 步骤：
> 1. 如果下一节点是子流程 判定 子流程中 是否具有 已完成任务，如果具有，则不具备撤回条件。如果正常则无所谓。
> 2. 像 之前一样的步骤，构建 Execution ，利用 Execution 生成 任务。（不过与普通任务不同 ，子流程前置 普通任务节点的 execution 已经结束，不是同一个，所以无法使用，需要根据边界子节点 父级节点 进行构建）
> 3. 处理掉已生成的 子流程任务，另外，由上图可以看出，子流程任务 b-1 具有 顶级 execution，这个也需要进行移除，不然会导致流程无法归档。
> 4. 清理掉相关依赖数据。
>
> 如果是嵌入式子流程 内部节点间处理，则处理方式基本和 普通节点的处理方式一致，只需要判断，如果 在 Process 找不到 flowElement，则在SubProcess进行找就行。所以之前的操作类直接满足效果。
>

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

判定是否具有已完成任务，此处需要判定标识是从 SubProcess 中获取的
~~~

    @Override
    public void existNextFinishedTask() {

        Map<String, SubProcess> subProcessMap = paramsTemplate.getSubProcessMap();

        List<HistoricTaskInstance> historicTaskInstances = CommandContextUtil.getHistoricTaskService(commandContext)
                .findHistoricTaskInstancesByQueryCriteria(
                        (HistoricTaskInstanceQueryImpl) new HistoricTaskInstanceQueryImpl()
                                .taskCompletedAfter(paramsTemplate.getHisTask().getEndTime())
                                .finished()
                );

        String key = subProcessMap.keySet().iterator().next();
        this.subProcess = subProcessMap.get(key);

        subProcess.getFlowElements().forEach(obj -> subProcessItemKeySet.add(obj.getId()));
        if (!historicTaskInstances.isEmpty()) {
            historicTaskInstances.forEach(obj -> {
                if (subProcessItemKeySet.contains(obj.getTaskDefinitionKey())) {
                    LOGGER.error("出现已完成任务，无法进行流程节点撤回: [" + obj + "]");
                    throw new FlowableRuntimeException("出现已完成任务，无法进行流程节点撤回");
                }
            });
        }

    }
~~~

构建 Execution：主要区别在于只能通过相邻节点进行获取，不然当出现多层级网关的时候，可能会出现层级错误bug
~~~
  @Override
    public void createExecution() {

        HistoricTaskInstance hisTask = paramsTemplate.getHisTask();
        ExecutionEntity executionEntity = CommandContextUtil.getExecutionEntityManager(commandContext).findById(hisTask.getExecutionId());

        /**
         *  subProcess 作为下一节点的时候，hisTask的execution会被关闭调。所以需要重新创建
         */
        if (null == executionEntity) {
            LOGGER.info("hisTask:execution 为 null");

            List<ExecutionEntity> executionEntityList = CommandContextUtil
                    .getExecutionEntityManager(commandContext)
                    .findExecutionsByParentExecutionAndActivityIds(hisTask.getProcessInstanceId(), paramsTemplate.getNextFlowIdList());
            if (executionEntityList.isEmpty()) {
                throw new FlowableRuntimeException("没有找到临近节点");
            }
            executionEntity = executionEntityList.get(0);
        }
        // 创建主线
        ExecutionEntity newExecution = CommandContextUtil.getExecutionEntityManager(commandContext).createChildExecution(executionEntity.getParent());

        // 创建新任务
        createExecution(newExecution);
        // 移除历史任务
        removeHisTask(hisTask);
    }

~~~
删除历史连线
~~~
    @Override
    public void deleteHisActInstance() {

        List<ActivityInstance> activityInstanceEntityList = CommandContextUtil.getActivityInstanceEntityManager(commandContext)
                .findActivityInstancesByQueryCriteria(
                        new ActivityInstanceQueryImpl()
                                .processInstanceId(paramsTemplate.getHisTask().getProcessInstanceId())
                );


        List<String> ids = new ArrayList<>();
        activityInstanceEntityList.forEach(obj -> {
            // 时间大于 任务创建时间 之后线条
            if (obj.getStartTime().getTime() > paramsTemplate.getHisTask().getCreateTime().getTime()
                    && subProcessItemKeySet.contains(obj.getActivityId())) {
                ids.add(obj.getId());
            }
            // 当前任务的连线 ID
            if (paramsTemplate.getHisTask().getTaskDefinitionKey().equals(obj.getActivityId())
                    && obj.getEndTime().getTime() > paramsTemplate.getHisTask().getCreateTime().getTime()
            ) {
                ids.add(obj.getId());
            }
        });

        LOGGER.debug("移除历史任务连线");
        ids.forEach(obj -> historyActivityInstanceMapper.delete(obj));

    }
~~~
删除正在进行任务
~~~
    @Override
    public void deleteRuntimeTasks() {
        HistoricTaskInstance hisTask = paramsTemplate.getHisTask();

        List<TaskEntity> taskEntityList = CommandContextUtil.getTaskService(commandContext).findTasksByProcessInstanceId(hisTask.getProcessInstanceId());

        taskEntityList.forEach(obj -> {
            if (subProcessItemKeySet.contains(obj.getTaskDefinitionKey())) {
                LOGGER.info("移除正在执行的下一节点任务");
                // 移除任务
                removeRuntimeTaskOperate(obj);
            }
        });

        // 获取 subProcess 的 ExecutionEntity
        Collection<ExecutionEntity> executionEntities = CommandContextUtil
                .getExecutionEntityManager(commandContext)
                .findExecutionsByParentExecutionAndActivityIds(hisTask.getProcessInstanceId(), Collections.singletonList(subProcess.getId()));

        executionEntities.forEach(obj -> {
            LOGGER.info("移除 subProcess 层级execution");
            List<ExecutionEntity> children = CommandContextUtil
                    .getExecutionEntityManager(commandContext)
                    .findChildExecutionsByParentExecutionId(obj.getId());

            // 移除级联子节点
            children.forEach(item -> CommandContextUtil
                    .getExecutionEntityManager(commandContext)
                    .delete(item));

            // 移除 subProcess 顶级
            CommandContextUtil
                    .getExecutionEntityManager(commandContext)
                    .delete(obj);
        });

    }

~~~

以上 完成对 上一节点任务撤回 架构设计 及 第 5 种情况 解决方案讲述。
git地址：[https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_examples](https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_examples)