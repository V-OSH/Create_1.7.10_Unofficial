package create.core.kinetic.source;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

/**
 * Empty container for the Creative Motor GUI — no items, just holds
 * the TE reference for server-side sync.
 */
public class CreativeMotorContainer extends Container {

    public final CreativeMotorTileEntity motor;

    public CreativeMotorContainer(CreativeMotorTileEntity motor) {
        this.motor = motor;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return motor.getWorldObj().getTileEntity(motor.xCoord, motor.yCoord, motor.zCoord) == motor
                && player.getDistanceSq(motor.xCoord + 0.5, motor.yCoord + 0.5, motor.zCoord + 0.5) <= 64.0;
    }
}
