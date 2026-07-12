/*
 * Registry shape adapted from Create 6.0.8's AllBlocks.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create;

import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.motor.CreativeMotorBlock;
import com.simibubi.create.foundation.utility.legacy.block.CreativeMotorItemBlock;
import com.simibubi.create.foundation.utility.legacy.block.ShaftItemBlock;
import com.simibubi.create.foundation.utility.legacy.block.CogWheelItemBlock;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public final class AllBlocks {

    public static final ShaftBlock SHAFT = new ShaftBlock();
    public static final CogWheelBlock COGWHEEL = new CogWheelBlock();
    public static final CreativeMotorBlock CREATIVE_MOTOR = new CreativeMotorBlock();

    private static boolean registered;

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        GameRegistry.registerBlock(SHAFT, ShaftItemBlock.class, "shaft");
        GameRegistry.registerBlock(COGWHEEL, CogWheelItemBlock.class, "cogwheel");
        GameRegistry.registerBlock(CREATIVE_MOTOR, CreativeMotorItemBlock.class, "creative_motor");
        SHAFT.setCreativeTab(AllCreativeModeTabs.BASE);
        COGWHEEL.setCreativeTab(AllCreativeModeTabs.BASE);
        CREATIVE_MOTOR.setCreativeTab(AllCreativeModeTabs.BASE);
        GameRegistry.addShapelessRecipe(new ItemStack(COGWHEEL), new ItemStack(SHAFT),
            new ItemStack(Blocks.planks, 1, OreDictionary.WILDCARD_VALUE));
    }

    private AllBlocks() {}
}
