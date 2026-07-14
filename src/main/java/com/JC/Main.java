package com.JC;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

import com.JC.listener.CombatListener;
import com.JC.manager.AttackCooldownManager;

import java.util.HashMap;
import java.util.Map;

public class Main extends PluginBase {

    private Config pluginConfig;
    private AttackCooldownManager cooldownManager;
    private boolean enabled = true;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.pluginConfig = getConfig();

        Map<String, Double> attackSpeed = new HashMap<>();
        for (String key : new String[]{"hand", "sword", "axe", "pickaxe", "shovel", "hoe", "trident", "default"}) {
            attackSpeed.put(key, pluginConfig.getDouble("attack-speed." + key, 4.0));
        }
        double minModifier = pluginConfig.getDouble("damage-scaling.min-modifier", 0.2);

        this.cooldownManager = new AttackCooldownManager(attackSpeed, minModifier);

        getServer().getPluginManager().registerEvents(new CombatListener(this, cooldownManager), this);
    }

    public Config getPluginConfig() {
        return pluginConfig;
    }

    public AttackCooldownManager getCooldownManager() {
        return cooldownManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("javacombat")) return false;

        if (args.length == 0) {
            sender.sendMessage(TextFormat.YELLOW + "use: /javacombat <reload|toggle|status>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                reloadConfig();
                this.pluginConfig = getConfig();
                sender.sendMessage(TextFormat.GREEN + "JavaCombat configuration reloaded.");
                return true;

            case "status":
                sender.sendMessage(TextFormat.AQUA + "JavaCombat status: "
                        + (enabled ? TextFormat.GREEN + "ACTIVE" : TextFormat.RED + "NON-ACTIVE"));
                return true;

            case "toggle":
                enabled = !enabled;
                sender.sendMessage(TextFormat.YELLOW + "Plugin now: "
                        + (enabled ? TextFormat.GREEN + "ACTIVE" : TextFormat.RED + "NON-ACTIVE"));
                return true;

            default:
                sender.sendMessage(TextFormat.YELLOW + "Use: /javacombat <reload|toggle|status>");
                return true;
        }
    }
  }
              
