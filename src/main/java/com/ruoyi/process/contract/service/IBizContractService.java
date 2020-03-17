package com.ruoyi.process.contract.service;

import com.ruoyi.process.contract.domain.BizContract;
import com.ruoyi.process.contract.domain.BizContractVo;
import org.activiti.engine.runtime.ProcessInstance;

import java.util.List;
import java.util.Map;

/**
 * 采购合同Service接口
 *
 * @author ruoyi
 * @date 2020-03-14
 */
public interface IBizContractService
{
    /**
     * 查询采购合同
     *
     * @param id 采购合同ID
     * @return 采购合同
     */
    public BizContractVo selectBizContractById(Long id);

    /**
     * 查询采购合同列表
     *
     * @param bizContract 采购合同
     * @return 采购合同集合
     */
    public List<BizContractVo> selectBizContractList(BizContract bizContract);

    /**
     * 新增采购合同
     *
     * @param bizContract 采购合同
     * @return 结果
     */
    public int insertBizContract(BizContract bizContract);

    /**
     * 修改采购合同
     *
     * @param bizContract 采购合同
     * @return 结果
     */
    public int updateBizContract(BizContract bizContract);

    /**
     * 批量删除采购合同
     *
     * @param ids 需要删除的采购合同ID
     * @return 结果
     */
    public int deleteBizContractByIds(Long[] ids);

    /**
     * 删除采购合同信息
     *
     * @param id 采购合同ID
     * @return 结果
     */
    public int deleteBizContractById(Long id);

    /**
     * 启动流程
     * @param entity
     * @param applyUserId
     * @return
     */
    ProcessInstance submitApply(BizContract entity, String applyUserId);

    /**
     * 查询我的待办列表
     * @param userId
     * @return
     */
    List<BizContractVo> findTodoTasks(BizContractVo bizContract, String userId);

    /**
     * @Description //我的已办
     * @Author shanpeng
     * @Date  21:02
     * @Param [bizContract, username]
     **/
    List<BizContractVo> findDoneTasks(BizContractVo bizContract, String userId);

    /**
     * 完成任务
     * @param contract
     * @param saveEntity
     * @param taskId
     * @param variables
     */
    void complete(BizContractVo contract, boolean saveEntity, String taskId, Map<String, Object> variables);
}
