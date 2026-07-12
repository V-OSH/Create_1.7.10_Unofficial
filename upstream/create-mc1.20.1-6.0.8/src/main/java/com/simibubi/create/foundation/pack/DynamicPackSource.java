package com.simibubi.create.foundation.pack;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

// TODO - Move into catnip
public record DynamicPackSource(String packId, PackType packType, Pack.Position packPosition,
								PackResources packResources) implements RepositorySource {
	@Override
	public void loadPacks(@NotNull Consumer<Pack> onLoad) {
		onLoad.accept(Pack.readMetaAndCreate(packId, Component.literal(packId), true, id -> packResources, packType, packPosition, PackSource.BUILT_IN));
	}
}
