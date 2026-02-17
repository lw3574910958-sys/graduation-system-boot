package com.lw.graduation.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统角色枚举
 * 定义系统中的具体角色权限，基于用户类型进行细分
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum SystemRole {
    
    /**
     * 系统管理员 - 拥有最高权限，可管理所有用户和系统配置
     */
    SYSTEM_ADMIN("system_admin", "系统管理员", UserType.ADMIN),
    
    /**
     * 院系管理员 - 管理特定院系的师生信息和毕业设计流程
     */
    DEPARTMENT_ADMIN("department_admin", "院系管理员", UserType.ADMIN),
    
    /**
     * 教师角色 - 指导学生毕业设计，发布和审核题目
     */
    TEACHER("teacher", "教师", UserType.TEACHER),
    
    /**
     * 学生角色 - 参与毕业设计，选题、提交文档等
     */
    STUDENT("student", "学生", UserType.STUDENT);

    /**
     * 角色编码
     */
    private final String code;
    
    /**
     * 角色名称
     */
    private final String name;
    
    /**
     * 对应的用户类型
     */
    private final UserType userType;

    /**
     * 根据角色编码获取角色枚举
     *
     * @param code 角色编码
     * @return 角色枚举，未找到返回null
     */
    public static SystemRole getByCode(String code) {
        for (SystemRole role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return null;
    }

    /**
     * 校验角色编码是否有效
     *
     * @param code 角色编码
     * @return 有效返回true，否则返回false
     */
    public static boolean isValid(String code) {
        return getByCode(code) != null;
    }

    /**
     * 获取指定用户类型的所有角色
     *
     * @param userType 用户类型
     * @return 该用户类型对应的角色数组
     */
    public static SystemRole[] getByUserType(UserType userType) {
        return java.util.Arrays.stream(values())
                .filter(role -> role.userType == userType)
                .toArray(SystemRole[]::new);
    }
    
    /**
     * 判断是否为管理员角色
     *
     * @return 是管理员返回true
     */
    public boolean isAdmin() {
        return this == SYSTEM_ADMIN || this == DEPARTMENT_ADMIN;
    }
    
    /**
     * 判断是否为系统管理员
     *
     * @return 是系统管理员返回true
     */
    public boolean isSystemAdmin() {
        return this == SYSTEM_ADMIN;
    }
    
    /**
     * 判断是否为院系管理员
     *
     * @return 是院系管理员返回true
     */
    public boolean isDepartmentAdmin() {
        return this == DEPARTMENT_ADMIN;
    }
}