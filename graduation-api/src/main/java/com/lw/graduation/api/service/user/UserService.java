package com.lw.graduation.api.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.user.UserChangePasswordDTO;
import com.lw.graduation.api.dto.user.UserCreateDTO;
import com.lw.graduation.api.dto.user.UserPageQueryDTO;
import com.lw.graduation.api.dto.user.UserUpdateDTO;
import com.lw.graduation.api.vo.user.UserListInfoVO;

/**
 * 用户服务接口
 * 定义用户管理模块的核心业务逻辑。
 *
 * @author lw
 */
public interface UserService{

    /**
     * 分页查询用户列表
     *
     * @param queryDTO 查询条件 DTO
     * @return 分页结果
     */
    IPage<UserListInfoVO> getUserPage(UserPageQueryDTO queryDTO);

    /**
     * 根据用户 ID获取用户详情（统一返回 UserListInfoVO）
     *
     * @param userId 用户 ID
     * @return 用户详情 VO
     */
    UserListInfoVO getUserByUserId(Long userId);

    /**
     * 创建新用户
     *
     * @param createDTO 创建用户 DTO
     */
    void createUser(UserCreateDTO createDTO);

    /**
     * 更新用户信息
     *
     * @param id        用户ID
     * @param updateDTO 更新用户 DTO
     */
    void updateUser(Long id, UserUpdateDTO updateDTO);

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    void deleteUser(Long id);

    /**
     * 用户修改自己的密码
     *
     * @param currentUserId 当前登录用户ID
     * @param dto           包含旧密码和新密码
     */
    void changeOwnPassword(Long currentUserId, UserChangePasswordDTO dto);

    /**
     * 启用用户账户
     *
     * @param id 用户ID
     */
    void enableUser(Long id);

    /**
     * 禁用用户账户
     *
     * @param id 用户 ID
     */
    void disableUser(Long id);

}