package breadbb.is_it_verity;

import net.minecraft.resources.Identifier;

import net.fabricmc.api.ModInitializer;

import breadbb.is_it_verity.emoji.EmojiBlocks;
import breadbb.is_it_verity.gdfaces.GdFaceBlocks;
import breadbb.is_it_verity.player.PlayerBlocks;
import breadbb.is_it_verity.sphere.VerityBlocks;
import breadbb.is_it_verity.wordle.WordleBlocks;

public class Is_it_verity implements ModInitializer {
	public static final String MOD_ID = "is_it_verity";

	@Override
	public void onInitialize() {
		VerityBlocks.init();
		EmojiBlocks.init();
		PlayerBlocks.init();
		WordleBlocks.init();
		GdFaceBlocks.init();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
