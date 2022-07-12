> **场景**：本章主要描述 正在执行会签任务 如何进行回退操作。
> ![会签任务进行回退操作](https://upload-images.jianshu.io/upload_images/14387783-0c5f38e0438ef5c4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
>
> **上一章：**flowable 上一节点任务撤回-（2）会签任务（已完成）
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


#### 会签原理分析请参考上一章！
 
 1. 不同于 已完成任务， 正在执行任务会签任务回退不需要 考虑节点任务的跳转，所以不需要 对 历史任务 处理痕迹这些进行操作，只需要修改任务（execution依然存在）就行了。
2. 但是在前一章的基础上，又出现了 手动添加 已完成execution的问题，（回退任务的execution已经被删掉）所以此处需要解决由上一章引出的 一种解决方案。
根据 Execution 生命周期的问题，可以只作为判定是否需要 删除已完成任务 execution。


> 步骤：
> 1. 配置父级 execution 参数
nrOfActiveInstances = 原本nrOfActiveInstances  +1
nrOfCompletedInstances = 原本nrOfCompletedInstances - 1
>
> 2. 判定回退任务的 execution是否被删除掉，如果还存在，则直接修改 execution，对任务进行重新生成。如果 execution 已经被删除，即: 改流程已经到达下一个任务节点，已经进行过 回退，则需要删除 完成任务回退增加的 Execution（参考上一章）。
>
>注意使用      ：CommandContextUtil.getAgenda(commandContext).planContinueMultiInstanceOperation(newExecution, executionEntity, loopCounter);进行任务创建。
>
> 3. 如果是串行会签，则需要删除 下一完成任务，如果是并行，则忽略。
>

####

##### 基于模板方法模式，构建编写通用 RollbackOperateStrategy.process() 操~作
由于 正在执行 会签的特殊性，不需要维护边界节点的相关数据，所以这里在 实现策略类中，重写 process() 方法
~~~
    @Override
    public void process(CommandContext commandContext, String assignee, Map<String, Object> variables) {

        this.commandContext = commandContext;
        this.assignee = assignee;
        this.variables = variables;

        // 串行会签
        if (paramsTemplate.getCurrentTaskElement().getBehavior() instanceof SequentialMultiInstanceBehavior) {
            isSequential = true;
        } else if (paramsTemplate.getCurrentTaskElement().getBehavior() instanceof MultiInstanceActivityBehavior) {
            isSequential = false;
        }

        LOGGER.info("创建实例");
        createExecution();
    }
~~~

##### 创建execution
~~~
    @Override
    public void createExecution() {

        HistoricTaskInstance hisTask = paramsTemplate.getHisTask();

        List<Task> currentTaskList = CommandContextUtil.getTaskService(commandContext)
                .createTaskQuery()
                .processInstanceId(hisTask.getProcessInstanceId())
                .taskDefinitionKey(hisTask.getTaskDefinitionKey())
                .list();

        if (currentTaskList.isEmpty()) {
            String msg = "当前会签任务已经完成";
            throw new FlowableRuntimeException(msg);
        }

        if (!isSequential) {
            LOGGER.info("处理并行会签");
            processMultiInstance();
        } else {
            LOGGER.info("处理串行会签");
            processSequentialInstance();
        }


    }
~~~


##### 处理串行会签 processSequentialInstance
~~~
 /**
     * 处理串行会签
     * 串行特殊场景 : 下一个顺序执行人已完成任务，当前历史任务不可回退
     * a -> b -> c -> d
     * b 任务完成时， a 任务不可回退
     */
    private void processSequentialInstance() {

        HistoricTaskInstance hisTask = paramsTemplate.getHisTask();
        // 确认是否具有下一线性完成任务
        existNextFinishedTask(hisTask);

        // 进行任务回退操作
        ExecutionEntity executionEntity = processCommon(hisTask);


        if (executionEntity.getId().equals(hisTask.getExecutionId())) {
            log.info("未生成过下一节点");

            // 移除正在执行任务
            List<TaskEntity> taskEntityList = CommandContextUtil.getTaskService(commandContext).findTasksByExecutionId(executionEntity.getId());
            taskEntityList.forEach(obj -> {
                LOGGER.info("移除正在当前任务记录 [ id = " + obj.getId() + " ] ");
                CommandContextUtil.getTaskService(commandContext).deleteTask(obj, true);
                HistoricTaskInstance historicTaskInstance = CommandContextUtil.getHistoricTaskService(commandContext).getHistoricTask(obj.getId());
                CommandContextUtil.getHistoricTaskService(commandContext).deleteHistoricTask((HistoricTaskInstanceEntity) historicTaskInstance);
            });

            // 配置任务执行人
            executionEntity.setVariable(assigneeExpr, assignee);
            // 计数器前置一位
            int loopCounter = (int) executionEntity.getVariable(RollbackConstants.MultiInstanceConstants.LOOP_COUNTER);
            executionEntity.setVariable(RollbackConstants.MultiInstanceConstants.LOOP_COUNTER, loopCounter - 1);
            // 将任务重新激活
            executionEntity.setActive(true);
            // 创建新任务
            CommandContextUtil.getAgenda(commandContext).planContinueProcessInCompensation(executionEntity);
        } else {
            log.info("已生成过下一任务节点,无法找到当前 execution");

            List<Task> taskList = CommandContextUtil.getTaskService(commandContext).createTaskQuery()
                    .processInstanceId(hisTask.getProcessInstanceId())
                    .taskDefinitionKey(hisTask.getTaskDefinitionKey())
                    .list();

            Task currentTask = taskList.get(0);

            Integer currentLoopCounter = (Integer) runtimeService.getVariableLocal(currentTask.getExecutionId(), RollbackConstants.MultiInstanceConstants.LOOP_COUNTER);
            List<String> assigneeList = (List<String>) runtimeService.getVariableLocal(currentTask.getProcessInstanceId(), assigneeListExpr);

            if (StringUtils.isEmpty(hisTask.getAssignee())) {
                throw new FlowableRuntimeException("没有找到历史任务执行人,无法进行 执行顺序判断");
            }

            int index = assigneeList.indexOf(hisTask.getAssignee());
            if (index == -1) {
                throw new FlowableRuntimeException("执行人不存在于初始参数,无法进行 执行顺序判断");
            }

            if (index != currentLoopCounter - 1) {
                throw new FlowableRuntimeException("任务执行人不是 当前执行人的前位 , 不合法回退");
            }
            // 持久化变量
            assigneeList.set(index, assignee);
            runtimeService.setVariableLocal(currentTask.getProcessInstanceId(), assigneeListExpr, assigneeList);

            // 修改变量
            ExecutionEntity newExecution = CommandContextUtil.getExecutionEntityManager(commandContext).findById(currentTask.getExecutionId());
            newExecution.setVariableLocal(RollbackConstants.MultiInstanceConstants.LOOP_COUNTER, currentLoopCounter - 1);
            newExecution.setVariableLocal(assigneeExpr, assignee);

            // 创建新任务
            CommandContextUtil.getAgenda(commandContext).planContinueMultiInstanceOperation(newExecution, executionEntity, currentLoopCounter - 1);
            // 移除当前任务
            CommandContextUtil.getTaskService(commandContext).deleteTask((TaskEntity) currentTask, true);
            // 移除历史任务
            HistoricTaskInstance historicTaskInstance = CommandContextUtil.getHistoricTaskService(commandContext).getHistoricTask(currentTask.getId());
            CommandContextUtil.getHistoricTaskService(commandContext).deleteHistoricTask((HistoricTaskInstanceEntity) historicTaskInstance);
        }

        // 移除当前历史任务
        removeHisTask(hisTask);
    }

~~~

##### processMultiInstance
~~~
 /**
     * 处理并行会签
     */
    private void processMultiInstance() {
        HistoricTaskInstance hisTask = paramsTemplate.getHisTask();

        // 通用操作部分
        ExecutionEntity executionEntity = processCommon(hisTask);

        // 未生成过下一节点
        if (executionEntity.getId().equals(hisTask.getExecutionId())) {
            log.info("未生成过下一节点");
            // 配置任务执行人
            executionEntity.setVariable(assigneeExpr, assignee);
            // 将任务重新激活
            executionEntity.setActive(true);
            // 创建新任务
            CommandContextUtil.getAgenda(commandContext).planContinueProcessInCompensation(executionEntity);
        } else {
            log.info("已生成过下一任务节点,无法找到当前 execution");

            List<HistoricVariableInstance> historicVariableInstanceList = CommandContextUtil.getHistoricVariableService().findHistoricVariableInstancesByQueryCriteria(
                    new HistoricVariableInstanceQueryImpl()
                            .processInstanceId(hisTask.getProcessInstanceId())
                            .executionId(hisTask.getExecutionId())
            );

            Map<String, Object> hisVarMap = historicVariableInstanceList.stream().collect(Collectors.toMap(HistoricVariableInstance::getVariableName, item -> item.getValue()));

            // 流程执行变量
            Integer loopCounter = (Integer) hisVarMap.get(RollbackConstants.MultiInstanceConstants.LOOP_COUNTER);

            List<ExecutionEntity> executionEntities = CommandContextUtil.getExecutionEntityManager(commandContext)
                    .findChildExecutionsByParentExecutionId(executionEntity.getId());

            List<ExecutionEntity> linkExecutions = executionEntities.stream()
                    .filter(obj -> {
                        if (!obj.isActive()) {
                            Integer currentLoopCounter = (Integer) obj.getVariable(RollbackConstants.MultiInstanceConstants.LOOP_COUNTER);
                            if (currentLoopCounter.equals(loopCounter)) {
                                return true;
                            }
                        }
                        return false;
                    }).collect(Collectors.toList());

            if (linkExecutions.isEmpty()) {
                throw new FlowableRuntimeException("没有找到映射节点");
            }

            ExecutionEntity newExecution = linkExecutions.get(0);

            newExecution.setCurrentFlowElement(paramsTemplate.getCurrentTaskElement());
            newExecution.setActive(true);
            newExecution.setVariables(hisVarMap);
            newExecution.setVariable(assigneeExpr, assignee);

            // 创建新任务
            CommandContextUtil.getAgenda(commandContext).planContinueMultiInstanceOperation(newExecution, executionEntity, loopCounter);
        }

        // 移除当前历史任务
        removeHisTask(hisTask);
    }

~~~

##### 通用处理父级execution逻辑 
~~~
/**
     * 通用处理逻辑
     *
     * @param hisTask
     * @return
     */
    private ExecutionEntity processCommon(HistoricTaskInstance hisTask) {


        ExecutionEntity executionEntity = CommandContextUtil
                .getExecutionEntityManager(commandContext).findById(hisTask.getExecutionId());

        if (null == executionEntity) {
            LOGGER.error("没有找到历史任务[ executionId = " + hisTask.getExecutionId() + " ]");

//            List<ExecutionEntity> executionEntityList = CommandContextUtil
//                    .getExecutionEntityManager(commandContext)
//                    .findExecutionsByParentExecutionAndActivityIds(hisTask.getProcessInstanceId(), Collections.singletonList(hisTask.getTaskDefinitionKey()));

            List<Task> taskEntityList = CommandContextUtil.getTaskService(commandContext)
                    .createTaskQuery()
                    .processInstanceId(hisTask.getProcessInstanceId())
                    .taskDefinitionKey(hisTask.getTaskDefinitionKey())
                    .list();


            executionEntity = CommandContextUtil.getExecutionEntityManager(commandContext).findById(taskEntityList.get(0).getExecutionId());
        }

        ExecutionEntity parentExecutionEntity = CommandContextUtil
                .getExecutionEntityManager(commandContext).findById(executionEntity.getParentId());

        /**
         *  将计数器 进行 前移
         */
        int nrOfActiveInstances = (int) parentExecutionEntity.getVariable(RollbackConstants.MultiInstanceConstants.NR_OF_ACTIVE_INSTANCES);
        int nrOfCompletedInstances = (int) parentExecutionEntity.getVariable(RollbackConstants.MultiInstanceConstants.NR_OF_COMPLETE_INSTANCES);

        runtimeService.setVariable(parentExecutionEntity.getId(), RollbackConstants.MultiInstanceConstants.NR_OF_ACTIVE_INSTANCES, nrOfActiveInstances + 1);
        runtimeService.setVariable(parentExecutionEntity.getId(), RollbackConstants.MultiInstanceConstants.NR_OF_COMPLETE_INSTANCES, nrOfCompletedInstances - 1);

        return parentExecutionEntity;
    }
~~~

##### 移除历史任务：
~~~

    public void existNextFinishedTask(HistoricTaskInstance hisTask) {

        List<HistoricTaskInstance> list = CommandContextUtil.getHistoricTaskService().findHistoricTaskInstancesByQueryCriteria(
                (HistoricTaskInstanceQueryImpl) new HistoricTaskInstanceQueryImpl()
                        .processInstanceId(hisTask.getProcessInstanceId())
                        .taskDefinitionKey(hisTask.getTaskDefinitionKey())
                        .finished()
                        .taskCompletedAfter(hisTask.getEndTime())
        );

        if (!list.isEmpty()) {
            String msg = "串行会签回滚，已经具有下一线性完成任务,无法进行任务回退";
            LOGGER.error(msg);
            throw new FlowableRuntimeException(msg);
        }

    }
~~~

以上 完成对 上一节点任务撤回 架构设计 及 第 3 种情况 解决方案讲述。
git地址：[https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_examples](https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_examples)
