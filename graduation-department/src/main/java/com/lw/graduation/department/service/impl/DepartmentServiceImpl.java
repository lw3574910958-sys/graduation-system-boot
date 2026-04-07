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
import com.lw.graduation.common.util.BeanMapperUtil;
import com.lw.graduation.common.util.CacheHelper;
import com.lw.graduation.domain.entity.department.SysDepartment;
import com.lw.graduation.domain.enums.common.IsDelete;
import com.lw.graduation.domain.entity.student.BizStudent;
import com.lw.graduation.domain.entity.teacher.BizTeacher;
import com.lw.graduation.infrastructure.mapper.department.SysDepartmentMapper;
import com.lw.graduation.infrastructure.mapper.student.BizStudentMapper;
import com.lw.graduation.infrastructure.mapper.teacher.BizTeacherMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
    private final CacheHelper cacheHelper;

    /**
     * 获取院系列表
     *
     * @param queryDTO 查询条件
     * @return 院系列表
     */
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
                .toList());
        voPage.setTotal(departmentPage.getTotal());

        return voPage;
    }

    /**
     * 获取院系详情
     *
     * @param id 院系ID
     * @return 院系详情
     */
    @Override
    public DepartmentVO getDepartmentById(Long id) {
        if (id == null) {
            return null;
        }

        String cacheKey = CacheConstants.KeyPrefix.DEPARTMENT_INFO + id;

        return cacheHelper.getFromCache(cacheKey, DepartmentVO.class, () -> {
            SysDepartment department = sysDepartmentMapper.selectById(id);
            if (department == null) {
                log.debug("院系不存在: {}", id);
                return null;
            }
            return convertToDepartmentVO(department);
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
        // 注意：不要手动 setCreatedAt/setUpdatedAt，由 MetaObjectHandler 处理

        // 4. 插入数据库
        try {
            sysDepartmentMapper.insert(department);
        } catch (Exception e) {
            log.warn("并发创建导致院系编码或名称重复：{}", createDTO.getCode());
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "院系编码或名称已存在");
        }

        // 5. 清除所有院系缓存
        clearAllDepartmentsCache();

        log.info("创建院系成功：code={}, name={}", createDTO.getCode(), createDTO.getName());
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
        // 注意：不要手动 setUpdatedAt，由 MetaObjectHandler 处理

        // 5. 执行更新
        sysDepartmentMapper.updateById(updateDepartment);

        // 6. 清除缓存
        clearDepartmentCache(id);
        clearAllDepartmentsCache();

        log.info("更新院系成功：id={}, code={}, name={}", id, updateDTO.getCode(), updateDTO.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(Long id) {
        // 1. 检查院系是否存在
        SysDepartment department = sysDepartmentMapper.selectById(id);
        if (department == null || department.getIsDeleted().equals(IsDelete.DELETED.getCode())) {
            throw new BusinessException(ResponseCode.NOT_FOUND);
        }

        // 2. 检查是否有学生或教师关联
        if (hasAssociatedStudentsOrTeachers(id)) {
            throw new BusinessException(ResponseCode.DEPARTMENT_HAS_ASSOCIATED_DATA);
        }

        // 3. 执行删除（逻辑删除，MyBatis-Plus 会自动处理@TableLogic 注解）
        sysDepartmentMapper.deleteById(id);

        // 4. 清除缓存
        clearDepartmentCache(id);
        clearAllDepartmentsCache();

        log.info("删除院系成功：id={}, code={}", id, department.getCode());
    }

    @Override
    public List<DepartmentVO> getAllDepartments() {
        String cacheKey = CacheConstants.KeyPrefix.ALL_DEPARTMENTS;
    
        // 先清除旧缓存，确保获取最新数据
        cacheHelper.evictCache(cacheKey);
        log.info("已清除院系列表缓存，准备重新查询");
            
        // 使用 Object.class 作为缓存类型，然后进行类型转换
        @SuppressWarnings("unchecked")
        List<DepartmentVO> result = (List<DepartmentVO>) cacheHelper.getFromCache(cacheKey, Object.class, () -> {
            LambdaQueryWrapper<SysDepartment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysDepartment::getIsDeleted, IsDelete.NOT_DELETED.getCode())
                    .orderByAsc(SysDepartment::getCode);
    
            List<SysDepartment> departments = sysDepartmentMapper.selectList(wrapper);
            log.info("查询到{}条院系数据", departments.size());
                
            List<DepartmentVO> voList = departments.stream()
                    .map(this::convertToDepartmentVO)
                    .toList();
                
            log.info("院系数据：{}", voList);
            return voList;
        }, CacheConstants.ExpireTime.ALL_DEPARTMENTS_EXPIRE);
    
        return result != null ? result : new ArrayList<>();
    }

    /**
     * 将 SysDepartment 实体转换为 DepartmentVO 视图对象
     *
     * @param department 院系实体
     * @return 院系视图对象
     */
    private DepartmentVO convertToDepartmentVO(SysDepartment department) {
        // 使用 BeanMapperUtil 简化对象转换
        return BeanMapperUtil.copyProperties(department, DepartmentVO.class);
    }

    /**
     * 统一清除单个院系缓存
     *
     * @param departmentId 院系 ID
     */
    private void clearDepartmentCache(Long departmentId) {
        if (departmentId != null) {
            String cacheKey = CacheConstants.KeyPrefix.DEPARTMENT_INFO + departmentId;
            cacheHelper.evictCache(cacheKey);
            log.debug("清除院系缓存：{}", cacheKey);
        }
    }

    /**
     * 统一清除所有院系列表缓存
     */
    private void clearAllDepartmentsCache() {
        String cacheKey = CacheConstants.KeyPrefix.ALL_DEPARTMENTS;
        cacheHelper.evictCache(cacheKey);
        log.debug("清除所有院系列表缓存：{}", cacheKey);
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
                     .eq(BizStudent::getIsDeleted, IsDelete.NOT_DELETED.getCode());
        long studentCount = bizStudentMapper.selectCount(studentWrapper);

        if (studentCount > 0) {
            return true;
        }

        // 检查是否有关联的教师
        LambdaQueryWrapper<BizTeacher> teacherWrapper = new LambdaQueryWrapper<>();
        teacherWrapper.eq(BizTeacher::getDepartmentId, departmentId)
                     .eq(BizTeacher::getIsDeleted, IsDelete.NOT_DELETED.getCode());
        long teacherCount = bizTeacherMapper.selectCount(teacherWrapper);

        return teacherCount > 0;
    }
}