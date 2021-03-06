package com.ocelot.mod.game.core.level;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.ocelot.mod.game.Game;
import com.ocelot.mod.game.core.level.tile.Tile;
import com.ocelot.mod.game.core.level.tile.property.IProperty;
import com.ocelot.mod.game.core.level.tile.property.TileStateContainer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;

/**
 * <em><b>Copyright (c) 2018 Ocelot5836.</b></em>
 * 
 * <br>
 * </br>
 * 
 * A class to hold the tiles in a level. It reloads the map each time the resource manager relods.
 * 
 * @author Ocelot5836
 */
public class TileMap implements IResourceManagerReloadListener {

	private double x;
	private double y;

	private int xmin;
	private int ymin;
	private int xmax;
	private int ymax;

	/** The speed at which the tilemap moves at */
	private double tween;

	private ResourceLocation mapLocation;
	private TileStateContainer[] containers;
	private int[] map;
	private int tileSize;
	private int numRows;
	private int numCols;
	private int width;
	private int height;

	private int rowOffset;
	private int colOffset;
	private int numRowsToDraw;
	private int numColsToDraw;

	/**
	 * Creates a new map of tiles with the specified size for each tile.
	 * 
	 * @param tileSize
	 *            The size for each tile
	 */
	public TileMap(int tileSize) {
		this.tileSize = tileSize;
		this.numRowsToDraw = Game.HEIGHT / tileSize + 2;
		this.numColsToDraw = Game.WIDTH / tileSize + 2;
		this.tween = 0.07;
	}

	/**
	 * Loads the map from the specified location into memory.
	 * 
	 * @param mapLocation
	 *            The location of the map in files
	 */
	public void loadMap(ResourceLocation mapLocation) {
		this.mapLocation = mapLocation;
		String loadedTile = "null";
		int lastX = 0;
		int lastY = 0;
		try {
			InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(mapLocation).getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			this.numCols = Integer.parseInt(br.readLine());
			this.numRows = Integer.parseInt(br.readLine());
			this.map = new int[numCols * numRows];
			this.containers = new TileStateContainer[map.length];
			this.width = numCols * tileSize;
			this.height = numRows * tileSize;

			this.xmin = 0;
			this.xmax = width - Game.WIDTH;
			this.ymin = 0;
			this.ymax = height - Game.HEIGHT;

			for (int y = 0; y < numRows; y++) {
				String line = br.readLine();
				String[] tokens = line.split(" ");
				for (int x = 0; x < numCols; x++) {
					map[x + y * numCols] = Integer.parseInt(tokens[x]);
					containers[x + y * numCols] = getTile(x, y).createContainer();
					loadedTile = tokens[x];
					lastX = y;
					lastY = x;
				}
			}
		} catch (Exception e) {
			Game.stop(e, "Could not load map " + mapLocation + "! Tile errored at " + loadedTile + "-" + lastX + ":" + lastY);
		}
	}

	/**
	 * Sets the position of the map to the specified position. Moves smoothly from the old position to the new position using the {@link #tween} value.
	 * 
	 * @param x
	 *            The new x position of the map
	 * @param y
	 *            The new y position of the map
	 */
	public void setPosition(double x, double y) {
		if (x < xmin)
			x = xmin;
		if (x > xmax)
			x = xmax;
		if (y < ymin)
			y = ymin;
		if (y > ymax)
			y = ymax;

		this.x += (x - this.x) * this.tween;
		this.y += (y - this.y) * this.tween;

		this.colOffset = (int) this.x / tileSize;
		this.rowOffset = (int) this.y / tileSize;
	}

	/**
	 * Updates the tiles in the tilemap.
	 */
	public void update() {
		for (int y = rowOffset; y < rowOffset + numRowsToDraw; y++) {
			for (int x = colOffset; x < colOffset + numColsToDraw; x++) {
				if (x >= numCols)
					break;

				if (x > 0 && x < numCols && y > 0 && y < numRows) {
					this.getTile(x, y).setContainer(containers[x + y * numCols]);
				}

				this.getTile(x, y).update();
			}
		}
	}

	/**
	 * Renders the tiles in the tilemap.
	 * 
	 * @param gui
	 *            A gui instance
	 * @param mc
	 *            A minecraft instance
	 * @param mouseX
	 *            The x position of the mouse
	 * @param mouseY
	 *            The y position of the mouse
	 * @param partialTicks
	 *            The partial ticks
	 */
	public void render(Gui gui, Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		for (int y = rowOffset - 1; y < rowOffset + numRowsToDraw; y++) {
			for (int x = colOffset - 1; x < colOffset + numColsToDraw; x++) {
				if (x >= numCols)
					break;

				if (!getTile(x, y).shouldRender())
					continue;

				this.getTile(x, y).render((int) -this.x + x * this.tileSize, (int) -this.y + y * this.tileSize, this, gui, mc, mouseX, mouseY, partialTicks);
			}
		}
	}

	/**
	 * @return The size of each tile in the tilemap
	 */
	public int getTileSize() {
		return tileSize;
	}

	/**
	 * @return The x position of the tilemap
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return The y position of the tilemap
	 */
	public double getY() {
		return y;
	}

	/**
	 * @return The width in pixels of the tilemap
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return The height in pixels of the tilemap
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return The width in tiles of the tilemap
	 */
	public int getNumCols() {
		return numCols;
	}

	/**
	 * @return The height in tiles of the tilemap
	 */
	public int getNumRows() {
		return numRows;
	}

	/**
	 * Tries to fetch a tile at the specified pos. If is is null or out of bounds, it returns {@link Tile#VOID}.
	 * 
	 * @param x
	 *            The x position of the tile
	 * @param y
	 *            The y position of the tile
	 * @return The tile found at that pos
	 */
	public Tile getTile(int x, int y) {
		if (x < 0 || x >= numCols || y < 0 || y >= numRows)
			return Tile.VOID;
		if (Tile.TILES[map[x + y * numCols]] == null)
			return Tile.VOID;
		return Tile.TILES[map[x + y * numCols]];
	}

	/**
	 * Tries to set a tile at the specified pos. If is is of bounds it returns and does nothing.
	 * 
	 * @param x
	 *            The x position of the tile
	 * @param y
	 *            The y position of the tile
	 * @param tile
	 *            The new tile to be placed at that position
	 */
	public void setTile(int x, int y, Tile tile) {
		if (x < 0 || x >= numCols || y < 0 || y >= numRows)
			return;
		map[x + y * numCols] = tile.getId();
		containers[x + y * numCols] = tile.createContainer();
	}

	/**
	 * Tries to set a tile property at the specified pos. If is is of bounds or null, it returns and does nothing.
	 * 
	 * @param x
	 *            The x position of the tile
	 * @param y
	 *            The y position of the tile
	 * @param property
	 *            The property to fetch
	 * @param value
	 *            The new value for the object
	 * @throws IllegalArgumentException
	 *             If the property does not exist in the tile's {@link TileStateContainer} at the specified pos
	 */
	public void setValue(int x, int y, IProperty property, Object value) {
		if (x < 0 || x >= numCols || y < 0 || y >= numRows || containers[x + y * numCols] == null)
			return;
		containers[x + y * numCols].setValue(property, value);
	}

	/**
	 * @return The speed at which the tilemap moves at
	 */
	public double getTween() {
		return tween;
	}

	/**
	 * Sets the speed at which the tilemap moves at.
	 * 
	 * @param tween
	 *            The new value
	 */
	public TileMap setTween(double tween) {
		this.tween = tween;
		return this;
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		this.loadMap(this.mapLocation);
	}
}