package com.ruoyi.process.user.controller;

import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.process.group.domain.ActIdGroup;
import com.ruoyi.process.group.service.IActIdGroupService;
import com.ruoyi.process.user.domain.ActIdUser;
import com.ruoyi.process.user.service.IActIdUserService;
import com.ruoyi.project.system.domain.SysUser;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.aspectj.weaver.loadtime.Aj;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程用户Controller
 *
 * @author Xianlu Tech
 * @date 2019-10-02
 */
@RestController
@RequestMapping("/process/user")
public class ActIdUserController extends BaseController {
    private String prefix = "process/user";

    @Autowired
    private IActIdUserService actIdUserService;

    @Autowired
    private IdentityService identityService;

    /**
     * 查询流程用户列表
     */
    @PreAuthorize("@ss.hasPermi('process:user:list')")
    @PostMapping("/list")
    public TableDataInfo list(ActIdUser actIdUser)
    {
        startPage();
        List<ActIdUser> list = actIdUserService.selectActIdUserList(actIdUser);
        return getDataTable(list);
    }

    @GetMapping("/all")
    public AjaxResult all(){
        List<ActIdUser> actIdUsers = actIdUserService.selectActIdUserList(null);
        return AjaxResult.success(actIdUsers);
    }

    /**
     * 导出流程用户列表
     */
    @PreAuthorize("@ss.hasPermi('process:user:export')")
    @PostMapping("/export")
    public AjaxResult export(ActIdUser actIdUser)
    {
        List<ActIdUser> list = actIdUserService.selectActIdUserList(actIdUser);
        ExcelUtil<ActIdUser> util = new ExcelUtil<ActIdUser>(ActIdUser.class);
        return util.exportExcel(list, "user");
    }

    /**
     * 新增保存流程用户
     */
    @PreAuthorize("@ss.hasPermi('process:user:add')")
    @Log(title = "流程用户", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult addSave(@RequestBody ActIdUser actIdUser)
    {
        int rows = actIdUserService.insertActIdUser(actIdUser);
        String[] groupIds = actIdUser.getGroupIds();
        for (String groupId: groupIds) {
            identityService.createMembership(actIdUser.getId(), groupId);
        }
        return toAjax(rows);
    }

    /**
     * 修改流程用户组
     */
    @GetMapping("/edit/{id}")
    @PreAuthorize("@ss.hasPermi('process:user:edit')")
    public AjaxResult edit(@PathVariable("id") String id) {
        ActIdUser actIdUser = actIdUserService.selectActIdUserById(id);
        AjaxResult ajax = AjaxResult.success();
        ajax.put(AjaxResult.DATA_TAG, actIdUser);
        ajax.put("groups",actIdUserService.selectGroupByUserId(id));
        return ajax;
    }

    /**
     * 修改保存流程用户
     */
    @PreAuthorize("@ss.hasPermi('process:user:edit')")
    @Log(title = "流程用户", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public AjaxResult editSave(@RequestBody ActIdUser actIdUser)
    {
        int rows = actIdUserService.updateActIdUser(actIdUser);
        String[] groupIds = actIdUser.getGroupIds();
        List<Group> groupList = identityService.createGroupQuery().groupMember(actIdUser.getId()).list();
        // 先删后增
        groupList.forEach(existGroup -> {
            identityService.deleteMembership(actIdUser.getId(), existGroup.getId());
        });
        for (String groupId: groupIds) {
            identityService.createMembership(actIdUser.getId(), groupId);
        }
        return toAjax(rows);
    }

    /**
     * 删除流程用户
     */
    @PreAuthorize("@ss.hasPermi('process:user:remove')")
    @Log(title = "流程用户", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    public AjaxResult remove(String ids) {
        return toAjax(actIdUserService.deleteActIdUserByIds(ids));
    }

    @PreAuthorize("@ss.hasPermi('process:user:list')")
    @PostMapping("/systemUserList")
    public TableDataInfo systemUserList(SysUser user) {
        startPage();
        List<SysUser> list = actIdUserService.selectUnAssociatedSystemUserList(user);
        return getDataTable(list);
    }

}
