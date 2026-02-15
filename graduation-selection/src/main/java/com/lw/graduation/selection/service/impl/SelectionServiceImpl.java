package com.lw.graduation.selection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lw.graduation.api.dto.selection.SelectionCreateDTO;
import com.lw.graduation.api.dto.selection.SelectionPageQueryDTO;
import com.lw.graduation.api.dto.selection.SelectionUpdateDTO;
import com.lw.graduation.api.service.selection.SelectionService;
import com.lw.graduation.api.vo.selection.SelectionVO;
import com.lw.graduation.common.constant.CacheConstants;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.domain.entity.selection.BizSelection;
import com.lw.graduation.domain.entity.topic.BizTopic;
import com.lw.graduation.domain.entity.student.BizStudent;
import com.lw.graduation.domain.enums.SelectionStatus;
import com.lw.graduation.infrastructure.mapper.selection.BizSelectionMapper;
import com.lw.graduation.infrastructure.mapper.topic.BizTopicMapper;
import com.lw.graduation.infrastructure.mapper.student.BizStudentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 选题服务实现类
 * 实现选题管理模块的核心业务逻辑。
 *
 * @author lw
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SelectionServiceImpl extends ServiceImpl<BizSelectionMapper, BizSelection> implements SelectionService {

    private final BizSelectionMapper bizSelectionMapper;
    private final BizTopicMapper bizTopicMapper;
    private final BizStudentMapper bizStudentMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 分页查询选题列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public IPage<SelectionVO> getSelectionPage(SelectionPageQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<BizSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getStudentId() != null, BizSelection::getStudentId, queryDTO.getStudentId())
                .eq(queryDTO.getTopicId() != null, BizSelection::getTopicId, queryDTO.getTopicId())
                .eq(queryDTO.getStatus() != null, BizSelection::getStatus, queryDTO.getStatus())
                .orderByDesc(BizSelection::getCreatedAt); // 按创建时间倒序

        // 2. 执行分页查询
        IPage<BizSelection> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<BizSelection> selectionPage = bizSelectionMapper.selectPage(page, wrapper);

        // 3. 转换为VO并补充关联信息
        IPage<SelectionVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        voPage.setRecords(selectionPage.getRecords().stream()
                .map(this::convertToSelectionVO)
                .collect(Collectors.toList()));
        voPage.setTotal(selectionPage.getTotal());

        return voPage;
    }

    /**
     * 根据ID获取选题详情（带缓存穿透防护）
     *
     * @param id 选题ID
     * @return 选题详情
     */
    @Override
    public SelectionVO getSelectionById(Long id) {
        if (id == null) {
            return null;
        }

        String cacheKey = CacheConstants.KeyPrefix.SELECTION_INFO + id;
        
        // 1. 查 Redis 缓存
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            if (CacheConstants.CacheValue.NULL_MARKER.equals(cached)) {
                log.debug("缓存命中空值标记，选题不存在: " + id);
                return null;
            }
            return (SelectionVO) cached;
        }

        // 2. 缓存未命中，查数据库
        BizSelection selection = bizSelectionMapper.selectById(id);
        if (selection == null) {
            // 缓存空值防止穿透
            redisTemplate.opsForValue().set(
                cacheKey,
                CacheConstants.CacheValue.NULL_MARKER,
                CacheConstants.CacheValue.NULL_EXPIRE,
                TimeUnit.SECONDS
            );
            log.debug("选题不存在，缓存空值标记: " + cacheKey);
            return null;
        }

        // 3. 转换并缓存结果
        SelectionVO result = convertToSelectionVO(selection);
        redisTemplate.opsForValue().set(
            cacheKey,
            result,
            CacheConstants.ExpireTime.SELECTION_INFO_EXPIRE,
            TimeUnit.SECONDS
        );
        log.debug("缓存选题信息: " + cacheKey);
        return result;
    }

    /**
     * 创建选题
     *
     * @param createDTO 创建参数
     */
    @Override
    @Transactional
    public void createSelection(SelectionCreateDTO createDTO) {
        // 1. 验证学生是否存在
        BizStudent student = bizStudentMapper.selectById(createDTO.getStudentId());
        if (student == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "学生不存在");
        }

        // 2. 验证课题是否存在且开放
        BizTopic topic = bizTopicMapper.selectById(createDTO.getTopicId());
        if (topic == null) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "课题不存在");
        }
        if (topic.getStatus() != 0) { // 0-开放状态
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "课题不在开放状态");
        }

        // 3. 检查该学生是否已有选题记录
        LambdaQueryWrapper<BizSelection> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(BizSelection::getStudentId, createDTO.getStudentId())
                .eq(BizSelection::getStatus, SelectionStatus.CONFIRMED.getValue()); // 已确认状态
        if (bizSelectionMapper.selectCount(studentWrapper) > 0) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "该学生已有确认的选题");
        }

        // 4. 检查该课题是否已被其他学生选中
        LambdaQueryWrapper<BizSelection> topicWrapper = new LambdaQueryWrapper<>();
        topicWrapper.eq(BizSelection::getTopicId, createDTO.getTopicId())
                .eq(BizSelection::getStatus, SelectionStatus.CONFIRMED.getValue());
        if (bizSelectionMapper.selectCount(topicWrapper) > 0) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "该课题已被其他学生选中");
        }

        // 5. 创建选题实体
        BizSelection selection = new BizSelection();
        selection.setStudentId(createDTO.getStudentId());
        selection.setTopicId(createDTO.getTopicId());
        selection.setTopicTitle(topic.getTitle()); // 保存选题时的标题快照
        selection.setStatus(createDTO.getStatus() != null ? createDTO.getStatus() : 0); // 默认待确认

        // 6. 插入数据库
        bizSelectionMapper.insert(selection);
        
        // 7. 清除相关缓存
        clearStudentSelectionsCache(createDTO.getStudentId());
        clearTopicSelectionsCache(createDTO.getTopicId());
    }

    /**
     * 更新选题
     *
     * @param id 选题ID
     * @param updateDTO 更新参数
     */
    @Override
    @Transactional
    public void updateSelection(Long id, SelectionUpdateDTO updateDTO) {
        // 1. 查询选题是否存在
        BizSelection existingSelection = bizSelectionMapper.selectById(id);
        if (existingSelection == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND);
        }

        // 2. 如果要更新为已确认状态，需要额外验证
        if (updateDTO.getStatus() != null && updateDTO.getStatus() == SelectionStatus.CONFIRMED.getValue()) {
            // 检查该学生是否已有其他确认的选题
            LambdaQueryWrapper<BizSelection> studentWrapper = new LambdaQueryWrapper<>();
            studentWrapper.eq(BizSelection::getStudentId, existingSelection.getStudentId())
                    .eq(BizSelection::getStatus, SelectionStatus.CONFIRMED.getValue())
                    .ne(BizSelection::getId, id); // 排除自己
            if (bizSelectionMapper.selectCount(studentWrapper) > 0) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "该学生已有确认的选题");
            }

            // 检查该课题是否已被其他学生确认选中
            LambdaQueryWrapper<BizSelection> topicWrapper = new LambdaQueryWrapper<>();
            topicWrapper.eq(BizSelection::getTopicId, existingSelection.getTopicId())
                    .eq(BizSelection::getStatus, SelectionStatus.CONFIRMED.getValue())
                    .ne(BizSelection::getId, id); // 排除自己
            if (bizSelectionMapper.selectCount(topicWrapper) > 0) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "该课题已被其他学生选中");
            }
        }

        // 3. 构建更新实体
        BizSelection updateSelection = new BizSelection();
        updateSelection.setId(id);
        if (updateDTO.getStudentId() != null) {
            updateSelection.setStudentId(updateDTO.getStudentId());
        }
        if (updateDTO.getTopicId() != null) {
            updateSelection.setTopicId(updateDTO.getTopicId());
            // 更新课题标题快照
            BizTopic topic = bizTopicMapper.selectById(updateDTO.getTopicId());
            if (topic != null) {
                updateSelection.setTopicTitle(topic.getTitle());
            }
        }
        if (updateDTO.getStatus() != null) {
            updateSelection.setStatus(updateDTO.getStatus());
        }
        updateSelection.setUpdatedAt(LocalDateTime.now());

        // 4. 执行更新
        bizSelectionMapper.updateById(updateSelection);
        
        // 5. 清除缓存
        clearSelectionCache(id);
        if (updateDTO.getStudentId() != null && !updateDTO.getStudentId().equals(existingSelection.getStudentId())) {
            clearStudentSelectionsCache(existingSelection.getStudentId());
            clearStudentSelectionsCache(updateDTO.getStudentId());
        }
        if (updateDTO.getTopicId() != null && !updateDTO.getTopicId().equals(existingSelection.getTopicId())) {
            clearTopicSelectionsCache(existingSelection.getTopicId());
            clearTopicSelectionsCache(updateDTO.getTopicId());
        }
    }

    /**
     * 删除选题
     *
     * @param id 选题ID
     */
    @Override
    @Transactional
    public void deleteSelection(Long id) {
        // 1. 检查选题是否存在
        BizSelection selection = bizSelectionMapper.selectById(id);
        if (selection == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND);
        }

        // 2. 已确认的选题不能直接删除
        if (selection.getStatus() != null && selection.getStatus() == SelectionStatus.CONFIRMED.getValue()) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "已确认的选题不能删除");
        }

        // 3. 执行删除（逻辑删除）
        bizSelectionMapper.deleteById(id);
        
        // 4. 清除缓存
        clearSelectionCache(id);
        clearStudentSelectionsCache(selection.getStudentId());
        clearTopicSelectionsCache(selection.getTopicId());
    }

    /**
     * 将BizSelection实体转换为SelectionVO
     *
     * @param selection 选题实体
     * @return 选题VO
     */
    private SelectionVO convertToSelectionVO(BizSelection selection) {
        SelectionVO vo = new SelectionVO();
        vo.setId(selection.getId());
        vo.setStudentId(selection.getStudentId());
        vo.setTopicId(selection.getTopicId());
        vo.setTopicTitle(selection.getTopicTitle());
        vo.setStatus(selection.getStatus());
        vo.setCreatedAt(selection.getCreatedAt());
        vo.setUpdatedAt(selection.getUpdatedAt());

        // 补充学生姓名信息
        if (selection.getStudentId() != null) {
            BizStudent student = bizStudentMapper.selectById(selection.getStudentId());
            if (student != null) {
                vo.setStudentName(student.getStudentId()); // 这里应该关联用户表获取真实姓名
            }
        }

        return vo;
    }

    /**
     * 清除单个选题缓存
     */
    private void clearSelectionCache(Long selectionId) {
        if (selectionId != null) {
            String cacheKey = CacheConstants.KeyPrefix.SELECTION_INFO + selectionId;
            redisTemplate.delete(cacheKey);
            log.debug("清除选题缓存: " + cacheKey);
        }
    }

    /**
     * 清除学生相关选题缓存
     */
    private void clearStudentSelectionsCache(Long studentId) {
        if (studentId != null) {
            // 可以扩展清除学生相关的选题列表缓存
            log.debug("清除学生选题相关缓存: " + studentId);
        }
    }

    /**
     * 清除课题相关选题缓存
     */
    private void clearTopicSelectionsCache(Long topicId) {
        if (topicId != null) {
            // 可以扩展清除课题相关的选题列表缓存
            log.debug("清除课题选题相关缓存: " + topicId);
        }
    }
}