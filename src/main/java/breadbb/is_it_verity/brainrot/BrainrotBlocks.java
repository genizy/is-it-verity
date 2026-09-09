package breadbb.is_it_verity.brainrot;

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
import breadbb.is_it_verity.obesity.ObesityBlock;
import breadbb.is_it_verity.sphere.VerityBlock;

public final class BrainrotBlocks {
	private static final List<Block> REGISTERED = new ArrayList<>();

	public static final ResourceKey<CreativeModeTab> TAB =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Is_it_verity.id("brainrot"));

	public static final Block SAHUR_BLOCK = registerTall("tung_tung_tung_sahur", 2);
	public static final Block SAHUR_HEAD_BLOCK = register("tung_tung_tung_sahur_head");
	public static final Block GUBBY_BLOCK = register("gubby");

	private BrainrotBlocks() {
	}

	public static Block register(String name) {
		Identifier id = Is_it_verity.id(name);

		Block block = Registry.register(
				BuiltInRegistries.BLOCK,
				id,
				new VerityBlock(defaultSettings().setId(ResourceKey.create(Registries.BLOCK, id)))
		);

		return withItem(id, block);
	}

	public static Block registerTall(String name, int height) {
		Identifier id = Is_it_verity.id(name);

		Block block = Registry.register(
				BuiltInRegistries.BLOCK,
				id,
				new ObesityBlock(defaultSettings().setId(ResourceKey.create(Registries.BLOCK, id)), 1, height, 1)
		);

		return withItem(id, block);
	}

	private static Block withItem(Identifier id, Block block) {
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
				.sound(SoundType.WOOD)
				.noOcclusion();
	}

	public static List<Block> registered() {
		return List.copyOf(REGISTERED);
	}

	public static void init() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB, FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.is_it_verity.brainrot"))
				.icon(() -> new ItemStack(SAHUR_HEAD_BLOCK))
				.displayItems((context, entries) -> {
					for (Block block : REGISTERED) {
						entries.accept(block);
					}
				})
				.build());
	}
}
