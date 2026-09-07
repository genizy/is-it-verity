package breadbb.is_it_verity.client.mesh;

import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.MeshQuadCollection;
import net.fabricmc.fabric.api.client.renderer.v1.model.ModelStateHelper;

public record MeshGeometry(String textureSlot, float[] triangles) implements UnbakedGeometry {
	private static final int STRIDE = 24;

	@Override
	public QuadCollection bake(TextureSlots textures, ModelBaker baker, ModelState settings, ModelDebugName name) {
		Material material = textures.getMaterial(textureSlot);

		Material.Baked sprite = baker.materials().get(material, name);

		MutableMesh mesh = Renderer.get().mutableMesh();
		QuadEmitter emitter = mesh.emitter();
		emitter.pushTransform(ModelStateHelper.asQuadTransform(settings, baker.materials()));

		for (int offset = 0; offset + STRIDE <= triangles.length; offset += STRIDE) {
			corner(emitter, 0, offset);
			corner(emitter, 1, offset);
			corner(emitter, 2, offset + 8);
			corner(emitter, 3, offset + 16);

			emitter.cullFace(null)
					.materialBake(sprite, MutableQuadView.BAKE_NORMALIZED)
					.emit();
		}

		emitter.popTransform();
		return new MeshQuadCollection(mesh.immutableCopy());
	}

	private void corner(QuadEmitter emitter, int index, int offset) {
		emitter.pos(index, triangles[offset], triangles[offset + 1], triangles[offset + 2])
				.normal(index, triangles[offset + 3], triangles[offset + 4], triangles[offset + 5])
				.uv(index, triangles[offset + 6], triangles[offset + 7]);
	}
}
