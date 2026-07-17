package com.audioviking.distractedcitizens;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

public class DistractedCitizensOverlay extends Overlay
{
	private final Client client;
	private final DistractedCitizensPlugin plugin;
	private final DistractedCitizensConfig config;

	@Inject
	private DistractedCitizensOverlay(
		Client client,
		DistractedCitizensPlugin plugin,
		DistractedCitizensConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Color color = config.highlightColor();
		Color fill = new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(50, color.getAlpha()));

		for (NPC npc : plugin.getDistractedCitizens())
		{
			if (config.highlightHull())
			{
				Shape hull = npc.getConvexHull();
				if (hull != null)
				{
					graphics.setColor(fill);
					graphics.fill(hull);
					graphics.setColor(color);
					graphics.draw(hull);
				}
			}

			if (config.highlightTile())
			{
				LocalPoint localPoint = npc.getLocalLocation();
				Polygon tile = Perspective.getCanvasTilePoly(client, localPoint);
				if (tile != null)
				{
					OverlayUtil.renderPolygon(graphics, tile, color);
				}
			}
		}

		return null;
	}
}
