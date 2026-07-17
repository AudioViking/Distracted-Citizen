package com.audioviking.distractedcitizens;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(DistractedCitizensConfig.GROUP)
public interface DistractedCitizensConfig extends Config
{
	String GROUP = "distractedcitizens";

	@ConfigItem(
		keyName = "voiceAlert",
		name = "Voice alert",
		description = "Say 'Distracted citizen' when a wealthy citizen becomes distracted"
	)
	default boolean voiceAlert()
	{
		return true;
	}

	@ConfigItem(
		keyName = "desktopNotification",
		name = "RuneLite notification",
		description = "Also send a standard RuneLite notification"
	)
	default boolean desktopNotification()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightHull",
		name = "Highlight hull",
		description = "Draw an outline around the distracted citizen"
	)
	default boolean highlightHull()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightTile",
		name = "Highlight tile",
		description = "Highlight the tile beneath the distracted citizen"
	)
	default boolean highlightTile()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "highlightColor",
		name = "Highlight color",
		description = "Color used for distracted citizens"
	)
	default Color highlightColor()
	{
		return new Color(0, 255, 255, 180);
	}
}
