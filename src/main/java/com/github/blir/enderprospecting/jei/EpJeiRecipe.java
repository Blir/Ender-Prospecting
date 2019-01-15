package com.github.blir.enderprospecting.jei;

public class EpJeiRecipe {
	public enum Recipe {
		TUNE_EYE, TUNE_COMPASS, REPAIR_COMPASS
	}
	public final Recipe recipe;
	public final String oreName;
	public EpJeiRecipe(Recipe recipe, String oreName) {
		this.recipe = recipe;
		this.oreName = oreName;
	}
}
