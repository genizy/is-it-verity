package breadbb.is_it_verity.gdfaces;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

import breadbb.is_it_verity.Is_it_verity;

public final class GdFaceBlocks {
	private static final List<Block> REGISTERED = new ArrayList<>();

	public static final ResourceKey<CreativeModeTab> TAB =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Is_it_verity.id("gd_faces"));

	public static final Block UNRATED = register("gd_unrated", 47);
	public static final Block AUTO = register("gd_auto", 74);
	public static final Block EASY = register("gd_easy", 75);
	public static final Block NORMAL = register("gd_normal", 72);
	public static final Block HARD = register("gd_hard", 74);
	public static final Block HARDER = register("gd_harder", 76);
	public static final Block INSANE = register("gd_insane", 79);
	public static final Block EASY_DEMON = register("gd_easy_demon", 89);
	public static final Block MEDIUM_DEMON = register("gd_medium_demon", 62);
	public static final Block HARD_DEMON = register("gd_hard_demon", 124);
	public static final Block INSANE_DEMON = register("gd_insane_demon", 72);
	public static final Block EXTREME_DEMON = register("gd_extreme_demon", 108);

	public static final BlockEntityType<GdFaceBlockEntity> BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			Is_it_verity.id("gd_face"),
			FabricBlockEntityTypeBuilder.create(GdFaceBlockEntity::new,
					REGISTERED.toArray(new Block[0])).build()
	);

	private GdFaceBlocks() {
	}

	public static Block register(String name, int soundTicks) {
		return register(name, soundTicks, defaultSettings());
	}

	public static Block register(String name, int soundTicks, BlockBehaviour.Properties settings) {
		Identifier id = Is_it_verity.id(name);
		SoundEvent sound = Registry.register(BuiltInRegistries.SOUND_EVENT, id,
				SoundEvent.createVariableRangeEvent(id));

		Block block = Registry.register(
				BuiltInRegistries.BLOCK,
				id,
				new GdFaceBlock(settings.setId(ResourceKey.create(Registries.BLOCK, id)), sound, soundTicks)
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
