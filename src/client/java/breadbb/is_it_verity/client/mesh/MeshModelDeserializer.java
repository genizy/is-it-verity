package breadbb.is_it_verity.client.mesh;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;

public class MeshModelDeserializer implements UnbakedModelDeserializer {
	public static final String TEXTURE_SLOT = "skin";

	@Override
	public UnbakedModel deserialize(JsonObject json, JsonDeserializationContext context) {
		JsonArray numbers = GsonHelper.getAsJsonArray(json, "mesh");
		float[] triangles = new float[numbers.size()];

		for (int index = 0; index < numbers.size(); index++) {
			triangles[index] = numbers.get(index).getAsFloat();
		}

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
				new MeshGeometry(TEXTURE_SLOT, triangles),
				guiLight,
				ambientOcclusion,
				transforms,
				textures,
				parentLocation
		);
	}
}
