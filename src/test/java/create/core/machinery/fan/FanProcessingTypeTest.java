package create.core.machinery.fan;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FanProcessingTypeTest {

    @Test
    void splashing_hasHighestPriority() {
        assertEquals(400, AllFanProcessingTypes.SPLASHING.getPriority());
    }

    @Test
    void haunting_hasSecondHighestPriority() {
        assertEquals(300, AllFanProcessingTypes.HAUNTING.getPriority());
    }

    @Test
    void smoking_hasThirdHighestPriority() {
        assertEquals(200, AllFanProcessingTypes.SMOKING.getPriority());
    }

    @Test
    void blasting_hasLowestPriority() {
        assertEquals(100, AllFanProcessingTypes.BLASTING.getPriority());
    }

    @Test
    void sorted_isSortedByPriorityDescending() {
        int last = Integer.MAX_VALUE;
        for (FanProcessingType type : AllFanProcessingTypes.SORTED) {
            assertTrue(type.getPriority() <= last,
                    "expected descending order, but " + type.getPriority()
                            + " > " + last);
            last = type.getPriority();
        }
    }

    @Test
    void sorted_containsAllFourTypes() {
        assertEquals(4, AllFanProcessingTypes.SORTED.size());
    }

    @Test
    void wrapItem_createsSingleSlotInventory() {
        net.minecraft.item.Item item = new net.minecraft.item.Item();
        item.setUnlocalizedName("testItem");
        net.minecraft.item.ItemStack stack = new net.minecraft.item.ItemStack(item, 1);
        net.minecraft.inventory.IInventory inv = AllFanProcessingTypes.wrapItem(stack);
        assertEquals(1, inv.getSizeInventory());
        assertEquals(item, inv.getStackInSlot(0).getItem());
    }

    @Test
    void splashing_cannotProcess_withoutRecipes() {
        net.minecraft.item.Item item = new net.minecraft.item.Item();
        item.setUnlocalizedName("testItem");
        net.minecraft.item.ItemStack stack = new net.minecraft.item.ItemStack(item, 1);
        // No splashing recipes registered → canProcess returns false
        assertFalse(AllFanProcessingTypes.SPLASHING.canProcess(stack));
    }
}
