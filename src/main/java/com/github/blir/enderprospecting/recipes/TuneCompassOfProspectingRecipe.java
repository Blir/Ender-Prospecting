package com.github.blir.enderprospecting.recipes;

import static com.github.blir.enderprospecting.EnderProspecting.*;

import java.util.function.Function;

import com.github.blir.enderprospecting.recipes.EPShapelessRecipe.MatchType;
import com.google.gson.JsonObject;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;

public class TuneCompassOfProspectingRecipe extends EPShapelessRecipe {

	private static final Function<ItemStack, MatchType> eyeMatcher = stack -> {
		return stack.getItem() == prospector ? MatchType.EP : MatchType.NONE;
	};
	
	public TuneCompassOfProspectingRecipe() {
		super((epOre, oreDictOre) -> {
			return createProspectorCompassForOre(epOre);
		}, eyeMatcher, eyeMatcher, eyeMatcher, eyeMatcher, stack -> {
			return stack.getItem() == Items.COMPASS ? MatchType.REG : MatchType.NONE;
		});
	}

	public static class Factory implements IRecipeFactory {
		@Override
		public IRecipe parse(JsonContext context, JsonObject json) {
			return new TuneCompassOfProspectingRecipe();
		}
	}
}