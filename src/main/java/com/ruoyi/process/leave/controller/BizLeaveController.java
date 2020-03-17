package com.ruoyi.process.leave.controller;


import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.process.leave.domain.BizLeaveVo;
import com.ruoyi.process.leave.service.IBizLeaveService;
import com.ruoyi.project.system.domain.SysUser;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请假业务Controller
 *
 * @author Xianlu Tech
 * @date 2019-10-11
 */
@RestController
@RequestMapping("/process/leave")
public class BizLeaveController extends BaseController {
    private String prefix = "process/leave";

    @Autowired
    private IBizLeaveService bizLeaveService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IdentityService identityService;


    /**
     * 查询请假业务列表
     */
    @PreAuthorize("@ss.hasPermi('process:leave:list')")
    @PostMapping("/list")
    public TableDataInfo list(@RequestBody BizLeaveVo bizLeave) {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        if (!SysUser.isAdmin(user.getUserId())) {
            bizLeave.setCreateBy(user.getUserName());
        }
        startPage();
        List<BizLeaveVo> list = bizLeaveService.selectBizLeaveList(bizLeave);
        return getDataTable(list);
    }

    /**
     * 导出请假业务列表
     */
    @PreAuthorize("@ss.hasPermi('process:leave:export')")
    @PostMapping("/export")
    public AjaxResult export(BizLeaveVo bizLeave) {
        List<BizLeaveVo> list = bizLeaveService.selectBizLeaveList(bizLeave);
        ExcelUtil<BizLeaveVo> util = new ExcelUtil<BizLeaveVo>(BizLeaveVo.class);
        return util.exportExcel(list, "leave");
    }


