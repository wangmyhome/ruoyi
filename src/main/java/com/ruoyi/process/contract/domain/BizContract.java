package com.ruoyi.process.contract.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.framework.aspectj.lang.annotation.Excel;
import com.ruoyi.framework.web.domain.BaseEntity;
import java.util.Date;

/**
 * 【请填写功能名称】对象 biz_contract
 *
 * @author ruoyi
 * @date 2020-03-14
 */
public class BizContract extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 签约类型 */
    @Excel(name = "签约类型")
    private String type;

    /** 合同名称 */
    @Excel(name = "合同名称")
    private String title;

    /** 简要说明 */
    @Excel(name = "简要说明")
    private String note;

    /** 签约时间 */
    @Excel(name = "签约时间", width = 30, dateFormat = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date signTime;

    /** 签约公司 */
    @Excel(name = "签约公司")
    private String company;

    /** 流程实例ID */
    @Excel(name = "流程实例ID")
    private String instanceId;

    /** 申请人 */
    @Excel(name = "申请人")
    private String applyUser;

    /** 申请时间 */
    @Excel(name = "申请时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date applyTime;

    /** 实际开始时间 */
    @Excel(name = "实际开始时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date realityStartTime;

    /** 实际结束时间 */
    @Excel(name = "实际结束时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date realityEndTime;

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }
    public void setType(String type)
    {
        this.type = type;
    }

    public String getType()
    {
        return type;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return title;
    }
    public void setNote(String note)
    {
        this.note = note;
    }

    public String getNote()
    {
        return note;
    }
    public void setSignTime(Date signTime)
    {
        this.signTime = signTime;
    }

    public Date getSignTime()
    {
        return signTime;
    }
    public void setCompany(String company)
    {
        this.company = company;
    }

    public String getCompany()
    {
        return company;
    }
    public void setInstanceId(String instanceId)
    {
        this.instanceId = instanceId;
    }

    public String getInstanceId()
    {
        return instanceId;
    }
    public void setApplyUser(String applyUser)
    {
        this.applyUser = applyUser;
    }

    public String getApplyUser()
    {
        return applyUser;
    }
    public void setApplyTime(Date applyTime)
    {
        this.applyTime = applyTime;
    }

    public Date getApplyTime()
    {
        return applyTime;
    }
    public void setRealityStartTime(Date realityStartTime)
    {
        this.realityStartTime = realityStartTime;
    }

    public Date getRealityStartTime()
    {
        return realityStartTime;
    }
    public void setRealityEndTime(Date realityEndTime)
    {
        this.realityEndTime = realityEndTime;
    }

    public Date getRealityEndTime()
    {
        return realityEndTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("type", getType())
            .append("title", getTitle())
            .append("note", getNote())
            .append("signTime", getSignTime())
            .append("company", getCompany())
            .append("instanceId", getInstanceId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("applyUser", getApplyUser())
            .append("applyTime", getApplyTime())
            .append("realityStartTime", getRealityStartTime())
            .append("realityEndTime", getRealityEndTime())
            .toString();
    }
}
