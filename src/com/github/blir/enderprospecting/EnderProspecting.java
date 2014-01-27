package com.github.blir.enderprospecting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.FMLRelaunchLog;

@Mod(modid = EnderProspecting.modid, name = "Ender Prospecting", version = "1.0.1.4")
@NetworkMod(clientSideRequired = true, serverSideRequired = true)
public class EnderProspecting {

	public static final String modid = "enderprospecting";
	public static final Logger logger = Logger.getLogger(modid);

	public static Item prospector;
	public static Item talisman;
	public static Item totem;

	public static int prospectorID;
	public static int talismanID;
	public static int totemID;

	@Instance(modid)
	public static EnderProspecting instance;

	@SidedProxy(clientSide = "com.github.blir.enderprospecting.ClientProxy", serverSide = "com.github.blir.enderprospecting.CommonProxy")
	public static CommonProxy proxy;

	private static Configuration config;
	private static Map<Short, String> ores = new HashMap<Short, String>();

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		logger.setParent(FMLLog.getLogger());

		config = new Configuration(evt.getSuggestedConfigurationFile());
		config.load();
		prospectorID = config.get(Configuration.CATEGORY_ITEM, "prospector",
				7291).getInt();
		talismanID = config.get(Configuration.CATEGORY_ITEM, "talisman", 7292)
				.getInt();
		totemID = config.get(Configuration.CATEGORY_ITEM, "totem", 7293)
				.getInt();
		config.addCustomCategoryComment(
				"ores",
				"These IDs are used to help the Ender Prospecting items record what they're tuned to, and shouldn't be edited under normal circumstances.");

		config.save();

		instance = this;
	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		prospector = new ItemEyeOfProspecting(prospectorID)
				.setUnlocalizedName("prospector");
		LanguageRegistry.addName(prospector, "Eye of Prospecting");

		talisman = new ItemTalismanOfProspecting(talismanID)
				.setUnlocalizedName("talisman");
		LanguageRegistry.addName(talisman, "Talisman of Prospecting");

		totem = new ItemTotemOfProspecting(totemID).setUnlocalizedName("totem");
		LanguageRegistry.addName(totem, "Totem of Prospecting");

		ItemStack itemProspectorBase = new ItemStack(prospector);
		ItemStack itemProspectorWildcard = new ItemStack(prospector, 1,
				OreDictionary.WILDCARD_VALUE);
		ItemStack itemTalismanBase = new ItemStack(talisman);
		ItemStack itemTalismanWildcard = new ItemStack(talisman, 1,
				OreDictionary.WILDCARD_VALUE);
		ItemStack itemTotemBase = new ItemStack(totem);
		ItemStack itemTotemWildcard = new ItemStack(totem, 1,
				OreDictionary.WILDCARD_VALUE);

		// special case recipes for coal

		ItemStack itemProspectorWorkingCoal = new ItemStack(prospector, 1,
				Block.oreCoal.blockID);
		ItemStack itemTalismanWorkingCoal = new ItemStack(talisman, 1,
				Block.oreCoal.blockID);
		ItemStack itemTotemWorkingCoal = new ItemStack(totem, 1,
				Block.oreCoal.blockID);

		GameRegistry.addShapelessRecipe(itemProspectorWorkingCoal,
				itemProspectorBase, new ItemStack(Block.oreCoal));

		GameRegistry.addShapedRecipe(itemTalismanWorkingCoal, "e e", " b ",
				"eee", 'e', itemProspectorWorkingCoal, 'b', itemTalismanBase);

		GameRegistry.addShapedRecipe(itemTotemWorkingCoal, "tet", "tbt", "   ",
				't', itemTalismanWorkingCoal, 'b', itemTotemBase, 'e',
				itemProspectorWorkingCoal);

		// recipe for base eye
		GameRegistry.addShapedRecipe(itemProspectorBase, "srs", "rer", "srs",
				's', new ItemStack(Block.stone), 'r', new ItemStack(
						Item.redstone), 'e', new ItemStack(Item.eyeOfEnder));
		GameRegistry.addShapelessRecipe(itemProspectorBase,
				itemProspectorWildcard);

