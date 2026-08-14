package breadbb.is_it_verity.emoji;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

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

public final class EmojiBlocks {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String INDEX = "/assets/" + Is_it_verity.MOD_ID + "/emoji_index.json";

	private EmojiBlocks() {
	}

	public static void init() {
		JsonObject index = readIndex();

		if (index == null) {
			return;
		}

		int count = 0;

		for (JsonElement element : index.getAsJsonArray("categories")) {
			JsonObject category = element.getAsJsonObject();
			String categoryId = category.get("id").getAsString();
			JsonArray names = category.getAsJsonArray("blocks");
			List<Block> blocks = new ArrayList<>(names.size());

			for (JsonElement name : names) {
				blocks.add(register(name.getAsString()));
			}

			if (blocks.isEmpty()) {
				continue;
			}

			registerTab(categoryId, List.copyOf(blocks));
			count += blocks.size();
		}

		LOGGER.info("Registered {} emoji blocks", count);
	}

	private static Block register(String name) {
		Identifier id = Is_it_verity.id(name);

		Block block = Registry.register(
				BuiltInRegistries.BLOCK,
				id,
				new Block(settings().setId(ResourceKey.create(Registries.BLOCK, id)))
		);

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
		Registry.register(
				BuiltInRegistries.ITEM,
				itemKey,
				new BlockItem(block, new Item.Properties().setId(itemKey))
		);

		return block;
	}

	private static void registerTab(String categoryId, List<Block> blocks) {
		String key = "emoji_" + categoryId;
		ResourceKey<CreativeModeTab> tabKey =
				ResourceKey.create(Registries.CREATIVE_MODE_TAB, Is_it_verity.id(key));

		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, tabKey, FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup." + Is_it_verity.MOD_ID + "." + key))
				.icon(() -> new ItemStack(blocks.get(0)))
				.displayItems((context, entries) -> {
					for (Block block : blocks) {
						entries.accept(block);
					}
				})
				.build());
	}

	private static BlockBehaviour.Properties settings() {
		return BlockBehaviour.Properties.of()
				.strength(1.0F)
				.sound(SoundType.STONE);
	}

	private static JsonObject readIndex() {
		try (InputStream stream = EmojiBlocks.class.getResourceAsStream(INDEX)) {

			try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				return JsonParser.parseReader(reader).getAsJsonObject();
			}
		} catch (Exception e) {
			throw new IllegalStateException("Could not read " + INDEX, e);
		}
	}
}
