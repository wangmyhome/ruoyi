package com.ruoyi.process.definition.controller;


import com.github.pagehelper.Page;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.framework.aspectj.lang.annotation.Log;
import com.ruoyi.framework.aspectj.lang.enums.BusinessType;
import com.ruoyi.framework.config.RuoYiConfig;
import com.ruoyi.framework.web.controller.BaseController;
import com.ruoyi.framework.web.domain.AjaxResult;
import com.ruoyi.framework.web.page.TableDataInfo;
import com.ruoyi.process.definition.domain.ProcessDefinition;
import com.ruoyi.process.definition.service.ProcessDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/process/definition")
public class ProcessDefinitionController extends BaseController {

    private static final Logger log = LoggerFactory.getLogger(ProcessDefinitionController.class);

    private String prefix = "process/definition";

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @PostMapping("/list")
    @PreAuthorize("@ss.hasPermi('process:definition:list')")
    public TableDataInfo list(ProcessDefinition processDefinition) {
        List<ProcessDefinition> list = processDefinitionService.listProcessDefinition(processDefinition);
        return getDataTable(list);
    }

    /**
     * 部署流程定义
     */
    @PreAuthorize("@ss.hasPermi('process:definition:upload')")
    @Log(title = "流程定义", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("processDefinition") MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                String extensionName = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.') + 1);
                if (!"bpmn".equalsIgnoreCase(extensionName)
                        && !"zip".equalsIgnoreCase(extensionName)
                        && !"bar".equalsIgnoreCase(extensionName)) {
                    return AjaxResult.error("流程定义文件仅支持 bpmn, zip 和 bar 格式！");
                }
                // p.s. 此时 FileUploadUtils.upload() 返回字符串 fileName 前缀为 Constants.RESOURCE_PREFIX，需剔除
                // 详见: FileUploadUtils.getPathFileName(...)
                String fileName = FileUploadUtils.upload(RuoYiConfig.getProfile() + "/processDefiniton", file);
                if (StringUtils.isNotBlank(fileName)) {
                    String realFilePath = RuoYiConfig.getProfile() + fileName.substring(Constants.RESOURCE_PREFIX.length());
                    processDefinitionService.deployProcessDefinition(realFilePath);
                    return AjaxResult.success();
                }
            }
            return AjaxResult.error("不允许上传空文件！");
        }
        catch (Exception e) {
            log.error("上传流程定义文件失败！", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('process:definition:remove')")
    @Log(title = "流程定义", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    public AjaxResult remove(String ids) {
        try {
            return toAjax(processDefinitionService.deleteProcessDeploymentByIds(ids));
        }
        catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @Log(title = "流程定义", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('process:definition:export')")
    @PostMapping("/export")
    public AjaxResult export() {
        List<ProcessDefinition> list = processDefinitionService.listProcessDefinition(new ProcessDefinition());
        ExcelUtil<ProcessDefinition> util = new ExcelUtil<>(ProcessDefinition.class);
        return util.exportExcel(list, "流程定义数据");
    }

}
