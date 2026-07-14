package com.JC.util;

import cn.nukkit.entity.Entity;
import cn.nukkit.math.Vector3;

public class KnockbackUtil {

    public static Vector3 calculate(Entity attacker, Entity target,
                                     double baseHorizontal, double baseVertical, double verticalLimit,
                                     boolean sprinting, double sprintBonusHorizontal, double sprintBonusVertical,
                                     int knockbackEnchantLevel) {

        double dx = target.x - attacker.x;
        double dz = target.z - attacker.z;

        while (dx * dx + dz * dz < 1.0E-4) {
            dx = (Math.random() - Math.random()) * 0.01;
            dz = (Math.random() - Math.random()) * 0.01;
        }

        double dist = Math.sqrt(dx * dx + dz * dz);
        dx /= dist;
        dz /= dist;

        double horizontal = baseHorizontal;
        double vertical = baseVertical;

        if (sprinting) {
            horizontal += sprintBonusHorizontal;
            vertical += sprintBonusVertical;
        }

        if (knockbackEnchantLevel > 0) {
            horizontal += knockbackEnchantLevel * 0.5;
        }

        if (vertical > verticalLimit) {
            vertical = verticalLimit;
        }

        return new Vector3(dx * horizontal, vertical, dz * horizontal);
    }
  }

