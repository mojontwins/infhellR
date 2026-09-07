package net.minecraft.game;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import net.minecraft.game.world.World;

public class Seasons {
	public static final int WINTER = 0;
	public static final int SPRING = 1;
	public static final int SUMMER = 2;
	public static final int AUTUMN = 3;
	
	public static final int SEASON_DURATION = 7;
	public static final String seasonNames[] = new String[] { "Winter", "Spring", "Summer", "Autumn" };
	public static final float dayLengths[] = new float [] { 0.4F, 0.5F, 0.6F, 0.5F };
	
	private static final int colorizerLeaves0[] = new int [SEASON_DURATION * 4];
	private static final int colorizerSky0[] = new int [SEASON_DURATION * 4];
	private static final int colorizerFog0[] = new int [SEASON_DURATION * 4];
	
	World world;
	public static int dayOfTheYear = 12;
	public static int currentSeason;
	public static int prevSeason;
	public static int nextSeason;
	public static int dayOfTheSeason;
	public static int dayLengthTicks;	
	public static int nightLengthTicks;
	public static float seasonProgress;
	
	public Seasons(World world) {
		this.world = world;
	}
	
	public static void updateSeasonCounters() {
		dayOfTheYear = dayOfTheYear % (SEASON_DURATION * 4);
		dayOfTheSeason = dayOfTheYear % SEASON_DURATION;
		currentSeason = dayOfTheYear / SEASON_DURATION;
		prevSeason = currentSeason - 1; if(prevSeason < 0) prevSeason += 4;
		nextSeason = currentSeason + 1; if(nextSeason > 3) nextSeason -= 4;
		seasonProgress = (float)(1 + dayOfTheSeason) / (float)SEASON_DURATION;
			
		// Linear interpolation code suggested by Jonkadelic
		int otherSeason = seasonProgress < 0.5F ? prevSeason : nextSeason;
		
		float currDayLength = dayLengths[currentSeason];
		float otherDayLength = dayLengths[otherSeason];
		
		// Distance to center
		float blendFactor = Math.abs(seasonProgress - 0.5F);
		
		// Linear interpolation
		float dayLength = currDayLength + ((otherDayLength - currDayLength) * blendFactor);
		float nightLength = 1 - dayLength;
		
		dayLengthTicks = (int)(24000 * dayLength);
		nightLengthTicks = (int)(24000 * nightLength);
	}
	
	public static String getStringForGui() {
		return seasonNames[currentSeason] + ", day " + (1 + dayOfTheSeason) + " [" + dayLengthTicks + "]";
	}
	
	public static int getLeavesColorForToday() {
		return colorizerLeaves0[dayOfTheYear];
	}
	
	public static int getFogColorForToday() {
		return colorizerFog0[dayOfTheYear];
	}
	
	public static int getSkyColorForToday() {
		return colorizerSky0[dayOfTheYear];
	}
	
	public static void loadRamp(String resource, int[] ramp) {
		try {
			BufferedImage bufferedImage = ImageIO.read(Seasons.class.getResource(resource));
			bufferedImage.getRGB(0, 0, 28, 1, ramp, 0, 18);
		} catch (Exception var1) {
			//var1.printStackTrace();
		}
	}
	
	static {
		loadRamp("/seasons/leavesColor0.png", colorizerLeaves0);
		loadRamp("/seasons/skyColor0.png", colorizerSky0);
		loadRamp("/seasons/fogColor0.png", colorizerFog0);
	}
}


