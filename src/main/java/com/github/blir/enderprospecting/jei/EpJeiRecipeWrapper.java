package com.github.blir.enderprospecting.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.blir.enderprospecting.EnderProspecting;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

public class EpJeiRecipeWrapper implements IRecipeWrapper {
	private EpJeiRecipe recipe;
	public EpJeiRecipeWrapper(EpJeiRecipe recipe) {
		this.recipe = recipe;
	}
	@Override
	public void getIngredients(IIngredients ingredients) {
		switch (recipe.recipe) {
		case REPAIR_COMPASS:
			ItemStack compass = EnderProspecting.createProspectorCompassForOre(recipe.oreName);
			ingredients.setOutput(ItemStack.class, compass);
			compass = EnderProspecting.createProspectorCompassForOre(recipe.oreName, 667);
			ingredients.setInputs(ItemStack.class, NonNullList.from(null, compass, new ItemStack(Items.BLAZE_POWDER)));
			break;
		case TUNE_COMPASS:
			ItemStack eye = EnderProspecting.createProspectorEyeForOre(recipe.oreName);
			compass = EnderProspecting.createProspectorCompassForOre(recipe.oreName);
			ingredients.setOutput(ItemStack.class, compass);
			ingredients.setInputs(ItemStack.class, NonNullList.from(null, eye, eye, eye, eye, new ItemStack(Items.COMPASS)));
			break;
		case TUNE_EYE:
			eye = EnderProspecting.createProspectorEyeForOre(recipe.oreName);
			List<ItemStack> ores = OreDictionary.getOres(recipe.oreName);
			List<List<ItemStack>> lists = new ArrayList<>();
			lists.add(Collections.singletonList(new ItemStack(EnderProspecting.prospector)));
			lists.add(ores);
			ingredients.setOutput(ItemStack.class, eye);
			ingredients.setInputLists(ItemStack.class, lists);
			break;
		}
	}
}
