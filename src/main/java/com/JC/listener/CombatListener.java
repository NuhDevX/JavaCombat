package com.JC.listener;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.particle.CriticalParticle;
import cn.nukkit.math.Vector3;

import com.JC.Main;
import com.JC.manager.AttackCooldownManager;
import com.JC.util.KnockbackUtil;

import java.util.List;

public class CombatListener implements Listener {

    private final Main plugin;
    private final AttackCooldownManager cooldownManager;

    public CombatListener(Main plugin, AttackCooldownManager cooldownManager) {
        this.plugin = plugin;
        this.cooldownManager = cooldownManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity victim = event.getEntity();

        if (!(damager instanceof Player)) return;
        if (!(victim instanceof EntityLiving)) return;

        Player attacker = (Player) damager;
        Item heldItem = attacker.getInventory().getItemInHand();
        int currentTick = Server.getInstance().getTick();

        boolean cooldownEnabled = plugin.getPluginConfig().getBoolean("modules.attack-cooldown", true);
        boolean critEnabled = plugin.getPluginConfig().getBoolean("modules.critical-hit", true);
        boolean sweepEnabled = plugin.getPluginConfig().getBoolean("modules.sweep-attack", true);
        boolean sprintResetEnabled = plugin.getPluginConfig().getBoolean("modules.sprint-reset", true);
        boolean javaKnockback = plugin.getPluginConfig().getBoolean("modules.java-knockback", true);
        boolean shieldEnabled = plugin.getPluginConfig().getBoolean("modules.shield-block", true);

        double progress = 1.0;
        double damageModifier = 1.0;

        if (cooldownEnabled) {
            progress = cooldownManager.getCooldownProgress(attacker, heldItem, currentTick);
            damageModifier = cooldownManager.getDamageModifier(progress);

            double baseDamage = event.getDamage(EntityDamageEvent.DamageModifier.BASE);
            event.setDamage((float) (baseDamage * damageModifier), EntityDamageEvent.DamageModifier.BASE);

            double enchantDamage = getEnchantDamageBonus(heldItem, victim);
            if (enchantDamage > 0) {
                event.setDamage((float) (event.getFinalDamage() + enchantDamage * damageModifier));
            }
        }

        boolean isCritical = false;
        if (critEnabled) {
            double minCritProgress = plugin.getPluginConfig().getDouble("critical-hit.min-cooldown-progress", 0.9);
            isCritical = attacker.getMotionY() < 0
                    && !attacker.isOnGround()
                    && !attacker.isSprinting()
                    && !attacker.hasEffect(cn.nukkit.potion.Effect.BLINDNESS)
                    && !attacker.isInsideOfWater()
                    && attacker.getRiding() == null
                    && (!cooldownEnabled || progress >= minCritProgress);

            if (isCritical) {
                double multiplier = plugin.getPluginConfig().getDouble("critical-hit.multiplier", 1.5);
                event.setDamage((float) (event.getFinalDamage() * multiplier));

                Server.getInstance().getScheduler(); // no-op placeholder to keep import usage clear
                victim.getLevel().addParticle(new CriticalParticle(victim.add(0, victim.getHeight() / 2, 0)));
            }
        }

        if (sprintResetEnabled && attacker.isSprinting()) {
            attacker.setSprinting(false);
        }

        if (javaKnockback) {
            int kbLevel = 0;
            Enchantment kb = heldItem.getEnchantment(Enchantment.ID_KNOCKBACK);
            if (kb != null) kbLevel = kb.getLevel();

            Vector3 kbVector = KnockbackUtil.calculate(
                    attacker, victim,
                    plugin.getPluginConfig().getDouble("knockback.base-horizontal", 0.4),
                    plugin.getPluginConfig().getDouble("knockback.base-vertical", 0.4),
                    plugin.getPluginConfig().getDouble("knockback.vertical-limit", 0.4),
                    attacker.isSprinting(),
                    plugin.getPluginConfig().getDouble("knockback.sprint-bonus-horizontal", 0.5),
                    plugin.getPluginConfig().getDouble("knockback.sprint-bonus-vertical", 0.1),
                    kbLevel
            );
            event.setKnockBack(1.0f); 
            victim.setMotion(kbVector);
        }

        if (sweepEnabled && isSword(heldItem) && !isCritical) {
            double minSweepProgress = plugin.getPluginConfig().getDouble("sweep-attack.min-cooldown-progress", 0.9);
            boolean canSweep = attacker.isOnGround()
                    && (!cooldownEnabled || progress >= minSweepProgress)
                    && (Math.abs(attacker.getMotionX()) > 0.05 || Math.abs(attacker.getMotionZ()) > 0.05);

            if (canSweep) {
                doSweepAttack(attacker, victim, heldItem, event.getFinalDamage());
            }
        }

        if (shieldEnabled) {
            Item offhand = attacker.getOffhandInventory() != null ? attacker.getOffhandInventory().getItem(0) : null;
        }

        if (cooldownEnabled) {
            cooldownManager.recordAttack(attacker, currentTick);
        }
    }

    private boolean isSword(Item item) {
        String name = item.getName() == null ? "" : item.getName().toLowerCase();
        return name.contains("sword");
    }

    private double getEnchantDamageBonus(Item item, Entity victim) {
        Enchantment sharpness = item.getEnchantment(Enchantment.ID_DAMAGE_ALL);
        if (sharpness == null) return 0;
        int level = sharpness.getLevel();
        if (level <= 0) return 0;
        return 0.5 * level + 0.5;
    }

    private void doSweepAttack(Player attacker, Entity primaryTarget, Item heldItem, double primaryDamage) {
        double multiplier = plugin.getPluginConfig().getDouble("sweep-attack.damage-multiplier", 0.5);
        double radius = plugin.getPluginConfig().getDouble("sweep-attack.radius", 1.6);
        double sweepDamage = primaryDamage * multiplier;
        if (sweepDamage <= 0) return;

        List<Entity> nearby = attacker.getLevel().getNearbyEntities(
                attacker.getBoundingBox().grow(radius, 0.25, radius), attacker
        ).length > 0 ? java.util.Arrays.asList(attacker.getLevel().getNearbyEntities(
                attacker.getBoundingBox().grow(radius, 0.25, radius), attacker
        )) : java.util.Collections.emptyList();

        for (Entity entity : nearby) {
            if (entity == primaryTarget || entity == attacker) continue;
            if (!(entity instanceof EntityLiving)) continue;

            EntityDamageByEntityEvent sweepEvent = new EntityDamageByEntityEvent(
                    attacker, entity, EntityDamageEvent.DamageCause.ENTITY_ATTACK, sweepDamage
            );
            entity.attack(sweepEvent);

            if (!sweepEvent.isCancelled()) {
                Vector3 kb = KnockbackUtil.calculate(
                        attacker, entity, 0.4, 0.4, 0.4,
                        false, 0, 0, 0
                );
                entity.setMotion(kb);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldownManager.clear(event.getPlayer());
    }
  }
              
