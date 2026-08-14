package breadbb.is_it_verity.wordle;

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

public final class WordleBlocks {
	public static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	public static final String[] STATES = {"guess", "wrong", "correct", "elsewhere"};

	private static final List<Block> REGISTERED = new ArrayList<>();

	public static final ResourceKey<CreativeModeTab> TAB =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Is_it_verity.id("wordle"));

	private WordleBlocks() {
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
		Block correctA = null;

		for (String state : STATES) {
			for (int i = 0; i < CHARACTERS.length(); i++) {
				Block block = register(state + "_" + Character.toLowerCase(CHARACTERS.charAt(i)));

				if (correctA == null && state.equals("correct")) {
					correctA = block;
				}
			}
		}

		Block icon = correctA;

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB, FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.is_it_verity.wordle"))
				.icon(() -> new ItemStack(icon))
				.displayItems((context, entries) -> {
					for (Block block : REGISTERED) {
						entries.accept(block);
					}
				})
				.build());
	}
}
