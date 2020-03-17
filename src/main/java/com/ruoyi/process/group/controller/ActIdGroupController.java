package com.ruoyi.process.group.controller;

import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.process.group.domain.ActIdGroup;
import com.ruoyi.process.group.service.IActIdGroupService;
import com.ruoyi.process.user.domain.ActIdUser;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程用户组Controller
 *
 * @author Xianlu Tech
 * @date 2019-10-02
 */
@RestController
@RequestMapping("/process/group")
public class ActIdGroupController extends BaseController
{
    private String prefix = "process/group";

    @Autowired
    private IActIdGroupService actIdGroupService;

    @Autowired
    private IdentityService identityService;

    /**
     * 获取所有的组列表
     */
    @GetMapping("/all")
    @ResponseBody
    public AjaxResult all(){
        List<ActIdGroup> actIdGroups = actIdGroupService.selectActIdGroupList(null);
        return AjaxResult.success(actIdGroups);
    }

    /**
     * 查询流程用户组列表
     */
    @PreAuthorize("@ss.hasPermi('process:group:list')")
    @PostMapping("/list")
    public TableDataInfo list(ActIdGroup actIdGroup)
    {
        startPage();
        List<ActIdGroup> list = actIdGroupService.selectActIdGroupList(actIdGroup);
        return getDataTable(list);
    }

    /**
     * 导出流程用户组列表
     */
    @PreAuthorize("@ss.hasPermi('process:group:export')")
    @PostMapping("/export")
    public AjaxResult export(ActIdGroup actIdGroup)
    {
        List<ActIdGroup> list = actIdGroupService.selectActIdGroupList(actIdGroup);
        ExcelUtil<ActIdGroup> util = new ExcelUtil<ActIdGroup>(ActIdGroup.class);
        return util.exportExcel(list, "group");
    }

    /**
     * 新增保存流程用户组
     */
    @PreAuthorize("@ss.hasPermi('process:group:add')")
    @Log(title = "流程用户组", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult addSave(@RequestBody ActIdGroup actIdGroup) {
        ActIdGroup existsGroup = actIdGroupService.selectActIdGroupById(actIdGroup.getId());
        if(!ObjectUtils.isEmpty(existsGroup)){
            return AjaxResult.error("新增流程用户组'" + actIdGroup.getName() + "'失败，组id'"+actIdGroup.getId()+"'已存在");
        }else{
            int rows = actIdGroupService.insertActIdGroup(actIdGroup);
            String[] userIds = actIdGroup.getUserIds();
            for (String userId: userIds) {
                identityService.createMembership(userId, actIdGroup.getId());
            }
            return toAjax(rows);
        }

    }

    /**
     * 修改流程用户组
     */
    @GetMapping("/edit/{id}")
    @PreAuthorize("@ss.hasPermi('process:group:edit')")
    public AjaxResult edit(@PathVariable("id") String id) {
        ActIdGroup actIdGroup = actIdGroupService.selectActIdGroupById(id);
        AjaxResult ajax = AjaxResult.success();
        ajax.put(AjaxResult.DATA_TAG, actIdGroup);
        ajax.put("users",actIdGroupService.selectUserByGroupId(id));
        return ajax;
    }

    /**
     * 修改保存流程用户组
     */
    @PreAuthorize("@ss.hasPermi('process:group:edit')")
    @Log(title = "流程用户组", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public AjaxResult editSave(@RequestBody ActIdGroup actIdGroup) {
        int rows = actIdGroupService.updateActIdGroup(actIdGroup);
        String[] userIds = actIdGroup.getUserIds();
        List<User> userList = identityService.createUserQuery().memberOfGroup(actIdGroup.getId()).list();
        // 先删后增
        userList.forEach(existUser -> {
            identityService.deleteMembership(existUser.getId(), actIdGroup.getId());
        });
        for (String userId: userIds) {
            identityService.createMembership(userId, actIdGroup.getId());
        }
        return toAjax(rows);
    }

    /**
     * 删除流程用户组
     */
    @PreAuthorize("@ss.hasPermi('process:group:remove')")
    @Log(title = "流程用户组", businessType = BusinessType.DELETE)
    @PostMapping( "/remove")
    public AjaxResult remove(String ids) {
        String[] groupIdList = Convert.toStrArray(ids);
        for (String groupId : groupIdList){
            List<ActIdUser> actIdUsers = actIdGroupService.selectUserByGroupId(groupId);
            for (ActIdUser actIdUser: actIdUsers){
                if(actIdUser.isFlag()){
                    identityService.deleteMembership(actIdUser.getId(),groupId);
                }
            }
        }
        return toAjax(actIdGroupService.deleteActIdGroupByIds(ids));
    }

}
