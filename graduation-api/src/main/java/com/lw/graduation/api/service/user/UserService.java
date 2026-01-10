package com.lw.graduation.api.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lw.graduation.api.dto.user.UserCreateDTO;
import com.lw.graduation.api.dto.user.UserPageQueryDTO;
import com.lw.graduation.api.dto.user.UserUpdateDTO;
import com.lw.graduation.api.vo.user.SysUserVO;

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
    IPage<SysUserVO> getUserPage(UserPageQueryDTO queryDTO);

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情 VO
     */
    SysUserVO getUserById(Long id);

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
     * 重置用户密码
     *
     * @param id 用户ID
     */
    void resetPassword(Long id);

    /**
     * 更新用户头像
     *
     * @param id 用户ID
     * @param avatar 头像URL或存储路径
     */
    void updateUserAvatar(Long id, String avatar);
}