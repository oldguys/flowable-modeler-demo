> **场景**：本章主要描述 需要经过多重网关 如何进行回退操作。
>
> ![需要经过多重网关的任务回退](https://upload-images.jianshu.io/upload_images/14387783-deaf86e1a20c22b4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
>
> **上一章：**[flowable 上一节点任务撤回-（3）会签任务（正在执行中）](https://www.jianshu.com/p/6daf767b1084)
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

 ![需要经过多重网关的任务回退](https://upload-images.jianshu.io/upload_images/14387783-deaf86e1a20c22b4.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
#### 如上图进行，经过了并行网关，任务分成了b-1 ，b-2，再过包容网关，任务可能又变成了 c-1 ，c-2， c-3，b-1，此时数据会变成怎样？

---
 Test-1： 从 a -> b
![image.png](https://upload-images.jianshu.io/upload_images/14387783-98746015b39c6e1f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

~~~
        ProcessInstance pi = runtimeService.startProcessInstanceByKey(key02);

        System.out.println("pi:" + pi.getProcessInstanceId());
        Map<String, Object> variables = new HashMap<>();
        variables.put("a", 2);

        taskService.createTaskQuery()
                .processInstanceId(pi.getProcessInstanceId())
                .list().forEach(obj -> taskService.complete(obj.getId(), variables));
~~~
效果： 并行网关 没有 条件过滤的能力 
![数据库效果](https://upload-images.jianshu.io/upload_images/14387783-3cdca9ba2178edca.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#### 官方文档： 并行网关:[https://flowable.com/open-source/docs/bpmn/ch07b-BPMN-Constructs/#parallel-gateway](https://flowable.com/open-source/docs/bpmn/ch07b-BPMN-Constructs/#parallel-gateway)


![官方文档](https://upload-images.jianshu.io/upload_images/14387783-330496498fa30aea.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

其中 Execution 效果：
![ACT_RU_EXECUTION](https://upload-images.jianshu.io/upload_images/14387783-05ac27ce0aab6eef.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

---

Test-2： 从 b-2 -> c 
![image.png](https://upload-images.jianshu.io/upload_images/14387783-21dfe7707f8dd9e8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![变量](https://upload-images.jianshu.io/upload_images/14387783-195b41bab1bffff0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![image.png](https://upload-images.jianshu.io/upload_images/14387783-521cee509deb2dd1.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

包容网关具有线条流判断的能力

---

Test-3： b-1 ，c-1 完成。 c -3 未完成
![image.png](https://upload-images.jianshu.io/upload_images/14387783-5c70061ba2b43051.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![ACT_RU_TASK](https://upload-images.jianshu.io/upload_images/14387783-3ff301b74ea6f883.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


![ACT_RU_EXECUTION](https://upload-images.jianshu.io/upload_images/14387783-a0e506ff8989567c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

---
Test-4： 从任务走到 d 的时候

![走到 e ](https://upload-images.jianshu.io/upload_images/14387783-8e959991b4621155.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


![ACT_RU_TASK](https://upload-images.jianshu.io/upload_images/14387783-0fd99501aee7eae0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![ACT_RU_EXECUTION](https://upload-images.jianshu.io/upload_images/14387783-4cae4a2023094b4c.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


> 结论：
> 1. 不同于 普通节点 和 会签， 出现并行 分支的时候， 也会出现 多个 execution，但是父级节点 直接就是流程。
> 2. 在线条汇总的时候，网关会出现一个 execution ， Active = false。完成任务之后 execution 又会消失掉，所以进行网关回退的时候，需要模拟 汇总网关 的 完成任务状态，不然会导致撤回后的任务 无法往前走。
> 3. 同理，跟之前会签一样，由于有造模拟完成的线条流，所以当其他线条中的其他任务进行再次回退的时候，需要移除已经创建的线条流。
> 
> 


##### com.example.oldguy.modules.examples.cmd.rollback.impl.DefaultTaskNextGatewayRollbackOperateStrategy：普通任务节点在多重网关间的策略

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

##### createExecution()
~~~
    @Override
    public void createExecution() {
        HistoricTaskInstance hisTask = paramsTemplate.getHisTask();

        // 获取正在执行 execution
        ExecutionEntity executionEntity = getExecutionEntity();

        ExecutionEntity newExecution = CommandContextUtil.getExecutionEntityManager(commandContext)
                .createChildExecution(executionEntity.getParent());
        // 创建新任务
        createExecution(newExecution);
        // 特殊处理并行网关
        processGateway(executionEntity.getParent());

        // 移除历史任务
        removeHisTask(hisTask);

    }
~~~

#####  processGateway(ExecutionEntity parent)
~~~
      /**
     * 使用并行网关进行 线条流汇总时候,会出现 特殊bug
     *
     * @param parent
     */
    protected void processGateway(ExecutionEntity parent) {

        // 当前正在运行所以任务
        List<TaskEntity> taskEntityList = CommandContextUtil.getTaskService(commandContext)
                .findTasksByProcessInstanceId(parent.getProcessInstanceId());

        boolean isExistPassGatewayTask = false;

        // 下一节点任务
        List<TaskEntity> nextTaskList = taskEntityList.stream()
                .filter(obj -> paramsTemplate.getNextFlowIdList().contains(obj.getTaskDefinitionKey()))
                .collect(Collectors.toList());

        if (!nextTaskList.isEmpty()) {
            log.info("已经生成过网关任务");
            isExistPassGatewayTask = true;

            // 网关的连线
            Map<String, SequenceFlow> sqFlowMap = new HashMap<>();

            paramsTemplate.getGatewayMap().values().forEach(obj -> {
                obj.getIncomingFlows().forEach(item -> {

                    if (null != paramsTemplate.getGatewayMap().get(item.getSourceRef())) {
                        log.info("跳过gateway 间连线:" + item);
                        return;
                    }
                    if (paramsTemplate.getHisTask().getTaskDefinitionKey().equals(item.getSourceRef())) {
                        log.info("跳过当前回退历史任务:" + item);
                        return;
                    }
                    sqFlowMap.put(item.getSourceRef(), item);
                });
            });

            // 创建网关相关连线 execution
            createCompleteGatewayExecution(parent, sqFlowMap);

            // 删除当前正在执行任务
            Set<String> nestTaskIdSet = new HashSet<>();
            nextTaskList.forEach(obj -> {
                removeRuntimeTaskOperate(obj);
                nestTaskIdSet.add(obj.getId());
            });

            // 移除正在执行下一节点历史任务
            List<HistoricTaskInstanceEntity> historicTaskInstanceList = CommandContextUtil.getHistoricTaskService(commandContext)
                    .findHistoricTasksByProcessInstanceId(parent.getProcessInstanceId());
            historicTaskInstanceList.forEach(obj -> {
                if (nestTaskIdSet.contains(obj.getId())) {
                    CommandContextUtil.getHistoricTaskService(commandContext).deleteHistoricTask(obj);
                }
            });
        }

        if (isExistPassGatewayTask) {


        } else {
            log.info("移除网关连线");

            List<ExecutionEntity> executionEntityList = CommandContextUtil.getExecutionEntityManager(commandContext)
                    .findExecutionsByParentExecutionAndActivityIds(parent.getProcessInstanceId(), paramsTemplate.getGatewayMap().keySet());

            Map<String, FlowElement> targetGatewayMap = paramsTemplate.getCurrentTaskElement()
                    .getOutgoingFlows().stream().filter(obj -> obj.getTargetFlowElement() instanceof Gateway)
                    .map(obj -> obj.getTargetFlowElement())
                    .collect(Collectors.toMap(FlowElement::getId, obj -> obj));

            List<ExecutionEntity> toRemoveList = new ArrayList<>();

            executionEntityList.forEach(obj -> {
                if (null != targetGatewayMap.get(obj.getActivityId())) {
                    toRemoveList.add(obj);
                    targetGatewayMap.put(obj.getActivityId(), null);
                }
            });

            if (!toRemoveList.isEmpty()) {
                toRemoveList.forEach(obj -> {
                    log.info("移除连线:" + obj);
                    CommandContextUtil.getExecutionEntityManager(commandContext).delete(obj);
                });
            }
        }
    }

~~~

~~~
    /**
     * 创建 Gateway 相关连线
     *
     * @param parent
     * @param sqFlowMap
     */
    protected void createCompleteGatewayExecution(ExecutionEntity parent, Map<String, SequenceFlow> sqFlowMap) {

        sqFlowMap.values().forEach(obj -> {

            ExecutionEntity newExecution = CommandContextUtil.getExecutionEntityManager(commandContext)
                    .createChildExecution(parent);

            newExecution.setCurrentFlowElement(obj.getTargetFlowElement());
            newExecution.setActive(false);

            log.debug("创建 gateway 连线 execution");
            CommandContextUtil.getAgenda(commandContext).planContinueProcessInCompensation(newExecution);
        });
    }

~~~

> PS: 注意连线间的任务操作，如多重网关的时候，网关连线应该跳过，已办任务也需要跳过。
>


##### 其他重载 方法
~~~
    @Override
    public void existNextFinishedTask() {
        HistoricTaskInstance hisTask = paramsTemplate.getHisTask();

        List<HistoricTaskInstance> historicTaskInstanceList = CommandContextUtil.getHistoricTaskService(commandContext)
                .findHistoricTaskInstancesByQueryCriteria(
                        (HistoricTaskInstanceQueryImpl) new HistoricTaskInstanceQueryImpl()
                                .finished()
                                .processInstanceId(hisTask.getProcessInstanceId())
                                .taskCompletedAfter(hisTask.getEndTime())
                );

        if (!historicTaskInstanceList
                .stream()
                .filter(obj -> paramsTemplate.getNextFlowIdList().contains(obj.getTaskDefinitionKey()))
                .collect(Collectors.toList())
                .isEmpty()) {
            String msg = "存在已完成下一节点任务";
            throw new FlowableRuntimeException(msg);
        }
    }



    @Override
    public void deleteRuntimeTasks() {

        HistoricTaskInstance hisTask = paramsTemplate.getHisTask();

        // 删除正在执行任务
        List<Task> taskList = CommandContextUtil.getTaskService(commandContext)
                .createTaskQuery()
                .processInstanceId(hisTask.getProcessInstanceId())
                .taskCreatedAfter(hisTask.getEndTime())
                .list();
        taskList.stream()
                .filter(obj -> paramsTemplate.getNextFlowIdList().contains(obj.getTaskDefinitionKey()))
                .forEach(obj -> {
                    log.info("删除运行时任务：" + obj);
                    removeRuntimeTaskOperate((TaskEntity) obj);
                });

        // 删除历史任务
        List<HistoricTaskInstanceEntity> historicTaskInstances = CommandContextUtil.getHistoricTaskService(commandContext)
                .findHistoricTasksByProcessInstanceId(hisTask.getProcessInstanceId());
        historicTaskInstances.forEach(obj -> {
            if (obj.getCreateTime().getTime() <= hisTask.getEndTime().getTime() && paramsTemplate.getNextFlowIdList().contains(obj.getTaskDefinitionKey())) {
                log.info("删除历史任务：" + obj);
                CommandContextUtil.getHistoricTaskService(commandContext).deleteHistoricTask(obj);
            }
        });
    }
~~~

以上 完成对 上一节点任务撤回 架构设计 及 第 4 种情况 解决方案讲述。
git地址：[https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_examples](https://github.com/oldguys/flowable-modeler-demo/tree/branch_with_flowable_examples)
