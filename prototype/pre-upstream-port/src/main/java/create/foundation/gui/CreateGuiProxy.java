package create.foundation.gui;

import cpw.mods.fml.common.network.IGuiHandler;
import create.core.kinetic.source.CreativeMotorContainer;
import create.core.kinetic.source.CreativeMotorTileEntity;
import create.core.kinetic.source.GuiCreativeMotor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Handles GUI opening for Create blocks.
 *
 * <p>GUI IDs:</p>
 * <ul>
 *   <li>0 = Creative Motor speed control</li>
 * </ul>
 */
public class CreateGuiProxy implements IGuiHandler {

    public static final int GUI_CREATIVE_MOTOR = 0;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world,
                                       int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        switch (id) {
            case GUI_CREATIVE_MOTOR:
                if (te instanceof CreativeMotorTileEntity) {
                    return new CreativeMotorContainer((CreativeMotorTileEntity) te);
                }
                return null;
            default:
                return null;
        }
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world,
                                       int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        switch (id) {
            case GUI_CREATIVE_MOTOR:
                if (te instanceof CreativeMotorTileEntity) {
                    return new GuiCreativeMotor((CreativeMotorTileEntity) te);
                }
                return null;
            default:
                return null;
        }
    }
}
