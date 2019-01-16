package com.github.blir.enderprospecting;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import com.github.blir.enderprospecting.entity.EntityEyeOfProspecting;
import com.github.blir.enderprospecting.item.ItemCompassOfProspecting;
import com.github.blir.enderprospecting.item.ItemEyeOfProspecting;
import com.github.blir.enderprospecting.proxy.Proxy;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

@Mod(modid = EnderProspecting.MODID, version = EnderProspecting.VERSION, useMetadata = true,
	updateJSON = "https://ddns.blir.app/root/static/forge/enderprospecting/update.json")
@Mod.EventBusSubscriber(modid=EnderProspecting.MODID)
public class EnderProspecting {

	/*
	 * TODO
	 * 
	 * test
	 * compass w/ unbreaking 3
	 * 
	 * enhancements
	 * i18n
	 * get recipes to show up in JEI/NEI (done!)
	 * eye
	 * 	rework textures
	 * 	layers, shared layer for base
	 * 	change border to higher contrast colors
	 * compass
	 * 	textures (+broken texture)
	 *  add alternative recipes
	 * 		progress bar
	 * 	upgraded compass recipe
	 * 
	 * bugs
	 * jei hover text is wrong color
	 * compass
	 * 	behaves strangely in item frame / minecart chest
	 * hover text - full opacity at last moment
	 * can't jei cheat
	 * 
	 */
	
    public static final String MODID = "enderprospecting";
    public static final String VERSION = "2.1.0.3";
    
    @Instance(MODID)
	public static EnderProspecting instance;
    
	public static Logger logger;
    
    @SidedProxy(clientSide = "com.github.blir.enderprospecting.proxy.ClientProxy",
    		serverSide = "com.github.blir.enderprospecting.proxy.ServerProxy")
    private static Proxy proxy;
    
    public static ItemEyeOfProspecting prospector;
    public static ItemCompassOfProspecting compass;
    
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
		prospector = new ItemEyeOfProspecting();
		compass = new ItemCompassOfProspecting();
        event.getRegistry().register(prospector);
        event.getRegistry().register(compass);
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void registerItemHandlers(ColorHandlerEvent.Item event) {
    	event.getItemColors().registerItemColorHandler(new ItemCompassOfProspecting.CompassColor(), compass);
    }
    
    @SubscribeEvent
    public static void modelEvent(ModelRegistryEvent event) {
		proxy.registerCustomModels();
    }
    
