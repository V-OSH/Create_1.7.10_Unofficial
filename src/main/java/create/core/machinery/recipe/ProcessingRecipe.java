package create.core.machinery.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * Abstract base for all Create processing recipes.
 * Does NOT implement vanilla {@code IRecipe} — uses our own {@link ProcessingRecipeRegistry}.
 */
public abstract class ProcessingRecipe {

    protected final List<ItemStack> ingredients;
    protected final List<ProcessingOutput> results;
    protected final int processingDuration;

    protected ProcessingRecipe(List<ItemStack> ingredients, List<ProcessingOutput> results, int processingDuration) {
        this.ingredients = new ArrayList<>(ingredients);
        this.results = new ArrayList<>(results);
        this.processingDuration = processingDuration;
    }

    /** Convenience constructor for single-ingredient recipes. */
    protected ProcessingRecipe(ItemStack ingredient, List<ProcessingOutput> results, int processingDuration) {
        this(Arrays.asList(ingredient), results, processingDuration);
    }

    public abstract RecipeType getRecipeType();

    public int getProcessingDuration() {
        return processingDuration;
    }

    public List<ItemStack> getIngredients() {
        return ingredients;
    }

    public List<ProcessingOutput> getResults() {
        return results;
    }

    /**
     * Check whether the given inventory satisfies all ingredient requirements.
     * Default implementation matches ingredient slot 0 against inventory slot 0.
     */
    public boolean matches(IInventory inv) {
        if (inv == null || inv.getSizeInventory() == 0) return false;
        if (ingredients.isEmpty()) return false;

        ItemStack input = inv.getStackInSlot(0);
        if (input == null) return false;

        ItemStack ingredient = ingredients.get(0);
        return input.stackSize >= ingredient.stackSize
                && input.getItem() == ingredient.getItem()
                && (ingredient.getItemDamage() == 32767 || input.getItemDamage() == ingredient.getItemDamage());
    }

    /** Roll all outputs and collect non-null results. */
    public List<ItemStack> rollResults() {
        List<ItemStack> rolled = new ArrayList<>();
        for (ProcessingOutput output : results) {
            ItemStack result = output.rollOutput();
            if (result != null) {
                rolled.add(result);
            }
        }
        return rolled;
    }
}
