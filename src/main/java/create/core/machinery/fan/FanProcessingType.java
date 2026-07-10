package create.core.machinery.fan;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * A processing type for the Encased Fan's air current.
 * Each type is activated by a different catalyst block (water, lava, fire, soul sand).
 */
public interface FanProcessingType {

    /** Check whether this processing type is active at the given position. */
    boolean isValidAt(World world, int x, int y, int z);

    /** Higher priority types are checked first. */
    int getPriority();

    /** Whether the given item stack can be processed by this type. */
    boolean canProcess(ItemStack stack);

    /** Process the stack and return results. May return null or empty list. */
    List<ItemStack> process(ItemStack stack);

    /** Affect a non-item entity in the air current. */
    void affectEntity(Entity entity);
}
