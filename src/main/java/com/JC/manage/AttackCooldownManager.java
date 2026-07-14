package com.JC.manager;

import cn.nukkit.Player;
import cn.nukkit.item.Item;

import java.util.HashMap;
import java.util.Map;

public class AttackCooldownManager {

    private final Map<String, Double> attackSpeedByCategory;
    private final double minModifier;

    private final Map<String, Integer> lastAttackTick = new HashMap<>();

    public AttackCooldownManager(Map<String, Double> attackSpeedByCategory, double minModifier) {
        this.attackSpeedByCategory = attackSpeedByCategory;
        this.minModifier = minModifier;
    }

    public double getCooldownTicks(Item item) {
        double attackSpeed = resolveAttackSpeed(item);
        if (attackSpeed <= 0) attackSpeed = attackSpeedByCategory.getOrDefault("default", 4.0);
        return 20.0 / attackSpeed;
    }

    private double resolveAttackSpeed(Item item) {
        String name = item.getName() == null ? "" : item.getName().toLowerCase();
        if (name.contains("sword")) return attackSpeedByCategory.getOrDefault("sword", 1.6);
        if (name.contains("axe") && !name.contains("pickaxe")) return attackSpeedByCategory.getOrDefault("axe", 0.8);
        if (name.contains("pickaxe")) return attackSpeedByCategory.getOrDefault("pickaxe", 1.2);
        if (name.contains("shovel") || name.contains("spade")) return attackSpeedByCategory.getOrDefault("shovel", 1.0);
        if (name.contains("hoe")) return attackSpeedByCategory.getOrDefault("hoe", 2.0);
        if (name.contains("trident")) return attackSpeedByCategory.getOrDefault("trident", 1.1);
        return attackSpeedByCategory.getOrDefault("hand", 4.0);
    }

    public double getCooldownProgress(Player player, Item heldItem, int currentTick) {
        Integer last = lastAttackTick.get(player.getName());
        if (last == null) return 1.0; 

        double cooldownTicks = getCooldownTicks(heldItem);
        double elapsed = currentTick - last;
        double progress = elapsed / cooldownTicks;
        if (progress < 0) progress = 0;
        if (progress > 1) progress = 1;
        return progress;
    }

    public double getDamageModifier(double progress) {
        return minModifier + (progress * progress) * (1.0 - minModifier);
    }

    public void recordAttack(Player player, int currentTick) {
        lastAttackTick.put(player.getName(), currentTick);
    }

    public void clear(Player player) {
        lastAttackTick.remove(player.getName());
    }
}
          
