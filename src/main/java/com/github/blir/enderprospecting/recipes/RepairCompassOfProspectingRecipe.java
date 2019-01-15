package com.github.blir.enderprospecting.recipes;

import static com.github.blir.enderprospecting.EnderProspecting.*;
import com.google.gson.JsonObject;

import net.minecraft.init.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class RepairCompassOfProspectingRecipe extends EPShapelessRecipe {

	public RepairCompassOfProspectingRecipe() {
		super((epOre, oreDictOre) -> {
			return createProspectorCompassForOre(epOre);
		}, stack -> {
			return stack.getItem() == compass ? MatchType.EP : MatchType.NONE;
		}, stack -> {
			return stack.getItem() == Items.BLAZE_POWDER ? MatchType.REG : MatchType.NONE;
		});
	}

	public static class Factory implements IRecipeFactory {
		@Override
		public IRecipe parse(JsonContext context, JsonObject json) {
			return new RepairCompassOfProspectingRecipe();
		}
	}
}
