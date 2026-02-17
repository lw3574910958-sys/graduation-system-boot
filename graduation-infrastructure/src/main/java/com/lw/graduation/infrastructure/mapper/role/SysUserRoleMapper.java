package com.lw.graduation.infrastructure.mapper.role;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lw.graduation.domain.entity.role.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联 Mapper 接口
 *
 * @author lw
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}