package net.minecraft.game.world.terrain.generate.bo3;
import java.nio.file.Path;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import net.minecraft.game.world.World;
import net.minecraft.game.world.terrain.generate.WorldGenerator;
public class WorldGenBo3Tree extends WorldGenerator {
	private static HashMap<String, Bo3Schematic> treePool = new HashMap<String, Bo3Schematic>();
	private static final String bo3ResourceDirectory = "/resources/bo3";
	private static final String bo3ResourceDirectoryTest = "/resources/unusedbo3";
	private String treeName = "";
	private boolean forceSpawn;
	private int leavesMeta = 0;
	private boolean extendBottom = false;
	
	private static boolean loadUnused = false;
	private static boolean debug = false;
	private static int added;

	public WorldGenBo3Tree() {
		this("", false);
	}

	public WorldGenBo3Tree(boolean forceSpawn) {
		this("", forceSpawn);
	}

	public WorldGenBo3Tree(String treeName) {
		this(treeName, false);
	}

	public WorldGenBo3Tree(String treeName, boolean forceSpawn) {
		this.treeName = treeName;
		this.setForceSpawn(forceSpawn);
	}

	/*
	 * This is a raw, broken, incomplete & limited implementation of BO3. Files read
	 * from the folder, parsed & stored as Bo3Schematic objects, which only supports
	 * a very, very limited subset of BO3 features.
	 */

	public void setTreeName(String treeName) {
		this.treeName = treeName;
	}

	public WorldGenBo3Tree withLeavesMeta(int meta) {
		this.leavesMeta = meta;
		return this;
	}
	
	@Override
	public boolean generate(World world, Random rand, int x, int y, int z) {
		if (this.treeName == null || "".equals(this.treeName))
			return false;

		Bo3Schematic schematic = treePool.get(this.treeName);

		if (schematic == null) {
			System.out.println(this.treeName + " does not exist!");
			return false;
		}
		
		schematic.withLeavesMeta(this.leavesMeta);

		// Random rotation
		schematic.setOrthoAngle(rand.nextInt(4));
		
		// Extend bottom
		schematic.setExtendBottom(this.extendBottom);

		if (forceSpawn || schematic.canSpawnHere(world, x, y, z)) {
			schematic.render(world, rand, x, y, z);
			return true;
		}

		return false;
	}

	boolean generateRandomTreeOf(String category, World world, Random rand, int x, int y, int z) {
		this.treeName = getRamdomTreeTypeOf(rand, category);
		return this.generate(world, rand, x, y, z);
	}
	
	public boolean isForceSpawn() {
		return forceSpawn;
	}

	public void setForceSpawn(boolean forceSpawn) {
		this.forceSpawn = forceSpawn;
	}
	
	static void readAndParse(String directory) {
		// Read & parse all schematics
				URI uri;
				
				URL resource;
				resource = WorldGenBo3Tree.class.getResource("/terrain.png");
				if (resource == null) {
					resource = WorldGenBo3Tree.class.getResource(directory);
					if(resource == null) throw new IllegalArgumentException("Not found: " + directory);
				}
			
				try {
					uri = resource.toURI();
	
					Path myPath;
					if (uri.getScheme().equals("jar")) {
						System.out.println ("Reading BO3 from JAR");
						FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
						myPath = fileSystem.getPath(directory);
					} else {
						System.out.println ("Reading BO3 from the file system");
						uri = WorldGenBo3Tree.class.getResource(directory).toURI();
						myPath = Paths.get(uri);
					}

					WorldGenBo3Tree.added = 0;
					Files.walkFileTree(myPath, new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

							// 1st tokenize file URI to get just the object name
							// This code is extremely unsafe but the data set is extremely controlled
							// so nothing more fancy is needed. Sorry!

							StringTokenizer tokenizer = new StringTokenizer(file.toString(), File.separator);
							String lastPart = "a.b";
							String prevPart = "none";
							while (tokenizer.hasMoreTokens()) {
								prevPart = lastPart;
								lastPart = tokenizer.nextToken();
							}
							tokenizer = new StringTokenizer(lastPart, ".");
							String name = tokenizer.nextToken();

							// Parse schematic
							Bo3Schematic schematic = new Bo3Schematic().fromFile(file).withCategoryAndName(prevPart, name);

							// Put in pool
							treePool.put(schematic.toString(), schematic);

							if(debug) System.out.println("Added " + schematic.toString());
							WorldGenBo3Tree.added ++;

							return FileVisitResult.CONTINUE;
						}
					});
					
					System.out.println ("Loaded " + WorldGenBo3Tree.added + " BO3 trees.");
				} catch (Exception e) {
					e.printStackTrace();
				}
	}

	static {
		readAndParse(bo3ResourceDirectory);
		
		if(loadUnused) {
			readAndParse(bo3ResourceDirectoryTest);
		}
	}

	public static String getRamdomTreeTypeOf(Random rand, String category) {
		List<String> thisCategoryTrees = new ArrayList<String> ();
		
		for(String treeName : treePool.keySet()) {
			if (treeName.startsWith(category)) {
				thisCategoryTrees.add(treeName.substring(treeName.indexOf(':') + 1));
			}
		}
		
		return thisCategoryTrees.get(rand.nextInt(thisCategoryTrees.size()));
	}

	public void setExtendBottom(boolean b) {
		this.extendBottom = b;
	}

}

