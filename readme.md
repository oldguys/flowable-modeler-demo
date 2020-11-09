## flowable 基于 ThreadPoolExecutor 进行任务批处理，统一事务处理

> **场景**：在普通的开发的时候，任务是单线程处理的，这这时候性能可能有点慢。基于 juc 包下的ThreadPoolExecutor 进行开发，可以转换成为批处理的，使性能成倍提高
>
>**出现主要的问题**：将任务切割成为子任务的时候，事务统一性被破坏。
>
> **环境**：
>   springboot：2.2.0.RELEASE
>   flowable：6.4.2
>
>
> git地址：[https://github.com/oldguys/flowable-modeler-demo/tree/feature_threadpoolexecutor_no_spring_proxy_transaction](https://github.com/oldguys/flowable-modeler-demo/tree/feature_threadpoolexecutor_no_spring_proxy_transaction)
>

### 分析步骤：
Step1. ThreadPoolExecutor 的基本用法，编写通用工具类
Step2. 基于面向接口开发，进行通用抽象
Step3. 分析spring事务，将基于注解的声明式事务，改为编程式事务
Step4. 使用 变量表示来决定是否使用统一事务


#### Step1: ThreadPoolExecutor  简单用法
基本处理代码
~~~

ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("flow-pool-%d")
                .build();

int corePoolSize = 10;
int maximumPoolSize = 10;
long keepAliveTime = 3;

TimeUnit unit = TimeUnit.SECONDS;
BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();

ExecutorService executorService =  new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                namedThreadFactory) {
            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                // 线程处理前置方法
            }

            @Override
            protected void afterExecute(Runnable r, Throwable t) {
               // 线程处理后置方法
            }
};

List<Future<?>> futures = new ArrayList<>();

for (int i = 0; i < group; i++) {

    int startIndex = i * groupSize;
    int endIndex = (i + 1) * groupSize;
    if (endIndex > toDoList.size()) {
         endIndex = toDoList.size();
     }
     List<?> items = toDoList.subList(startIndex, endIndex);
    futures.add(executorService.submit(new SingleTransactionPoolTask(execution, items, flag)));
}

try {
  for (Future<?> future : futures) {
       future.get();
  }
} catch (Exception e) {
    e.printStackTrace();
    // 业务操作
} finally {
    executorService.shutdown();
}

~~~
1. 构造方法

| **名称**        | **类型**                | **含义**         |
| --------------- | ----------------------- | ---------------- |
| corePoolSize    | int                     | 核心线程池大小   |
| maximumPoolSize | int                     | 最大线程池大小   |
| keepAliveTime   | long                    | 线程最大空闲时间 |
| unit            | TimeUnit                | 时间单位         |
| workQueue       | BlockingQueue<Runnable> | 线程等待队列     |
| threadFactory   | ThreadFactory           | 线程创建工厂     |

2. ThreadPoolExecutor 重写方法

| 方法名                                                   | 作用                 |
| -------------------------------------------------------- | :------------------- |
| protected void beforeExecute(Thread t, Runnable r) { }   | 线程处理前置调用     |
| protected void afterExecute(Runnable r, Throwable t) { } | 线程处理后置调用     |
| protected void terminated() { }                          | 线程处理结束之后调用 |

>
> 在进行主线程拆分成多子线程并发处理的时候，经常会遇到部分主线程的数据无法在子线程获取到，此时就可以通过重写线程池 **beforeExecute()** 方法，将主线程数据同步到子线程中。如：工作流的**Authentication.setAuthenticatedUserId(currentUserId);**
> 基于ThreadLocal 的全局变量设置
>

3.  线程池调用任务
此处为线程池实际处理方法，

**ExecutionService.submit(Runnable task);**
~~~
    /**
     * Submits a Runnable task for execution and returns a Future
     * representing that task. The Future's {@code get} method will
     * return {@code null} upon <em>successful</em> completion.
     *
     * @param task the task to submit
     * @return a Future representing pending completion of the task
     * @throws RejectedExecutionException if the task cannot be
     *         scheduled for execution
     * @throws NullPointerException if the task is null
     */
    Future<?> submit(Runnable task);
~~~
**SingleTransactionPoolTask** 实现 runnable 接口
~~~
public class SingleTransactionPoolTask implements Runnable {

    private final ThreadExecution threadExecution;

    private final List<?> list;

    private final BatchTransactionFlag flag;

    public SingleTransactionPoolTask(ThreadExecution threadExecution, List<?> list, BatchTransactionFlag flag) {
        this.threadExecution = threadExecution;
        this.list = list;
        this.flag = flag;
    }

    @Override
    public void run() {
        try {
            threadExecution.threadExecute(list);
        } finally {
            flag.getCompleteThreads().incrementAndGet();
        }
    }
}
~~~

