package com.simibubi.create.content.logistics.packagerLink;

import java.util.Comparator;

import com.simibubi.create.content.logistics.BigItemStack;

public class RequestPromise {
	
	public int ticksExisted;
	public BigItemStack promisedStack;
	
	public RequestPromise(BigItemStack promisedStack) {
		this.promisedStack = promisedStack;
		ticksExisted = 0;
	}
	
	public void tick() {
		ticksExisted++;
	}

	public static Comparator<? super RequestPromise> ageComparator() {
		return (i1, i2) -> Integer.compare(i2.ticksExisted, i1.ticksExisted);
	}
	
}
