/*
 * Entry-point structure adapted from Create 6.0.8's Create class for Forge 1.7.10.
 * Create is Copyright (c) simibubi and contributors, licensed under the MIT License.
 */
package com.simibubi.create;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Create.ID, name = Create.NAME, version = Create.VERSION)
public class Create {

    public static final String ID = "create";
    public static final String NAME = "Create 1.7.10 Unofficial";
    public static final String VERSION = Tags.VERSION;
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Initializing the Create 6.0.8 reverse-port foundation");
        AllBlocks.register();
        AllBlockEntityTypes.register();
    }
}
