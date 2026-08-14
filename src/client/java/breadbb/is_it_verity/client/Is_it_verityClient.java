package breadbb.is_it_verity.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;

import breadbb.is_it_verity.Is_it_verity;
import breadbb.is_it_verity.client.sphere.SphereModelDeserializer;

public class Is_it_verityClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		UnbakedModelDeserializer.register(Is_it_verity.id("sphere"), new SphereModelDeserializer());
	}
}
