/*
 * Legacy value-setting semantics adapted from Create 6.0.8's KineticScrollValueBehaviour.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.content.kinetics.motor;

public final class KineticMotorValueSettings {

    public static int toSignedSpeed(int row, int value) {
        int magnitude = Math.max(1, Math.min(CreativeMotorBlockEntity.MAX_SPEED, Math.abs(value)));
        return row == 0 ? -magnitude : magnitude;
    }

    private KineticMotorValueSettings() {}
}
