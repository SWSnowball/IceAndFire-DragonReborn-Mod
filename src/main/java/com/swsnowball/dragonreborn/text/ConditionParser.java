// com.swsnowball.dragonreborn.text.ConditionParser.java
package com.swsnowball.dragonreborn.text;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.swsnowball.dragonreborn.data.DragonExtendedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.text.html.parser.Entity;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConditionParser {
    private static final Logger LOGGER = LogManager.getLogger();

    // 正则表达式匹配条件
    private static final Pattern SINGLE_CONDITION_PATTERN =
            Pattern.compile("([a-zA-Z_]+)\\s*(>=|<=|>|<|==|!=)\\s*([-+]?\\d*\\.?\\d+)");

    // 解析条件字符串并检查是否满足
    public static boolean checkCondition(String condition, DragonExtendedData data,
                                         Map<String, String> context, EntityDragonBase dragon) {
        if (condition == null || condition.isEmpty()) return true;

        LOGGER.debug("检查条件: {}", condition);

        // 如果是复合条件（包含&&或||）
        if (condition.contains("&&") || condition.contains("||")) {
            return checkComplexCondition(condition, data, context, dragon);
        }

        // 单条件解析
        return parseSingleCondition(condition, data, context, dragon);
    }

    // 解析单条件
    private static boolean parseSingleCondition(String condition, DragonExtendedData data,
                                                Map<String, String> context, EntityDragonBase dragon) {
        LOGGER.debug("解析单条件: {}", condition);

        // 使用正则表达式匹配
        Matcher matcher = SINGLE_CONDITION_PATTERN.matcher(condition.trim());

        if (matcher.matches()) {
            String property = matcher.group(1);
            String operator = matcher.group(2);
            String valueStr = matcher.group(3);

            LOGGER.debug("匹配成功 - 属性: {}, 操作符: {}, 值: {}",
                    property, operator, valueStr);

            // 获取属性值
            float propertyValue = getPropertyValue(property, data, context, dragon);
            float compareValue;

            try {
                compareValue = Float.parseFloat(valueStr);
            } catch (NumberFormatException e) {
                LOGGER.error("无法解析数值: {}", valueStr);
                return false;
            }

            LOGGER.debug("比较: {} {} {}", propertyValue, operator, compareValue);
            return compare(propertyValue, operator, compareValue);
        } else {
            LOGGER.warn("无法解析的条件格式: {}", condition);
            return false;
        }
    }

    // 解析复合条件
    public static boolean checkComplexCondition(String condition, DragonExtendedData data,
                                                Map<String, String> context, EntityDragonBase dragon) {
        LOGGER.debug("解析复合条件: {}", condition);

        // 处理括号
        condition = condition.trim();

        // 检查&&优先级
        if (condition.contains("&&")) {
            // 分割&&条件
            String[] andConditions = condition.split("&&");

            for (String cond : andConditions) {
                cond = cond.trim();
                if (!checkCondition(cond, data, context, dragon)) {
                    LOGGER.debug("&&条件失败: {}", cond);
                    return false;
                }
            }
            return true;
        }

        // 检查||条件
        if (condition.contains("||")) {
            String[] orConditions = condition.split("\\|\\|");

            for (String cond : orConditions) {
                cond = cond.trim();
                if (checkCondition(cond, data, context, dragon)) {
                    LOGGER.debug("||条件满足: {}", cond);
                    return true;
                }
            }
            return false;
        }

        // 既不是&&也不是||，当作单条件处理
        return parseSingleCondition(condition, data, context, dragon);
    }

    // 获取属性值（保持不变）
    private static float getPropertyValue(String property, DragonExtendedData data,
                                          Map<String, String> context, EntityDragonBase dragon) {
        switch (property.toLowerCase()) {
            case "closeness":
                return data.getCloseness();
            case "moodweight":
                return data.getMoodWeight();
            case "loneliness":
                return data.getLoneliness();
            case "modeldead":
                return dragon.isModelDead() ? 1:0;
            case "flying":
                return dragon.isFlying() ? 1:0;
            default:
                // 尝试从上下文中获取
                if (context != null && context.containsKey(property)) {
                    try {
                        return Float.parseFloat(context.get(property));
                    } catch (NumberFormatException e) {
                        LOGGER.warn("无法从上下文解析数值: {}", context.get(property));
                        return 0.0f;
                    }
                }
                LOGGER.warn("未知属性: {}", property);
                return 0.0f;
        }
    }

    // 比较操作（保持不变）
    private static boolean compare(float a, String operator, float b) {
        switch (operator) {
            case ">": return a > b;
            case ">=": return a >= b;
            case "<": return a < b;
            case "<=": return a <= b;
            case "==": return Math.abs(a - b) < 0.001f;
            case "!=": return Math.abs(a - b) >= 0.001f;
            default:
                LOGGER.warn("未知操作符: {}", operator);
                return false;
        }
    }
}