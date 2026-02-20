package com.swsnowball.dragonreborn.command;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.swsnowball.dragonreborn.data.DragonDataManager;
import com.swsnowball.dragonreborn.data.DragonExtendedData;
import com.swsnowball.dragonreborn.util.DragonInteractionUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "dragonreborn")
public class DragonDataCommand {
    private static final Logger LOGGER = LogManager.getLogger();

    // 用于建议的属性名
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_PROPERTIES =
            (context, builder) -> {
                builder.suggest("dragon_name");
                builder.suggest("closeness");
                builder.suggest("moodWeight");
                builder.suggest("loneliness");
                builder.suggest("lonely");
                builder.suggest("player_isNear");
                builder.suggest("closeness_bonus");
                builder.suggest("interaction_request_cooldown");
                builder.suggest("is_waiting_for_player");
                builder.suggest("all");
                return builder.buildFuture();
            };

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        // 主命令 /dragonreborn
        dispatcher.register(
                Commands.literal("dragonreborn")
                        .requires(source -> source.hasPermission(2)) // 需要op权限
                        .then(Commands.literal("data")
                                .then(Commands.literal("get")
                                        .executes(context -> getAllData(context, null))
                                        .then(Commands.argument("property", StringArgumentType.word())
                                                .suggests(SUGGEST_PROPERTIES)
                                                .executes(context -> getDataByProperty(
                                                        context,
                                                        StringArgumentType.getString(context, "property")
                                                ))
                                        )
                                )
                                .then(Commands.literal("set")
                                        .then(Commands.argument("property", StringArgumentType.word())
                                                .suggests(SUGGEST_PROPERTIES)
                                                .then(Commands.argument("value", FloatArgumentType.floatArg(0.0f, 1.0f))
                                                        .executes(context -> setData(
                                                                context,
                                                                StringArgumentType.getString(context, "property"),
                                                                FloatArgumentType.getFloat(context, "value")
                                                        ))
                                                )
                                        )
                                )
                                .then(Commands.literal("add")
                                        .then(Commands.argument("property", StringArgumentType.word())
                                                .suggests(SUGGEST_PROPERTIES)
                                                .then(Commands.argument("value", FloatArgumentType.floatArg(-1.0f, 1.0f))
                                                        .executes(context -> addData(
                                                                context,
                                                                StringArgumentType.getString(context, "property"),
                                                                FloatArgumentType.getFloat(context, "value")
                                                        ))
                                                )
                                        )
                                )
                                .then(Commands.literal("reset")
                                        .executes(DragonDataCommand::resetAllData)
                                )
                        )
                        .then(Commands.literal("debug")
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    Player player = source.getPlayerOrException();

                                    // 获取玩家正在看的龙
                                    EntityDragonBase dragon = DragonInteractionUtil.unifiedServerDetection(player, 10.0);

                                    if (dragon != null) {
                                        DragonExtendedData data = DragonDataManager.getOrCreateData(dragon);

                                        // 发送调试信息
                                        MutableComponent message = Component.literal("§6=== 龙数据调试信息 ===\n")
                                                .append("§e龙名: §f" + dragon.getName().getString() + "\n")
                                                .append("§eUUID: §f" + dragon.getUUID() + "\n")
                                                .append("§e主人: §f" + (dragon.getOwner() != null ? dragon.getOwner().getName().getString() : "无") + "\n")
                                                .append("§e生命值: §f" + dragon.getHealth() + "/" + dragon.getMaxHealth() + "\n")
                                                .append("§e死亡状态: §f" + dragon.isModelDead() + "\n")
                                                .append("§eNBT标签: \n§f");

                                        dragon.saveWithoutId(new net.minecraft.nbt.CompoundTag()).getAllKeys()
                                                .forEach(key -> message.append("  " + key + ": " + dragon.getPersistentData().get(key) + "\n"));

                                        source.sendSuccess(() -> message, false);
                                        return 1;
                                    } else {
                                        source.sendFailure(Component.literal("§c没有检测到龙！"));
                                        return 0;
                                    }
                                })
                        )
                        .then(Commands.literal("help")
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    MutableComponent help = Component.literal("§6=== 永恒羁绊指令帮助 ===\n")
                                            .append("§e/dragonreborn data get [属性] §f- 查看龙数据\n")
                                            .append("§e/dragonreborn data set <属性> <值> §f- 设置龙数据\n")
                                            .append("§e/dragonreborn data add <属性> <值> §f- 增减龙数据\n")
                                            .append("§e/dragonreborn data reset §f- 重置龙数据\n")
                                            .append("§e/dragonreborn debug §f- 调试信息\n")
                                            .append("§e可用属性: closeness, moodWeight, loneliness\n")
                                            .append("§e范围: 0.0 ~ 1.0");
                                    source.sendSuccess(() -> help, false);
                                    return 1;
                                })
                        )
        );
    }

    // ================= 命令实现 =================

    private static int getAllData(CommandContext<CommandSourceStack> context, String property) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();

        EntityDragonBase dragon = DragonInteractionUtil.unifiedServerDetection(player, 10.0);

        if (dragon == null) {
            source.sendFailure(Component.literal("§c没有检测到龙！请对准龙使用指令。"));
            return 0;
        }

        DragonExtendedData data = DragonDataManager.getOrCreateData(dragon);

        MutableComponent message = Component.literal("§6=== " + dragon.getName().getString() + " 的精神数据 ===\n")
                .append("§e亲密度: §f" + String.format("%.2f", data.getCloseness()) +
                        " (§a" + data.getClosenessDescription() + "§f)\n")
                .append("§e心情权重: §f" + String.format("%.2f", data.getMoodWeight()) +
                        " (§a" + data.getMoodDescription() + "§f)\n")
                .append("§e孤独值: §f" + String.format("%.2f", data.getLoneliness()) + "\n")
                .append("§e孤独状态: §f" + data.getLonelyStatus() + "\n")
                .append("§e玩家靠近: §f" + data.getPlayerIsNear() + "\n")
                .append("§e亲密度加成: §f" + data.getCloseness_bonus() + "\n")
                .append("§e互动请求冷却: §f" + data.getIRC() + "\n")
                .append("§e是否等待玩家回应: §f" + data.getWaitingForPlayer() + "\n")
                .append("§e龙名: §f" + data.getDragonName());

        source.sendSuccess(() -> message, false);
        return 1;
    }

    private static int getDataByProperty(CommandContext<CommandSourceStack> context, String property) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();

        if (property.equals("all")) {
            return getAllData(context, null);
        }

        EntityDragonBase dragon = DragonInteractionUtil.unifiedServerDetection(player, 10.0);

        if (dragon == null) {
            source.sendFailure(Component.literal("§c没有检测到龙！请对准龙使用指令。"));
            return 0;
        }

        DragonExtendedData data = DragonDataManager.getOrCreateData(dragon);
        MutableComponent message;

        switch (property) {
            case "closeness":
                message = Component.literal("§e" + dragon.getName().getString() + " 的亲密度: §f" +
                        String.format("%.4f", data.getCloseness()) + " (§a" + data.getClosenessDescription() + "§f)");
                break;
            case "moodWeight":
                message = Component.literal("§e" + dragon.getName().getString() + " 的心情权重: §f" +
                        String.format("%.4f", data.getMoodWeight()) + " (§a" + data.getMoodDescription() + "§f)");
                break;
            case "loneliness":
                message = Component.literal("§e" + dragon.getName().getString() + " 的孤独值: §f" +
                        String.format("%.4f", data.getLoneliness()));
                break;
            case "lonely":
                message = Component.literal("§e" + dragon.getName().getString() + " 的孤独状态: §f" +
                        data.getLonelyStatus());
                break;
            case "player_isNear":
                message = Component.literal("§e" + dragon.getName().getString() + " 的玩家靠近状态: §f" +
                        data.getPlayerIsNear());
                break;
            default:
                source.sendFailure(Component.literal("§c未知属性: " + property));
                return 0;
        }

        source.sendSuccess(() -> message, false);
        return 1;
    }

    private static int setData(CommandContext<CommandSourceStack> context, String property, float value) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();

        EntityDragonBase dragon = DragonInteractionUtil.unifiedServerDetection(player, 10.0);

        if (dragon == null) {
            source.sendFailure(Component.literal("§c没有检测到龙！请对准龙使用指令。"));
            return 0;
        }

        DragonExtendedData data = DragonDataManager.getOrCreateData(dragon);
        String formattedValue = String.format("%.4f", value);

        switch (property) {
            case "closeness":
                data.setCloseness(value);
                source.sendSuccess(() -> Component.literal("§a已将 " + dragon.getName().getString() +
                        " 的亲密度设置为: §e" + formattedValue), false);
                break;
            case "moodWeight":
                data.setMoodWeight(value);
                source.sendSuccess(() -> Component.literal("§a已将 " + dragon.getName().getString() +
                        " 的心情权重设置为: §e" + formattedValue), false);
                break;
            case "loneliness":
                data.setLoneliness(value);
                source.sendSuccess(() -> Component.literal("§a已将 " + dragon.getName().getString() +
                        " 的孤独值设置为: §e" + formattedValue), false);
                break;
            case "closeness_bonuns":
                data.setCloseness_bonus(value);
                source.sendSuccess(() -> Component.literal("§a已将 " + dragon.getName().getString() +
                        " 的亲密度加成设置为: §e" + formattedValue), false);
                break;
            case "interaction_request_cooldown":
                data.setIRC((int) value);
                source.sendSuccess(() -> Component.literal("§a已将 " + dragon.getName().getString() +
                        " 的互动请求冷却设置为: §e" + formattedValue), false);
                break;
            case "is_waiting_for_player":
                data.setWaitingForPlayer(value == 1? true:false);
                source.sendSuccess(() -> Component.literal("§a已将 " + dragon.getName().getString() +
                        " 的等待玩家状态设置为: §e" + formattedValue), false);
                break;
            default:
                source.sendFailure(Component.literal("§c无法设置属性: " + property +
                        " (只能设置数值型属性)"));
                return 0;
        }

        DragonDataManager.saveData(dragon, data);
        return 1;
    }

    private static int addData(CommandContext<CommandSourceStack> context, String property, float delta) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();

        EntityDragonBase dragon = DragonInteractionUtil.unifiedServerDetection(player, 10.0);

        if (dragon == null) {
            source.sendFailure(Component.literal("§c没有检测到龙！请对准龙使用指令。"));
            return 0;
        }

        DragonExtendedData data = DragonDataManager.getOrCreateData(dragon);
        float oldValue, newValue;
        String operation = delta >= 0 ? "增加" : "减少";
        String absDelta = String.format("%.4f", Math.abs(delta));

        switch (property) {
            case "closeness":
                oldValue = data.getCloseness();
                data.addCloseness(delta);
                newValue = data.getCloseness();
                source.sendSuccess(() -> Component.literal("§a" + dragon.getName().getString() +
                        " 的亲密度 " + operation + "了 §e" + absDelta + "§a (§f" +
                        String.format("%.4f", oldValue) + " → " + String.format("%.4f", newValue) + "§a)"), false);
                break;
            case "moodWeight":
                oldValue = data.getMoodWeight();
                data.addMoodWeight(delta);
                newValue = data.getMoodWeight();
                source.sendSuccess(() -> Component.literal("§a" + dragon.getName().getString() +
                        " 的心情权重 " + operation + "了 §e" + absDelta + "§a (§f" +
                        String.format("%.4f", oldValue) + " → " + String.format("%.4f", newValue) + "§a)"), false);
                break;
            case "loneliness":
                oldValue = data.getLoneliness();
                data.setLoneliness(data.getLoneliness() + delta); // 使用setter确保范围
                newValue = data.getLoneliness();
                source.sendSuccess(() -> Component.literal("§a" + dragon.getName().getString() +
                        " 的孤独值 " + operation + "了 §e" + absDelta + "§a (§f" +
                        String.format("%.4f", oldValue) + " → " + String.format("%.4f", newValue) + "§a)"), false);
                break;
            default:
                source.sendFailure(Component.literal("§c无法增减属性: " + property +
                        " (只能操作数值型属性)"));
                return 0;
        }

        DragonDataManager.saveData(dragon, data);
        return 1;
    }

    private static int resetAllData(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        Player player = source.getPlayerOrException();

        EntityDragonBase dragon = DragonInteractionUtil.unifiedServerDetection(player, 10.0);

        if (dragon == null) {
            source.sendFailure(Component.literal("§c没有检测到龙！请对准龙使用指令。"));
            return 0;
        }

        DragonExtendedData newData = new DragonExtendedData();
        newData.setDragonName(dragon.getName().getString());

        DragonDataManager.saveData(dragon, newData);
        source.sendSuccess(() -> Component.literal("§a已重置 " + dragon.getName().getString() +
                " 的所有精神数据为默认值"), false);
        return 1;
    }
}