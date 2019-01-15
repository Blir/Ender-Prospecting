package com.github.blir.enderprospecting.jei;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.github.blir.enderprospecting.EnderProspecting;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

public class EpJeiOreIngredient {

	public static Collection<EpJeiOreIngredient> all() {
		Collection<EpJeiOreIngredient> c = new ArrayList<>();
		EpJeiPlugin.oreDictionaryOres().forEach(ore -> {
			c.add(new EpJeiOreIngredient(ore));
		});
		return c;
	}
	
	private String oreName;
	
	public EpJeiOreIngredient(String oreName) {
		this.oreName = oreName;
	}
	
	@Override
	public int hashCode() {
		return oreName.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EpJeiOreIngredient))
			return false;
		return equals((EpJeiOreIngredient) obj);
	}
	
	public boolean equals(EpJeiOreIngredient other) {
		return this.oreName.equals(other.oreName);
	}
	
	@Override
	public String toString() {
		return String.format("EpJeiOreIngredient[oreName=%s]", oreName);
	}
	
	public static class Helper implements IIngredientHelper<EpJeiOreIngredient> {
		@Override
		public List<EpJeiOreIngredient> expandSubtypes(List<EpJeiOreIngredient> i) {
			return i;
		}
		@Override
		public EpJeiOreIngredient getMatch(Iterable<EpJeiOreIngredient> ingredients, EpJeiOreIngredient match) {
			for (EpJeiOreIngredient i : ingredients)
				if (match.equals(i))
					return i;
			return null;
		}
		@Override
		public String getDisplayName(EpJeiOreIngredient i) {
			return i.oreName.substring(3);
		}
		@Override
		public String getUniqueId(EpJeiOreIngredient i) {
			return EnderProspecting.MODID + ":" + i.oreName;
		}
		@Override
		public String getWildcardId(EpJeiOreIngredient i) {
			return getUniqueId(i);
		}
		@Override
		public String getModId(EpJeiOreIngredient i) {
			return EnderProspecting.MODID;
		}
		@Override
		public Iterable<Color> getColors(EpJeiOreIngredient i) {
			return () -> Collections.emptyIterator();
		}
		@Override
		public String getResourceId(EpJeiOreIngredient i) {
			return getUniqueId(i);
		}
		@Override
		public EpJeiOreIngredient copyIngredient(EpJeiOreIngredient i) {
			return new EpJeiOreIngredient(i.oreName);
		}
		@Override
		public String getErrorInfo(EpJeiOreIngredient i) {
			return i.toString();
		}
	}
	
	public static class Renderer implements IIngredientRenderer<EpJeiOreIngredient> {
		@Override
		public void render(Minecraft mc, int x, int y, EpJeiOreIngredient i) {
			NonNullList<ItemStack> ores = OreDictionary.getOres(i.oreName, false);
			mc.getRenderItem().renderItemIntoGUI(ores.get(0), x, y);
		}
		@Override
		public List<String> getTooltip(Minecraft mc, EpJeiOreIngredient i, ITooltipFlag f) {
			NonNullList<ItemStack> ores = OreDictionary.getOres(i.oreName, false);
			List<String> t = new ArrayList<>();
			ItemStack ore = ores.get(0);
			ore.getItem().addInformation(ore, null, t, f);
			return t;
		}
	}
}
