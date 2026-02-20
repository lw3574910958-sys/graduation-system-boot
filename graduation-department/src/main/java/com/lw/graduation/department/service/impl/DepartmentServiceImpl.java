package com.lw.graduation.department.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lw.graduation.api.dto.department.DepartmentCreateDTO;
import com.lw.graduation.api.dto.department.DepartmentPageQueryDTO;
import com.lw.graduation.api.dto.department.DepartmentUpdateDTO;
import com.lw.graduation.api.service.department.DepartmentService;
import com.lw.graduation.api.vo.department.DepartmentVO;
import com.lw.graduation.common.constant.CacheConstants;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.common.util.CacheHelper;
import com.lw.graduation.domain.entity.department.SysDepartment;
import com.lw.graduation.domain.entity.student.BizStudent;
import com.lw.graduation.domain.entity.teacher.BizTeacher;
import com.lw.graduation.infrastructure.mapper.department.SysDepartmentMapper;
import com.lw.graduation.infrastructure.mapper.student.BizStudentMapper;
import com.lw.graduation.infrastructure.mapper.teacher.BizTeacherMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 院系服务实现类
 * 实现院系管理模块的核心业务逻辑。
 *
 * @author lw
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl extends ServiceImpl<SysDepartmentMapper, SysDepartment> implements DepartmentService {

    private final SysDepartmentMapper sysDepartmentMapper;
    private final BizStudentMapper bizStudentMapper;
    private final BizTeacherMapper bizTeacherMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheHelper cacheHelper;

    @Override
    public IPage<DepartmentVO> getDepartmentPage(DepartmentPageQueryDTO queryDTO) {
        // 1. 构建查询条件
        LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(queryDTO.getCode() != null, SysDepartment::getCode, queryDTO.getCode())
                .like(queryDTO.getName() != null, SysDepartment::getName, queryDTO.getName())
                .orderByDesc(SysDepartment::getCreatedAt); // 按创建时间倒序

        // 2. 执行分页查询
        IPage<SysDepartment> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<SysDepartment> departmentPage = sysDepartmentMapper.selectPage(page, wrapper);

        // 3. 转换为VO
        IPage<DepartmentVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        voPage.setRecords(departmentPage.getRecords().stream()
                .map(this::convertToDepartmentVO)
                .collect(Collectors.toList()));
        voPage.setTotal(departmentPage.getTotal());

        return voPage;
    }

    @Override
    public DepartmentVO getDepartmentById(Long id) {
        if (id == null) {
            return null;
        }

        String cacheKey = CacheConstants.KeyPrefix.DEPARTMENT_INFO + id;

        return cacheHelper.getFromCache(cacheKey, DepartmentVO.class, () -> {
            SysDepartment department = sysDepartmentMapper.selectById(id);
            return department != null ? convertToDepartmentVO(department) : null;
        }, CacheConstants.ExpireTime.DEPARTMENT_INFO_EXPIRE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDepartment(DepartmentCreateDTO createDTO) {
        // 1. 检查编码是否已存在
        LambdaQueryWrapper<SysDepartment> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(SysDepartment::getCode, createDTO.getCode());
        if (sysDepartmentMapper.selectCount(codeWrapper) > 0) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "院系编码已存在");
        }

        // 2. 检查名称是否已存在
        LambdaQueryWrapper<SysDepartment> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(SysDepartment::getName, createDTO.getName());
        if (sysDepartmentMapper.selectCount(nameWrapper) > 0) {
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "院系名称已存在");
        }

        // 3. 创建院系实体
        SysDepartment department = new SysDepartment();
        department.setCode(createDTO.getCode());
        department.setName(createDTO.getName());

        // 4. 插入数据库
        sysDepartmentMapper.insert(department);

        // 5. 清除所有院系缓存
        clearAllDepartmentsCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDepartment(Long id, DepartmentUpdateDTO updateDTO) {
        // 1. 查询院系是否存在
        SysDepartment existingDepartment = sysDepartmentMapper.selectById(id);
        if (existingDepartment == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND);
        }

        // 2. 检查编码是否已存在（排除自己）
        if (updateDTO.getCode() != null && !updateDTO.getCode().equals(existingDepartment.getCode())) {
            LambdaQueryWrapper<SysDepartment> codeWrapper = new LambdaQueryWrapper<>();
            codeWrapper.eq(SysDepartment::getCode, updateDTO.getCode())
                    .ne(SysDepartment::getId, id);
            if (sysDepartmentMapper.selectCount(codeWrapper) > 0) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "院系编码已存在");
            }
        }

        // 3. 检查名称是否已存在（排除自己）
        if (updateDTO.getName() != null && !updateDTO.getName().equals(existingDepartment.getName())) {
            LambdaQueryWrapper<SysDepartment> nameWrapper = new LambdaQueryWrapper<>();
            nameWrapper.eq(SysDepartment::getName, updateDTO.getName())
                    .ne(SysDepartment::getId, id);
            if (sysDepartmentMapper.selectCount(nameWrapper) > 0) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "院系名称已存在");
            }
        }

        // 4. 构建更新实体
        SysDepartment updateDepartment = new SysDepartment();
        updateDepartment.setId(id);
        if (updateDTO.getCode() != null) {
            updateDepartment.setCode(updateDTO.getCode());
        }
        if (updateDTO.getName() != null) {
            updateDepartment.setName(updateDTO.getName());
        }
        updateDepartment.setUpdatedAt(LocalDateTime.now());

        // 5. 执行更新
        sysDepartmentMapper.updateById(updateDepartment);

        // 6. 清除缓存
        clearDepartmentCache(id);
        clearAllDepartmentsCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(Long id) {
        // 1. 检查院系是否存在
        SysDepartment department = sysDepartmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND);
        }

        // 2. 检查是否有学生或教师关联
        if (hasAssociatedStudentsOrTeachers(id)) {
            throw new BusinessException(ResponseCode.DEPARTMENT_HAS_ASSOCIATED_DATA);
        }

        // 3. 执行删除（逻辑删除）
        sysDepartmentMapper.deleteById(id);

        // 4. 清除缓存
        clearDepartmentCache(id);
        clearAllDepartmentsCache();
    }

    @Override
    public List<DepartmentVO> getAllDepartments() {
        String cacheKey = CacheConstants.KeyPrefix.ALL_DEPARTMENTS;

        // 使用Object.class作为缓存类型，然后进行类型转换
        @SuppressWarnings("unchecked")
        List<DepartmentVO> result = (List<DepartmentVO>) cacheHelper.getFromCache(cacheKey, Object.class, () -> {
            LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysDepartment::getIsDeleted, 0)
                    .orderByAsc(SysDepartment::getCode);

            List<SysDepartment> departments = sysDepartmentMapper.selectList(wrapper);
            return departments.stream()
                    .map(this::convertToDepartmentVO)
                    .collect(Collectors.toList());
        }, CacheConstants.ExpireTime.ALL_DEPARTMENTS_EXPIRE);
        
        return result != null ? result : new ArrayList<>();
    }

    /**
     * 将SysDepartment实体转换为DepartmentVO
     */
    private DepartmentVO convertToDepartmentVO(SysDepartment department) {
        DepartmentVO vo = new DepartmentVO();
        vo.setId(department.getId());
        vo.setCode(department.getCode());
        vo.setName(department.getName());
        vo.setCreatedAt(department.getCreatedAt());
        vo.setUpdatedAt(department.getUpdatedAt());
        return vo;
    }

    /**
     * 清除单个院系缓存
     */
    private void clearDepartmentCache(Long departmentId) {
        if (departmentId != null) {
            String cacheKey = CacheConstants.KeyPrefix.DEPARTMENT_INFO + departmentId;
            redisTemplate.delete(cacheKey);
            log.debug("清除院系缓存: {}", cacheKey);
        }
    }

    /**
     * 清除所有院系列表缓存
     */
    private void clearAllDepartmentsCache() {
        String cacheKey = CacheConstants.KeyPrefix.ALL_DEPARTMENTS;
        redisTemplate.delete(cacheKey);
        log.debug("清除所有院系列表缓存: {}", cacheKey);
    }

    /**
     * 检查院系是否有关联的学生或教师
     *
     * @param departmentId 院系ID
     * @return 有关联返回true
     */
    private boolean hasAssociatedStudentsOrTeachers(Long departmentId) {
        // 检查是否有关联的学生
        LambdaQueryWrapper<BizStudent> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(BizStudent::getDepartmentId, departmentId)
                     .eq(BizStudent::getIsDeleted, 0);
        long studentCount = bizStudentMapper.selectCount(studentWrapper);

        if (studentCount > 0) {
            return true;
        }

        // 检查是否有关联的教师
        LambdaQueryWrapper<BizTeacher> teacherWrapper = new LambdaQueryWrapper<>();
        teacherWrapper.eq(BizTeacher::getDepartmentId, departmentId)
                     .eq(BizTeacher::getIsDeleted, 0);
        long teacherCount = bizTeacherMapper.selectCount(teacherWrapper);

        return teacherCount > 0;
    }
}