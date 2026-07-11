package create.core.kinetic.source;

import create.foundation.networking.PacketHandler;
import create.foundation.networking.PacketMotorSpeed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

/**
 * Creative Motor speed slider GUI.
 *
 * <p>Shows a horizontal slider from -256 to +256 RPM with a current-value
 * label. Click-and-drag to adjust. Sends {@link PacketMotorSpeed} on change.</p>
 */
public class GuiCreativeMotor extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation("create", "textures/gui/creative_motor.png");

    private final CreativeMotorTileEntity motor;
    private final int motorX, motorY, motorZ;

    // Slider state
    private int currentSpeed;
    private boolean dragging;

    // Slider area (relative to guiLeft/guiTop)
    private static final int SLIDER_X = 20;
    private static final int SLIDER_Y = 40;
    private static final int SLIDER_W = 160;
    private static final int SLIDER_H = 12;
    private static final int HANDLE_W = 8;

    public GuiCreativeMotor(CreativeMotorTileEntity motor) {
        super(new CreativeMotorContainer(motor));
        this.motor = motor;
        this.motorX = motor.xCoord;
        this.motorY = motor.yCoord;
        this.motorZ = motor.zCoord;
        this.currentSpeed = motor.getConfiguredSpeed();
        this.xSize = 200;
        this.ySize = 80;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // Draw background
        GL11.glColor4f(1, 1, 1, 1);
        mc.getTextureManager().bindTexture(TEXTURE);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        // Draw slider track
        int sx = guiLeft + SLIDER_X;
        int sy = guiTop + SLIDER_Y;
        drawRect(sx, sy, sx + SLIDER_W, sy + SLIDER_H, 0xFF333333);

        // Draw handle
        float fraction = (currentSpeed + 256f) / 512f; // -256..256 → 0..1
        int hx = sx + (int) (fraction * (SLIDER_W - HANDLE_W));
        drawRect(hx, sy - 1, hx + HANDLE_W, sy + SLIDER_H + 1, 0xFFAAAAAA);

        // Draw center marker
        int cx = sx + SLIDER_W / 2;
        drawRect(cx - 1, sy, cx + 1, sy + SLIDER_H, 0xFF666666);

        // Direction labels
        drawCenteredString("CCW", sx + 12, sy + SLIDER_H + 4, 0xAAAAAA);
        drawCenteredString("CW", sx + SLIDER_W - 12, sy + SLIDER_H + 4, 0xAAAAAA);

        // RPM value
        drawCenteredString(currentSpeed + " RPM", guiLeft + xSize / 2, guiTop + 12, 0xFFFFFF);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Title
        fontRendererObj.drawString("Creative Motor", 8, 6, 0x404040);

        // Draw a little arrow for direction
        if (currentSpeed > 0) {
            fontRendererObj.drawString("→ CW", xSize - 40, ySize - 14, 0x00AA00);
        } else if (currentSpeed < 0) {
            fontRendererObj.drawString("← CCW", xSize - 45, ySize - 14, 0xAA0000);
        } else {
            fontRendererObj.drawString("0", xSize - 30, ySize - 14, 0x666666);
        }
    }

    @Override
    public void handleMouseInput() {
        int mouseX = Mouse.getEventX() * width / mc.displayWidth;
        int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
        int button = Mouse.getEventButton();
        boolean pressed = Mouse.getEventButtonState();
        int dwheel = Mouse.getEventDWheel();

        if (button == 0 && pressed) {
            // Check if click is within the slider area
            int sx = guiLeft + SLIDER_X;
            int sy = guiTop + SLIDER_Y;
            if (mouseX >= sx && mouseX <= sx + SLIDER_W
                    && mouseY >= sy - 2 && mouseY <= sy + SLIDER_H + 2) {
                dragging = true;
                updateSpeedFromMouse(mouseX);
                return;
            }
        }

        if (button == 0 && !pressed) {
            if (dragging) {
                dragging = false;
                sendSpeed();
                return;
            }
        }

        if (dragging) {
            updateSpeedFromMouse(mouseX);
            return;
        }

        // Mouse wheel scrolls by 8 RPM steps
        if (dwheel != 0) {
            int step = dwheel > 0 ? 8 : -8;
            currentSpeed = clamp(currentSpeed + step, -256, 256);
            sendSpeed();
        }

        super.handleMouseInput();
    }

    private void updateSpeedFromMouse(int mouseX) {
        int sx = guiLeft + SLIDER_X;
        int ex = sx + SLIDER_W - HANDLE_W;
        int px = mouseX - HANDLE_W / 2;
        px = Math.max(sx, Math.min(ex, px));
        float fraction = (float) (px - sx) / (SLIDER_W - HANDLE_W);
        currentSpeed = clamp((int) (fraction * 512 - 256), -256, 256);
    }

    private void sendSpeed() {
        PacketHandler.INSTANCE.sendToServer(
                new PacketMotorSpeed(motorX, motorY, motorZ, currentSpeed));
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private void drawCenteredString(String text, int x, int y, int color) {
        fontRendererObj.drawString(text, x - fontRendererObj.getStringWidth(text) / 2, y, color);
    }
}
