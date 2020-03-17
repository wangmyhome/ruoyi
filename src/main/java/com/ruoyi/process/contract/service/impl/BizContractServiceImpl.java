package com.ruoyi.process.contract.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.Page;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.page.PageDomain;
import com.ruoyi.framework.web.page.TableSupport;
import com.ruoyi.process.contract.domain.BizContract;
import com.ruoyi.process.contract.domain.BizContractVo;
import com.ruoyi.process.contract.mapper.BizContractMapper;
import com.ruoyi.process.contract.service.IBizContractService;
import com.ruoyi.process.leave.domain.BizLeaveVo;
import com.ruoyi.process.todoitem.domain.BizTodoItem;
import com.ruoyi.process.todoitem.service.IBizTodoItemService;
import com.ruoyi.project.system.domain.SysUser;
import com.ruoyi.project.system.mapper.SysUserMapper;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 采购合同Service业务层处理
 *
 * @author ruoyi
 * @date 2020-03-14
 */
@Service
public class BizContractServiceImpl implements IBizContractService
{
    @Autowired
    private BizContractMapper bizContractMapper;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private IBizTodoItemService bizTodoItemService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private HistoryService historyService;

    /**
     * 查询采购合同
     *
     * @param id 采购合同ID
     * @return 采购合同
     */
    @Override
    public BizContractVo selectBizContractById(Long id)
    {
        return bizContractMapper.selectBizContractById(id);
    }

    /**
     * 查询采购合同列表
     *
     * @param bizContract 采购合同
     * @return 采购合同
     */
    @Override
    public List<BizContractVo> selectBizContractList(BizContract bizContract)
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();