    /**
     * 新增保存请假业务
     */
    @PreAuthorize("@ss.hasPermi('process:leave:add')")
    @Log(title = "请假业务", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult addSave(@RequestBody BizLeaveVo bizLeave) {
        Long userId = SecurityUtils.getLoginUser().getUser().getUserId();
        if (SysUser.isAdmin(userId)) {
            return AjaxResult.error("提交申请失败：不允许管理员提交申请！");
        }
        return toAjax(bizLeaveService.insertBizLeave(bizLeave));
    }

    /**
     * 修改请假业务
     */
    @GetMapping("/edit/{id}")
    @PreAuthorize("@ss.hasPermi('process:leave:edit')")
    public AjaxResult edit(@PathVariable("id") Long id) {
        BizLeaveVo leave = bizLeaveService.selectBizLeaveById(id);
        String createUserName = leave.getCreateBy();
        String loginUserName = SecurityUtils.getUsername();
        if(!SecurityUtils.isAdmin(SecurityUtils.getLoginUser().getUser().getUserId()) && !createUserName.equals(loginUserName)){
            return AjaxResult.error("不允许非创建人修改！");
        }
        BizLeaveVo bizLeave = bizLeaveService.selectBizLeaveById(id);
        return AjaxResult.success(bizLeave);
    }
    /**
     * 修改保存请假业务
     */
    @PreAuthorize("@ss.hasPermi('process:leave:edit')")
    @Log(title = "请假业务", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public AjaxResult editSave(@RequestBody BizLeaveVo bizLeave) {
        BizLeaveVo leave = bizLeaveService.selectBizLeaveById(bizLeave.getId());
        String createUserName = leave.getCreateBy();
        String loginUserName = SecurityUtils.getUsername();
        if( !SecurityUtils.isAdmin(SecurityUtils.getLoginUser().getUser().getUserId()) && !createUserName.equals(loginUserName)){
            return AjaxResult.error("不允许非创建人修改！");
        }
        return toAjax(bizLeaveService.updateBizLeave(bizLeave));
    }

    /**
     * 删除请假业务
     */
    @PreAuthorize("@ss.hasPermi('process:leave:remove')")
    @Log(title = "请假业务", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    public AjaxResult remove(String ids) {
        BizLeaveVo leave = bizLeaveService.selectBizLeaveById(Long.parseLong(ids));
        String createUserName = leave.getCreateBy();
        String loginUserName = SecurityUtils.getUsername();
        if(!SecurityUtils.isAdmin(SecurityUtils.getLoginUser().getUser().getUserId()) && !createUserName.equals(loginUserName)){
            return AjaxResult.error("不允许非创建人删除！");
        }
        return toAjax(bizLeaveService.deleteBizLeaveByIds(ids));
    }

    /**
     * 提交申请
     */
    @Log(title = "请假业务", businessType = BusinessType.UPDATE)
    @PostMapping( "/submitApply")
    public AjaxResult submitApply(Long id) {
        BizLeaveVo leave = bizLeaveService.selectBizLeaveById(id);
        String createUserName = leave.getCreateBy();
        String applyUserId = SecurityUtils.getUsername();
        if(!createUserName.equals(applyUserId)){
            return AjaxResult.error("不允许非创建人提交申请！");
        }
        bizLeaveService.submitApply(leave, applyUserId);
        return AjaxResult.success();
    }

    /**
     * 我的待办列表
     * @param bizLeave
     * @return
     */
    @PreAuthorize("@ss.hasPermi('process:leave:taskList')")
    @PostMapping("/taskList")
    public TableDataInfo taskList(BizLeaveVo bizLeave) {
        System.out.println("***"+bizLeave);
        startPage();
        List<BizLeaveVo> list = bizLeaveService.findTodoTasks(bizLeave, SecurityUtils.getUsername());
        return getDataTable(list);
    }

    /**
     * 加载审批弹窗信息
     * @param taskId
     * @return
     */
    @GetMapping("/verifyInfo/{taskId}")
    public AjaxResult verifyInfo(@PathVariable("taskId") String taskId ) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        BizLeaveVo bizLeave = bizLeaveService.selectBizLeaveById(new Long(processInstance.getBusinessKey()));
        bizLeave.setTaskId(taskId);
        bizLeave.setTaskName(task.getName());
        return AjaxResult.success(bizLeave);
    }

    /**
     * 完成任务
     *
     * @return
     */
    @RequestMapping(value = "/complete/{taskId}", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxResult complete(@PathVariable("taskId") String taskId, @RequestParam(value = "saveEntity", required = false) String saveEntity,
                           @ModelAttribute("preloadLeave") BizLeaveVo leave, HttpServletRequest request) {
        boolean saveEntityBoolean = BooleanUtils.toBoolean(saveEntity);
        Map<String, Object> variables = new HashMap<String, Object>();
        Enumeration<String> parameterNames = request.getParameterNames();
        String comment = null;          // 批注
        try {
            while (parameterNames.hasMoreElements()) {
                String parameterName = (String) parameterNames.nextElement();
                if (parameterName.startsWith("p_")) {
                    // 参数结构：p_B_name，p为参数的前缀，B为类型，name为属性名称
                    String[] parameter = parameterName.split("_");
                    if (parameter.length == 3) {
                        String paramValue = request.getParameter(parameterName);
                        Object value = paramValue;
                        if (parameter[1].equals("B")) {
                            value = BooleanUtils.toBoolean(paramValue);
                        } else if (parameter[1].equals("DT")) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            value = sdf.parse(paramValue);
                        } else if (parameter[1].equals("COM")) {
                            comment = paramValue;
                        }
                        variables.put(parameter[2], value);
                    } else {
                        throw new RuntimeException("invalid parameter for activiti variable: " + parameterName);
                    }
                }
            }
            if (StringUtils.isNotEmpty(comment)) {
                identityService.setAuthenticatedUserId(SecurityUtils.getUsername());
                taskService.addComment(taskId, leave.getInstanceId(), comment);
            }
            bizLeaveService.complete(leave, saveEntityBoolean, taskId, variables);

            return AjaxResult.success("任务已完成");
        } catch (Exception e) {
            logger.error("error on complete task {}, variables={}", new Object[]{taskId, variables, e});
            return AjaxResult.error("完成任务失败");
        }
    }

    /**
     * preloadLeave
     */
    @ModelAttribute("preloadLeave")
    public BizLeaveVo getLeave(@RequestParam(value = "id", required = false) Long id, HttpSession session) {
        if (id != null) {
            return bizLeaveService.selectBizLeaveById(id);
        }
        return new BizLeaveVo();
    }


    /**
     * 我的已办列表
     * @param bizLeave
     * @return
     */
    @PreAuthorize("@ss.hasPermi('process:leave:taskDoneList')")
    @PostMapping("/taskDoneList")
    public TableDataInfo taskDoneList(BizLeaveVo bizLeave) {
        startPage();
        List<BizLeaveVo> list = bizLeaveService.findDoneTasks(bizLeave, SecurityUtils.getUsername());
        return getDataTable(list);
    }

}
