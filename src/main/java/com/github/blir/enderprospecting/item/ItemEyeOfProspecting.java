package com.github.blir.enderprospecting.item;

import static com.github.blir.enderprospecting.EnderProspecting.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.blir.enderprospecting.entity.EntityEyeOfProspecting;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemEyeOfProspecting extends Item {
	
	private Map<UUID, Long> lastAttempt = new HashMap<UUID, Long>();
	
	public ItemEyeOfProspecting() {
		setCreativeTab(CreativeTabs.MISC);
		canRepair = false;
		setUnlocalizedName("prospector");
		setRegistryName("prospector");
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		String formattedOreName = formatOreName(stack);
		tooltip.add(formattedOreName == null ? "Base eye; not tuned" : "Tuned to " + formattedOreName);
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
    {
		ItemStack stack = player.getHeldItem(hand);
		
        if (world.isRemote)
        	return new ActionResult(EnumActionResult.PASS, stack);
        
        String oreName = getOreName(stack);
        if (oreName == null)
        	return new ActionResult(EnumActionResult.FAIL, stack);

    	UUID playerUUID = player.getUniqueID();
    	long currentTime = System.currentTimeMillis();
    	Long lastAttempt = this.lastAttempt.get(playerUUID);
    	if (lastAttempt != null && (currentTime - lastAttempt) < 2000)
    		return new ActionResult(EnumActionResult.PASS, stack);
    	
		this.lastAttempt.put(playerUUID, currentTime);
        
        BlockPos closest = getClosestTrackableOre(world, player, oreName, 24, 12);
        
        if (closest != null) {
        	
        	EntityEyeOfProspecting eye = new EntityEyeOfProspecting(world, player.posX, player.posY + (double)(player.height / 2.0F), player.posZ, oreName, closest);
            world.spawnEntity(eye);
            world.playSound((EntityPlayer)null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ENDEREYE_LAUNCH, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
            world.playEvent((EntityPlayer)null, 1003, new BlockPos(player), 0);

            player.addStat(StatList.getObjectUseStats(this));
        	
        	if (!player.capabilities.isCreativeMode)
				stack.setCount(stack.getCount() - 1);
        	
        	return new ActionResult(EnumActionResult.SUCCESS, stack);
        } else {
        	player.sendMessage(new TextComponentString("The eye remains motionless; there must be no nearby ores."));
        	return new ActionResult(EnumActionResult.FAIL, stack);
        }
    }

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
		return onItemRightClick(world, player, hand).getType();
    }
}
