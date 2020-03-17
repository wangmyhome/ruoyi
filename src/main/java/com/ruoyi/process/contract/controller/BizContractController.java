package com.ruoyi.process.contract.controller;

import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.process.contract.domain.BizContract;
import com.ruoyi.process.contract.domain.BizContractVo;
import com.ruoyi.process.contract.service.IBizContractService;
import com.ruoyi.process.leave.domain.BizLeaveVo;
import com.ruoyi.project.system.domain.SysUser;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.web.page.TableDataInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 采购合同Controller
 *
 * @author ruoyi
 * @date 2020-03-14
 */
@RestController
@RequestMapping("/process/contract")
public class BizContractController extends BaseController
{
    @Autowired
    private IBizContractService bizContractService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private IdentityService identityService;

    /**
     * 查询采购合同列表
     */
    @PreAuthorize("@ss.hasPermi('process:contract:list')")
    @GetMapping("/list")
    public TableDataInfo list(BizContractVo bizContract)
    {
        SysUser user = SecurityUtils.getLoginUser().getUser();
        if (!SysUser.isAdmin(user.getUserId())) {
            bizContract.setCreateBy(user.getUserName());
        }
        startPage();
        List<BizContractVo> list = bizContractService.selectBizContractList(bizContract);
        return getDataTable(list);
    }

    /**
     * 导出采购合同列表
     */
    @PreAuthorize("@ss.hasPermi('process:contract:export')")
    @Log(title = "采购合同", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public AjaxResult export(BizContract bizContract)
    {
        List<BizContractVo> list = bizContractService.selectBizContractList(bizContract);
        ExcelUtil<BizContract> util = new ExcelUtil<BizContract>(BizContract.class);
        return null;
//        return util.exportExcel(list, "contract");
    }

    /**
     * 获取采购合同详细信息
     */
    @PreAuthorize("@ss.hasPermi('process:contract:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(bizContractService.selectBizContractById(id));
    }

    /**
     * 新增采购合同
     */
    @PreAuthorize("@ss.hasPermi('process:contract:add')")
    @Log(title = "采购合同", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BizContract bizContract)
    {
        Long userId = SecurityUtils.getLoginUser().getUser().getUserId();
//        if (SysUser.isAdmin(userId)) {
//            return AjaxResult.error("提交申请失败：不允许管理员提交申请！");
//        }
        return toAjax(bizContractService.insertBizContract(bizContract));
    }

    /**
     * 修改请假业务
     */
    @GetMapping("/edit/{id}")
    @PreAuthorize("@ss.hasPermi('process:contract:edit')")
    @Log(title = "采购合同", businessType = BusinessType.UPDATE)
    public AjaxResult edit(@PathVariable("id") Long id)
    {
        System.out.println("------"+id);
        /*BizContract contract = bizContractService.selectBizContractById(id);
        String createUserName = contract.getCreateBy();
        String loginUserName = SecurityUtils.getUsername();
        if(!SecurityUtils.isAdmin(SecurityUtils.getLoginUser().getUser().getUserId()) && !createUserName.equals(loginUserName)){
            return AjaxResult.error("不允许非创建人修改！");
        }*/
        BizContract bizContract = bizContractService.selectBizContractById(id);
        return AjaxResult.success(bizContract);
    }

    /**
     * 删除采购合同
     */
    @PreAuthorize("@ss.hasPermi('process:contract:remove')")
    @Log(title = "采购合同", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        System.out.println("shanchufang0000000"+ids[0].toString());
        /*BizContract contract = bizContractService.selectBizContractById(ids[0]);
        String createUserName = contract.getCreateBy();
        String loginUserName = SecurityUtils.getUsername();
        if(!SecurityUtils.isAdmin(SecurityUtils.getLoginUser().getUser().getUserId()) && !createUserName.equals(loginUserName)){
            return AjaxResult.error("不允许非创建人删除！");
        }*/
        return toAjax(bizContractService.deleteBizContractByIds(ids));
    }

    /**
     * 提交申请
     */
    @Log(title = "合同业务", businessType = BusinessType.UPDATE)
    @PostMapping( "/submitApply")
    public AjaxResult submitApply(Long id) {
        BizContract contract = bizContractService.selectBizContractById(id);
        String createUserName = contract.getCreateBy();
        String applyUserId = SecurityUtils.getUsername();
        if(!createUserName.equals(applyUserId)){
            return AjaxResult.error("不允许非创建人提交申请！");
        }
        bizContractService.submitApply(contract, applyUserId);
        return AjaxResult.success();
    }


    /**
     * 我的待办列表
     * @param bizContract
     * @return
     */
    @PreAuthorize("@ss.hasPermi('process:contract:taskList')")
    @PostMapping("/taskList")
    public TableDataInfo taskList(BizContractVo bizContract) {
        System.out.println("***"+bizContract);
        startPage();
        List<BizContractVo> list = bizContractService.findTodoTasks(bizContract, SecurityUtils.getUsername());
        return getDataTable(list);
    }


    /**
     * 我的已办列表
     * @param bizContract
     * @return
     */
    @PreAuthorize("@ss.hasPermi('process:contract:taskDoneList')")
    @PostMapping("/taskDoneList")
    public TableDataInfo taskDoneList(BizContractVo bizContract) {
        startPage();
        List<BizContractVo> list = bizContractService.findDoneTasks(bizContract, SecurityUtils.getUsername());
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
        BizContractVo bizLeave = bizContractService.selectBizContractById(new Long(processInstance.getBusinessKey()));
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
                               @ModelAttribute("preloadContract") BizContractVo contract, HttpServletRequest request) {
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
                taskService.addComment(taskId, contract.getInstanceId(), comment);
            }
            bizContractService.complete(contract, saveEntityBoolean, taskId, variables);

            return AjaxResult.success("任务已完成");
        } catch (Exception e) {
            logger.error("error on complete task {}, variables={}", new Object[]{taskId, variables, e});
            return AjaxResult.error("完成任务失败");
        }
    }

    /**
     * preloadLeave
     */
    @ModelAttribute("preloadContract")
    public BizContractVo getLeave(@RequestParam(value = "id", required = false) Long id, HttpSession session) {
        if (id != null) {
            return bizContractService.selectBizContractById(id);
        }
        return new BizContractVo();
    }


}
