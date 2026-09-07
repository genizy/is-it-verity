package breadbb.is_it_verity.gdfaces;

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
import breadbb.is_it_verity.sphere.VerityBlock;

public final class GdFaceBlocks {
	private static final List<Block> REGISTERED = new ArrayList<>();

	public static final ResourceKey<CreativeModeTab> TAB =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Is_it_verity.id("gd_faces"));

	public static final Block UNRATED = register("gd_unrated");
	public static final Block AUTO = register("gd_auto");
	public static final Block EASY = register("gd_easy");
	public static final Block NORMAL = register("gd_normal");
	public static final Block HARD = register("gd_hard");
	public static final Block HARDER = register("gd_harder");
	public static final Block INSANE = register("gd_insane");
	public static final Block EASY_DEMON = register("gd_easy_demon");
	public static final Block MEDIUM_DEMON = register("gd_medium_demon");
	public static final Block HARD_DEMON = register("gd_hard_demon");
	public static final Block INSANE_DEMON = register("gd_insane_demon");
	public static final Block EXTREME_DEMON = register("gd_extreme_demon");

	private GdFaceBlocks() {
	}

	public static Block register(String name) {
		return register(name, defaultSettings());
	}

	public static Block register(String name, BlockBehaviour.Properties settings) {
		Identifier id = Is_it_verity.id(name);

		Block block = Registry.register(
				BuiltInRegistries.BLOCK,
				id,
				new VerityBlock(settings.setId(ResourceKey.create(Registries.BLOCK, id)))
		);

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
		Registry.register(
				BuiltInRegistries.ITEM,
				itemKey,
				new BlockItem(block, new Item.Properties().setId(itemKey))
		);

		REGISTERED.add(block);
		return block;
	}

	public static BlockBehaviour.Properties defaultSettings() {
		return BlockBehaviour.Properties.of()
				.strength(1.0F)
				.sound(SoundType.STONE)
				.noOcclusion();
	}

	public static List<Block> registered() {
		return List.copyOf(REGISTERED);
	}

	public static void init() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB, FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.is_it_verity.gd_faces"))
				.icon(() -> new ItemStack(EXTREME_DEMON))
				.displayItems((context, entries) -> {
					for (Block face : REGISTERED) {
						entries.accept(face);
					}
				})
				.build());
	}
}
