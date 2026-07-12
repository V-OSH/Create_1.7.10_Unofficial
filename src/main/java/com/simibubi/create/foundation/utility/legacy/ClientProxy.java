package com.simibubi.create.foundation.utility.legacy;

import com.simibubi.create.content.kinetics.motor.MotorValueSettingsScreen;

import net.minecraft.client.Minecraft;

public class ClientProxy extends CommonProxy {

    @Override
    public void openMotorSpeedScreen(int x, int y, int z, int currentSpeed) {
        Minecraft.getMinecraft().displayGuiScreen(new MotorValueSettingsScreen(x, y, z, currentSpeed));
    }
}
