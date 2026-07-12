/*
 * Legacy value-setting semantics adapted from Create 6.0.8's KineticScrollValueBehaviour.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics.motor;

public final class KineticMotorValueSettings {

    public static final int CLOCKWISE_ROW = 0;
    public static final int COUNTER_CLOCKWISE_ROW = 1;

    public static int toSignedSpeed(int row, int value) {
        int magnitude = Math.max(1, Math.min(CreativeMotorBlockEntity.MAX_SPEED, Math.abs(value)));
        return row == CLOCKWISE_ROW ? -magnitude : magnitude;
    }

    private KineticMotorValueSettings() {}
}