4. 返回线程调用的 处理方法 
主要进行子线程中是否有异常，如果具有异常则应该进行的对应业务处理
~~~
try {
  for (Future<?> future : futures) {
       future.get();
  }
} catch (Exception e) {
    e.printStackTrace();
    // 业务操作
} finally {
    executorService.shutdown();
}

~~~




#### Step2: 基于面向接口开发，将业务操作进行多态

![基本的类关系图](https://upload-images.jianshu.io/upload_images/14387783-16e836e0bbda8822.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


**ThreadExecution**  抽象子任务接口，具体不同业务编写指定的实现类，形成多态。通用工具类统一调用接口
~~~
public interface ThreadExecution {

    /**
     *  处理线程任务
     * @param list
     */
    void threadExecute(List<?> list);
}
~~~
**SingleTransactionPoolTask** 通用任务实现类，基于 **依赖倒置原则** 调用 ThreadExecution
~~~
public class SingleTransactionPoolTask implements Runnable {

    private final ThreadExecution threadExecution;

    private final List<?> list;

    private final BatchTransactionFlag flag;

    public SingleTransactionPoolTask(ThreadExecution threadExecution, List<?> list, BatchTransactionFlag flag) {
        this.threadExecution = threadExecution;
        this.list = list;
        this.flag = flag;
    }

    @Override
    public void run() {
        try {
            threadExecution.threadExecute(list);
        } finally {
            flag.getCompleteThreads().incrementAndGet();
        }
    }
}

~~~


实现 ThreadExecution 接口，进行业务多态
**BatchStartProcessThreadExecutionImpl**
~~~
@Slf4j
public class BatchStartProcessThreadExecutionImpl implements ThreadExecution {

    private RuntimeService runtimeService;

    private List<BatchStartProcessInstanceRsp.ProcessInstanceItem> records;

    public BatchStartProcessThreadExecutionImpl(List<BatchStartProcessInstanceRsp.ProcessInstanceItem> records) {
        this.records = records;
        this.runtimeService = SpringContextUtils.getBean(RuntimeService.class);
    }

    @Override
    public void threadExecute(List list) {
        // 省略业务代码
    }
}
~~~
**BatchTaskCompleteTaskWithBatchTransactionThreadExecutionImpl**
~~~
@Slf4j
public class BatchTaskCompleteTaskWithBatchTransactionThreadExecutionImpl implements ThreadExecution {

    private List<BatchCompleteTaskRsp.CompleteTaskItem> result;

    private FlowTaskService flowTaskService;

    public BatchTaskCompleteTaskWithBatchTransactionThreadExecutionImpl(List<BatchCompleteTaskRsp.CompleteTaskItem> result) {
        this.result = result;
        this.flowTaskService = SpringContextUtils.getBean(FlowTaskService.class);
    }

    @Override
    public void threadExecute(List list) {
        // 省略业务代码
    }
}
~~~


#### Step3. 分析spring事务，将基于注解的声明式事务，改为编程式事务
> 在进行spring开发的时候，基本都是基于spring的声明式事务（**@Transactional**）进行开发，可以做到非常高效。但是基于多线程开发的时候，**通过debug，可以发现，主线程还没有进行异常处理环节，子线程事务已经提交，并且在数据库已经可以查询到。** 这个并不满足于业务需求。（如图）
>
>
![dubug看出还未到执行回退业务操作](https://upload-images.jianshu.io/upload_images/14387783-4dca2f928f21ec8b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![子线程事务已经提交，数据库可以查询到结果](https://upload-images.jianshu.io/upload_images/14387783-f6b4da4dec4aff2a.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

>
> 基于对Spring事务bean之间关系的了解，事务都是围绕着 **TransactionManager**，实现类为：**org.springframework.jdbc.datasource.DataSourceTransactionManager**，可以找到接口**org.springframework.transaction.PlatformTransactionManager**，并且该接口具有如下的方法
>

**PlatformTransactionManager**接口的方法

| 方法名                                                       | 功能           |
| ------------------------------------------------------------ | -------------- |
| TransactionStatus getTransaction(@Nullable TransactionDefinition definition) | 获取当前的事务 |
| void commit(TransactionStatus status)                        | 提交事务       |
| void rollback(TransactionStatus status)                      | 回滚事务       |

所以获取事务的代码则为
~~~
// 获取事务
TransactionStatus transactionStatus = transactionManager.getTransaction(TransactionDefinition.withDefaults());

// 提交事务
transactionManager.commit(transactionStatus);

// 回滚事务
transactionManager.rollback(transactionStatus);
~~~

>
> 所以原本计划是：根据传入参数，把事务从子线程中获取，再回到主线程中提交。不过事与愿违的是，提交事务并没有想象中那么直接。会抛出异常
>
**DefaultCommonThreadExecutionServiceBean**
~~~
@Service
public class DefaultCommonThreadExecutionServiceBean implements CommonThreadExecutionService {

    @Resource
    private DataSourceTransactionManager transactionManager;

    @Override
//    @Transactional(rollbackFor = Exception.class)
    public int executeBatch(ThreadExecution threadExecution, List<?> sequence, List<TransactionStatus> transactionStatusList) {

        TransactionStatus transactionStatus = transactionManager.getTransaction(TransactionDefinition.withDefaults());
        transactionStatusList.add(transactionStatus);

        threadExecution.threadExecute(sequence);

        return 0;
    }
}
~~~
**FlowThreadPoolExecutor** 代码段
~~~
  DataSourceTransactionManager transactionManager = SpringContextUtils.getBean(DataSourceTransactionManager.class);
        try {
            for (Future future : futures) {
                future.get();
            }

            transactionStatusList.forEach(obj -> {
                transactionManager.commit(obj);
            });

        } catch (Exception e) {
            e.printStackTrace();

            transactionStatusList.forEach(obj -> {
                transactionManager.rollback(obj);
            });

        } finally {
            executorService.shutdown();
        }
~~~

![系统抛出异常](https://upload-images.jianshu.io/upload_images/14387783-9331d03b4491e010.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

![navicat 出现事务数据库被锁，无法清除数据](https://upload-images.jianshu.io/upload_images/14387783-db7b60a0e53158e7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


>
> 根据spring事务源码分析可知，spring的事务也是基于ThreadLocal的，所以出现了跨越线程的时候，就会出现无法执行完成。并且由navicat无法操作数据库可以看出，数据库事务并未提交，出现了行锁。
>
>
**org.springframework.transaction.support.TransactionSynchronizationManager#unbindResource**
~~~
	/**
	 * Unbind a resource for the given key from the current thread.
	 * @param key the key to unbind (usually the resource factory)
	 * @return the previously bound value (usually the active resource object)
	 * @throws IllegalStateException if there is no value bound to the thread
	 * @see ResourceTransactionManager#getResourceFactory()
	 */
	public static Object unbindResource(Object key) throws IllegalStateException {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Object value = doUnbindResource(actualKey);
		if (value == null) {
			throw new IllegalStateException(
					"No value for key [" + actualKey + "] bound to thread [" + Thread.currentThread().getName() + "]");
		}
		return value;
	}
~~~

**org.springframework.transaction.support.TransactionSynchronizationManager**
![获取线程资源](https://upload-images.jianshu.io/upload_images/14387783-d7e93a82adc48da8.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

>
> 虽然源码中可以获取看到事务提交代码是 数据库连接的提交，但是其中还是必须执行 清除当前线程绑定的事务，才能彻底释放数据库连接。
>

提交事务：**org.springframework.jdbc.datasource.DataSourceTransactionManager#doCommit**
~~~
	@Override
	protected void doCommit(DefaultTransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		Connection con = txObject.getConnectionHolder().getConnection();
		if (status.isDebug()) {
			logger.debug("Committing JDBC transaction on Connection [" + con + "]");
		}
		try {
			con.commit();
		}
		catch (SQLException ex) {
			throw new TransactionSystemException("Could not commit JDBC transaction", ex);
		}
	}
~~~
抛出异常：**org.springframework.transaction.support.AbstractPlatformTransactionManager#cleanupAfterCompletion**

~~~
	/**
	 * Clean up after completion, clearing synchronization if necessary,
	 * and invoking doCleanupAfterCompletion.
	 * @param status object representing the transaction
	 * @see #doCleanupAfterCompletion
	 */
	private void cleanupAfterCompletion(DefaultTransactionStatus status) {
		status.setCompleted();
		if (status.isNewSynchronization()) {
			TransactionSynchronizationManager.clear();
		}
		if (status.isNewTransaction()) {
			doCleanupAfterCompletion(status.getTransaction());
		}
		if (status.getSuspendedResources() != null) {
			if (status.isDebug()) {
				logger.debug("Resuming suspended transaction after completion of inner transaction");
			}
			Object transaction = (status.hasTransaction() ? status.getTransaction() : null);
			resume(transaction, (SuspendedResourcesHolder) status.getSuspendedResources());
		}
	}

~~~

>
> 所以最终解决还是需要在子线程进行提交，此时，又可以使用线程池的重写  **java.util.concurrent.ThreadPoolExecutor#afterExecute**
>并且通过变量来确定子线程是否已经执行完成，如果执行完成，才进行事务的提交
>

**BatchTransactionFlag**
~~~
@Getter
public class BatchTransactionFlag {

    private final AtomicInteger completeThreads = new AtomicInteger();

    private final AtomicInteger successThreads = new AtomicInteger();

    private final int groupSize;

    private boolean batchTransaction;

    private Map<Long, TransactionStatus> longTransactionStatusMap;

    private final List<?> toDoList;

    public BatchTransactionFlag(int groupSize, boolean batchTransaction, List<?> toDoList) {
        this.groupSize = groupSize;
        this.batchTransaction = batchTransaction;
        this.toDoList = toDoList;
        if (batchTransaction) {
            longTransactionStatusMap = new ConcurrentHashMap<>();
        }
    }
}

~~~
**CommonThreadExecutionService**实现
~~~
@Slf4j
@Service
public class DefaultCommonThreadExecutionServiceBean implements CommonThreadExecutionService {

    @Resource
    private DataSourceTransactionManager transactionManager;

    @Override
    public int executeBatch(ThreadExecution threadExecution, List sequence, Map<Long, TransactionStatus> longTransactionStatusMap, BatchTransactionFlag flag) {

        synchronized (flag) {
            TransactionStatus transactionStatus = transactionManager.getTransaction(TransactionDefinition.withDefaults());
            longTransactionStatusMap.put(Thread.currentThread().getId(), transactionStatus);
            try {
                threadExecution.threadExecute(sequence);
                flag.getSuccessThreads().incrementAndGet();
            } finally {
                flag.getCompleteThreads().incrementAndGet();
                log.info("完成任务：" + Thread.currentThread().getName());
            }
        }
        return 0;
    }
}
~~~
>
> 经过测试发现，需要调用数据库修改的步骤，还是需要同步块的，不使用会导致数据库死锁，导致处理超时
>

#### Step4. 使用 变量表示来决定是否使用统一事务

>从上面可以看到由于面向接口进行处理，所以根据需要 **统一事务** 跟 **不需要统一事务** 又可以使用不同实现类来进行控制，并且在编写线程池的时候也配合做判断。

线程池执行的代码
~~~

        for (int i = 0; i < group; i++) {

            int startIndex = i * groupSize;
            int endIndex = (i + 1) * groupSize;
            if (endIndex > toDoList.size()) {
                endIndex = toDoList.size();
            }
            List<?> items = toDoList.subList(startIndex, endIndex);
            if (batchTransaction) {
                futures.add(executorService.submit(new BatchTransactionPoolTask(execution, items, flag.getLongTransactionStatusMap(), flag)));
            } else {
                futures.add(executorService.submit(new SingleTransactionPoolTask(execution, items, flag)));
            }
        }
~~~

线程池的构建
~~~
private static ThreadPoolExecutor createThreadPoolExecutorInstance(int corePoolSize,
                                                                       int maximumPoolSize,
                                                                       long keepAliveTime,
                                                                       TimeUnit unit,
                                                                       BlockingQueue<Runnable> workQueue,
                                                                       BatchTransactionFlag flag
    ) {

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("flow-pool-%d")
                .build();

        String currentUserId = SecurityUtils.getCurrentUserId();
        DataSourceTransactionManager transactionManager = SpringContextUtils.getBean(DataSourceTransactionManager.class);


        return new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                namedThreadFactory) {
            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                Authentication.setAuthenticatedUserId(currentUserId);
            }

            @Override
            protected void afterExecute(Runnable r, Throwable t) {

                if (flag.isBatchTransaction()) {

                    try {
                        while (flag.getCompleteThreads().get() != flag.getGroupSize()) {
                            log.info(Thread.currentThread().getName() + " 等待主线程：getGroupSize:" + flag.getGroupSize() + "\tgetCompleteThreads：" + flag.getCompleteThreads().get());
                            log.info("开启事务个数：" + flag.getLongTransactionStatusMap().size());
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    TransactionStatus status = flag.getLongTransactionStatusMap().get(Thread.currentThread().getId());
                    if (flag.getSuccessThreads().get() == flag.getCompleteThreads().get()) {
                        log.info(Thread.currentThread().getName() + ":全部执行成功,提交事务");
                        transactionManager.commit(status);
                    } else {
                        log.info(Thread.currentThread().getName() + ":具有线程执行失败,回滚事务");
                        transactionManager.rollback(status);
                    }
                }
            }
        };
    }
~~~

>
>这样就可以做到 动态判断是否需要统一事务。
> 详细demo可以查看git代码
>  git地址：[https://github.com/oldguys/flowable-modeler-demo/tree/feature_threadpoolexecutor_no_spring_proxy_transaction](https://github.com/oldguys/flowable-modeler-demo/tree/feature_threadpoolexecutor_no_spring_proxy_transaction)
>
