package create.core.machinery.recipe;

import java.util.List;
import net.minecraft.item.ItemStack;

/** Recipe for the Millstone. Single input, up to 4 outputs with per-item chance. */
public class MillingRecipe extends ProcessingRecipe {

    public MillingRecipe(ItemStack ingredient, List<ProcessingOutput> results, int processingDuration) {
        super(ingredient, results, processingDuration);
    }

    @Override
    public RecipeType getRecipeType() {
        return RecipeType.MILLING;
    }
}
