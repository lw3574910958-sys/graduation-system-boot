package com.lw.graduation.codegen;

import lombok.Data;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * 从 classpath 下的 codegen-config.yml 加载代码生成配置
 */
public class YamlConfigLoader {

    private static final String CONFIG_FILE = "codegen-config.yml";

    public static DbConfig loadDbConfig() {
        Config config = loadConfig();
        return config.getDatabase();
    }

    public static Map<String, String> loadTableModuleMap() {
        Config config = loadConfig();
        return config.getCodegen().getTableModuleMap();
    }

    private static Config loadConfig() {
        // 修复：使用兼容新版本的构造方式
        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(new Constructor(Config.class, loaderOptions));
        try (InputStream is = YamlConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is == null) {
                throw new IllegalStateException("❌ 未找到配置文件: " + CONFIG_FILE);
            }
            Config config = yaml.load(is);
            if (config == null) {
                throw new IllegalStateException("❌ 配置文件为空或格式错误");
            }
            return config;
        } catch (Exception e) {
            throw new RuntimeException("❌ 加载 " + CONFIG_FILE + " 失败", e);
        }
    }

    // ====== 配置模型类 ======

    /**
     * 配置模型类
     */
    @Data
    public static class Config {
        private DbConfig database;
        private CodegenConfig codegen = new CodegenConfig();
    }

    /**
     * 数据库连接信息
     */
    @Data
    public static class DbConfig {
        private String url;
        private String username;
        private String password;

    }

    /**
     * 代码生成配置
     */
    @Data
    public static class CodegenConfig {
        private Map<String, String> tableModuleMap = Collections.emptyMap();
    }
}