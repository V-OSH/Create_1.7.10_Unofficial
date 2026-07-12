package com.simibubi.create.foundation.utility.legacy;

import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.foundation.utility.legacy.client.MotorValueSettingsScreen;
import com.simibubi.create.foundation.utility.legacy.render.CreativeMotorBlockRenderer;
import com.simibubi.create.foundation.utility.legacy.render.CreativeMotorRenderer;
import com.simibubi.create.foundation.utility.legacy.render.KineticShaftRenderer;
import com.simibubi.create.foundation.utility.legacy.render.ShaftBlockRenderer;
import com.simibubi.create.foundation.utility.legacy.render.CogWheelBlockRenderer;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraft.client.Minecraft;

public class ClientProxy extends CommonProxy {

    @Override
    public void registerRenderers() {
        int motorRenderType = RenderingRegistry.getNextAvailableRenderId();
        CreativeMotorBlock.setRenderType(motorRenderType);
        RenderingRegistry.registerBlockHandler(new CreativeMotorBlockRenderer(motorRenderType));
        int shaftRenderType = RenderingRegistry.getNextAvailableRenderId();
        ShaftBlock.setRenderType(shaftRenderType);
        RenderingRegistry.registerBlockHandler(new ShaftBlockRenderer(shaftRenderType));
        int cogwheelRenderType = RenderingRegistry.getNextAvailableRenderId();
        CogWheelBlock.setRenderType(cogwheelRenderType);
        RenderingRegistry.registerBlockHandler(new CogWheelBlockRenderer(cogwheelRenderType));
        ClientRegistry.bindTileEntitySpecialRenderer(KineticBlockEntity.class, new KineticShaftRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(CreativeMotorBlockEntity.class, new CreativeMotorRenderer());
    }

    @Override
    public void openMotorSpeedScreen(int x, int y, int z, int currentSpeed) {
        Minecraft.getMinecraft().displayGuiScreen(new MotorValueSettingsScreen(x, y, z, currentSpeed));
    }
}
