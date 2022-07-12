> **场景**：本章主要描述 已完成会签任务 如何进行回退操作。
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

##### 其他章节：

---
前置知识：
1. 会签分为 并行 串行 两种。
并行会签：会同时创建所有的任务，依赖于变量进行操作。如图所示

![并行会签-正在执行任务列表](https://upload-images.jianshu.io/upload_images/14387783-cde95dd09b5b294a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![并行会签 -execution](https://upload-images.jianshu.io/upload_images/14387783-6056836ee11f9230.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![并行会签-变量](https://upload-images.jianshu.io/upload_images/14387783-72587216c02963cb.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

串行会签：按照变量顺序逐次创建任务，依赖于变量进行操作。如图所示

![串行会签-正在执行任务](https://upload-images.jianshu.io/upload_images/14387783-83f97784fd827370.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![串行会签-execution](https://upload-images.jianshu.io/upload_images/14387783-78d0b1e07bd61021.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![串行会签-变量](https://upload-images.jianshu.io/upload_images/14387783-b8804dab453d9801.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

> ###### 根据上面的图可以分析出 关系：
> 
> execution 层次结构：
> execution[流程实例] < Execution[会签顶级实例]  < Execution[实际会签任务]  
> 并行
> 1:1:3（设置变量数决定）
> 串行
> 1:1:1
> ---
> 其中可以看到 flowable 本身的变量
>
> 父级流程  execution变量：
> nrOfActiveInstances：未完成任务个数
> nrOfCompletedInstances：已完成任务个数
> nrOfInstances：总任务个数
> 
> 任务变量 execution
> loopCounter：任务计数器
> 

上面是执行前的情况，当所有会签任务完成之后

![所有会签任务完成之后-任务列表](https://upload-images.jianshu.io/upload_images/14387783-5632bb8c46bdb4ec.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![所有会签任务完成之后-execution](https://upload-images.jianshu.io/upload_images/14387783-728400d7be9abdbf.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![所有会签任务完成之后-ACT_RU_VARIABLE](https://upload-images.jianshu.io/upload_images/14387783-feae03038aeecb30.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

>**结论1：** 从上面图片分析出，当会签任务走完，原本的变量会被清理掉。所以要 从 普通节点 退回到 会签节点，就必须模拟出这些变量。
>
>**结论2：** 由于Execution也消失了，所以需要模拟出 2个层级的 execution。并且历史execution变量将不可用
>

---
##### CompletedMultiInstanceTaskAndNextDefaultTaskRollbackOperateStrategy: 回退已完成会签任务

![普通节点回退到会签节点](https://upload-images.jianshu.io/upload_images/14387783-0cb69c0c1aa9bdb3.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

前置知识参考 第一章：

> d-1 回退到 并行 c-1
>步骤：
> 1. 判断 d-1 是否已经完成，如果完成，则无法进行回退。如果未完成，则到下一步骤
> 2. 构建 并行 c-1 的 父级 Execution，并构建并构建 任务 execution（简称：c-1任务），此时需要根据并行 ， 串行 对变量进行修改。（PS：串行的时候 还需要判断 回退历史任务的执行人是否为 启动会签任务时候入参 集合 的最后一个参数，如果不是的话，则不应该回退。）
> 3. 配置 刚刚构建的 并行 c-1 的 父级 Execution的变量：
nrOfActiveInstances = 1
nrOfCompletedInstances = 会签总人数-1 
nrOfInstances = 会签总人数
execution.multiInstanceRoot = true
>
> 4. 配置任务变量
>  loopCounter = 会签总人数最后一个集合下标（ 即：List.size()-1 ）
>  execution.active = true
>
> 5. 如果是并行会签，则需要补全 其他已完成任务的 execution，即创建 N-1 个 与 c-1 任务 同级的 exection ，但是 active = false，不生成任务。（不生成完成会签任务execution会导致计数器无法执行计满，会导致无法到下一流程节点）。 串行会签由于使用的是同一个 execution，所以不需要补充。
> 
> 6. 构建任务的时候使用语句与构建普通任务的语句不一样：（使用同一个会出现直接构建整个节点）
> ~~~
>        CommandContextUtil.getAgenda(commandContext)
>                .planContinueMultiInstanceOperation(newExecution, parentExecution, assignees.size() - 1);
>
>~~~
>7. 之后的步骤与之前的操作一样，删除当前执行任务，清除回退的历史任务，清除痕迹及关联，此处就不过多赘述。 可以参考第一章：
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

##### 创建 父级 会签 execution
~~~

    @Override
    public void createExecution() {

        if (paramsTemplate.getCurrentTaskElement().getBehavior() instanceof SequentialMultiInstanceBehavior) {
            isSequence = true;
        }

        // 获取 execution
        ExecutionEntity executionEntity = getExecutionEntity();

        VariableInstanceEntity obj = CommandContextUtil.getVariableService(commandContext)
                .findVariableInstanceByExecutionAndName(executionEntity.getProcessInstanceId(), assigneeListExpr);

        if (obj == null || !(obj.getValue() instanceof Collection)) {
            throw new FlowableRuntimeException("没有可用会签参数:" + assigneeListExpr);
        }
        // 会签执行人变量
        Collection assignees = (Collection) obj.getValue();

        List<HistoricVariableInstance> historicVariableInstances = CommandContextUtil.getHistoricVariableService()
                .findHistoricVariableInstancesByQueryCriteria(
                        new HistoricVariableInstanceQueryImpl()
                                .executionId(paramsTemplate.getHisTask().getExecutionId())
                );

        if (historicVariableInstances.isEmpty()) {
            throw new FlowableRuntimeException("没有可用会签任务参数");
        }

        // 历史变量
        Map<String, Object> hisVarMap = historicVariableInstances.stream().collect(Collectors.toMap(HistoricVariableInstance::getVariableName, item -> item.getValue()));

        if (hisVarMap.containsKey(assigneeExpr) && hisVarMap.containsKey(RollbackConstants.MultiInstanceConstants.LOOP_COUNTER)) {
            log.info("变量有效");
        } else {
            throw new FlowableRuntimeException("缺少会签任务变量");
        }

        /**
         *  串行 最终的 loopCounter assignee 都会是最后一个人
         */
        if (isSequence) {

            List<String> assigneeList = (List<String>) runtimeService.getVariableLocal(paramsTemplate.getHisTask().getProcessInstanceId(), assigneeListExpr);
            if (!assigneeList.get(assigneeList.size() - 1).equals(paramsTemplate.getHisTask().getAssignee())) {
                String msg = "不是串行最后一个节点，无法进行回退 ";
                throw new FlowableRuntimeException(msg);
            }
            // 替换任务执行变量
            assigneeList.set(assigneeList.size() - 1, assignee);
            runtimeService.setVariableLocal(paramsTemplate.getHisTask().getProcessInstanceId(), assigneeListExpr, assigneeList);
        }

        // 流程执行变量
        Integer loopCounter = (Integer) hisVarMap.get(RollbackConstants.MultiInstanceConstants.LOOP_COUNTER);

        // 会签主任务
        ExecutionEntity parentExecution = CommandContextUtil.getExecutionEntityManager(commandContext)
                .createChildExecution(executionEntity.getParent());

        parentExecution.setCurrentFlowElement(paramsTemplate.getCurrentTaskElement());
        parentExecution.setActive(false);
        // 配置 会签 root execution
        parentExecution.setMultiInstanceRoot(true);

        // 配置主 execution 变量
        Map<String, Object> parentVarMap = new HashMap<>();
        parentVarMap.put(RollbackConstants.MultiInstanceConstants.NR_OF_ACTIVE_INSTANCES, 1);
        parentVarMap.put(RollbackConstants.MultiInstanceConstants.NR_OF_COMPLETE_INSTANCES, assignees.size() - 1);
        parentVarMap.put(RollbackConstants.MultiInstanceConstants.NR_OF_INSTANCE, assignees.size());
        parentExecution.setVariablesLocal(parentVarMap);

        if (isSequence) {
            log.info("创建 串行 会签任务");
            createSequenceMultiInstance(parentExecution, assignees);
        } else {
            log.info("创建 并行 会签任务");
            createParallelMultiInstance(parentExecution, assignees, loopCounter);
        }

        removeHisTask(paramsTemplate.getHisTask());
    }
~~~

##### 创建 串行会签任务：createSequenceMultiInstance
~~~
    /**
     * 创建并行会签任务
     *
     * @param parentExecution
     * @param loopCounter
     */
    private void createParallelMultiInstance(ExecutionEntity parentExecution, Collection assignees, Integer loopCounter) {

        for (int i = 0; i < assignees.size(); i++) {
            if (i != loopCounter) {
                Map<String, Object> varMap = new HashMap<>();
                varMap.put(RollbackConstants.MultiInstanceConstants.LOOP_COUNTER, i);
                varMap.put(assigneeExpr, "已完成任务");

//                // 创建 新执行任务
                ExecutionEntity newExecution = newExecution = CommandContextUtil.getExecutionEntityManager(commandContext)
                        .createChildExecution(parentExecution);
                newExecution.setCurrentFlowElement(paramsTemplate.getCurrentTaskElement());
                newExecution.setActive(false);

                newExecution.setVariablesLocal(varMap);

//
//                CommandContextUtil.getExecutionEntityManager(commandContext)
//                        .update(newExecution);
            } else {
                ExecutionEntity newExecution = CommandContextUtil.getExecutionEntityManager(commandContext)
                        .createChildExecution(parentExecution);
                newExecution.setCurrentFlowElement(paramsTemplate.getCurrentTaskElement());

                Map<String, Object> varMap = new HashMap<>();

                varMap.put(assigneeExpr, assignee);
                varMap.put(RollbackConstants.MultiInstanceConstants.LOOP_COUNTER, i);

                newExecution.setVariablesLocal(varMap);
                newExecution.setActive(true);

                CommandContextUtil.getAgenda(commandContext).planContinueMultiInstanceOperation(newExecution, parentExecution, loopCounter);
            }
        }


    }


}
~~~

##### 创建 并行会签任务：createSequenceMultiInstance
~~~
    private void createSequenceMultiInstance(ExecutionEntity parentExecution, Collection assignees) {

        ExecutionEntity newExecution = CommandContextUtil.getExecutionEntityManager(commandContext)
                .createChildExecution(parentExecution);

        Map<String, Object> varMap = new HashMap<>();
        varMap.put(assigneeExpr, assignee);
        varMap.put(RollbackConstants.MultiInstanceConstants.LOOP_COUNTER, assignees.size() - 1);
        newExecution.setCurrentFlowElement(paramsTemplate.getCurrentTaskElement());

        newExecution.setVariablesLocal(varMap);
        newExecution.setActive(true);
        CommandContextUtil.getAgenda(commandContext)
                .planContinueMultiInstanceOperation(newExecution, parentExecution, assignees.size() - 1);

    }
~~~

以上 完成对 上一节点任务撤回 架构设计 及 第 2 种情况 解决方案讲述。
git地址：[https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_examples](https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_examples)
