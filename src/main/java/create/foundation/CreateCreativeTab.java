package create.foundation;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Create's creative mode tab.
 */
public final class CreateCreativeTab extends CreativeTabs {

    public static final CreateCreativeTab TAB = new CreateCreativeTab();

    private CreateCreativeTab() {
        super("create");
    }

    @Override
    public ItemStack getIconItemStack() {
        return new ItemStack(AllBlocks.COGWHEEL);
    }

    @Override
    public Item getTabIconItem() {
        return Item.getItemFromBlock(AllBlocks.COGWHEEL);
    }
}
