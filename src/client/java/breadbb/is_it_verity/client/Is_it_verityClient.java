package breadbb.is_it_verity.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;

import breadbb.is_it_verity.Is_it_verity;
import breadbb.is_it_verity.client.gdfaces.GdFaceRenderer;
import breadbb.is_it_verity.client.mesh.MeshModelDeserializer;
import breadbb.is_it_verity.client.sphere.SphereModelDeserializer;
import breadbb.is_it_verity.gdfaces.GdFaceBlocks;

public class Is_it_verityClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		UnbakedModelDeserializer.register(Is_it_verity.id("sphere"), new SphereModelDeserializer());
		UnbakedModelDeserializer.register(Is_it_verity.id("mesh"), new MeshModelDeserializer());
		BlockEntityRendererRegistry.register(GdFaceBlocks.BLOCK_ENTITY, GdFaceRenderer::new);
	}
}
