package com.lw.graduation.codegen;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import com.lw.graduation.common.constant.CommonConstants;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * 高级代码生成器：支持按表隔离包路径 + 自动建目录 + 多模块输出
 *
 * @author lw
 */
@Slf4j
public class CodeGenerator {
    
    public static void main(String[] args) {
        log.info("🚀 启动 MyBatis-Plus 代码生成器");

        // 加载配置
        YamlConfigLoader.DbConfig dbConfig = YamlConfigLoader.loadDbConfig();
        Map<String, String> TABLE_MODULE_MAP = YamlConfigLoader.loadTableModuleMap();

        if (TABLE_MODULE_MAP.isEmpty()) {
            throw new IllegalStateException("❌ codegen-config.yml 中未配置 table-module-map");
        }

        String maskedUrl = dbConfig.getUrl().replaceAll("(password=)([^&]*)", "$1******");
        log.info("🔗 数据库连接: {}", maskedUrl);

        // 动态计算项目根目录（关键！）
        String projectRoot = getProjectRoot();
        log.info("🏠 项目根目录: {}", projectRoot);

        // 3. 自动创建所有输出目录（防止静默失败）
        createRequiredDirectories(projectRoot, TABLE_MODULE_MAP);

        // 4. 逐表生成（每张表独立包路径）
        for (Map.Entry<String, String> entry : TABLE_MODULE_MAP.entrySet()) {
            String tableName = entry.getKey();
            String module = entry.getValue();
            generateTable(dbConfig, projectRoot, tableName, module);
        }

        System.out.println("\n✅ 全部代码生成完毕！");
    }

    /**
     * 动态获取项目根目录（graduation-system-boot）
     */
    private static String getProjectRoot() {
        // 获取当前类的 class 文件 URL
        URL resourceUrl = CodeGenerator.class.getResource("/");
        if (resourceUrl == null) {
            throw new IllegalStateException("无法获取类路径资源，CodeGenerator.class.getResource(\"/\") 返回 null");
        }
        String resourcePath = resourceUrl.getPath();
        try {
            String decodedPath = URLDecoder.decode(resourcePath, StandardCharsets.UTF_8);
            int index = decodedPath.indexOf("/graduation-codegen/target/classes");
            if (index == -1) {
                throw new IllegalStateException(
                        "无法识别项目结构。期望路径包含 '/graduation-codegen/target/classes'，但实际为: " + decodedPath
                );
            }
            return decodedPath.substring(0, index);
        } catch (Exception e) {
            throw new RuntimeException("解析项目根目录失败", e);
        }
    }

    /**
     * 创建所有必要输出目录（包括 Java 包路径和资源路径）
     */
    private static void createRequiredDirectories(String projectRoot, Map<String, String> tableModuleMap) {
        log.info("📂 正在创建输出目录...");

        // Entity 根目录（后续会按模块自动创建子包）
        File entityDir = new File(projectRoot, "graduation-domain/src/main/java/com/lw/graduation/domain/entity");
        if (!entityDir.mkdirs() && !entityDir.exists()) {
            throw new RuntimeException("创建Entity目录失败: " + entityDir.getAbsolutePath());
        }

        // Mapper 根目录（含 base）
        File mapperBaseDir = new File(projectRoot, "graduation-infrastructure/src/main/java/com/lw/graduation/infrastructure/mapper");
        if (!mapperBaseDir.mkdirs() && !mapperBaseDir.exists()) {
            throw new RuntimeException("创建Mapper基础目录失败: " + mapperBaseDir.getAbsolutePath());
        }

        // XML 目录（统一存放，也可按模块分）
        File xmlDir = new File(projectRoot, "graduation-infrastructure/src/main/resources/mapper");
        if (!xmlDir.mkdirs() && !xmlDir.exists()) {
            throw new RuntimeException("创建XML目录失败: " + xmlDir.getAbsolutePath());
        }
        // 按模块预创建 entity 和 mapper 子目录（非必需，但更安全）
        for (String module : tableModuleMap.values()) {
            File entityModuleDir = new File(projectRoot, "graduation-domain/src/main/java/com/lw/graduation/domain/entity/" + module);
            if (!entityModuleDir.mkdirs() && !entityModuleDir.exists()) {
                throw new RuntimeException("创建模块Entity目录失败: " + entityModuleDir.getAbsolutePath());
            }

            File mapperModuleDir = new File(projectRoot, "graduation-infrastructure/src/main/java/com/lw/graduation/infrastructure/mapper/" + module);
            if (!mapperModuleDir.mkdirs() && !mapperModuleDir.exists()) {
                throw new RuntimeException("创建模块Mapper目录失败: " + mapperModuleDir.getAbsolutePath());
            }

            File xmlModuleDir = new File(projectRoot, "graduation-infrastructure/src/main/resources/mapper/" + module);
            if (!xmlModuleDir.mkdirs() && !xmlModuleDir.exists()) {
                throw new RuntimeException("创建模块Mapper目录失败: " + mapperModuleDir.getAbsolutePath());
            }
        }

        log.info("✅ 输出目录创建完成");
    }

