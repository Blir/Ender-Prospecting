package com.github.blir.enderprospecting.proxy;

import static com.github.blir.enderprospecting.EnderProspecting.*;

import java.util.HashMap;
import java.util.Map;

import com.github.blir.enderprospecting.entity.EntityEyeOfProspecting;
import com.github.blir.enderprospecting.entity.RenderEyeOfProspecting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.oredict.OreDictionary;

public class ClientProxy implements Proxy {

	@Override
	public void registerCustomModels() {
		
		String baseLoc = MODID + ":prospector";
		final ModelResourceLocation defaultModel = new ModelResourceLocation(baseLoc, "inventory");
		final Map<String, ModelResourceLocation> models = new HashMap<String, ModelResourceLocation>();
		
		models.put("oreCoal",     new ModelResourceLocation(baseLoc + "_coal"    ));
		models.put("oreGold",     new ModelResourceLocation(baseLoc + "_gold"    ));
		models.put("oreIron",     new ModelResourceLocation(baseLoc + "_iron"    ));
		models.put("oreLapis",    new ModelResourceLocation(baseLoc + "_lapis"   ));
		models.put("oreDiamond",  new ModelResourceLocation(baseLoc + "_diamond" ));
		models.put("oreRedstone", new ModelResourceLocation(baseLoc + "_redstone"));
		models.put("oreEmerald",  new ModelResourceLocation(baseLoc + "_emerald" ));
		models.put("oreQuartz",   new ModelResourceLocation(baseLoc + "_quartz"  ));
		models.put("oreCopper",   new ModelResourceLocation(baseLoc + "_copper"  ));
		models.put("oreTin",      new ModelResourceLocation(baseLoc + "_tin"     ));
		
		ModelResourceLocation[] toRegister = models.values().toArray(new ModelResourceLocation[11]);
		toRegister[10] = defaultModel;
		
		ModelLoader.registerItemVariants(prospector, toRegister);
		ModelLoader.setCustomMeshDefinition(prospector, new ItemMeshDefinition() {
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack) {
				String oreName = getOreName(stack);
				
				if (oreName == null)
					return defaultModel;
				
				ModelResourceLocation model = models.get(oreName);
				
				if (model == null)
					return defaultModel;
				
				return model;
			}
		});
		
		ModelLoader.setCustomModelResourceLocation(compass, 0, new ModelResourceLocation(baseLoc + "_compass"));
	}
	
	@Override
	public void registerRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityEyeOfProspecting.class, new IRenderFactory<EntityEyeOfProspecting>() {
			@Override
			public Render<? super EntityEyeOfProspecting> createRenderFor(RenderManager manager) {
				return new RenderEyeOfProspecting(manager);
			}
		});
	}
}
