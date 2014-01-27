package com.github.blir.enderprospecting;

import net.minecraft.client.renderer.entity.RenderSnowball;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {
	@Override
	public void registerRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityEyeOfProspecting.class, new RenderSnowball(EnderProspecting.prospector));
	}
}
