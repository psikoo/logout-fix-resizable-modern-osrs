package com.LogoutFixResizableModern;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("LogoutFixResizableModern")
public interface LogoutFixResizableModernConfig extends Config
{
	@ConfigItem(
		keyName = "reorderStones",
		name = "Reorder Stones",
		description = "Order stones to have the same order as other layouts"
	)
	default boolean reorderStones()
	{
		return false;
	}
}
