package com.github.blir.enderprospecting.recipes;

import static com.github.blir.enderprospecting.EnderProspecting.*;
import com.google.gson.JsonObject;

import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class TuneEyeOfProspectingRecipe extends EPShapelessRecipe {

	public TuneEyeOfProspectingRecipe() {
		super((epOre, oreDictOre) -> {
			return createProspectorEyeForOre(oreDictOre);
		}, stack -> {
			return stack.getItem() == prospector ? MatchType.REG : MatchType.NONE;
		}, stack -> {
			return MatchType.ORE_DICT;
		});
	}

	public static class Factory implements IRecipeFactory {
		@Override
		public IRecipe parse(JsonContext context, JsonObject json) {
			return new TuneEyeOfProspectingRecipe();
		}
	}
}
