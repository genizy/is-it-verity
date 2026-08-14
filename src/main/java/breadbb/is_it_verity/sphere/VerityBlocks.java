package breadbb.is_it_verity.sphere;

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

public final class VerityBlocks {
	private static final List<Block> REGISTERED = new ArrayList<>();

	public static final ResourceKey<CreativeModeTab> TAB =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Is_it_verity.id("is_it_verity"));

	// ─── veritys ─────────────────────────────────────────────────────────────
	public static final Block VERITY_BLOCK = register("verity");
	public static final Block LOVITY_BLOCK = register("lovity");
	public static final Block FALSITY_BLOCK = register("falsity");
	public static final Block CRUELTY_BLOCK = register("cruelty");
	public static final Block BLACK_VERITY_BLOCK = register("black_verity");
	public static final Block GREEN_VERITY_BLOCK = register("green_verity");
	public static final Block QUACKITY_BLOCK = register("quackity");
	// ─────────────────────────────────────────────────────────────────────────

	private VerityBlocks() {
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
				.title(Component.translatable("itemGroup.is_it_verity.is_it_verity"))
				.icon(() -> new ItemStack(VERITY_BLOCK))
				.displayItems((context, entries) -> {
					for (Block verity : REGISTERED) {
						entries.accept(verity);
					}
				})
				.build());
	}
}
