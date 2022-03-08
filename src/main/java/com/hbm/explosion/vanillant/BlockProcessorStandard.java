package com.hbm.explosion.vanillant;

import java.util.HashSet;
import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

public class BlockProcessorStandard implements IBlockProcessor {
	
	protected IDropChanceMutator chance;
	protected IFortuneMutator fortune;
	protected IBlockMutator convert;
	
	public BlockProcessorStandard() { }
	
	public BlockProcessorStandard withChance(IDropChanceMutator chance) {
		this.chance = chance;
		return this;
	}
	
	public BlockProcessorStandard withFortune(IFortuneMutator fortune) {
		this.fortune = fortune;
		return this;
	}
	
	public BlockProcessorStandard withBlockEffect(IBlockMutator convert) {
		this.convert = convert;
		return this;
	}

	@Override
	public void process(ExplosionVNT explosion, World world, double x, double y, double z, HashSet<ChunkPosition> affectedBlocks) {

		Iterator iterator = affectedBlocks.iterator();
		float dropChance = 1.0F / explosion.size;
		
		while(iterator.hasNext()) {
			ChunkPosition chunkposition = (ChunkPosition) iterator.next();
			int blockX = chunkposition.chunkPosX;
			int blockY = chunkposition.chunkPosY;
			int blockZ = chunkposition.chunkPosZ;
			Block block = world.getBlock(blockX, blockY, blockZ);
			
			if(block.getMaterial() != Material.air) {
				if(block.canDropFromExplosion(null)) {
					
					if(chance != null) {
						dropChance = chance.mutateDropChance(explosion, block, blockX, blockY, blockZ, dropChance);
					}
					
					int dropFortune = fortune == null ? 0 : fortune.mutateFortune(explosion, block, blockX, blockY, blockZ);
					
					block.dropBlockAsItemWithChance(world, blockX, blockY, blockZ, world.getBlockMetadata(blockX, blockY, blockZ), dropChance, dropFortune);
				}
				
				block.onBlockExploded(world, blockX, blockY, blockZ, null);
			}
		}
		
		
		if(this.convert != null) {
			iterator = affectedBlocks.iterator();
			
			while(iterator.hasNext()) {
				ChunkPosition chunkposition = (ChunkPosition) iterator.next();
				int blockX = chunkposition.chunkPosX;
				int blockY = chunkposition.chunkPosY;
				int blockZ = chunkposition.chunkPosZ;
				Block block = world.getBlock(blockX, blockY, blockZ);
				
				if(block.getMaterial() == Material.air) {
					this.convert.mutateAtPosition(explosion, blockX, blockY, blockZ);
				}
			}
		}
	}

	public BlockProcessorStandard setNoDrop() {
		this.chance = new DropChanceNever();
		return this;
	}
	public BlockProcessorStandard setAllDrop() {
		this.chance = new DropChanceAlways();
		return this;
	}
}
