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

import net.minecraft.world.level.material.PushReaction;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;

import breadbb.is_it_verity.Is_it_verity;
import breadbb.is_it_verity.obesity.ObesityBlock;
import breadbb.is_it_verity.obesity.ObesityFillerBlock;

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
	public static final Block FREAKITY_BLOCK = register("freakity");
	public static final Block MOGGITY_BLOCK = register("moggity");
	public static final Block POOPITY_BLOCK = register("poopity");
	public static final Block GAYITY_BLOCK = register("gayity");
	public static final Block OBSURITY_BLOCK = register("obsurity");
	public static final Block POINTY_BLOCK = register("pointy");
	public static final Block SQUARITY_BLOCK = register("squarity");
	public static final Block OBESITY_FILLER = registerFiller("obesity_filler");
	public static final Block OBESITY_BLOCK = registerObesity("obesity", 1);
	public static final Block MEDIUM_OBESITY_BLOCK = registerObesity("medium_obesity", 2);
	public static final Block BIG_OBESITY_BLOCK = registerObesity("big_obesity", 3);
	public static final Block LARGE_OBESITY_BLOCK = registerObesity("large_obesity", 4);
	public static final Block OBESE_OBESITY_BLOCK = registerObesity("obese_obesity", 5);
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

	public static Block registerObesity(String name, int size) {
		Identifier id = Is_it_verity.id(name);

		Block block = Registry.register(
				BuiltInRegistries.BLOCK,
				id,
				new ObesityBlock(defaultSettings().setId(ResourceKey.create(Registries.BLOCK, id)), size)
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

	public static Block registerFiller(String name) {
		Identifier id = Is_it_verity.id(name);

		return Registry.register(
				BuiltInRegistries.BLOCK,
				id,
				new ObesityFillerBlock(BlockBehaviour.Properties.of()
						.strength(1.0F)
						.sound(SoundType.STONE)
						.noOcclusion()
						.noTerrainParticles()
						.isSuffocating((state, level, pos) -> false)
						.isViewBlocking((state, level, pos) -> false)
						.noLootTable()
						.pushReaction(PushReaction.BLOCK)
						.setId(ResourceKey.create(Registries.BLOCK, id)))
		);
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
