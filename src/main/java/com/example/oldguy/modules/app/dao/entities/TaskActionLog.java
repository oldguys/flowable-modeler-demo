package com.example.oldguy.modules.app.dao.entities;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.flowable.task.api.TaskInfo;
import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * @ClassName: TaskActionLog
 * @Author: huangrenhao
 * @Description: 任务处理痕迹
 * @CreateTime： 2020/11/2 0002 下午 5:27
 * @Version：
 **/
@Data
@TableName("test_task_action_log")
@NoArgsConstructor
public class TaskActionLog {

    @TableId(type = IdType.ID_WORKER)
    private Long id;

    private String taskId;

    private String taskName;

    /**
     * @see taskActionType
     * 任务状态
     */
    private Integer type;

    /**
     * 任务操作描述
     */
    private String action;

    /**
     * 批注ID
     */
    private String commentId;

    private Date startTime;

    private Date endTime;

    public TaskActionLog(TaskInfo task, taskActionType type, String commentId) {
        setTaskId(task.getTenantId());
        setTaskName(task.getName());
        setStartTime(task.getCreateTime());

        setEndTime(new Date());

        setType(type.ordinal());
        setAction(type.getAction());

        if (!StringUtils.isEmpty(commentId)) {
            setCommentId(commentId);
        }
    }

    @Getter
    @AllArgsConstructor
    public enum taskActionType {

        /**
         * 完成任务
         */
        COMPLETE_TASK("完成任务"),
        /**
         * 撤回任务
         */
        ROLLBACK_TASK("撤回任务"),
        /**
         * 驳回任务
         */
        REJECT_TASK("退回任务");

        private String action;

    }
}

