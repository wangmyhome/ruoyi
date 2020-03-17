package com.ruoyi.process.contract.domain;

import java.util.Date;

/**
 * @author shanpeng
 * @ClassName BizContractVo
 * @description TODO
 * @date 2020/3/15 20:09
 * @Version 1.0
 */
public class BizContractVo extends BizContract{

    /** 申请人姓名 */
    private String applyUserName;

    /** 任务ID */
    private String taskId;

    /** 任务名称 */
    private String taskName;

    /** 办理时间 */
    private Date doneTime;

    /** 创建人 */
    private String createUserName;

    public String getApplyUserName() {
        return applyUserName;
    }

    public void setApplyUserName(String applyUserName) {
        this.applyUserName = applyUserName;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Date getDoneTime() {
        return doneTime;
    }

    public void setDoneTime(Date doneTime) {
        this.doneTime = doneTime;
    }

    public String getCreateUserName() {
        return createUserName;
    }

    public void setCreateUserName(String createUserName) {
        this.createUserName = createUserName;
    }

}
