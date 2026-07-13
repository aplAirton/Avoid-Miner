package com.airton.avoidminer;

public final class SeismicBootsRules {
    public static final double MIN_FALL_DISTANCE = 4.0;
    public static final double MIN_DOWNWARD_SPEED = -0.35;
    public static final double MIN_RADIUS = 8.0;
    public static final double MAX_RADIUS = 16.0;

    private SeismicBootsRules() {
    }

    public static boolean shouldArm(boolean sneaking, boolean onGround, double downwardSpeed,
                                    double fallDistance) {
        return sneaking && !onGround && downwardSpeed <= MIN_DOWNWARD_SPEED
                && fallDistance >= MIN_FALL_DISTANCE;
    }

    public static boolean shouldTriggerImpact(boolean trackedArmed, boolean sneaking,
                                              double fallDistance) {
        return fallDistance >= MIN_FALL_DISTANCE && (trackedArmed || sneaking);
    }

    public static double shockwaveRadius(double fallDistance) {
        return Math.clamp(MIN_RADIUS + (fallDistance - MIN_FALL_DISTANCE) * 0.35,
                MIN_RADIUS, MAX_RADIUS);
    }

    public static float shockwaveDamage(double fallDistance) {
        return (float) Math.clamp(6.0 + (fallDistance - MIN_FALL_DISTANCE) * 0.65, 6.0, 18.0);
    }

    public static float damageAtDistance(float baseDamage, double distance, double radius) {
        double progress = Math.clamp(distance / radius, 0.0, 1.0);
        return (float) (baseDamage * (1.0 - progress * 0.5));
    }
}
