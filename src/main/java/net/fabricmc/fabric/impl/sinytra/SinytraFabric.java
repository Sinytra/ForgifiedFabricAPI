package net.fabricmc.fabric.impl.sinytra;

import net.minecraft.resource.DirectoryResourcePack;
import net.minecraft.resource.ResourcePackProfile;

import net.minecraft.resource.ResourcePackSource;
import net.minecraft.text.Text;

import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("fabric_api")
public class SinytraFabric {
	public SinytraFabric() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.NORMAL, false, AddPackFindersEvent.class, this::addPackFinder);
	}

	/**
	 * Some mods (ARRP) may use a resource pack named {@code fabric} to sort their own packs around. So just provide it.
	 */
	private void addPackFinder(AddPackFindersEvent event) {
		event.addRepositorySource(profileAdder -> profileAdder.accept(
				ResourcePackProfile.create(
						"fabric",
						Text.of("fabric"),
						true,
						name -> new DirectoryResourcePack(name, ModList.get().getModContainerById("fabric_api").get().getModInfo().getOwningFile()
								.getFile().findResource("dummyrp"), true),
						event.getPackType(),
						ResourcePackProfile.InsertionPosition.TOP,
						ResourcePackSource.BUILTIN
				)
		));
	}
}
