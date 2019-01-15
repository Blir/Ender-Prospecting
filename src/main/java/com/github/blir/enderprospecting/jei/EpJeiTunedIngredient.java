package com.github.blir.enderprospecting.jei;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.github.blir.enderprospecting.EnderProspecting;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class EpJeiTunedIngredient {

	public static Collection<EpJeiTunedIngredient> all() {
		Collection<EpJeiTunedIngredient> c = new ArrayList<>();
		allFor(EnderProspecting.prospector, c);
		allFor(EnderProspecting.compass, c);
		return c;
	}
	
	private static void allFor(Item item, Collection<EpJeiTunedIngredient> c) {
		EpJeiPlugin.oreDictionaryOres().forEach(ore -> {
			c.add(new EpJeiTunedIngredient(item, ore));
		});
	}
	
	public static String getDisplayName(Item i) {
		return i == EnderProspecting.prospector ? "Eye of Prospecting" : "Compass of Prospecting";
	}
	
	private Item item;
	private String oreName;
	
	public EpJeiTunedIngredient(Item item, String oreName) {
		this.item = item;
		this.oreName = oreName;
	}
	
	@Override
	public int hashCode() {
		return oreName.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EpJeiTunedIngredient))
			return false;
		return equals((EpJeiTunedIngredient) obj);
	}
	
	public boolean equals(EpJeiTunedIngredient other) {
		return this.item == other.item && this.oreName.equals(other.oreName);
	}
	
	@Override
	public String toString() {
		return String.format("EpJeiTunedIngredient[item=%s,oreName=%s]", item, oreName);
	}
	
	public static class Helper implements IIngredientHelper<EpJeiTunedIngredient> {
		@Override
		public List<EpJeiTunedIngredient> expandSubtypes(List<EpJeiTunedIngredient> i) {
			return i;
		}
		@Override
		public EpJeiTunedIngredient getMatch(Iterable<EpJeiTunedIngredient> ingredients, EpJeiTunedIngredient match) {
			for (EpJeiTunedIngredient i : ingredients)
				if (match.equals(i))
					return i;
			return null;
		}
		@Override
		public String getDisplayName(EpJeiTunedIngredient i) {
			return EpJeiTunedIngredient.getDisplayName(i.item);
		}
		@Override
		public String getUniqueId(EpJeiTunedIngredient i) {
			return i.item.getRegistryName() + ":" + i.oreName;
		}
		@Override
		public String getWildcardId(EpJeiTunedIngredient i) {
			return getUniqueId(i);
		}
		@Override
		public String getModId(EpJeiTunedIngredient i) {
			return EnderProspecting.MODID;
		}
		@Override
		public Iterable<Color> getColors(EpJeiTunedIngredient i) {
			return () -> Collections.emptyIterator();
		}
		@Override
		public String getResourceId(EpJeiTunedIngredient i) {
			return getUniqueId(i);
		}
		@Override
		public EpJeiTunedIngredient copyIngredient(EpJeiTunedIngredient i) {
			return new EpJeiTunedIngredient(i.item, i.oreName);
		}
		@Override
		public String getErrorInfo(EpJeiTunedIngredient i) {
			return i.toString();
		}
	}
	
	public static class Renderer implements IIngredientRenderer<EpJeiTunedIngredient> {
		@Override
		public void render(Minecraft mc, int x, int y, EpJeiTunedIngredient i) {
			ItemStack stack = EnderProspecting.populateItemStackForOre(i.item, i.oreName);
			mc.getRenderItem().renderItemIntoGUI(stack, x, y);
		}
		@Override
		public List<String> getTooltip(Minecraft mc, EpJeiTunedIngredient i, ITooltipFlag f) {
			List<String> t = new ArrayList<>();
			t.add(getDisplayName(i.item));
			ItemStack stack = EnderProspecting.populateItemStackForOre(i.item, i.oreName);
			i.item.addInformation(stack, null, t, f);
			return t;
		}
	}
}
