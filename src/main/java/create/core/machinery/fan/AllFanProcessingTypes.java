package create.core.machinery.fan;

import create.core.machinery.recipe.ProcessingRecipe;
import create.core.machinery.recipe.ProcessingRecipeRegistry;
import create.core.machinery.recipe.RecipeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/**
 * Registry of all fan processing types. Each type is activated by a
 * different catalyst block and processes items differently.
 */
public final class AllFanProcessingTypes {

    /** Activated by water. Washes items (e.g., ore washing), extinguishes fire. */
    public static final FanProcessingType SPLASHING = new FanProcessingType() {
        @Override
        public boolean isValidAt(World world, int x, int y, int z) {
            Block block = world.getBlock(x, y, z);
            return block == Blocks.water || block == Blocks.flowing_water;
        }

        @Override
        public int getPriority() { return 400; }

        @Override
        public boolean canProcess(ItemStack stack) {
            return ProcessingRecipeRegistry.find(RecipeType.SPLASHING, wrapItem(stack)) != null;
        }

        @Override
        public List<ItemStack> process(ItemStack stack) {
            ProcessingRecipe recipe = ProcessingRecipeRegistry.find(RecipeType.SPLASHING, wrapItem(stack));
            if (recipe != null) {
                return recipe.rollResults();
            }
            return null;
        }

        @Override
        public void affectEntity(Entity entity) {
            entity.extinguish();
            if (entity instanceof EntityLivingBase) {
                // slight "washing" damage to endermen
            }
        }
    };

    /** Activated by soul sand. Haunts items (e.g., sand → soul sand). */
    public static final FanProcessingType HAUNTING = new FanProcessingType() {
        @Override
        public boolean isValidAt(World world, int x, int y, int z) {
            Block block = world.getBlock(x, y, z);
            return block == Blocks.soul_sand;
        }

        @Override
        public int getPriority() { return 300; }

        @Override
        public boolean canProcess(ItemStack stack) {
            return ProcessingRecipeRegistry.find(RecipeType.HAUNTING, wrapItem(stack)) != null;
        }

        @Override
        public List<ItemStack> process(ItemStack stack) {
            ProcessingRecipe recipe = ProcessingRecipeRegistry.find(RecipeType.HAUNTING, wrapItem(stack));
            if (recipe != null) {
                return recipe.rollResults();
            }
            return null;
        }

        @Override
        public void affectEntity(Entity entity) {
            if (entity instanceof EntityLivingBase) {
                ((EntityLivingBase) entity).addPotionEffect(
                        new PotionEffect(Potion.blindness.getId(), 100, 0));
                ((EntityLivingBase) entity).addPotionEffect(
                        new PotionEffect(Potion.moveSlowdown.getId(), 100, 0));
            }
        }
    };

    /** Activated by fire. Smokes food items (uses vanilla furnace recipes for food). */
    public static final FanProcessingType SMOKING = new FanProcessingType() {
        @Override
        public boolean isValidAt(World world, int x, int y, int z) {
            Block block = world.getBlock(x, y, z);
            return block == Blocks.fire;
        }

        @Override
        public int getPriority() { return 200; }

        @Override
        public boolean canProcess(ItemStack stack) {
            return FurnaceRecipes.smelting().getSmeltingResult(stack) != null;
        }

        @Override
        public List<ItemStack> process(ItemStack stack) {
            ItemStack result = FurnaceRecipes.smelting().getSmeltingResult(stack);
            if (result != null) {
                return Arrays.asList(result.copy());
            }
            return null;
        }

        @Override
        public void affectEntity(Entity entity) {
            entity.setFire(1);
        }
    };

    /** Activated by lava. Smelts items (uses vanilla furnace recipes). Higher damage. */
    public static final FanProcessingType BLASTING = new FanProcessingType() {
        @Override
        public boolean isValidAt(World world, int x, int y, int z) {
            Block block = world.getBlock(x, y, z);
            return block == Blocks.lava || block == Blocks.flowing_lava;
        }

        @Override
        public int getPriority() { return 100; }

        @Override
        public boolean canProcess(ItemStack stack) {
            return FurnaceRecipes.smelting().getSmeltingResult(stack) != null;
        }

        @Override
        public List<ItemStack> process(ItemStack stack) {
            ItemStack result = FurnaceRecipes.smelting().getSmeltingResult(stack);
            if (result != null) {
                return Arrays.asList(result.copy());
            }
            return null;
        }

        @Override
        public void affectEntity(Entity entity) {
            entity.setFire(5);
            entity.attackEntityFrom(DamageSource.inFire, 2.0f);
        }
    };

    /** All types sorted by priority descending (highest checked first). */
    public static final List<FanProcessingType> SORTED = sortByPriority();

    private static List<FanProcessingType> sortByPriority() {
        List<FanProcessingType> types = Arrays.asList(SPLASHING, HAUNTING, SMOKING, BLASTING);
        types.sort(Comparator.comparingInt(FanProcessingType::getPriority).reversed());
        return types;
    }

    /** Find the first processing type active at the given position, or null. */
    public static FanProcessingType getAt(World world, int x, int y, int z) {
        for (FanProcessingType type : SORTED) {
            if (type.isValidAt(world, x, y, z)) {
                return type;
            }
        }
        return null;
    }

    /** Wrap a single ItemStack in an IInventory for recipe lookup. */
    public static IInventory wrapItem(ItemStack stack) {
        InventoryBasic inv = new InventoryBasic("fan", true, 1);
        inv.setInventorySlotContents(0, stack);
        return inv;
    }
}
