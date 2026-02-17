package com.lw.graduation.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统权限枚举
 * 定义系统中的具体操作权限
 *
 * @author lw
 */
@Getter
@AllArgsConstructor
public enum Permission {
    
    // 用户管理权限
    USER_VIEW("user:view", "查看用户信息"),
    USER_CREATE("user:create", "创建用户"),
    USER_UPDATE("user:update", "更新用户信息"),
    USER_DELETE("user:delete", "删除用户"),
    
    // 题目管理权限
    TOPIC_VIEW("topic:view", "查看题目"),
    TOPIC_CREATE("topic:create", "创建题目"),
    TOPIC_UPDATE("topic:update", "更新题目"),
    TOPIC_DELETE("topic:delete", "删除题目"),
    TOPIC_AUDIT("topic:audit", "审核题目"),
    
    // 选题管理权限
    SELECTION_VIEW("selection:view", "查看选题"),
    SELECTION_CREATE("selection:create", "创建选题"),
    SELECTION_UPDATE("selection:update", "更新选题"),
    SELECTION_DELETE("selection:delete", "删除选题"),
    SELECTION_AUDIT("selection:audit", "审核选题"),
    
    // 文档管理权限
    DOCUMENT_VIEW("document:view", "查看文档"),
    DOCUMENT_UPLOAD("document:upload", "上传文档"),
    DOCUMENT_DOWNLOAD("document:download", "下载文档"),
    DOCUMENT_REVIEW("document:review", "审阅文档"),
    DOCUMENT_DELETE("document:delete", "删除文档"),
    
    // 成绩管理权限
    GRADE_VIEW("grade:view", "查看成绩"),
    GRADE_INPUT("grade:input", "录入成绩"),
    GRADE_UPDATE("grade:update", "更新成绩"),
    GRADE_STATISTICS("grade:statistics", "成绩统计"),
    
    // 系统管理权限
    SYSTEM_CONFIG("system:config", "系统配置"),
    SYSTEM_LOG("system:log", "查看系统日志"),
    SYSTEM_MONITOR("system:monitor", "系统监控"),
    
    // 院系管理权限
    DEPARTMENT_VIEW("department:view", "查看院系信息"),
    DEPARTMENT_MANAGE("department:manage", "管理院系");

    /**
     * 权限编码
     */
    private final String code;
    
    /**
     * 权限描述
     */
    private final String description;

    /**
     * 根据权限编码获取权限枚举
     *
     * @param code 权限编码
     * @return 权限枚举，未找到返回null
     */
    public static Permission getByCode(String code) {
        for (Permission permission : values()) {
            if (permission.code.equals(code)) {
                return permission;
            }
        }
        return null;
    }

    /**
     * 校验权限编码是否有效
     *
     * @param code 权限编码
     * @return 有效返回true，否则返回false
     */
    public static boolean isValid(String code) {
        return getByCode(code) != null;
    }
    
    /**
     * 判断是否为查看类权限
     *
     * @return 是查看权限返回true
     */
    public boolean isViewPermission() {
        return this.code.endsWith(":view");
    }
    
    /**
     * 判断是否为管理类权限
     *
     * @return 是管理权限返回true
     */
    public boolean isManagePermission() {
        return this.code.contains(":manage") || this.code.contains(":config");
    }
}