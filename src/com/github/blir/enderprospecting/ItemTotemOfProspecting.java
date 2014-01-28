package com.github.blir.enderprospecting;

import static com.github.blir.enderprospecting.EnderProspecting.canTrack;
import static com.github.blir.enderprospecting.EnderProspecting.formatOreName;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class ItemTotemOfProspecting extends Item {

	@SideOnly(Side.CLIENT)
	private Icon[] icons;

	private Map<String, Point2D.Double> previousPositions = new HashMap<String, Point2D.Double>();
	// storing Vec3D's doesn't work for some reason
	private Map<String, Point3D> previousBlockPositions = new HashMap<String, Point3D>();

	@SideOnly(Side.CLIENT)
	private byte ticks;

	public ItemTotemOfProspecting(int par1) {
		super(par1);
		setCreativeTab(CreativeTabs.tabMisc);
		this.canRepair = false;
		this.maxStackSize = 1;
		this.hasSubtypes = true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister reg) {
		icons = new Icon[10];
		for (int idx = 0; idx < icons.length; idx++) {
			icons[idx] = reg.registerIcon(EnderProspecting.modid + ":"
					+ getUnlocalizedName().substring(5) + idx);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Icon getIconFromDamage(int dmg) {
		if (dmg == Block.oreCoal.blockID) {
			return icons[9];
		}
		if (dmg == 0) {
			return icons[0];
		}
		String oreName = EnderProspecting.getOreName((short) dmg);
		if (oreName == null) {
			return icons[1];
		}
		if (oreName.equals("oreGold")) {
			return icons[4];
		}
		if (oreName.equals("oreIron")) {
			return icons[2];
		}
		if (oreName.equals("oreLapis")) {
			return icons[7];
		}
		if (oreName.equals("oreDiamond")) {
			return icons[6];
		}
		if (oreName.equals("oreRedstone")) {
			return icons[3];
		}
		if (oreName.equals("oreEmerald")) {
			return icons[5];
		}
		if (oreName.equals("oreQuartz")) {
			return icons[8];
		}
		return icons[1];
	}

	@Override
	public void onUpdate(ItemStack is, World world, Entity entity, int par4,
			boolean par5) {
		if (!world.isRemote || getDamage(is) == 0
				|| !(entity instanceof EntityPlayer)) {
			return;
		}

		ticks++;

		if (ticks % 10 != 0) {
			// only check every 10 ticks to reduce lag
			return;
		}

		EntityPlayer player = (EntityPlayer) entity;

		Point2D.Double previousPosition = previousPositions.get(player
				.getDisplayName());
		boolean moved = previousPosition == null
				|| player.posX != previousPosition.x
				|| player.posZ != previousPosition.y;
		previousPositions.put(player.getDisplayName(), new Point2D.Double(
				player.posX, player.posZ));
		if (!moved) {
			// if they haven't moved since the last search, don't bother
			// searching
			return;
		}

		Vec3 playerPos = player.getPosition(1.0F);

		Vec3 blockPos = null;
		// search for the block that this totem tracks
		for (int i1 = -6; i1 <= 6; i1++) {
			for (int i2 = -3; i2 <= 3; i2++) {
				for (int i3 = -6; i3 <= 6; i3++) {
					int x = i1 + (int) player.posX;
					int y = i2 + (int) player.posY;
					int z = i3 + (int) player.posZ;
					if (canTrack(is, world, x, y, z)) {
						Vec3 prospective = world.getWorldVec3Pool()
								.getVecFromPool(x, y, z);
						if (blockPos == null
								|| playerPos.distanceTo(prospective) < playerPos
										.distanceTo(blockPos)) {
							// no current block or block is closer
							blockPos = prospective;
						}
					}
				}
			}
		}

		Point3D prevBlockPos = previousBlockPositions.get(player
				.getDisplayName());
		boolean differentBlock = blockPos == null || prevBlockPos == null
				|| (prevBlockPos.isEquivalentTo(blockPos));
		if (blockPos != null && differentBlock) {

			player.addChatMessage("Your " + formatOreName(is)
					+ " totem pulls from your inventory...");
		}
		previousBlockPositions.put(player.getDisplayName(),
				blockPos == null ? null : new Point3D(blockPos));
	}

	@Override
	public void addInformation(ItemStack is, EntityPlayer player, List list,
			boolean b) {
		if (getDamage(is) != 0) {
			list.add("Tuned to " + formatOreName(is));
		} else {
			list.add("Base totem; not tuned");
		}
	}
}
