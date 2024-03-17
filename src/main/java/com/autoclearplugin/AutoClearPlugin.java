package com.autoclearplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class AutoClearPlugin extends JavaPlugin implements Listener {
    private int cleanupSeconds = 300;
    private int taskId;

    @Override
    public void onEnable() {
        // 生成默认配置文件
        generateConfig();

        // 加载配置
        loadConfig();

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(this, this);

        // 启动定时任务
        startCleanupTask();
    }

    private void generateConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }

            File configFile = new File(getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                saveResource("config.yml", false); // 使用相对路径保存资源
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        if (config.contains("cleanupInterval")) {
            cleanupSeconds = config.getInt("cleanupInterval");
            getLogger().info("Loaded cleanup interval: " + cleanupSeconds + " seconds");
        } else {
            cleanupSeconds = 300;
            getLogger().warning("Cleanup interval not found in config. Using default value.");
        }
    }

    private void startCleanupTask() {
        taskId = getServer().getScheduler().runTaskTimer(this, () -> {
            for (int i = 10; i >= 1; i--) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(ChatColor.YELLOW + "清理掉落物倒计时: " + i + " 秒");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(ChatColor.GREEN + "正在清理掉落物...");
            }

            cleanupItems();
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(ChatColor.GREEN + "清理完成！");
            }
        }, 0L, 5800L).getTaskId();
    }

    private void cleanupItems() {
        getLogger().info("AutoClear cleanup task running.");

        // 清理掉落物的逻辑
        getServer().getWorlds().forEach(world -> world.getEntitiesByClass(Item.class).forEach(Entity::remove));
    }

    // 处理插件指令
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("autoclear")) {
            if (args.length > 0) {
                // 处理指令参数
                if (args[0].equalsIgnoreCase("reload")) {
                    // 执行重新加载配置等操作
                    reloadConfig();
                    loadConfig();
                    sender.sendMessage("重载完成！");
                    return true;
                } else if (args[0].equalsIgnoreCase("manual")) {
                    // 执行手动清理等操作
                    cleanupItems();
                    sender.sendMessage("手动清理完成！");
                    return true;
                }

            } else {
                // 显示帮助信息
                sender.sendMessage("使用方法: /autoclear <reload|manual>");
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDisable() {
        // 在插件卸载时取消定时任务
        Bukkit.getScheduler().cancelTask(taskId);
    }
}
