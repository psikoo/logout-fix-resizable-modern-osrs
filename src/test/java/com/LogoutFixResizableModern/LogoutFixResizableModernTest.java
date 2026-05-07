package com.LogoutFixResizableModern;

import com.LogoutFixResizableModern.LogoutFixResizableModernPlugin;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LogoutFixResizableModernTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LogoutFixResizableModernPlugin.class);
		RuneLite.main(args);
	}
}