    @EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
    	logger = evt.getModLog();
		instance = this;
		EntityRegistry.registerModEntity(new ResourceLocation(MODID, "eyeOfProspecting"),
				EntityEyeOfProspecting.class, "Eye of Prospecting", 1, this, 80, 3, true);
		proxy.registerRenderers();
    }
    
    @EventHandler
	public void init(FMLInitializationEvent evt) {
    }
    
    @EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
    }
    
    public static WritableNBT getStackTagCompound(ItemStack stack, boolean ensureExists) {
		NBTTagCompound nbt = stack.writeToNBT(new NBTTagCompound());
		NBTTagCompound tag = nbt.getCompoundTag("tag");
		if (ensureExists)
			nbt.setTag("tag", tag);
		return new WritableNBT(nbt, tag);
	}
    
    public static ItemStack createProspectorEyeForOre(String oreName) {
    	return populateItemStackForOre(prospector, oreName, 0);
    }
    
    public static ItemStack createProspectorCompassForOre(String oreName) {
    	return populateItemStackForOre(compass, oreName, 0);
    }
    
    public static ItemStack createProspectorCompassForOre(String oreName, int metadata) {
    	return populateItemStackForOre(compass, oreName, metadata);
    }
    
    public static ItemStack populateItemStackForOre(Item item, String oreName) {
    	return populateItemStackForOre(item, oreName, 0);
    }
    
    public static ItemStack populateItemStackForOre(Item item, String oreName, int metadata) {
    	ItemStack stack = new ItemStack(item, 1, metadata);
    	if (oreName == null || oreName.length() == 0)
    		return stack;
    	WritableNBT nbt = getStackTagCompound(stack, true);
    	nbt.tag.setString("ore", oreName);
    	stack.deserializeNBT(nbt.nbt);
    	return stack;
    }
    
    public static BlockPos getClosestTrackableOre(World world, EntityPlayer player, String oreName, int horizontalRange, int verticalRange) {
        Vec3d playerPos = player.getPositionEyes(1.0F);
    	int playerX = (int) playerPos.x, playerY = (int) playerPos.y, playerZ = (int) playerPos.z;
        BlockPos closest = null;
        double closestDist = 0;
        
        for (int xOff = -horizontalRange; xOff <= horizontalRange; xOff++) {
			for (int yOff = -verticalRange; yOff <= verticalRange; yOff++) {
				for (int zOff = -horizontalRange; zOff <= horizontalRange; zOff++) {
					int x = xOff + playerX, y = yOff + playerY, z = zOff + playerZ;
					BlockPos prospective = new BlockPos(x, y, z);
					if (canTrack(oreName, world, prospective, player)) {
						double prospectiveDist = prospective.getDistance(playerX, playerY, playerZ);
						if (closest == null || prospectiveDist < closestDist) {
							closest = prospective;
							closestDist = prospectiveDist;
						}
					}
				}
			}
        }
        return closest;
    }
    
    /* where stack is an EP item */
	public static String formatOreName(ItemStack stack) {
		String oreName = getOreName(stack);
		return oreName == null ? null : oreName.substring(3);
	}
    
    /* where stack is an EP item */
	public static String getOreName(ItemStack stack) {
		String oreName = stack.writeToNBT(new NBTTagCompound()).getCompoundTag("tag").getString("ore");
		return oreName.length() == 0 ? null : oreName;
	}
	
	public static boolean canTrack(String oreName, World world, BlockPos blockPos, EntityPlayer player) {
		return canTrack_pickBlock(oreName, world, blockPos, player);
	}
	
	private static boolean canTrack_pickBlock(String oreName, World world, BlockPos blockPos, EntityPlayer player) {
		IBlockState blockState = world.getBlockState(blockPos);
		NonNullList<ItemStack> trackableOres = OreDictionary.getOres(oreName);
		ItemStack pickBlock = blockState.getBlock().getPickBlock(blockState, null, world, blockPos, player);
		// could also use the old approaches and make sure to pass item meta/dmg
		return OreDictionary.containsMatch(false, trackableOres, pickBlock);
	}
	
	private static boolean canTrack_old(String oreName, World world, BlockPos blockPos, EntityPlayer player) {
		IBlockState blockState = world.getBlockState(blockPos);
		NonNullList<ItemStack> trackableOres = OreDictionary.getOres(oreName);
		Block blockBlock = blockState.getBlock();
		ItemStack blockStack = new ItemStack(blockBlock);
		return OreDictionary.containsMatch(false, trackableOres, blockStack);
	}
	
	private static int highlightFixTicks;
	private static ItemStack highlightFixStack;
	private static long highlightFixLastUpdateTick;
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	@SideOnly(Side.CLIENT)
	public static void onDrawScreenPre(RenderGameOverlayEvent.Pre event) {
		if(event.getType() == ElementType.ALL) {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.currentScreen == null) {
				
				boolean changed = false;
				if (highlightFixStack == null || highlightFixStack.getItem() != mc.ingameGUI.highlightingItemStack.getItem()) {
					changed = true;
					highlightFixStack = mc.ingameGUI.highlightingItemStack;
				}
				
				if (mc.ingameGUI.highlightingItemStack.getItem() == compass) {
					if (changed)
						highlightFixTicks = 40;
					long worldTime = mc.world.getTotalWorldTime();
					if (highlightFixLastUpdateTick != mc.world.getTotalWorldTime() && highlightFixTicks > 0) {
						highlightFixLastUpdateTick = worldTime;
						highlightFixTicks--;
					}
					mc.ingameGUI.remainingHighlightTicks = highlightFixTicks;
				}
			}
		}
	}
	
	private static String overlayString;
	private static int overlayTicks;
	private static long overlayLastUpdateTick;
	
	public static void setOverlay(String s) {
		overlayString = s;
		overlayTicks = 25;
	}
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	@SideOnly(Side.CLIENT)
	public static void onDrawScreenPost(RenderGameOverlayEvent.Post event) {
		if (event.getType() == ElementType.ALL) {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.currentScreen == null) {
				EntityPlayer player = mc.player;
				if (player.getHeldItemMainhand().getItem() == compass || player.getHeldItemOffhand().getItem() == compass) {
					if (overlayTicks > 0) {
						long worldTime = mc.world.getTotalWorldTime();
						if (overlayLastUpdateTick != worldTime) {
							overlayLastUpdateTick = worldTime;
							overlayTicks--;
						}
						// credit : mostly copied from Botania, thanks Vazkii
						int alpha = Math.min(255, (int) (overlayTicks * 256.0F / 10.0F));
						//logger.info("alpha: " + alpha);
						int color = 0x00CC00 + (alpha << 24);
						int x = event.getResolution().getScaledWidth() / 2 - mc.fontRenderer.getStringWidth(overlayString) / 2;
						int y = event.getResolution().getScaledHeight() - 70;

						GlStateManager.enableBlend();
						GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
						mc.fontRenderer.drawStringWithShadow(overlayString, x, y, color);
						GlStateManager.disableBlend();
					}
				} else {
					overlayTicks = 0;
				}
			}
		}
	}
}
