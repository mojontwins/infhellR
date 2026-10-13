package net.minecraft.game.world.terrain.generate.feature.mineshaft;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.game.world.World;
import net.minecraft.game.world.block.Block;
import net.minecraft.game.world.terrain.generate.feature.FeatureAABB;
import net.minecraft.game.world.terrain.generate.feature.structure.StructurePiece;

/** The starting room of a mineshaft; every other passage hangs off this piece. */
public class MineRoomPiece extends StructurePiece {
	private final List<FeatureAABB> connectorBoxes = new ArrayList<FeatureAABB>();

	public MineRoomPiece(Random rand, int absoluteX, int absoluteZ) {
		super(0);
		this.box = new FeatureAABB(absoluteX, 50, absoluteZ, absoluteX + 7 + rand.nextInt(6), 54 + rand.nextInt(6), absoluteZ + 7 + rand.nextInt(6));
	}

	@Override
	public void buildConnections(StructurePiece parent, List<StructurePiece> pieces, Random rand) {
		int innerHeight = this.box.getYSize() - 3 - 1;
		if(innerHeight <= 0) {
			innerHeight = 1;
		}

		int offset;
		StructurePiece child;
		FeatureAABB childBox;
		for(offset = 0; offset < this.box.getXSize(); offset += 4) {
			offset += rand.nextInt(this.box.getXSize());
			if(offset + 3 > this.box.getXSize()) {
				break;
			}

			child = this.owner.requestNextPiece(this, pieces, rand, this.box.x1 + offset, this.box.y1 + rand.nextInt(innerHeight) + 1, this.box.z1 - 1, 2, this.getPieceType());
			if(child != null) {
				childBox = child.getBox();
				this.connectorBoxes.add(new FeatureAABB(childBox.x1, childBox.y1, this.box.z1, childBox.x2, childBox.y2, this.box.z1 + 1));
				child.buildConnections(this, pieces, rand);
			}
		}

		for(offset = 0; offset < this.box.getXSize(); offset += 4) {
			offset += rand.nextInt(this.box.getXSize());
			if(offset + 3 > this.box.getXSize()) {
				break;
			}

			child = this.owner.requestNextPiece(this, pieces, rand, this.box.x1 + offset, this.box.y1 + rand.nextInt(innerHeight) + 1, this.box.z2 + 1, 0, this.getPieceType());
			if(child != null) {
				childBox = child.getBox();
				this.connectorBoxes.add(new FeatureAABB(childBox.x1, childBox.y1, this.box.z2 - 1, childBox.x2, childBox.y2, this.box.z2));
				child.buildConnections(this, pieces, rand);
			}
		}

		for(offset = 0; offset < this.box.getZSize(); offset += 4) {
			offset += rand.nextInt(this.box.getZSize());
			if(offset + 3 > this.box.getZSize()) {
				break;
			}

			child = this.owner.requestNextPiece(this, pieces, rand, this.box.x1 - 1, this.box.y1 + rand.nextInt(innerHeight) + 1, this.box.z1 + offset, 1, this.getPieceType());
			if(child != null) {
				childBox = child.getBox();
				this.connectorBoxes.add(new FeatureAABB(this.box.x1, childBox.y1, childBox.z1, this.box.x1 + 1, childBox.y2, childBox.z2));
				child.buildConnections(this, pieces, rand);
			}
		}

		for(offset = 0; offset < this.box.getZSize(); offset += 4) {
			offset += rand.nextInt(this.box.getZSize());
			if(offset + 3 > this.box.getZSize()) {
				break;
			}

			child = this.owner.requestNextPiece(this, pieces, rand, this.box.x2 + 1, this.box.y1 + rand.nextInt(innerHeight) + 1, this.box.z1 + offset, 3, this.getPieceType());
			if(child != null) {
				childBox = child.getBox();
				this.connectorBoxes.add(new FeatureAABB(this.box.x2 - 1, childBox.y1, childBox.z1, this.box.x2, childBox.y2, childBox.z2));
				child.buildConnections(this, pieces, rand);
			}
		}
	}

	@Override
	public boolean generate(World world, Random rand, FeatureAABB clip, boolean mostlySolid) {
		if(this.hasLiquidInBox(world, clip)) {
			return false;
		}
		if(mostlySolid && !this.isMostlySolid(world, clip)) {
			return false;
		}

		this.fillWithBlockVariation(world, clip, this.box.x1, this.box.y1, this.box.z1, this.box.x2, this.box.y1, this.box.z2, Block.dirt.blockID, Block.adobe.blockID, rand, 2, true);
		this.fillWithBlocks(world, clip, this.box.x1, this.box.y1 + 1, this.box.z1, this.box.x2, Math.min(this.box.y1 + 3, this.box.y2), this.box.z2, 0, 0, false);

		for(FeatureAABB connector : this.connectorBoxes) {
			this.fillWithBlocks(world, clip, connector.x1, connector.y2 - 2, connector.z1, connector.x2, connector.y2, connector.z2, 0, 0, false);
		}

		this.randomlyRareFillWithBlocks(world, clip, this.box.x1, this.box.y1 + 4, this.box.z1, this.box.x2, this.box.y2, this.box.z2, 0, false);
		return true;
	}
}
