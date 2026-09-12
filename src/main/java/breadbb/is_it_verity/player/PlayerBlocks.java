package breadbb.is_it_verity.player;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;

import breadbb.is_it_verity.Is_it_verity;

public final class PlayerBlocks {
	private static final List<Block> REGISTERED = new ArrayList<>();

	public static final ResourceKey<CreativeModeTab> TAB =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Is_it_verity.id("player_blocks"));

	// player heads -------------------
	public static final Block CUBIX_TUBE = register("cubix_tube");
	public static final Block NATHANLIVE = register("nathanlive");
	public static final Block MRTICKLES = register("mrtickles");
	public static final Block RIVVERSS = register("rivverss");
	public static final Block NINE2K16 = register("nine2k16");
	public static final Block ACOLOTLGAMES = register("acolotlgames");
	public static final Block SPINKLEDORB = register("spinkledorb");
	public static final Block WOOSHMC = register("wooshmc");
	public static final Block MAGMAVR = register("magmavr");
	public static final Block ZAPRYN = register("zapryn");
	public static final Block SEARAVIOLI = register("searavioli");
	public static final Block ZENITHZV = register("zenithzv");
	public static final Block ARKZ_ONNOKIA = register("arkz_onnokia");
	public static final Block STARIAZ = register("stariaz");
	public static final Block BABONCIA5 = register("baboncia5");
	public static final Block FIVESYLVEON = register("5sylveon");
 	// ---------------
	private PlayerBlocks() {
	}

	public static Block register(String name) {
		return register(name, defaultSettings());
	}

	public static Block register(String name, BlockBehaviour.Properties settings) {
		Identifier id = Is_it_verity.id(name);

		Block block = Registry.register(
				BuiltInRegistries.BLOCK,
				id,
				new Block(settings.setId(ResourceKey.create(Registries.BLOCK, id)))
		);

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
		Registry.register(
				BuiltInRegistries.ITEM,
				itemKey,
				new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix())
		);

		REGISTERED.add(block);
		return block;
	}

	public static BlockBehaviour.Properties defaultSettings() {
		return BlockBehaviour.Properties.of()
				.strength(1.0F)
				.sound(SoundType.STONE);
	}

	public static List<Block> registered() {
		return List.copyOf(REGISTERED);
	}

	public static void init() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB, FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.is_it_verity.player_blocks"))
				.icon(() -> new ItemStack(CUBIX_TUBE))
				.displayItems((context, entries) -> {
					for (Block head : REGISTERED) {
						entries.accept(head);
					}
				})
				.build());
	}
}
