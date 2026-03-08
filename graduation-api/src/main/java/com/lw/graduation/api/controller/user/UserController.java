package com.lw.graduation.api.controller.user;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.lw.graduation.api.dto.user.UserChangePasswordDTO;
import com.lw.graduation.api.dto.user.UserCreateDTO;
import com.lw.graduation.api.dto.user.UserPageQueryDTO;
import com.lw.graduation.api.dto.user.UserUpdateDTO;
import com.lw.graduation.api.service.user.UserService;
import com.lw.graduation.api.vo.admin.AdminVO;
import com.lw.graduation.api.vo.student.StudentVO;
import com.lw.graduation.api.vo.teacher.TeacherVO;
import com.lw.graduation.api.vo.user.UserListInfoVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 用户管理控制器
 * 提供用户信息的增删改查、分页查询、详情获取、密码重置等API端点。
 * 需要登录并具有管理员权限才能访问。
 *
 * @author lw
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户信息的增删改查、分页查询、详情获取、密码重置等接口")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询条件 DTO
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询用户列表")
    @SaCheckRole({"system_admin"})
    public Result<IPage<UserListInfoVO>> getUserPage(UserPageQueryDTO queryDTO) {
        return Result.success(userService.getUserPage(queryDTO));
    }

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情 VO
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户详情")
    @SaCheckRole({"system_admin"})
    public Result<UserListInfoVO> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    /**
     * 创建新用户
     *
     * @param createDTO 创建用户 DTO
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "创建新用户")
    @SaCheckRole({"system_admin"})
    public Result<Void> createUser(@Validated @RequestBody UserCreateDTO createDTO) {
        userService.createUser(createDTO);
        return Result.success();
    }

    /**
     * 更新用户信息
     *
     * @param id        用户ID
     * @param updateDTO 更新用户 DTO
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息")
    @SaCheckRole({"system_admin"})
    public Result<Void> updateUser(@PathVariable Long id, @Validated @RequestBody UserUpdateDTO updateDTO) {
        userService.updateUser(id, updateDTO);
        return Result.success();
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    @SaCheckRole("system_admin")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 修改自己的密码
     *
     * @param dto 修改密码 DTO
     * @return 修改结果
     */
    @PostMapping("/my/password")
    @Operation(summary = "修改自己的密码")
    @SaCheckLogin
    public Result<Void> changeOwnPassword(@Validated @RequestBody UserChangePasswordDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        userService.changeOwnPassword(currentUserId, dto);
        return Result.success();
    }

    /**
     * 根据用户 ID 获取学生详情
     *
     * @param userId 用户 ID
     * @return 学生详情 VO
     */
    @GetMapping("/student/{userId}")
    @Operation(summary = "根据用户 ID 获取学生详情")
    @SaCheckRole({"system_admin"})
    public Result<StudentVO> getStudentByUserId(@PathVariable Long userId) {
        return Result.success(userService.getStudentByUserId(userId));
    }

    /**
     * 根据用户 ID 获取教师详情
     *
     * @param userId 用户 ID
     * @return 教师详情 VO
     */
    @GetMapping("/teacher/{userId}")
    @Operation(summary = "根据用户 ID 获取教师详情")
    @SaCheckRole({"system_admin"})
    public Result<TeacherVO> getTeacherByUserId(@PathVariable Long userId) {
        return Result.success(userService.getTeacherByUserId(userId));
    }

    @GetMapping("/admin/{userId}")
    @Operation(summary = "根据用户 ID 获取管理员详情")
    @SaCheckRole({"system_admin"})
    public Result<AdminVO> getAdminByUserId(@PathVariable Long userId) {
        return Result.success(userService.getAdminByUserId(userId));
    }

}
