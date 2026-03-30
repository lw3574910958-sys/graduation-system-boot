package com.lw.graduation.common.constant;

/**
 * 系统角色常量类
 * 集中管理系统中的各种角色标识，避免硬编码
 * 与前端 SYSTEM_ROLE 枚举保持完全同步
 *
 * @author lw
 */
public class RoleConstants {

    /**
     * 系统管理员角色
     */
    public static final String SYSTEM_ADMIN = "system_admin";

    /**
     * 院系管理员角色
     */
    public static final String DEPARTMENT_ADMIN = "department_admin";

    /**
     * 教师角色
     */
    public static final String TEACHER = "teacher";

    /**
     * 学生角色
     */
    public static final String STUDENT = "student";

    /**
     * 所有角色数组（用于需要多个角色的场景）
     */
    public static final String[] ALL_ROLES = {SYSTEM_ADMIN, DEPARTMENT_ADMIN, TEACHER, STUDENT};

    /**
     * 管理员角色数组（系统管理员 + 院系管理员）
     */
    public static final String[] ADMIN_ROLES = {SYSTEM_ADMIN, DEPARTMENT_ADMIN};

    /**
     * 非学生角色数组（系统管理员 + 院系管理员 + 教师）
     */
    public static final String[] NON_STUDENT_ROLES = {SYSTEM_ADMIN, DEPARTMENT_ADMIN, TEACHER};

    /**
     * 私有构造函数，防止实例化
     */
    private RoleConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
