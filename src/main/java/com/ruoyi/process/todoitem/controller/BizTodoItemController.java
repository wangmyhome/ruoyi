package com.ruoyi.process.todoitem.controller;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.process.leave.service.IBizLeaveService;
import com.ruoyi.process.todoitem.domain.BizTodoItem;
import com.ruoyi.process.todoitem.service.IBizTodoItemService;
import com.ruoyi.project.system.domain.SysUser;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 待办事项Controller
 *
 * @author Xianlu Tech
 * @date 2019-11-08
 */
@RestController
@RequestMapping("/process/todoitem")
public class BizTodoItemController extends BaseController {
    private String prefix = "process/todoitem";

    @Autowired
    private IBizTodoItemService bizTodoItemService;

    @Autowired
    private IBizLeaveService bizLeaveService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    /**
     * 查询待办事项列表
     */
    @PreAuthorize("@ss.hasPermi('process:todoitem:list')")
    @PostMapping("/list")
    public TableDataInfo list(@RequestBody BizTodoItem bizTodoItem) {
        bizTodoItem.setIsHandle("0");
        SysUser user = SecurityUtils.getLoginUser().getUser();
        if (!SysUser.isAdmin(user.getUserId())) {
            bizTodoItem.setTodoUserId(user.getUserName());
        }
        startPage();
        List<BizTodoItem> list = bizTodoItemService.selectBizTodoItemList(bizTodoItem);
        return getDataTable(list);
    }

    /**
     * 查询已办事项列表
     */
    @PreAuthorize("@ss.hasPermi('process:todoitem:doneList')")
    @PostMapping("/doneList")
    public TableDataInfo doneList(@RequestBody BizTodoItem bizTodoItem) {
        bizTodoItem.setIsHandle("1");
        SysUser user = SecurityUtils.getLoginUser().getUser();
        if (!SysUser.isAdmin(user.getUserId())) {
            bizTodoItem.setTodoUserId(user.getUserName());
        }
        startPage();
        List<BizTodoItem> list = bizTodoItemService.selectBizTodoItemList(bizTodoItem);
        return getDataTable(list);
    }

    /**
     * 导出待办事项列表
     */
    @PreAuthorize("@ss.hasPermi('process:todoitem:export')")
    @PostMapping("/export")
    public AjaxResult export(BizTodoItem bizTodoItem) {
        bizTodoItem.setIsHandle("0");
        List<BizTodoItem> list = bizTodoItemService.selectBizTodoItemList(bizTodoItem);
        ExcelUtil<BizTodoItem> util = new ExcelUtil<BizTodoItem>(BizTodoItem.class);
        return util.exportExcel(list, "todoitem");
    }

    /**
     * 导出已办事项列表
     */
    @PreAuthorize("@ss.hasPermi('process:todoitem:doneExport')")
    @PostMapping("/doneExport")
    public AjaxResult doneExport(BizTodoItem bizTodoItem) {
        bizTodoItem.setIsHandle("1");
        List<BizTodoItem> list = bizTodoItemService.selectBizTodoItemList(bizTodoItem);
        ExcelUtil<BizTodoItem> util = new ExcelUtil<BizTodoItem>(BizTodoItem.class);
        return util.exportExcel(list, "todoitem");
    }
    /**
     * 新增保存待办事项
     */
    @PreAuthorize("@ss.hasPermi('process:todoitem:add')")
    @Log(title = "待办事项", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult addSave(BizTodoItem bizTodoItem) {
        return toAjax(bizTodoItemService.insertBizTodoItem(bizTodoItem));
    }

    /**
     * 修改保存待办事项
     */
    @PreAuthorize("@ss.hasPermi('process:todoitem:edit')")
    @Log(title = "待办事项", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public AjaxResult editSave(BizTodoItem bizTodoItem) {
        return toAjax(bizTodoItemService.updateBizTodoItem(bizTodoItem));
    }

    /**
     * 删除待办事项
     */
    @PreAuthorize("@ss.hasPermi('process:todoitem:remove')")
    @Log(title = "待办事项", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    public AjaxResult remove(String ids) {
        return toAjax(bizTodoItemService.deleteBizTodoItemByIds(ids));
    }
}
