package com.github.blir.enderprospecting.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.github.blir.enderprospecting.EnderProspecting.*;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.IForgeRegistryEntry;

public abstract class EPShapelessRecipe extends IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	public enum MatchType {
		EP, ORE_DICT, REG, NONE
	}
	
	private Function<ItemStack, MatchType>[] matchers;
	private BiFunction<String, String, ItemStack> resultFn;
	
	public EPShapelessRecipe(BiFunction<String, String, ItemStack> resultFn, Function<ItemStack, MatchType>... matchers) {
		this.resultFn = resultFn;
		this.matchers = matchers;
	}
	
	@Override
	public boolean matches(InventoryCrafting inv, World worldIn) {
		return getCraftingResult(inv) != ItemStack.EMPTY;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		List<Function<ItemStack, MatchType>> matchers = new ArrayList<>(Arrays.asList(this.matchers));
		String epOre = null, oreDictOre = null;
		for (int i = 0; i < inv.getHeight(); i++) {
            for (int j = 0; j < inv.getWidth(); j++) {
                ItemStack stack = inv.getStackInRowAndColumn(j, i);
                if (stack != null && !stack.isEmpty()) {
	                ListIterator<Function<ItemStack, MatchType>> it = matchers.listIterator();
	                MatchType matchType = MatchType.NONE;
	                while (it.hasNext() && matchType == MatchType.NONE) {
	                	matchType = it.next().apply(stack);
	                	switch (matchType) {
	                	case EP:
	                		// not necessarily a match yet
	                		String oreName = getOreName(stack);
	                		if (epOre == null) {
	                			it.remove();
	                			epOre = oreName;
	                		} else if (epOre.equals(oreName)) {
	                			it.remove();
	                		} else {
	                			// mixed ore types
	                			return ItemStack.EMPTY;
	                		}
	                		break;
	                	case ORE_DICT:
	                		// not necessarily a match yet
	                		int[] ids = OreDictionary.getOreIDs(stack);
		                	for (int id : ids) {
		                		oreDictOre = OreDictionary.getOreName(id);
		                		if (oreDictOre.startsWith("ore")) {
		                			break;
		                		} else {
		                			oreDictOre = null;
		                		}
		                	}
		                	if (oreDictOre == null) {
		                		// try the next matcher
		                		matchType = MatchType.NONE;
		                		continue;
		                	} else {
		                		it.remove();
		                	}
	                		break;
	                	case REG:
	                		it.remove();
	                		// nothing else to do
	                		break;
	                	case NONE:
	                		// try the next matcher
	                		break;
	                	}
	                }
	                if (matchType == MatchType.NONE) {
	                	// no matches were found, the stack is invalid
	                	return ItemStack.EMPTY;
	                }
                }
            }
        }
		return matchers.isEmpty() ? resultFn.apply(epOre, oreDictOre) : ItemStack.EMPTY;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= matchers.length;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return resultFn.apply("oreCoal", "oreCoal");
	}
}
