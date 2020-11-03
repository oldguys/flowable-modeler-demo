package com.example.oldguy.modules.app.dao.entities;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.flowable.identitylink.api.IdentityLinkInfo;
import org.flowable.task.api.TaskInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: ActTaskEntity
 * @Author: huangrenhao
 * @Description:
 * @CreateTime： 2020/11/3 0003 下午 6:08
 * @Version：
 **/
@Data
@TableName("ACT_RU_TASK")
public class ActTaskEntity  implements TaskInfo {

    @TableId("ID_")
    private String id;

    @TableField("REV_")
    private Integer revision;

    @TableField("PROC_INST_ID_")
    private String processInstanceId;

    @TableField("TENANT_ID_")
    private String tenantId;

    @TableField("PROC_DEF_ID_")
    private String processDefinitionId;

    @TableField("SUSPENSION_STATE_")
    private Integer suspensionState;

    @TableField("NAME_")
    private String name;

    @TableField("EXECUTION_ID_")
    private String executionId;

    @TableField("TASK_DEF_ID_")
    private String taskDefinitionId;

    @TableField("SCOPE_ID_")
    private String scopeId;

    @TableField("SUB_SCOPE_ID_")
    private String subScopeId;

    @TableField("SCOPE_TYPE_")
    private String scopeType;

    @TableField("SCOPE_DEFINITION_ID_")
    private String scopeDefinitionId;

    @TableField("PARENT_TASK_ID_")
    private String parentTaskId;

    @TableField("DESCRIPTION_")
    private String description;

    @TableField("TASK_DEF_KEY_")
    private String taskDefinitionKey;

    @TableField("OWNER_")
    private String owner;

    @TableField("ASSIGNEE_")
    private String assignee;

    @TableField("DELEGATION_")
    private String delegation;

    @TableField("PRIORITY_")
    private Integer priority;

    @TableField("CREATE_TIME_")
    private Date createTime;

    @TableField("DUE_DATE_")
    private Date dueTime;

    @TableField("CATEGORY_")
    private String category;

    @TableField("FORM_KEY_")
    private String formKey;

    @TableField("CLAIM_TIME_")
    private Date claimTime;

    @TableField("SUB_TASK_COUNT_")
    private Integer subTaskCount;

    @TableField("VAR_COUNT_")
    private Integer variableCount;

    @TableField("ID_LINK_COUNT_")
    private Integer identityLinkCount;

    @TableField("IS_COUNT_ENABLED_")
    private boolean countEnabled;

    @Override
    public Date getDueDate() {
        return null;
    }

    @Override
    public Map<String, Object> getTaskLocalVariables() {
        return null;
    }

    @Override
    public Map<String, Object> getProcessVariables() {
        return null;
    }

    @Override
    public List<? extends IdentityLinkInfo> getIdentityLinks() {
        return null;
    }
}
