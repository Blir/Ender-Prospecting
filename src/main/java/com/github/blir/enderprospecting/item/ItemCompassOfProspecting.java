package com.github.blir.enderprospecting.item;

import static com.github.blir.enderprospecting.EnderProspecting.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.blir.enderprospecting.EnderProspecting;
import com.github.blir.enderprospecting.WritableNBT;
import com.github.blir.enderprospecting.entity.EntityEyeOfProspecting;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCompassOfProspecting extends Item {
    
	public ItemCompassOfProspecting() {
		setCreativeTab(CreativeTabs.MISC);
		setNoRepair();
		setMaxDamage(1000);
		setMaxStackSize(1);
		setUnlocalizedName("prospector_compass");
		setRegistryName("prospector_compass");
		addPropertyOverride(new ResourceLocation("angle"), new AngleProperty());
	}
	
	@SideOnly(Side.CLIENT)
	public static class CompassColor implements IItemColor {
		@Override
		public int colorMultiplier(ItemStack stack, int tintIndex) {
			if (tintIndex != 1)
				return 0xFFFFFFFF;
			String oreName = getOreName(stack);
			if (oreName == null)
				return 0x00738863;
			switch (oreName) {
			case "oreIron":
				return 0x00bc9980;
			case "oreCoal":
				return 0x003f3f3f;
			case "oreCopper":
				return 0x00fd8327;
			case "oreDiamond":
				return 0x005decf5;
			case "oreEmerald":
				return 0x0017dd62;
			case "oreGold":
				return 0x00ffffb5;
			case "oreLapis":
				return 0x00336085;
			case "oreQuartz":
				return 0x00e7e1d8;
			case "oreRedstone":
				return 0x00ff0000;
			case "oreTin":
				return 0x00d2d2d2;
			default:
				return 0x00738863;
			}
		}
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged
        		|| isEnabled(oldStack) != isEnabled(newStack)
        		|| isHighSensitivity(oldStack) != isHighSensitivity(newStack);
    }

	@Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
		return !newStack.isItemEqual(oldStack);
    }
	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		String formattedOreName = formatOreName(stack);
		tooltip.add(formattedOreName == null ? "Base compass; not tuned" : "Tuned to " + formattedOreName);
		boolean enabled = isEnabled(stack);
		tooltip.add(enabled ? "Currently enabled" : "Currently disabled");
		if (!enabled) {
			tooltip.add("Right click to enable");
			tooltip.add("Shift+Right click to toggle sensitivity");
		}
		tooltip.add(isHighSensitivity(stack) ? "Sensitivity: High" : "Sensitivity: Low");
		if (stack.getItemDamage() >= stack.getMaxDamage())
			tooltip.add("Broken");
	}
	
	private static boolean isEnabled(ItemStack stack) {
		return getStackTagCompound(stack, false).tag.getBoolean(en);
	}
	
	private static boolean isHighSensitivity(ItemStack stack) {
		return getStackTagCompound(stack, false).tag.getBoolean(sens);
	}
    
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (entity == null || !(entity instanceof EntityPlayer))
            return;
        EntityPlayer player = (EntityPlayer) entity;
        if (world == null)
            world = player.world;
        if (stack.getItemDamage() >= getMaxDamage(stack)) {
        	if (isEnabled(stack)) {
                WritableNBT nbt = getStackTagCompound(stack, true);
                nbt.tag.setBoolean(en, false);
                stack.deserializeNBT(nbt.nbt);
        	}
        	return;
        }
        String oreName = getOreName(stack);
        if (oreName == null)
        	return;
        
        WritableNBT nbt = getStackTagCompound(stack, true);
        long lastUpdate = nbt.tag.getLong("lastUpdate");
        long worldTime = world.getTotalWorldTime();

        boolean shouldUpdate = (worldTime - lastUpdate) >= 20 || lastUpdate == 0;
        boolean isEnabled = isEnabled(stack);
        if (shouldUpdate) {
        	
        	int cooldown = nbt.tag.getInteger("cooldown");
        	if (cooldown > 0) {
        		nbt.tag.setInteger("cooldown", cooldown - 1);
        		if (!isEnabled) {
        			// should never be executed afaik
    	        	nbt.tag.setLong("lastUpdate", worldTime);
    	        	stack.deserializeNBT(nbt.nbt);
        		}
        	}
        	if (isEnabled) {
	        	nbt.tag.setLong("lastUpdate", worldTime);
	        	//stack.deserializeNBT(nbt.nbt);
				BlockPos blockPos = getBlockPos(nbt);
				int dmg;
				if (blockPos != null) {
					IBlockState blockState = world.getBlockState(blockPos);
					int harvestLevel = world.getBlockState(blockPos).getBlock().getHarvestLevel(blockState);
	        		dmg = 2 + harvestLevel;
				} else {
					dmg = 1;
				}
				if (isHighSensitivity(stack))
					dmg *= 3;
	    		stack.damageItem(dmg, player);
        	}
        }
        
        if (isEnabled)
        	updateRot(oreName, shouldUpdate, worldTime, nbt, world, player, stack);
    }
	
	@Override
	public void setDamage(ItemStack stack, int d) {
		int m = getMaxDamage(stack);
		if (d > m)
			d = m;
		super.setDamage(stack, d);
    }
	
	private BlockPos getBlockPos(WritableNBT nbt) {
		if (nbt.tag.getBoolean("hasBlockPos")) {
	        int[] pos = nbt.tag.getIntArray("blockPos");
	        if (pos.length != 0)
	        	return new BlockPos(pos[0], pos[1], pos[2]);
		}
		return null;
	}
	
	private void updateRot(String oreName, boolean shouldUpdate, long worldTime, WritableNBT nbt, World world, EntityPlayer player, ItemStack stack) {
		BlockPos blockPos = null;
		if (shouldUpdate) {
			blockPos = getClosestTrackableOre(world, player, oreName, isHighSensitivity(stack) ? 24 : 12, 2);
			nbt.tag.setBoolean("hasBlockPos", blockPos != null);
    		nbt.tag.setIntArray("blockPos", blockPos == null ? new int[3] : new int[] {
    				blockPos.getX(), blockPos.getY(), blockPos.getZ()
    		});
		} else {
			blockPos = getBlockPos(nbt);
		}
        
        double d0;
        
        if (blockPos == null) {
            d0 = Math.random() * (Math.PI * 2D);
        } else {
            double d1 = (double)player.rotationYaw;
            d1 = positiveModulo(d1 / 360.0D, 1.0D);
            double d2 = Math.atan2((double)blockPos.getZ() + 0.5 - player.posZ, (double)blockPos.getX() + 0.5 - player.posX) / (Math.PI * 2D);
            d0 = 0.5D - (d1 - 0.25D - d2);
        }
        
        d0 = wobble(world, d0, worldTime, nbt);

        float f = positiveModulo((float)d0, 1.0F);
        
        nbt.tag.setFloat("angle", f);
		stack.deserializeNBT(nbt.nbt);
	}

    public static float positiveModulo(float numerator, float denominator) {
        return (numerator % denominator + denominator) % denominator;
    }
	
	public static double positiveModulo(double numerator, double denominator) {
        return (numerator % denominator + denominator) % denominator;
    }
    
    private double wobble(World world, double someAngle, long worldTime, WritableNBT nbt)
    {
    	long lastUpdateTick = nbt.tag.getLong("lastUpdateTick");
        if (worldTime != lastUpdateTick)
        {
            lastUpdateTick = worldTime;
            nbt.tag.setLong("lastUpdateTick", lastUpdateTick);
            double rotation = nbt.tag.getDouble("rotation");
            double d0 = someAngle - rotation;
            d0 = positiveModulo(d0 + 0.5D, 1.0D) - 0.5D;
            double rota = nbt.tag.getDouble("rota");
            rota += d0 * 0.1D;
            rota *= 0.8D;
            nbt.tag.setDouble("rota", rota);
            double or = rotation;
            rotation = positiveModulo(rotation + rota, 1.0D);
            nbt.tag.setDouble("rotation", rotation);
            return rotation;
        }
        return nbt.tag.getDouble("rotation");
    }
	
    private static final String sens = "highSensivity";
    private static final String en = "enabled";
    
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
        if (stack.getItemDamage() >= getMaxDamage(stack))
        	return new ActionResult(EnumActionResult.FAIL, stack);
		WritableNBT nbt = getStackTagCompound(stack, true);
		if (nbt.tag.getInteger("cooldown") == 0) {
			String tag = player.isSneaking() ? sens : en;
			boolean val = !nbt.tag.getBoolean(tag);
			nbt.tag.setBoolean(tag, val);
			setOverlay(tag.equals(en) ? (val ? "Enabled" : "Disabled") : (val ? "High Sensitivity" : "Low Sensitivity"));
			stack.deserializeNBT(nbt.nbt);
			if (isEnabled(stack))
				nbt.tag.setInteger("cooldown", 3);
	        return new ActionResult(EnumActionResult.SUCCESS, stack);
		}
        return new ActionResult(EnumActionResult.FAIL, stack);
    }

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		return onItemRightClick(world, player, hand).getType();
    }
	
	private static class AngleProperty implements IItemPropertyGetter {
		@Override
		@SideOnly(Side.CLIENT)
	    public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
			return getStackTagCompound(stack, false).tag.getFloat("angle");
	    }
	}
}
