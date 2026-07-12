package com.simibubi.create.foundation.utility.legacy.render;

/** The time-to-angle rule used by Create's kinetic block-entity renderer. */
public final class KineticRenderMath {

    private static final float DEGREES_PER_RPM_TICK = 3.0f / 10.0f;

    private KineticRenderMath() {}

    public static float angleDegrees(long worldTime, float partialTicks, float speed) {
        return wrapDegrees((worldTime + partialTicks) * speed * DEGREES_PER_RPM_TICK);
    }

    public static float angleDelta(float ticks, float speed) {
        return ticks * speed * DEGREES_PER_RPM_TICK;
    }

    private static float wrapDegrees(float angle) {
        float wrapped = angle % 360.0f;
        return wrapped < 0 ? wrapped + 360.0f : wrapped;
    }
}
