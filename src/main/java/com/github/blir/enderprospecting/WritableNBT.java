package com.github.blir.enderprospecting;

import net.minecraft.nbt.NBTTagCompound;

public class WritableNBT {

	public final NBTTagCompound nbt, tag;
	
	public WritableNBT(NBTTagCompound nbt, NBTTagCompound tag) {
		this.nbt = nbt;
		this.tag = tag;
	}
}
