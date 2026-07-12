package create.core.machinery.recipe;

import java.util.Random;
import net.minecraft.item.ItemStack;

/** A recipe output with a per-item chance. Each unit in the stack rolls independently. */
public class ProcessingOutput {

    public static final ProcessingOutput EMPTY = new ProcessingOutput(null, 0);

    private static final Random R = new Random();

    private final ItemStack stack;
    private final float chance;

    public ProcessingOutput(ItemStack stack, float chance) {
        this.stack = stack != null ? stack.copy() : null;
        this.chance = Math.max(0, Math.min(1, chance));
    }

    public ItemStack getStack() {
        return stack != null ? stack.copy() : null;
    }

    public float getChance() {
        return chance;
    }

    /** Roll per unit in the stack. Returns the resulting stack, or null if all rolls failed. */
    public ItemStack rollOutput() {
        if (stack == null || stack.stackSize == 0) return null;

        ItemStack result = stack.copy();
        int count = result.stackSize;
        for (int i = 0; i < count; i++) {
            if (R.nextFloat() > chance) {
                result.stackSize--;
            }
        }
        return result.stackSize > 0 ? result : null;
    }
}