		// recipe for base talisman
		GameRegistry.addShapedRecipe(itemTalismanBase, "bsb", "w w", "bbb",
				'b', new ItemStack(Item.slimeBall), 'w', new ItemStack(
						Item.stick), 's', new ItemStack(Item.silk));
		GameRegistry.addShapelessRecipe(itemTalismanBase, itemTalismanWildcard);

		// recipe for base totem
		GameRegistry.addShapedRecipe(itemTotemBase, "wiw", "twt", " w ", 'w',
				new ItemStack(Block.planks, 1, OreDictionary.WILDCARD_VALUE),
				'i', new ItemStack(Item.ingotIron), 't', new ItemStack(
						Item.ghastTear));
		GameRegistry.addShapelessRecipe(itemTotemBase, itemTotemWildcard);

		EntityRegistry
				.registerGlobalEntityID(EntityEyeOfProspecting.class,
						"Eye of Prospecting",
						EntityRegistry.findGlobalUniqueEntityId());
		EntityRegistry.registerModEntity(EntityEyeOfProspecting.class,
				"Eye of Prospecting", 1, this, 80, 3, true);

		proxy.registerRenderers();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		// go through all ores in OreDictionary
		// create a recipe for each one
		ItemStack itemProspectorBase = new ItemStack(prospector);
		ItemStack itemTalismanBase = new ItemStack(talisman);
		ItemStack itemTotemBase = new ItemStack(totem);
		String[] oreNames = OreDictionary.getOreNames();
		for (String oreName : oreNames) {
			if (oreName.startsWith("ore")) {
				List<ItemStack> ores = OreDictionary.getOres(oreName);
				short meta = registerOre(oreName);
				for (ItemStack ore : ores) {
					ItemStack itemProspectorWorking = new ItemStack(prospector,
							1, meta);
					ItemStack itemTalismanWorking = new ItemStack(talisman, 1,
							meta);
					ItemStack itemTotemWorking = new ItemStack(totem, 1, meta);

					GameRegistry.addShapelessRecipe(itemProspectorWorking,
							itemProspectorBase, ore);
					GameRegistry.addShapedRecipe(itemTalismanWorking, "e e",
							" b ", "eee", 'e', itemProspectorWorking, 'b',
							itemTalismanBase);
					GameRegistry.addShapedRecipe(itemTotemWorking, "tet",
							"tbt", "   ", 't', itemTalismanWorking, 'b',
							itemTotemBase, 'e', itemProspectorWorking);
				}
			}
		}
	}

	@EventHandler
	public void onServerStopped(FMLServerStoppedEvent evt) {
		config.save();
	}

	/* where stack is an EP item */
	public static boolean canTrack(ItemStack stack, World world, int x, int y,
			int z) {
		int blockID = world.getBlockId(x, y, z);
		if (stack.getItemDamage() == Block.oreCoal.blockID
				&& blockID == Block.oreCoal.blockID) {
			// special case for coal
			return true;
		}
		List<ItemStack> trackableOres = OreDictionary.getOres(OreDictionary
				.getOreID(ores.get((short) stack.getItemDamage())));
		for (ItemStack trackableOre : trackableOres) {
			if (trackableOre.itemID == blockID
					&& (trackableOre.getItemDamage() == world.getBlockMetadata(
							x, y, z) || trackableOre.getItemDamage() == OreDictionary.WILDCARD_VALUE)) {
				// id & metadata must match unless the ore definition uses the
				// wildcard value
				return true;
			}
		}
		return false;
	}

	/* where stack is an EP item */
	public static String formatOreName(ItemStack stack) {
		return formatOreName((short) stack.getItemDamage());
	}

	/* where meta is an EP item's meta */
	public static String formatOreName(short meta) {
		if (meta == Block.oreCoal.blockID) {
			// special case for coal
			return "Coal";
		}
		String name = ores.get(meta);
		return name.startsWith("ore") ? name.substring(3) : name;
	}

	/* where stack is an ore */
	public static short registerOre(String oreName) {
		short hash = (short) oreName.hashCode();
		for (int i = 0; hash <= 0 || hash == Block.oreCoal.blockID
				|| ores.get(hash) != null; i++) {
			// use quadratic probing until a unique ID is found
			hash += i;
		}
		hash = (short) config.get("ores", oreName, hash).getInt();
		ores.put(hash, oreName);
		logger.log(Level.INFO, "Registered {0} with metadata {1}",
				new Object[] { oreName, hash });
		return hash;
	}
}