        // PageHelper 仅对第一个 List 分页
        Page<BizContractVo> list = (Page<BizContractVo>) bizContractMapper.selectBizContractList(bizContract);
        Page<BizContractVo> returnList = new Page<>();
        for (BizContractVo leave: list) {
            SysUser sysUser = userMapper.selectUserByUserName(leave.getCreateBy());
            if (sysUser != null) {
                leave.setCreateUserName(sysUser.getNickName());
            }
            SysUser sysUser2 = userMapper.selectUserByUserName(leave.getApplyUser());
            if (sysUser2 != null) {
                leave.setApplyUserName(sysUser.getNickName());
            }
            // 当前环节
            if (StringUtils.isNotBlank(leave.getInstanceId())) {
                List<Task> taskList = taskService.createTaskQuery()
                        .processInstanceId(leave.getInstanceId())
//                        .singleResult();
                        .list();    // 例如请假会签，会同时拥有多个任务
                if (!CollectionUtils.isEmpty(taskList)) {
                    Task task = taskList.get(0);
                    leave.setTaskId(task.getId());
                    leave.setTaskName(task.getName()+"审批");
                } else {
                    leave.setTaskName("已完成审批");
                }
            } else {
                leave.setTaskName("未启动审批");
            }
            returnList.add(leave);
        }
        returnList.setTotal(CollectionUtils.isEmpty(list) ? 0 : list.getTotal());
        returnList.setPageNum(pageNum);
        returnList.setPageSize(pageSize);
        return returnList;
    }

    /**
     * 新增采购合同
     *
     * @param bizContract 采购合同
     * @return 结果
     */
    @Override
    public int insertBizContract(BizContract bizContract)
    {
        bizContract.setCreateBy(SecurityUtils.getLoginUser().getUsername());
        bizContract.setCreateTime(DateUtils.getNowDate());
        return bizContractMapper.insertBizContract(bizContract);
    }

    /**
     * 修改采购合同
     *
     * @param bizContract 采购合同
     * @return 结果
     */
    @Override
    public int updateBizContract(BizContract bizContract)
    {
        bizContract.setUpdateTime(DateUtils.getNowDate());
        return bizContractMapper.updateBizContract(bizContract);
    }

    /**
     * 批量删除采购合同
     *
     * @param ids 需要删除的采购合同ID
     * @return 结果
     */
    @Override
    public int deleteBizContractByIds(Long[] ids)
    {
        return bizContractMapper.deleteBizContractByIds(ids);
    }

    /**
     * 删除采购合同信息
     *
     * @param id 采购合同ID
     * @return 结果
     */
    @Override
    public int deleteBizContractById(Long id)
    {
        return bizContractMapper.deleteBizContractById(id);
    }

    @Override
    public ProcessInstance submitApply(BizContract entity, String applyUserId) {
        entity.setApplyUser(applyUserId);
        entity.setApplyTime(DateUtils.getNowDate());
        entity.setUpdateBy(applyUserId);
        bizContractMapper.updateBizContract(entity);
        String businessKey = entity.getId().toString(); // 实体类 ID，作为流程的业务 key

        // 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
        identityService.setAuthenticatedUserId(applyUserId);

        ProcessInstance processInstance = runtimeService // 启动流程时设置业务 key
                .startProcessInstanceByKey("contract_purchase_exclusive", businessKey);
        String processInstanceId = processInstance.getId();
        entity.setInstanceId(processInstanceId); // 建立双向关系
        bizContractMapper.updateBizContract(entity);

        // 下一节点处理人待办事项
        bizTodoItemService.insertTodoItem(processInstanceId, entity, "contract_purchase_exclusive");
        return processInstance;
    }

    /**
     * 查询待办任务
     */
    @Transactional(readOnly = true)
    public List<BizContractVo> findTodoTasks(BizContractVo contract, String userId) {
        List<BizContractVo> results = new ArrayList<>();
        List<Task> tasks = new ArrayList<Task>();

        // 根据当前人的ID查询
        List<Task> todoList = taskService.createTaskQuery().processDefinitionKey("contract_purchase_exclusive").taskAssignee(userId).list();

        // 根据当前人未签收的任务
        List<Task> unsignedTasks = taskService.createTaskQuery().processDefinitionKey("contract_purchase_exclusive").taskCandidateUser(userId).list();

        // 合并
        tasks.addAll(todoList);
        tasks.addAll(unsignedTasks);

        // 根据流程的业务ID查询实体并关联
        for (Task task : tasks) {
            String processInstanceId = task.getProcessInstanceId();

            // 条件过滤 1
            if (StringUtils.isNotBlank(contract.getInstanceId()) && !contract.getInstanceId().equals(processInstanceId)) {
                continue;
            }

            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            String businessKey = processInstance.getBusinessKey();
            BizContractVo contract2 = bizContractMapper.selectBizContractById(new Long(businessKey));

            // 条件过滤 2
            if (StringUtils.isNotBlank(contract.getType()) && !contract.getType().equals(contract2.getType())) {
                continue;
            }

            contract2.setTaskId(task.getId());
            contract2.setTaskName(task.getName());

            SysUser sysUser = userMapper.selectUserByUserName(contract2.getApplyUser());
            contract2.setApplyUserName(sysUser.getNickName());

            results.add(contract2);
        }
        return results;
    }


    /**
     * 查询已办列表
     * @param bizContract
     * @param userId
     * @return
     */
    @Override
    public List<BizContractVo> findDoneTasks(BizContractVo bizContract, String userId) {
        List<BizContractVo> results = new ArrayList<>();
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
                .processDefinitionKey("contract_purchase_exclusive")
                .taskAssignee(userId)
                .finished()
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();

        // 根据流程的业务ID查询实体并关联
        for (HistoricTaskInstance instance : list) {
            String processInstanceId = instance.getProcessInstanceId();

            // 条件过滤 1
            if (StringUtils.isNotBlank(bizContract.getInstanceId()) && !bizContract.getInstanceId().equals(processInstanceId)) {
                continue;
            }

            HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

            String businessKey = processInstance.getBusinessKey();
            BizContractVo contract2 = bizContractMapper.selectBizContractById(new Long(businessKey));

            // 条件过滤 2
            if (StringUtils.isNotBlank(bizContract.getType()) && !bizContract.getType().equals(contract2.getType())) {
                continue;
            }

            contract2.setTaskId(instance.getId());
            contract2.setTaskName(instance.getName());
            contract2.setDoneTime(instance.getEndTime());

            SysUser sysUser = userMapper.selectUserByUserName(contract2.getApplyUser());
            contract2.setApplyUserName(sysUser.getNickName());

            results.add(contract2);
        }
        return results;
    }


    /**
     * 完成任务
     * @param contract
     * @param saveEntity
     * @param taskId
     * @param variables
     */
    @Override
    public void complete(BizContractVo contract, boolean saveEntity, String taskId, Map<String, Object> variables) {
        if (saveEntity) {
            bizContractMapper.updateBizContract(contract);
        }
        // 只有签收任务，act_hi_taskinst 表的 assignee 字段才不为 null
        taskService.claim(taskId, SecurityUtils.getLoginUser().getUsername());
        taskService.complete(taskId, variables);

        // 更新待办事项状态
        BizTodoItem query = new BizTodoItem();
        query.setTaskId(taskId);
        // 考虑到候选用户组，会有多个 todoitem 办理同个 task
        List<BizTodoItem> updateList = CollectionUtils.isEmpty(bizTodoItemService.selectBizTodoItemList(query)) ? null : bizTodoItemService.selectBizTodoItemList(query);
        for (BizTodoItem update: updateList) {
            // 找到当前登录用户的 todoitem，置为已办
            if (update.getTodoUserId().equals(SecurityUtils.getLoginUser().getUsername())) {
                update.setIsView("1");
                update.setIsHandle("1");
                update.setHandleUserId(SecurityUtils.getLoginUser().getUsername());
                update.setHandleUserName(SecurityUtils.getLoginUser().getUser().getNickName());
                update.setHandleTime(DateUtils.getNowDate());
                bizTodoItemService.updateBizTodoItem(update);
            } else {
                bizTodoItemService.deleteBizTodoItemById(update.getId()); // 删除候选用户组其他 todoitem
            }
        }

        // 下一节点处理人待办事项
        bizTodoItemService.insertTodoItem(contract.getInstanceId(), contract, "contract_purchase_exclusive");
    }

}
