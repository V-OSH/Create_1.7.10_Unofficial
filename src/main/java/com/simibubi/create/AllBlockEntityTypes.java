/*
 * Registry shape adapted from Create 6.0.8's AllBlockEntityTypes.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import cpw.mods.fml.common.registry.GameRegistry;

public final class AllBlockEntityTypes {

    private static boolean registered;

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        GameRegistry.registerTileEntity(KineticBlockEntity.class, Create.ID + ":simple_kinetic");
    }

    private AllBlockEntityTypes() {}
}
