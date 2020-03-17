package com.ruoyi.process.todoitem.service.impl;

import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.process.contract.domain.BizContract;
import com.ruoyi.process.general.mapper.ProcessMapper;
import com.ruoyi.process.leave.domain.BizLeaveVo;
import com.ruoyi.process.todoitem.domain.BizTodoItem;
import com.ruoyi.process.todoitem.mapper.BizTodoItemMapper;
import com.ruoyi.process.todoitem.service.IBizTodoItemService;
import com.ruoyi.project.system.domain.SysUser;
import com.ruoyi.project.system.mapper.SysUserMapper;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 待办事项Service业务层处理
 *
 * @author Xianlu Tech
 * @date 2019-11-08
 */
@Service
@Transactional
public class BizTodoItemServiceImpl implements IBizTodoItemService {
    @Resource
    private BizTodoItemMapper bizTodoItemMapper;

    @Resource
    private SysUserMapper userMapper;

    @Resource
    private ProcessMapper processMapper;

    @Autowired
    private TaskService taskService;

    /**
     * 查询待办事项
     *
     * @param id 待办事项ID
     * @return 待办事项
     */
    @Override
    public BizTodoItem selectBizTodoItemById(Long id) {
        return bizTodoItemMapper.selectBizTodoItemById(id);
    }

    /**
     * 查询待办事项列表
     *
     * @param bizTodoItem 待办事项
     * @return 待办事项
     */
    @Override
    public List<BizTodoItem> selectBizTodoItemList(BizTodoItem bizTodoItem) {
        return bizTodoItemMapper.selectBizTodoItemList(bizTodoItem);
    }

    /**
     * 新增待办事项
     *
     * @param bizTodoItem 待办事项
     * @return 结果
     */
    @Override
    public int insertBizTodoItem(BizTodoItem bizTodoItem) {
        return bizTodoItemMapper.insertBizTodoItem(bizTodoItem);
    }

    /**
     * 修改待办事项
     *
     * @param bizTodoItem 待办事项
     * @return 结果
     */
    @Override
    public int updateBizTodoItem(BizTodoItem bizTodoItem) {
        return bizTodoItemMapper.updateBizTodoItem(bizTodoItem);
    }

    /**
     * 删除待办事项对象
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteBizTodoItemByIds(String ids) {
        return bizTodoItemMapper.deleteBizTodoItemByIds(Convert.toStrArray(ids));
    }

    /**
     * 删除待办事项信息
     *
     * @param id 待办事项ID
     * @return 结果
     */
    @Override
    public int deleteBizTodoItemById(Long id) {
        return bizTodoItemMapper.deleteBizTodoItemById(id);
    }

    /**
     * @Description //请假流程
     * @Author shanpeng
     * @Date  10:19
     * @Param [instanceId, leave, module]
     * @return int
     **/
    @Override
    public int insertTodoItem(String instanceId, BizLeaveVo leave, String module) {
        BizTodoItem todoItem = new BizTodoItem();
        todoItem.setItemName(leave.getTitle());
        todoItem.setItemContent(leave.getReason());
        todoItem.setIsView("0");
        todoItem.setIsHandle("0");
        todoItem.setModule(module);
        todoItem.setTodoTime(DateUtils.getNowDate());
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(instanceId).active().list();
        int counter = 0;
        for (Task task: taskList) {

            // todoitem 去重
            BizTodoItem bizTodoItem = bizTodoItemMapper.selectTodoItemByTaskId(task.getId());
            if (bizTodoItem != null) continue;

            BizTodoItem newItem = new BizTodoItem();
            BeanUtils.copyProperties(todoItem, newItem);
            newItem.setTaskId(task.getId());
            newItem.setTaskName("task" + task.getTaskDefinitionKey().substring(0, 1).toUpperCase() + task.getTaskDefinitionKey().substring(1));
            newItem.setNodeName(task.getName());
            String assignee = task.getAssignee();
            if (StringUtils.isNotBlank(assignee)) {
                newItem.setTodoUserId(assignee);
                SysUser user = userMapper.selectUserByUserName(assignee);
                newItem.setTodoUserName(user.getNickName());
                bizTodoItemMapper.insertBizTodoItem(newItem);
                counter++;
            } else {
                List<String> todoUserIdList = processMapper.selectTodoUserListByTaskId(task.getId());
                System.out.println("长度:"+todoUserIdList.size());
                for (String todoUserId: todoUserIdList) {
                    System.out.println("ID:"+todoUserId);
                    SysUser todoUser = userMapper.selectUserByUserName(todoUserId);
                    System.out.println(todoUser);
                    newItem.setTodoUserId(todoUser.getUserName());
                    newItem.setTodoUserName(todoUser.getNickName());
                    bizTodoItemMapper.insertBizTodoItem(newItem);
                    counter++;
                }
            }
        }
        return counter;
    }

    /**
     * @Description //采购合同流程
     * @Author shanpeng
     * @Date  10:20
     * @Param [instanceId, contract, module]
     * @return int
     **/
    @Override
    public int insertTodoItem(String instanceId, BizContract contract, String module) {
        BizTodoItem todoItem = new BizTodoItem();
        todoItem.setItemName(contract.getTitle());
        todoItem.setItemContent(contract.getNote());
        todoItem.setIsView("0");
        todoItem.setIsHandle("0");
        todoItem.setModule(module);
        todoItem.setTodoTime(DateUtils.getNowDate());
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(instanceId).active().list();
        int counter = 0;
        for (Task task: taskList) {

            // todoitem 去重
            BizTodoItem bizTodoItem = bizTodoItemMapper.selectTodoItemByTaskId(task.getId());
            if (bizTodoItem != null) continue;

            BizTodoItem newItem = new BizTodoItem();
            BeanUtils.copyProperties(todoItem, newItem);
            newItem.setTaskId(task.getId());
            newItem.setTaskName("task" + task.getTaskDefinitionKey().substring(0, 1).toUpperCase() + task.getTaskDefinitionKey().substring(1));
            newItem.setNodeName(task.getName());
            String assignee = task.getAssignee();
            if (StringUtils.isNotBlank(assignee)) {
                newItem.setTodoUserId(assignee);
                SysUser user = userMapper.selectUserByUserName(assignee);
                newItem.setTodoUserName(user.getNickName());
                bizTodoItemMapper.insertBizTodoItem(newItem);
                counter++;
            } else {
                List<String> todoUserIdList = processMapper.selectTodoUserListByTaskId(task.getId());
                System.out.println("长度:"+todoUserIdList.size());
                for (String todoUserId: todoUserIdList) {
                    System.out.println("ID:"+todoUserId);
                    SysUser todoUser = userMapper.selectUserByUserName(todoUserId);
                    System.out.println(todoUser);
                    newItem.setTodoUserId(todoUser.getUserName());
                    newItem.setTodoUserName(todoUser.getNickName());
                    bizTodoItemMapper.insertBizTodoItem(newItem);
                    counter++;
                }
            }
        }
        return counter;
    }
}
