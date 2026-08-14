package breadbb.is_it_verity.client.sphere;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;

public class SphereModelDeserializer implements UnbakedModelDeserializer {
	public static final String TEXTURE_SLOT = "sphere";

	private static final int DEFAULT_RINGS = 12;
	private static final int DEFAULT_SEGMENTS = 24;
	private static final float DEFAULT_RADIUS = 0.5F;

	@Override
	public UnbakedModel deserialize(JsonObject json, JsonDeserializationContext context) {
		int rings = Math.max(2, json.has("rings") ? json.get("rings").getAsInt() : DEFAULT_RINGS);
		int segments = Math.max(3, json.has("segments") ? json.get("segments").getAsInt() : DEFAULT_SEGMENTS);
		float radius = Math.clamp(json.has("radius") ? json.get("radius").getAsFloat() : DEFAULT_RADIUS,
				0.05F, 0.5F);
		Boolean ambientOcclusion = json.has("ambientocclusion")
				? GsonHelper.getAsBoolean(json, "ambientocclusion")
				: Boolean.FALSE;

		UnbakedModel.GuiLight guiLight = json.has("gui_light")
				? UnbakedModel.GuiLight.getByName(GsonHelper.getAsString(json, "gui_light"))
				: null;

		ItemTransforms transforms = json.has("display")
				? context.deserialize(GsonHelper.getAsJsonObject(json, "display"), ItemTransforms.class)
				: null;

		TextureSlots.Data textures = json.has("textures")
				? TextureSlots.parseTextureMap(GsonHelper.getAsJsonObject(json, "textures"))
				: TextureSlots.Data.EMPTY;

		String parent = GsonHelper.getAsString(json, "parent", "");
		Identifier parentLocation = parent.isEmpty() ? null : Identifier.parse(parent);

		return new CuboidModel(
				new SphereGeometry(TEXTURE_SLOT, rings, segments, radius),
				guiLight,
				ambientOcclusion,
				transforms,
				textures,
				parentLocation
		);
	}
}