    /**
     * 为单张表生成代码
     */
    private static void generateTable(YamlConfigLoader.DbConfig dbConfig,
                                      String projectRoot,
                                      String tableName,
                                      String module) {
        log.info("\n📝 生成表 [{}] → 模块 [{}]", tableName, module);

        // 构建完整的物理输出路径（含模块子目录）
        String entityOutputPath = projectRoot + "/graduation-domain/src/main/java/com/lw/graduation/domain/entity/" + module;
        String mapperOutputPath = projectRoot + "/graduation-infrastructure/src/main/java/com/lw/graduation/infrastructure/mapper/" + module;
        String xmlOutputPath = projectRoot + "/graduation-infrastructure/src/main/resources/mapper/" + module;

        Map<OutputFile, String> pathInfo = new HashMap<>();
        pathInfo.put(OutputFile.entity, entityOutputPath);
        pathInfo.put(OutputFile.mapper, mapperOutputPath);
        pathInfo.put(OutputFile.xml, xmlOutputPath);

        FastAutoGenerator.create(dbConfig.getUrl(), dbConfig.getUsername(), dbConfig.getPassword())
                .globalConfig(builder -> builder
                        .author("lw")
//                        .enableSwagger()
                        .dateType(DateType.TIME_PACK)
                        .commentDate(CommonConstants.DateTimeFormat.DATE_ONLY)
                        .disableOpenDir() // 不自动打开文件夹
                )
                .packageConfig(builder -> builder
                        .parent("com.lw.graduation")
                        .entity("domain.entity." + module)          // 动态：entity.user
                        .mapper("infrastructure.mapper." + module)  // 动态：mapper.user
                        .xml("mapper/" + module) // XML 统一放 resources/mapper/
                        .pathInfo(pathInfo)
                )
                .strategyConfig(builder -> builder
                        // 数据库表名
                        .addInclude(tableName)
                        // 实体策略
                        .entityBuilder()
                            .enableLombok()
                            .enableFileOverride()
                            .enableTableFieldAnnotation()
                            .logicDeleteColumnName("is_deleted")
                            .naming(NamingStrategy.underline_to_camel)
                            .columnNaming(NamingStrategy.underline_to_camel)
                            .enableFileOverride()
                        // Mapper策略
                        .mapperBuilder()
                            .enableBaseResultMap()
                            .enableBaseColumnList()
                            .superClass("com.lw.graduation.infrastructure.mapper.MyBaseMapper")
                            .enableFileOverride()
                        // Service策略
                        .serviceBuilder()
                            .disableService()
                            .disableServiceImpl()
                            .enableFileOverride()
                        // Controller策略
                        .controllerBuilder()
                            .disable()
                            .enableFileOverride()
                        // Restful 风格
                        .enableRestStyle()
                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();

        log.info("✅ [{}] 生成成功", tableName);
    }
}