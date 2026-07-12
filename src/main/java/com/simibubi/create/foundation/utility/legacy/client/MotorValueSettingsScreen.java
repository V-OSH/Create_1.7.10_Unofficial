/*
 * Legacy GUI bridge for Create 6.0.8's ValueSettingsScreen.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create.foundation.utility.legacy.client;

import org.lwjgl.input.Mouse;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlockEntity;
import com.simibubi.create.content.kinetics.motor.KineticMotorValueSettings;
import com.simibubi.create.foundation.utility.legacy.networking.MotorSpeedPacket;

import cpw.mods.fml.client.config.GuiSlider;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class MotorValueSettingsScreen extends GuiScreen {

    private static final int CLOCKWISE = KineticMotorValueSettings.CLOCKWISE_ROW;
    private static final int COUNTER_CLOCKWISE = KineticMotorValueSettings.COUNTER_CLOCKWISE_ROW;
    private static final int SPEED = 2;
    private static final int CONFIRM = 3;

    private final int x;
    private final int y;
    private final int z;
    private final int initialMagnitude;
    private int directionRow;
    private GuiSlider speedSlider;

    public MotorValueSettingsScreen(int x, int y, int z, int currentSpeed) {
        this.x = x;
        this.y = y;
        this.z = z;
        directionRow = currentSpeed < 0 ? CLOCKWISE : COUNTER_CLOCKWISE;
        initialMagnitude = Math.max(1, Math.abs(currentSpeed));
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int left = width / 2 - 100;
        int top = height / 2 - 44;
        buttonList.add(new GuiButton(CLOCKWISE, left, top, 98, 20,
            I18n.format("gui.create.motor.clockwise")));
        buttonList.add(new GuiButton(COUNTER_CLOCKWISE, left + 102, top, 98, 20,
            I18n.format("gui.create.motor.counter_clockwise")));
        speedSlider = new GuiSlider(SPEED, left, top + 26, 200, 20, "", " RPM", 1,
            CreativeMotorBlockEntity.MAX_SPEED, initialMagnitude, false, true);
        buttonList.add(speedSlider);
        buttonList.add(new GuiButton(CONFIRM, left + 50, top + 52, 100, 20, I18n.format("gui.done")));
        updateDirectionButtons();
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == CLOCKWISE || button.id == COUNTER_CLOCKWISE) {
            directionRow = button.id;
            updateDirectionButtons();
            return;
        }
        if (button.id == CONFIRM) {
            AllPackets.CHANNEL.sendToServer(new MotorSpeedPacket(x, y, z, directionRow, speedSlider.getValueInt()));
            mc.displayGuiScreen(null);
        }
    }

    private void updateDirectionButtons() {
        ((GuiButton) buttonList.get(CLOCKWISE)).enabled = directionRow != CLOCKWISE;
        ((GuiButton) buttonList.get(COUNTER_CLOCKWISE)).enabled = directionRow != COUNTER_CLOCKWISE;
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel == 0 || speedSlider == null) {
            return;
        }
        int step = isShiftKeyDown() ? 16 : 1;
        int value = speedSlider.getValueInt() + (wheel > 0 ? step : -step);
        speedSlider.setValue(Math.max(1, Math.min(CreativeMotorBlockEntity.MAX_SPEED, value)));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        String title = I18n.format("gui.create.motor.title");
        drawCenteredString(fontRendererObj, title, width / 2, height / 2 - 66, 0xffffff);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
