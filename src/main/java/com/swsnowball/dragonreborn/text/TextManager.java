// TextManager.java - 使用配置目录的完整版本
package com.swsnowball.dragonreborn.text;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.google.gson.*;
import com.swsnowball.dragonreborn.DragonRebornMod;
import com.swsnowball.dragonreborn.data.DragonExtendedData;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class TextManager {
    private static final Logger LOGGER = LogManager.getLogger();

    // 配置目录：.minecraft/config/dragonreborn/
    private static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve(DragonRebornMod.MOD_ID);

    // 文字文件目录：.minecraft/config/dragonreborn/texts/
    private static final Path TEXTS_DIR = CONFIG_DIR.resolve("texts");

    // 存储事件对应的文字组树
    private static final Map<String, List<TextGroup>> eventGroups = new HashMap<>();
    private static final Set<String> registeredEvents = new HashSet<>();

    // ================= 文字组数据结构（保持不变） =================

    public static class TextGroup {
        public String id;
        public int priority = 0;
        public Map<String, String> conditions = new HashMap<>();
        public List<String> texts = new ArrayList<>();
        public List<TextGroup> subgroups = new ArrayList<>();
        public String rawCondition;

        public boolean checkConditions(DragonExtendedData data, Map<String, String> context, EntityDragonBase dragon) {
            if (rawCondition != null && !rawCondition.isEmpty()) {
                return ConditionParser.checkComplexCondition(rawCondition, data, context, dragon);
            }

            for (Map.Entry<String, String> entry : conditions.entrySet()) {
                String condition = entry.getKey() + " " + entry.getValue();
                if (!ConditionParser.checkCondition(condition, data, context, dragon)) {
                    return false;
                }
            }
            return true;
        }

        public List<String> getAllSubValidTexts(DragonExtendedData data, Map<String, String> context, EntityDragonBase dragon) {
            List<String> result = new ArrayList<>();

            for (TextGroup subgroup : subgroups) {
                if (subgroup.checkConditions(data, context, dragon)) {
                    result.addAll(subgroup.getAllSubValidTexts(data, context, dragon));
                    LOGGER.info("条件满足，已加载子文字组。");
                }
            }

            return result;
        }

        public List<List> getAllValidTexts(DragonExtendedData data, Map<String, String> context, EntityDragonBase dragon) {
            List<List> results = new ArrayList<>();
            List<String> result = new ArrayList<>();
            List<String> sub_result = new ArrayList<>();

            if (checkConditions(data, context, dragon)) {
                result.addAll(texts);
                LOGGER.info("条件满足，已加载文字组：\n{}", texts);

                for (TextGroup subgroup : subgroups) {
                    if (subgroup.checkConditions(data, context, dragon)) {
                        sub_result.addAll(subgroup.getAllSubValidTexts(data, context, dragon));
                        LOGGER.info("条件满足，已加载子文字组。");
                    }
                }
            } else {
                LOGGER.info("条件不满足，寻找下一文字组。");
            }

            results.add(result);
            results.add(sub_result);

            return results;
        }
    }

    // ================= 初始化方法 =================

    /**
     * 初始化文字系统 - 在模组启动时调用
     */
    public static void init() {
        eventGroups.clear();
        registeredEvents.clear();

        LOGGER.info("=== 初始化龙之文字系统 ===");
        LOGGER.info("配置目录: {}", CONFIG_DIR.toAbsolutePath());
        LOGGER.info("文字目录: {}", TEXTS_DIR.toAbsolutePath());

        try {
            // 1. 确保目录存在
            ensureDirectories();

            // 2. 提取默认文件（如果不存在）
            extractDefaultFiles();

            // 3. 加载所有文字文件
            loadAllTexts();

            LOGGER.info("✅ 文字系统初始化完成！共加载 {} 个事件", eventGroups.size());
            LOGGER.info("已注册事件: {}", registeredEvents);

        } catch (Exception e) {
            LOGGER.error("❌ 文字系统初始化失败: {}", e.getMessage(), e);
            createFallbackTexts();
        }
    }

    /**
     * 确保目录存在
     */
    private static void ensureDirectories() throws IOException {
        if (!Files.exists(CONFIG_DIR)) {
            LOGGER.info("创建配置目录: {}", CONFIG_DIR);
            Files.createDirectories(CONFIG_DIR);
        }

        if (!Files.exists(TEXTS_DIR)) {
            LOGGER.info("创建文字目录: {}", TEXTS_DIR);
            Files.createDirectories(TEXTS_DIR);

            // 创建子目录结构
            Files.createDirectories(TEXTS_DIR.resolve("triggers/interaction"));
            Files.createDirectories(TEXTS_DIR.resolve("triggers/life"));
            Files.createDirectories(TEXTS_DIR.resolve("triggers/behavior"));
        }
    }

    /**
     * 从JAR中提取默认文件到配置目录
     */
    private static void extractDefaultFiles() {
        LOGGER.info("检查默认文件...");

        // 需要提取的文件列表
        String[] defaultFiles = {
                "texts/triggers/interaction/petting.json",
                "texts/triggers/events/player_get_back.json",
                "texts/triggers/life/death.json",
                "texts/triggers/events/player_leaving.json",
                "texts/triggers/interaction/interaction_request.json",
                "texts/triggers/interaction/interaction_satisfied.json",
                "texts/triggers/interaction/interaction_ignored.json"
        };

        int extractedCount = 0;

        for (String relativePath : defaultFiles) {
            Path targetFile = TEXTS_DIR.resolve(relativePath);

            // 如果文件已经存在，跳过
            if (Files.exists(targetFile)) {
                LOGGER.debug("文件已存在，跳过: {}", relativePath);
                continue;
            }

            // 尝试从JAR中提取
            if (extractFromJar(relativePath, targetFile)) {
                extractedCount++;
            }
        }

        LOGGER.info("提取了 {} 个默认文件", extractedCount);
    }

    /**
     * 从JAR中提取单个文件
     */
    private static boolean extractFromJar(String jarPath, Path targetFile) {
        // 在JAR中的完整路径
        String resourcePath = "assets/dragonreborn/" + jarPath;

        try (InputStream inputStream = TextManager.class.getClassLoader()
                .getResourceAsStream(resourcePath)) {

            if (inputStream == null) {
                LOGGER.warn("JAR中找不到资源: {}", resourcePath);
                return false;
            }

            // 确保父目录存在
            Files.createDirectories(targetFile.getParent());

            // 复制文件
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("✅ 提取文件: {} -> {}", jarPath, targetFile.getFileName());

            return true;

        } catch (IOException e) {
            LOGGER.error("提取文件失败 {}: {}", jarPath, e.getMessage());
            return false;
        }
    }

    /**
     * 加载配置目录中的所有文字文件
     */
    private static void loadAllTexts() throws IOException {
        LOGGER.info("加载文字文件...");

        if (!Files.exists(TEXTS_DIR)) {
            LOGGER.error("文字目录不存在: {}", TEXTS_DIR);
            createFallbackTexts();
            return;
        }

        // 使用Files.walk递归查找所有JSON文件
        try (var stream = Files.walk(TEXTS_DIR)) {
            List<Path> jsonFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .toList();

            LOGGER.info("找到 {} 个JSON文件", jsonFiles.size());

            for (Path file : jsonFiles) {
                loadTextFile(file);
            }
        }
    }

    /**
     * 加载单个文字文件
     */
    private static void loadTextFile(Path filePath) {
        try {
            LOGGER.debug("加载文件: {}", filePath.getFileName());

            // 读取文件内容
            String content = Files.readString(filePath, StandardCharsets.UTF_8);

            // 解析JSON
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();

            // 检查必要字段
            if (!json.has("event")) {
                LOGGER.warn("文件缺少 'event' 字段: {}", filePath.getFileName());
                return;
            }

            String event = json.get("event").getAsString();
            List<TextGroup> groups = parseTextGroups(json);

            if (groups.isEmpty()) {
                LOGGER.warn("文件没有有效的文字组: {}", filePath.getFileName());
                return;
            }

            // 存储到内存
            eventGroups.put(event, groups);
            registeredEvents.add(event);

            LOGGER.info("加载事件: {} ({} 个文字组)", event, groups.size());

        } catch (IOException e) {
            LOGGER.error("读取文件失败 {}: {}", filePath.getFileName(), e.getMessage());
        } catch (JsonSyntaxException e) {
            LOGGER.error("JSON语法错误 {}: {}", filePath.getFileName(), e.getMessage());
        } catch (Exception e) {
            LOGGER.error("加载文件失败 {}: {}", filePath.getFileName(), e.getMessage());
        }
    }

    // ================= JSON解析方法（保持不变） =================

    private static List<TextGroup> parseTextGroups(JsonObject json) {
        List<TextGroup> groups = new ArrayList<>();

        if (json.has("text_groups")) {
            JsonObject groupsObj = json.getAsJsonObject("text_groups");

            for (String groupId : groupsObj.keySet()) {
                JsonObject groupJson = groupsObj.getAsJsonObject(groupId);
                TextGroup group = parseTextGroup(groupId, groupJson);
                groups.add(group);
            }
        }

        // 按优先级排序
        groups.sort(Comparator.comparingInt(g -> -g.priority)); // 降序，优先级高的在前

        return groups;
    }

    private static TextGroup parseTextGroup(String id, JsonObject json) {
        TextGroup group = new TextGroup();
        group.id = id;

        // 解析优先级
        if (json.has("priority")) {
            group.priority = json.get("priority").getAsInt();
        }

        // 解析条件
        if (json.has("conditions")) {
            JsonObject conditions = json.getAsJsonObject("conditions");
            for (String key : conditions.keySet()) {
                group.conditions.put(key, conditions.get(key).getAsString());
            }
        }

        // 解析原始条件字符串
        if (json.has("condition")) {
            group.rawCondition = json.get("condition").getAsString();
        }

        // 解析文字列表
        if (json.has("texts")) {
            JsonArray textsArray = json.getAsJsonArray("texts");
            textsArray.forEach(element -> {
                group.texts.add(element.getAsString());
            });
        }

        // 递归解析子组
        if (json.has("subgroups")) {
            JsonObject subgroups = json.getAsJsonObject("subgroups");
            for (String subId : subgroups.keySet()) {
                TextGroup subgroup = parseTextGroup(subId, subgroups.getAsJsonObject(subId));
                group.subgroups.add(subgroup);
            }

            // 子组也按优先级排序
            group.subgroups.sort(Comparator.comparingInt(g -> -g.priority));
        }

        return group;
    }

    // ================= 公开API方法（保持不变） =================

    /**
     * 根据事件和条件获取合适的文字
     */
    public static List<String> getTextForEvent(String event, DragonExtendedData data,
                                               Map<String, String> context, EntityDragonBase dragon) {
        List<TextGroup> groups = eventGroups.getOrDefault(event, new ArrayList<>());

        if (groups.isEmpty()) {
            LOGGER.warn("事件 {} 没有找到对应的文字组", event);
            return new ArrayList<>();
        }

        // 按优先级查找第一个满足条件的文字组
        for (TextGroup group : groups) {
            List<List> validResults = group.getAllValidTexts(data, context, dragon);
            List<String> mainTexts = validResults.get(0);
            List<String> subTexts = validResults.get(1);

            if (!mainTexts.isEmpty() || !subTexts.isEmpty()) {
                List<String> result = new ArrayList<>();
                Random random = new Random();

                // 优先选择子组文字（优先级更高），如果没有则选择主组文字
                if (!subTexts.isEmpty()) {
                    String text = subTexts.get(random.nextInt(subTexts.size()));
                    text = replaceVariables(text, data, context);
                    result.add(text);
                } else if (!mainTexts.isEmpty()) {
                    String text = mainTexts.get(random.nextInt(mainTexts.size()));
                    text = replaceVariables(text, data, context);
                    result.add(text);
                }

                LOGGER.debug("为事件 {} 选择了文字: {}", event, result);
                return result;
            }
        }

        LOGGER.warn("事件 {} 没有满足条件的文字", event);
        return new ArrayList<>();
    }

    /**
     * 获取所有已注册的事件（调试用）
     */
    public static Set<String> getRegisteredEvents() {
        return new HashSet<>(registeredEvents);
    }

    /**
     * 重新加载所有文字文件（用于热重载）
     */
    public static void reload() {
        LOGGER.info("重新加载文字文件...");
        init();
    }

    // ================= 辅助方法 =================

    private static String replaceVariables(String text, DragonExtendedData data,
                                           Map<String, String> context) {
        String result = text;

        // 基础变量
        result = result.replace("<龙名>", data.getDragonName());
        result = result.replace("{龙名}", data.getDragonName());

        // 亲密度描述
        result = result.replace("<亲密度>", data.getClosenessDescription());
        result = result.replace("{亲密度}", data.getClosenessDescription());

        // 心情描述
        result = result.replace("<心情>", data.getMoodDescription());
        result = result.replace("{心情}", data.getMoodDescription());

        // 上下文变量
        if (context != null) {
            for (Map.Entry<String, String> entry : context.entrySet()) {
                result = result.replace("<" + entry.getKey() + ">", entry.getValue());
                result = result.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return result;
    }

    private static void createFallbackTexts() {
        LOGGER.warn("创建后备文字...");

        // 创建简单的抚摸事件
        TextGroup group = new TextGroup();
        group.id = "fallback_petting";
        group.priority = 1;
        group.texts.add("你轻轻抚摸<龙名>的鳞片。");
        group.texts.add("<龙名>享受地眯起了眼睛。");

        eventGroups.put("petting", Arrays.asList(group));
        registeredEvents.add("petting");

        LOGGER.info("已创建后备文字");
    }
}