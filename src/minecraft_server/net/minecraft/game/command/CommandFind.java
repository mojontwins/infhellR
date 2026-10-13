package net.minecraft.game.command;

import net.minecraft.game.MathHelper;
import net.minecraft.game.entity.player.EntityPlayer;
import net.minecraft.game.world.World;
import net.minecraft.game.world.chunk.ChunkCoordinates;
import net.minecraft.game.world.chunk.ChunkProvider;
import net.minecraft.game.world.chunk.IChunkProvider;
import net.minecraft.game.world.terrain.ChunkProviderGenerate;
import net.minecraft.game.world.terrain.generate.feature.Feature;
import net.minecraft.game.world.terrain.generate.feature.FeatureProvider;

public class CommandFind extends CommandBase {

	/** Default search radius, in chunks, around the player's current chunk. */
	private static final int DEFAULT_SEARCH_RADIUS = 1024;

	@Override
	public String getString() {
		return "find";
	}

	@Override
	public int getMinParams() {
		return 1;
	}

	@Override
	public int execute(String[] tokens, int idx, ChunkCoordinates coordinates, World theWorld, EntityPlayer thePlayer) {
		// Locate the FeatureProvider that drives this world's spawn selection. Single
		// player wraps the real generator (a ChunkProviderGenerate, or ChunkProviderSky
		// which extends it) inside a ChunkProvider cache, so unwrap that cache to reach
		// the provider's featureProvider instance.
		FeatureProvider featureProvider = null;
		IChunkProvider chunkProvider = theWorld.chunkProvider;
		if(chunkProvider instanceof ChunkProvider) {
			chunkProvider = ((ChunkProvider)chunkProvider).getInnerChunkProvider();
		}
		if(chunkProvider instanceof ChunkProviderGenerate) {
			featureProvider = ((ChunkProviderGenerate)chunkProvider).featureProvider;
		}
		
		if(featureProvider == null) {
			this.theCommandSender.printMessage(theWorld, "find: no feature provider in this dimension.");
			return -1;
		}
		
		// Resolve the requested feature class. Accepts the simple class name
		// ("FeatureIcePalace") or the fully-qualified name.
		String featureName = tokens[1];
		Class<?> featureClass = FeatureProvider.getRegisteredFeatureClass(featureName);
		if(featureClass == null) {
			this.theCommandSender.printMessage(theWorld, "find: unknown feature '" + featureName + "'.");
			return -1;
		}
		
		this.theCommandSender.printMessage(theWorld, "FINDING ** This may take a WHILE ** sit back.");
		
		// Radius: default 1024 chunks, optionally overridden by a second argument.
		int radius = DEFAULT_SEARCH_RADIUS;
		if(idx > 2) {
			int override = this.toIntWithDefault(tokens[2], -1);
			if(override > 0) radius = override;
		}
		
		// Anchor the search on the player's current chunk.
		int playerChunkX;
		int playerChunkZ;
		if(thePlayer != null) {
			playerChunkX = MathHelper.floor_double(thePlayer.posX) >> 4;
			playerChunkZ = MathHelper.floor_double(thePlayer.posZ) >> 4;
		} else {
			playerChunkX = coordinates.posX >> 4;
			playerChunkZ = coordinates.posZ >> 4;
		}
		
		// Scan the square of chunks within `radius` of the player, running the same
		// deterministic spawn query the chunk generator uses, and keep the closest
		// feature of the requested type.
		Feature closestFeature = null;
		long closestDistanceSq = Long.MAX_VALUE;
		int closestChunkX = 0;
		int closestChunkZ = 0;
		
		for(int dx = -radius; dx <= radius; dx ++) {
			for(int dz = -radius; dz <= radius; dz ++) {
				int chunkX = playerChunkX + dx;
				int chunkZ = playerChunkZ + dz;
				
				Feature feature = featureProvider.getFeatureForChunkCoords(chunkX, chunkZ);

				if(feature == null || feature.getClass() != featureClass) {
					continue;
				}
				
				// Distance from the player's block position to the feature center.
				long deltaX = (long)MathHelper.floor_double(feature.centerX) - (long)MathHelper.floor_double(thePlayer != null ? thePlayer.posX : coordinates.posX);
				long deltaZ = (long)MathHelper.floor_double(feature.centerZ) - (long)MathHelper.floor_double(thePlayer != null ? thePlayer.posZ : coordinates.posZ);
				long distanceSq = deltaX * deltaX + deltaZ * deltaZ;
				if(distanceSq < closestDistanceSq) {
					closestDistanceSq = distanceSq;
					closestFeature = feature;
					closestChunkX = chunkX;
					closestChunkZ = chunkZ;
				}
			}
		}
		
		if(closestFeature == null) {
			this.theCommandSender.printMessage(theWorld, "find: no " + featureClass.getSimpleName() + " within " + radius + " chunks.");
			return -1;
		}
		
		int distanceInBlocks = (int)Math.sqrt(closestDistanceSq);
		int distanceInChunks = (int)Math.ceil(distanceInBlocks / 16.0D);
		this.theCommandSender.printMessage(theWorld, "Closest " + featureClass.getSimpleName() + " @ chunk " + closestChunkX + ", " + closestChunkZ
				+ " (center " + closestFeature.centerX + ", " + closestFeature.centerZ + "), " + distanceInBlocks + " blocks away");
		
		return distanceInChunks;
	}

	@Override
	public String getHelp() {
		return "Finds the closest feature of the given class within a radius (default 1024 chunks).\n"
				+ "/find <FeatureClassName> [<radiusChunks>]\n"
				+ "Feature classes: FeatureVolcano, FeatureAmazonVillage, FeatureOceanRuins, FeatureHollowHill,\n"
				+ "FeatureWreck, FeatureShip, FeatureBigShip, FeatureSlimeBossLair, FeatureIcePalace, FeatureFossil, FeatureStoneArch\n"
				+ "Returns: distance in chunks to the closest match, -1 if none";
	}

}