package net.minecraft.game;

public class Direction { 
	//                                     S   W   N   E   U   D   -    SW  NW  NE  SE
	public static final int offsetX[] = {  0, -1,  0,  1,  0,  0,  0,   -1, -1,  1,  1 };
	public static final int offsetZ[] = {  1,  0, -1,  0,  0,  0,  0,    1, -1, -1,  1 };
	public static final int offsetY[] = {  0,  0,  0,  0,  1, -1,  0,    0,  0,  0,  0 };

	public static final int headInvisibleFace[] = { 3, 4, 2, 5 };
	public static final int vineGrowth[] = { -1, -1, 2, 0, 1, 3 };
	public static final int footInvisibleFaceRemap[] = { 2, 3, 0, 1 };	

	public static final int SOUTH = 0;
	public static final int WEST = 1;
	public static final int NORTH = 2;
	public static final int EAST = 3;
	
	public static final int UP = 4;
	public static final int DOWN = 5;
	
	public static final int NONE = 6;
	
	public static final int SW = 7, NW = 8, NE = 9, SE = 10;
	
	public static final int HORZ_PLANE[] = {SOUTH, WEST, NORTH, EAST};
	public static final int VERT_AXIS[] = {UP, NONE, DOWN};
	
	public static final int HORZ_PLANE_FULL[] = {SOUTH, SW, WEST, NW, NORTH, NE, EAST, SE};
	public static final int HORZ_PLANE_DIAGONALS[] = {SW, NW, NE, SE};

	public static int getCW(int direction) {
		if(direction < 4) return (direction + 1) & 3;
		else if(direction > 6) return 7 + ((direction - 6) & 3);
		return direction;
	}
	
}
