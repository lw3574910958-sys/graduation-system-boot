package com.lw.graduation.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lw.graduation.api.dto.user.UserChangePasswordDTO;
import com.lw.graduation.api.dto.user.UserCreateDTO;
import com.lw.graduation.api.dto.user.UserPageQueryDTO;
import com.lw.graduation.api.dto.user.UserUpdateDTO;
import com.lw.graduation.api.service.user.UserService;
import com.lw.graduation.api.vo.user.UserListInfoVO;
import com.lw.graduation.auth.util.PasswordUtil;
import com.lw.graduation.common.constant.CacheConstants;
import com.lw.graduation.common.enums.ResponseCode;
import com.lw.graduation.common.exception.BusinessException;
import com.lw.graduation.common.util.BeanMapperUtil;
import com.lw.graduation.common.util.CacheHelper;

import com.lw.graduation.common.util.EnumUtils;
import com.lw.graduation.domain.entity.admin.BizAdmin;
import com.lw.graduation.domain.entity.department.SysDepartment;
import com.lw.graduation.domain.entity.student.BizStudent;
import com.lw.graduation.domain.entity.teacher.BizTeacher;
import com.lw.graduation.domain.entity.user.SysUser;
import com.lw.graduation.domain.enums.common.IsDelete;
import com.lw.graduation.domain.enums.common.IsDepartment;
import com.lw.graduation.domain.enums.user.AccountStatus;
import com.lw.graduation.domain.enums.user.UserType;
import com.lw.graduation.infrastructure.mapper.admin.BizAdminMapper;
import com.lw.graduation.infrastructure.mapper.department.SysDepartmentMapper;
import com.lw.graduation.infrastructure.mapper.student.BizStudentMapper;
import com.lw.graduation.infrastructure.mapper.teacher.BizTeacherMapper;
import com.lw.graduation.infrastructure.mapper.user.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 用户服务实现类
 * 实现用户管理模块的核心业务逻辑。
 *
 * @author lw
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    private final SysUserMapper sysUserMapper; // 引入用户数据访问接口
    private final PasswordUtil passwordUtil; // 注入密码工具类
    private final CacheHelper cacheHelper; // 注入缓存助手
    private final BizStudentMapper bizStudentMapper; // 注入学生 Mapper
    private final BizTeacherMapper bizTeacherMapper; // 注入教师 Mapper
    private final SysDepartmentMapper sysDepartmentMapper; // 注入院系 Mapper
    private final BizAdminMapper bizAdminMapper; // 注入管理员 Mapper

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询条件 DTO
     * @return 分页结果
     */
    @Override
    public IPage<UserListInfoVO> getUserPage(UserPageQueryDTO queryDTO){
        // 1. 构建查询条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(queryDTO.getUsername()), SysUser::getUsername, queryDTO.getUsername())
                .like(StringUtils.isNotBlank(queryDTO.getRealName()), SysUser::getRealName, queryDTO.getRealName())
                .eq(queryDTO.getUserType() != null, SysUser::getUserType, queryDTO.getUserType())
                .eq(queryDTO.getStatus() != null, SysUser::getStatus, queryDTO.getStatus())
                .eq(SysUser::getIsDeleted, IsDelete.NOT_DELETED.getCode()) // 只查询未删除的用户
                .orderByDesc(SysUser::getCreatedAt); // 按创建时间倒序

        // 2. 执行分页查询
        IPage<SysUser> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        IPage<SysUser> userPage = sysUserMapper.selectPage(page, wrapper);

        // 3. 将实体列表转换为 VO 列表（优化：减少不必要的对象创建）
        IPage<UserListInfoVO> voPage = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        voPage.setRecords(userPage.getRecords().stream()
                .map(this::convertToUserListInfoVO) // 转换方法
                .toList());
        voPage.setTotal(userPage.getTotal());

        return voPage;
    }

    /**
     * 根据用户 ID获取用户详情（统一返回 UserListInfoVO）
     *
     * @param userId 用户 ID
     * @return 用户详情 VO
     */
    @Override
    public UserListInfoVO getUserByUserId(Long userId) {
        if (userId == null) {
            return null;
        }

        String cacheKey = CacheConstants.KeyPrefix.USER_INFO + userId;

        return cacheHelper.getFromCache(cacheKey, UserListInfoVO.class, () -> {
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null) {
                log.debug("用户不存在: {}", userId);
                return null; // 返回 null，CacheHelper 会处理空值标记
            }
            return convertToUserListInfoVO(user);
        }, CacheConstants.ExpireTime.USER_INFO_EXPIRE);
    }

    /**
     * 创建新用户
     *
     * @param createDTO 创建用户 DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserCreateDTO createDTO) {
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, createDTO.getUsername());
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResponseCode.USERNAME_EXISTS);
        }

        // 2. 验证用户类型是否有效
        if (!EnumUtils.isValidCode(UserType.class, createDTO.getUserType())) {
            throw new BusinessException(ResponseCode.USER_TYPE_INVALID);
        }

        // 3. 验证状态
        Integer status = createDTO.getStatus();
        if (status != null && !EnumUtils.isValidCode(AccountStatus.class, status)) {
            throw new BusinessException(ResponseCode.INVALID_STATUS);
        }

        // 4. 验证密码格式
        String password = createDTO.getPassword();
        if(!passwordUtil.isValidPassword(password)){
            throw new BusinessException(ResponseCode.PASSWORD_FORMAT_ERROR);
        }

        // 5. 创建用户实体
        SysUser user = new SysUser();
        user.setUsername(createDTO.getUsername());
        user.setRealName(createDTO.getRealName());
        user.setPassword(passwordUtil.encryptPassword(password));
        user.setUserType(createDTO.getUserType());
        user.setStatus(status != null ? status : AccountStatus.ENABLED.getCode());
        user.setLoginFailCount(0);
        user.setLastLoginAt(null);
        user.setAvatar(createDTO.getAvatar());
        //使用MyMetaObjectHandler自动填充时间
        user.setIsDeleted(IsDelete.NOT_DELETED.getCode());

        // 6. 插入数据库
        try {
            sysUserMapper.insert(user);
        } catch (DuplicateKeyException e) {
            log.warn("并发创建导致用户名重复：{}", createDTO.getUsername());
            throw new BusinessException(ResponseCode.USERNAME_EXISTS);
        }

        // 7. 创建对应的业务表数据
        try {
            createBusinessUserData(user, createDTO);
        } catch (Exception e) {
            log.error("创建业务表数据失败，回滚用户创建：{}", e.getMessage());
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "创建业务数据失败：" + e.getMessage());
        }
    }

    /**
     * 创建业务表数据（学生/教师/管理员）
     *
     * @param user 用户实体
     * @param createDTO 创建参数
     */
    private void createBusinessUserData(SysUser user, UserCreateDTO createDTO) {
        String userType = user.getUserType();

        if (UserType.STUDENT.getCode().equals(userType)) {
            // 创建学生数据
            BizStudent student = getBizStudent(user, createDTO);
            bizStudentMapper.insert(student);
        } else if (UserType.TEACHER.getCode().equals(userType)) {
            // 创建教师数据
            BizTeacher teacher = getBizTeacher(user, createDTO);
            bizTeacherMapper.insert(teacher);
        } else if (UserType.SYSTEM_ADMIN.getCode().equals(userType) || UserType.DEPARTMENT_ADMIN.getCode().equals(userType)) {
            // 创建管理员数据
            BizAdmin admin = getBizAdmin(user, createDTO);
            bizAdminMapper.insert(admin);
        } else {
            log.warn("不支持的用户类型：{}", userType);
        }
    }

    /**
     * 获取教师业务数据
     *
     * @param user 用户实体
     * @param createDTO 创建参数
     * @return 教师业务数据
     */
    private static BizTeacher getBizTeacher(SysUser user, UserCreateDTO createDTO) {
        BizTeacher teacher = new BizTeacher();
        teacher.setUserId(user.getId());
        // 工号：优先使用前端传递的 teacherId，否则使用用户名
        teacher.setTeacherId(createDTO.getTeacherId() != null ? createDTO.getTeacherId() : user.getUsername());
        // 院系 ID：前端传 0 表示无院系，转换为 null 存入数据库（所有表主键使用 ASSIGN_ID，不存在 id=0）
        teacher.setDepartmentId(createDTO.getDepartmentId() != null && createDTO.getDepartmentId() == 0 ? null : createDTO.getDepartmentId());
        // 性别：使用前端传递的 gender
        teacher.setGender(createDTO.getGender());
        // 职称：使用前端传递的 title 字段
        teacher.setTitle(createDTO.getTitle());
        teacher.setPhone(createDTO.getPhone());
        teacher.setEmail(createDTO.getEmail());
        return teacher;
    }

    /**
     * 获取学生业务数据
     *
     * @param user 用户实体
     * @param createDTO 创建参数
     * @return 学生业务数据
     */
    private static BizStudent getBizStudent(SysUser user, UserCreateDTO createDTO) {
        BizStudent student = new BizStudent();
        student.setUserId(user.getId());
        // 学号：优先使用前端传递的 studentId，否则使用用户名
        student.setStudentId(createDTO.getStudentId() != null ? createDTO.getStudentId() : user.getUsername());
        // 院系 ID：前端传 0 表示无院系，转换为 null 存入数据库（所有表主键使用 ASSIGN_ID，不存在 id=0）
        student.setDepartmentId(createDTO.getDepartmentId() != null && createDTO.getDepartmentId() == 0 ? null : createDTO.getDepartmentId());
        // 性别：使用前端传递的 gender
        student.setGender(createDTO.getGender());
        // 专业：使用前端传递的 major
        student.setMajor(createDTO.getMajor());
        // 班级：使用前端传递的 className 字段
        student.setClassName(createDTO.getClassName());
        student.setPhone(createDTO.getPhone());
        student.setEmail(createDTO.getEmail());
        return student;
    }

    /**
     * 获取管理员业务数据
     *
     * @param user 用户实体
     * @param createDTO 创建参数
     * @return 管理员业务数据
     */
    private static BizAdmin getBizAdmin(SysUser user, UserCreateDTO createDTO) {
        BizAdmin admin = new BizAdmin();
        admin.setUserId(user.getId());
        // 管理员编号：优先使用前端传递的 adminId，否则使用用户名
        admin.setAdminId(createDTO.getAdminId() != null ? createDTO.getAdminId() : user.getUsername());
        // 院系 ID：前端传 0 表示无院系，转换为 null 存入数据库（所有表主键使用 ASSIGN_ID，不存在 id=0）
        admin.setDepartmentId(createDTO.getDepartmentId() != null && createDTO.getDepartmentId() == 0 ? null : createDTO.getDepartmentId());
        // 角色级别：根据院系 ID 是否为空判断（null 表示系统管理员，有院系表示院系管理员）
        admin.setRoleLevel(admin.getDepartmentId() != null ? IsDepartment.DEPARTMENT.getCode() : IsDepartment.NOT_DEPARTMENT.getCode());
        admin.setPhone(createDTO.getPhone());
        admin.setEmail(createDTO.getEmail());
        return admin;
    }

    /**
     * 更新业务表数据（学生/教师/管理员）
     *
     * @param userId 用户 ID
     * @param userType 用户类型
     * @param updateDTO 更新参数
     */
    private void updateBusinessUserData(Long userId, String userType, UserUpdateDTO updateDTO) {
        if (UserType.STUDENT.getCode().equals(userType)) {
            // 更新学生数据
            LambdaQueryWrapper<BizStudent> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizStudent::getUserId, userId);
            BizStudent student = bizStudentMapper.selectOne(wrapper);
            if (student != null) {
                // 院系 ID：前端传 0 表示无院系，转换为 null 存入数据库（所有表主键使用 ASSIGN_ID，不存在 id=0）
                student.setDepartmentId(updateDTO.getDepartmentId() != null && updateDTO.getDepartmentId() == 0 ? null : updateDTO.getDepartmentId());
                // 学号：使用前端传递的 studentId
                if (updateDTO.getStudentId() != null) {
                    student.setStudentId(updateDTO.getStudentId());
                }
                // 性别：使用前端传递的 gender
                if (updateDTO.getGender() != null) {
                    student.setGender(updateDTO.getGender());
                }
                // 专业：使用前端传递的 major
                if (updateDTO.getMajor() != null) {
                    student.setMajor(updateDTO.getMajor());
                }
                // 班级：使用前端传递的 className 字段
                if (updateDTO.getClassName() != null) {
                    student.setClassName(updateDTO.getClassName());
                }
                student.setPhone(updateDTO.getPhone());
                student.setEmail(updateDTO.getEmail());
                bizStudentMapper.updateById(student);
            }
        } else if (UserType.TEACHER.getCode().equals(userType)) {
            // 更新教师数据
            LambdaQueryWrapper<BizTeacher> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizTeacher::getUserId, userId);
            BizTeacher teacher = bizTeacherMapper.selectOne(wrapper);
            if (teacher != null) {
                // 院系 ID：前端传 0 表示无院系，转换为 null 存入数据库（所有表主键使用 ASSIGN_ID，不存在 id=0）
                teacher.setDepartmentId(updateDTO.getDepartmentId() != null && updateDTO.getDepartmentId() == 0 ? null : updateDTO.getDepartmentId());
                // 工号：使用前端传递的 teacherId
                if (updateDTO.getTeacherId() != null) {
                    teacher.setTeacherId(updateDTO.getTeacherId());
                }
                // 性别：使用前端传递的 gender
                if (updateDTO.getGender() != null) {
                    teacher.setGender(updateDTO.getGender());
                }
                // 职称：使用前端传递的 title 字段
                if (updateDTO.getTitle() != null) {
                    teacher.setTitle(updateDTO.getTitle());
                }
                teacher.setPhone(updateDTO.getPhone());
                teacher.setEmail(updateDTO.getEmail());
                bizTeacherMapper.updateById(teacher);
            }
        } else if (UserType.SYSTEM_ADMIN.getCode().equals(userType) || UserType.DEPARTMENT_ADMIN.getCode().equals(userType)) {
            // 更新管理员数据
            LambdaQueryWrapper<BizAdmin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BizAdmin::getUserId, userId);
            BizAdmin admin = bizAdminMapper.selectOne(wrapper);
            if (admin != null) {
                // 院系 ID：前端传 0 表示无院系，转换为 null 存入数据库（所有表主键使用 ASSIGN_ID，不存在 id=0）
                admin.setDepartmentId(updateDTO.getDepartmentId() != null && updateDTO.getDepartmentId() == 0 ? null : updateDTO.getDepartmentId());
                // 管理员编号：使用前端传递的 adminId
                if (updateDTO.getAdminId() != null) {
                    admin.setAdminId(updateDTO.getAdminId());
                }
                // 角色级别：根据院系 ID 是否为空判断（null 表示系统管理员，有院系表示院系管理员）
                admin.setRoleLevel(admin.getDepartmentId() != null ? IsDepartment.DEPARTMENT.getCode() : IsDepartment.NOT_DEPARTMENT.getCode());
                admin.setPhone(updateDTO.getPhone());
                admin.setEmail(updateDTO.getEmail());
                bizAdminMapper.updateById(admin);
            }
        } else {
            log.warn("不支持的用户类型：{}", userType);
        }
    }

    /**
     * 更新用户信息
     *
     * @param id        用户 ID
     * @param updateDTO 更新用户 DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserUpdateDTO updateDTO) {
        // 1. 查询用户是否存在
        SysUser existingUser = sysUserMapper.selectById(id);
        if (existingUser == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        // 2. 检查用户名是否已存在（排除自己）
        // 注意：UserUpdateDTO 中没有 username 字段，所以这里不需要检查用户名唯一性
        // 如果需要支持用户名修改，需要在 DTO 中添加 username 字段

        // 验证用户类型
        if (!EnumUtils.isValidCode(UserType.class, updateDTO.getUserType())) {
            throw new BusinessException(ResponseCode.USER_TYPE_INVALID);
        }

        // 密码格式验证（只有当提供了新密码时才验证）
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().trim().isEmpty()) {
            if(!passwordUtil.isValidPassword(updateDTO.getPassword())){
                throw new BusinessException(ResponseCode.PASSWORD_FORMAT_ERROR);
            }
        }

        // 状态
        if (!EnumUtils.isValidCode(AccountStatus.class, updateDTO.getStatus())) {
            throw new BusinessException(ResponseCode.INVALID_STATUS);
        }

        // 3. 构建更新实体
        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setRealName(updateDTO.getRealName());
        updateUser.setUserType(updateDTO.getUserType());
        // 只有提供了新密码时才更新密码
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().trim().isEmpty()) {
            updateUser.setPassword(passwordUtil.encryptPassword(updateDTO.getPassword()));
        }
        updateUser.setStatus(updateDTO.getStatus());
        updateUser.setAvatar(updateDTO.getAvatar()); // 设置头像

        // 4. 执行更新
        sysUserMapper.updateById(updateUser);

        // 5. 更新对应的业务表数据
        try {
            updateBusinessUserData(id, existingUser.getUserType(), updateDTO);
        } catch (Exception e) {
            log.error("更新业务表数据失败：{}", e.getMessage());
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "更新业务数据失败：" + e.getMessage());
        }

        // 6. 清除缓存
        clearUserCache(id);
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        // 1. 检查用户是否存在
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        // 2. 执行删除（MyBatis-Plus会自动处理逻辑删除，通过@TableLogic注解）
        sysUserMapper.deleteById(id);

        // 3. 清除缓存
        clearUserCache(id);
    }

   /**
     * 修改自身密码
     *
     * @param currentUserId 当前用户ID
     * @param dto           修改密码 DTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeOwnPassword(Long currentUserId, UserChangePasswordDTO dto) {
        // 1. 查询当前用户
        SysUser user = sysUserMapper.selectById(currentUserId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        // 2. 验证旧密码
        if (!passwordUtil.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResponseCode.OLD_PASSWORD_ERROR);
        }

        // 3. 校验新密码强度
        if (!passwordUtil.isValidPassword(dto.getNewPassword())) {
            throw new BusinessException(ResponseCode.PASSWORD_FORMAT_ERROR);
        }

        // 4. 更新密码
        SysUser updateUser = new SysUser();
        updateUser.setId(currentUserId);
        updateUser.setPassword(passwordUtil.encryptPassword(dto.getNewPassword()));
        // 注意：不要手动 setUpdatedAt，由 MetaObjectHandler 处理

        sysUserMapper.updateById(updateUser);
        clearUserCache(currentUserId);
    }

    /**
     * 启用用户
     *
     * @param id 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableUser(Long id) {
        updateUserStatus(id, AccountStatus.ENABLED);
    }

    /**
     * 禁用用户
     *
     * @param id 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableUser(Long id) {
        updateUserStatus(id, AccountStatus.DISABLED);
    }

    /**
     * 更新用户状态的私有方法
     *
     * @param id 用户ID
     * @param status 目标状态
     */
    private void updateUserStatus(Long id, AccountStatus status) {
        // 1. 检查用户是否存在
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResponseCode.USER_NOT_FOUND);
        }

        // 2. 检查当前状态是否与目标状态相同
        AccountStatus currentStatus = EnumUtils.fromCode(AccountStatus.class, user.getStatus());
        if (currentStatus == status) {
            String action = status.isEnabled() ? "启用" : "禁用";
            throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(),
                String.format("账户已经是%s状态", action));
        }

        // 3. 更新状态
        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setStatus(status.getCode());

        sysUserMapper.updateById(updateUser);

        // 4. 清除缓存
        clearUserCache(id);

        String action = status.isEnabled() ? "启用" : "禁用";
        log.info("用户 {} 账户{}成功，ID: {}", user.getUsername(), action, id);
    }


    /**
     * 将 SysUser 实体转换为 UserListInfoVO 视图对象
     *
     * @param user 用户实体
     * @return 用户视图对象
     */
    private UserListInfoVO convertToUserListInfoVO(SysUser user) {
        // 1. 复制基本属性
        UserListInfoVO vo = BeanMapperUtil.copyProperties(user, UserListInfoVO.class);

        // 2. 根据用户类型，从对应的业务表中获取额外字段
        if (UserType.STUDENT.getCode().equals(user.getUserType())) {
            // 学生：从 biz_student 表获取
            BizStudent student = bizStudentMapper.selectOne(
                new LambdaQueryWrapper<BizStudent>().eq(BizStudent::getUserId, user.getId())
            );
            if (student != null) {
                vo.setStudentId(student.getStudentId());
                vo.setGender(student.getGender());
                vo.setMajor(student.getMajor());
                vo.setClassName(student.getClassName());
                vo.setDepartmentId(student.getDepartmentId());
                vo.setPhone(student.getPhone());
                vo.setEmail(student.getEmail());

                // 填充院系名称
                if (student.getDepartmentId() != null) {
                    SysDepartment dept = sysDepartmentMapper.selectById(student.getDepartmentId());
                    if (dept != null) {
                        vo.setDepartmentName(dept.getName());
                    }
                }
            }
        } else if (UserType.TEACHER.getCode().equals(user.getUserType())) {
            // 教师：从 biz_teacher 表获取
            BizTeacher teacher = bizTeacherMapper.selectOne(
                new LambdaQueryWrapper<BizTeacher>().eq(BizTeacher::getUserId, user.getId())
            );
            if (teacher != null) {
                vo.setTeacherId(teacher.getTeacherId());
                vo.setGender(teacher.getGender());
                vo.setTitle(teacher.getTitle());
                vo.setDepartmentId(teacher.getDepartmentId());
                vo.setPhone(teacher.getPhone());
                vo.setEmail(teacher.getEmail());

                // 填充院系名称
                if (teacher.getDepartmentId() != null) {
                    SysDepartment dept = sysDepartmentMapper.selectById(teacher.getDepartmentId());
                    if (dept != null) {
                        vo.setDepartmentName(dept.getName());
                    }
                }
            }
        } else if (UserType.SYSTEM_ADMIN.getCode().equals(user.getUserType()) || UserType.DEPARTMENT_ADMIN.getCode().equals(user.getUserType())) {
            // 管理员：从 biz_admin 表获取
            BizAdmin admin = bizAdminMapper.selectOne(
                new LambdaQueryWrapper<BizAdmin>().eq(BizAdmin::getUserId, user.getId())
            );
            if (admin != null) {
                vo.setAdminId(admin.getAdminId());
                vo.setDepartmentId(admin.getDepartmentId());
                vo.setPhone(admin.getPhone());
                vo.setEmail(admin.getEmail());
                vo.setRoleLevel(admin.getRoleLevel());

                // 填充院系名称
                if (admin.getRoleLevel().equals(IsDepartment.DEPARTMENT.getCode())) {
                    SysDepartment dept = sysDepartmentMapper.selectById(admin.getDepartmentId());
                    if (dept != null) {
                        vo.setDepartmentName(dept.getName());
                    }
                }
            }
        }

        return vo;
    }

    /**
     * 统一清除用户缓存
     */
    private void clearUserCache(Long userId) {
        if (userId != null) {
            String cacheKey = CacheConstants.KeyPrefix.USER_INFO + userId;
            cacheHelper.evictCache(cacheKey);
            log.debug("清除用户缓存：{}", cacheKey);
        }
    }
}
