package breadbb.is_it_verity.client.sphere;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
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
	public static final String HORN_SLOT = "horn";

	private static final int DEFAULT_RINGS = 12;
	private static final int DEFAULT_SEGMENTS = 24;
	private static final float DEFAULT_RADIUS = 0.5F;
	private static final float DEFAULT_OUTLINE = 0.0F;
	private static final float DEFAULT_TILT = 25.0F;
	private static final float DEFAULT_LENGTH = 0.15F;
	private static final float DEFAULT_WIDTH = 0.1F;

	@Override
	public UnbakedModel deserialize(JsonObject json, JsonDeserializationContext context) {
		int rings = Math.max(2, json.has("rings") ? json.get("rings").getAsInt() : DEFAULT_RINGS);
		int segments = Math.max(3, json.has("segments") ? json.get("segments").getAsInt() : DEFAULT_SEGMENTS);
		float radius = Math.clamp(json.has("radius") ? json.get("radius").getAsFloat() : DEFAULT_RADIUS,
				0.05F, 0.5F);
		float outline = Math.clamp(json.has("outline") ? json.get("outline").getAsFloat() : DEFAULT_OUTLINE,
				0.0F, radius * 0.5F);
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
				new SphereGeometry(TEXTURE_SLOT, HORN_SLOT, rings, segments, radius, outline, horns(json)),
				guiLight,
				ambientOcclusion,
				transforms,
				textures,
				parentLocation
		);
	}

	private static List<SphereGeometry.Horn> horns(JsonObject json) {
		List<SphereGeometry.Horn> horns = new ArrayList<>();

		if (!json.has("horns")) {
			return List.copyOf(horns);
		}

		for (JsonElement element : GsonHelper.getAsJsonArray(json, "horns")) {
			JsonObject horn = element.getAsJsonObject();
			float tilt = GsonHelper.getAsFloat(horn, "tilt", DEFAULT_TILT);

			horns.add(new SphereGeometry.Horn(
					GsonHelper.getAsFloat(horn, "around", 0.0F),
					tilt,
					GsonHelper.getAsFloat(horn, "aim", tilt),
					Math.clamp(GsonHelper.getAsFloat(horn, "length", DEFAULT_LENGTH), 0.0F, 0.5F),
					Math.clamp(GsonHelper.getAsFloat(horn, "width", DEFAULT_WIDTH), 0.01F, 0.5F)
			));
		}

		return List.copyOf(horns);
	}
}
