package com.ocelot.mod.game.core.level.tile;

import com.ocelot.mod.game.core.level.TileMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class AirTile extends Tile {

	public AirTile() {
		this.setShouldNotRender();
	}

	@Override
	public void update() {
	}

	@Override
	public void render(int x, int y, TileMap tileMap, Gui gui, Minecraft mc, int mouseX, int mouseY, float partialTicks) {
	}
}