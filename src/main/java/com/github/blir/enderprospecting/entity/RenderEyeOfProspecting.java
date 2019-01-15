package com.github.blir.enderprospecting.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RenderEyeOfProspecting extends RenderSnowball {

	public RenderEyeOfProspecting(RenderManager manager) {
		super(manager, null, Minecraft.getMinecraft().getRenderItem());
	}

	@Override
	public ItemStack getStackToRender(Entity entity)
    {
        return ((EntityEyeOfProspecting) entity).createItemEyeOfProspecting();
    }
}
