package com.github.blir.enderprospecting.jei;

import java.util.ArrayList;
import java.util.Collection;

import com.github.blir.enderprospecting.EnderProspecting;
import com.github.blir.enderprospecting.recipes.EPShapelessRecipe;
import com.github.blir.enderprospecting.recipes.RepairCompassOfProspectingRecipe;
import com.github.blir.enderprospecting.recipes.TuneCompassOfProspectingRecipe;
import com.github.blir.enderprospecting.recipes.TuneEyeOfProspectingRecipe;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IModIngredientRegistration;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import net.minecraftforge.oredict.OreDictionary;

@JEIPlugin
public class EpJeiPlugin implements IModPlugin {
	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {
		EnderProspecting.logger.info("EpJeiPlugin::registerItemSubtypes");
		subtypeRegistry.registerSubtypeInterpreter(EnderProspecting.compass, EnderProspecting::getOreName);
		subtypeRegistry.registerSubtypeInterpreter(EnderProspecting.prospector, EnderProspecting::getOreName);
	}

	@Override
	public void registerIngredients(IModIngredientRegistration ingredientRegistry) {
		EnderProspecting.logger.info("EpJeiPlugin::registerIngredients");
		ingredientRegistry.register(EpJeiTunedIngredient.class, EpJeiTunedIngredient.all(),
				new EpJeiTunedIngredient.Helper(), new EpJeiTunedIngredient.Renderer());
		/*ingredientRegistry.register(EpJeiOreIngredient.class, EpJeiOreIngredient.all(),
				new EpJeiOreIngredient.Helper(), new EpJeiOreIngredient.Renderer());*/
	}
	
	@Override
	public void register(IModRegistry registry) {
		EnderProspecting.logger.info("EpJeiPlugin::register");
		Collection<EpJeiRecipe> recipes = new ArrayList<>();
		addRecipes(recipes, EpJeiRecipe.Recipe.REPAIR_COMPASS);
		addRecipes(recipes, EpJeiRecipe.Recipe.TUNE_COMPASS);
		addRecipes(recipes, EpJeiRecipe.Recipe.TUNE_EYE);
		registry.addRecipes(recipes, VanillaRecipeCategoryUid.CRAFTING);
		registry.handleRecipes(EpJeiRecipe.class, EpJeiRecipeWrapper::new, VanillaRecipeCategoryUid.CRAFTING);
	}
	
	public static Collection<String> oreDictionaryOres() {
		Collection<String> c = new ArrayList<>();
		for (String ore : OreDictionary.getOreNames())
			if (ore.startsWith("ore") && !OreDictionary.getOres(ore).isEmpty())
				c.add(ore);
		return c;
	}
	
	private void addRecipes(Collection<EpJeiRecipe> recipes, EpJeiRecipe.Recipe recipe) {
		oreDictionaryOres().forEach(ore -> {
			recipes.add(new EpJeiRecipe(recipe, ore));
		});
	}
}
