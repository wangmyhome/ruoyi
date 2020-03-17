package com.ruoyi.process.contract.mapper;

import com.ruoyi.process.contract.domain.BizContract;
import com.ruoyi.process.contract.domain.BizContractVo;

import java.util.List;

/**
 * 【请填写功能名称】Mapper接口
 *
 * @author ruoyi
 * @date 2020-03-14
 */
public interface BizContractMapper
{
    /**
     * 查询【请填写功能名称】
     *
     * @param id 【请填写功能名称】ID
     * @return 【请填写功能名称】
     */
    public BizContractVo selectBizContractById(Long id);

    /**
     * 查询【请填写功能名称】列表
     *
     * @param bizContract 【请填写功能名称】
     * @return 【请填写功能名称】集合
     */
    public List<BizContractVo> selectBizContractList(BizContract bizContract);

    /**
     * 新增【请填写功能名称】
     *
     * @param bizContract 【请填写功能名称】
     * @return 结果
     */
    public int insertBizContract(BizContract bizContract);

    /**
     * 修改【请填写功能名称】
     *
     * @param bizContract 【请填写功能名称】
     * @return 结果
     */
    public int updateBizContract(BizContract bizContract);

    /**
     * 删除【请填写功能名称】
     *
     * @param id 【请填写功能名称】ID
     * @return 结果
     */
    public int deleteBizContractById(Long id);

    /**
     * 批量删除【请填写功能名称】
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteBizContractByIds(Long[] ids);

    /**
     * 修改请假业务
     *
     * @param contract 请假业务
     * @return 结果
     */
    public int updateBizContract(BizContractVo contract);
}
