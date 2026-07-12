/*
 * Registry shape adapted from Create 6.0.8's AllCreativeModeTabs.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public final class AllCreativeModeTabs {

    public static final CreativeTabs BASE = new CreativeTabs(Create.ID) {
        @Override
        public Item getTabIconItem() {
            return Item.getItemFromBlock(AllBlocks.SHAFT);
        }
    };

    private AllCreativeModeTabs() {}
}
