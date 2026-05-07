package com.LogoutFixResizableModern;

import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Logout Fix Resizable Modern"
)
public class LogoutFixResizableModernPlugin extends Plugin
{
	private Widget newStone;
	private Widget newIcon;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Override
	protected void startUp() throws Exception
	{
		log.debug("LogoutFixResizableModern started");
		clientThread.invokeLater(() -> updateUIComponents());
	}

	@Override
	protected void shutDown() throws Exception
	{
		if (client.getGameState() != GameState.LOGGED_IN) return;
		// Get widgets
		Widget sideStatic = client.getWidget(InterfaceID.ToplevelPreEoc.SIDE_STATIC_LAYER);
		Widget stoneSource = client.getWidget(InterfaceID.ToplevelPreEoc.STONE10);
		Widget iconSource = client.getWidget(InterfaceID.ToplevelPreEoc.ICON10);
		if (sideStatic == null || stoneSource == null || iconSource == null) return;
		// Hide X button
		sideStatic.deleteAllChildren();
		stoneSource.setHidden(false);
		iconSource.setHidden(false);
		log.debug("LogoutFixResizableModern stopped");
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.TOPLEVEL_PRE_EOC)
		{
			clientThread.invokeLater(() -> updateUIComponents());
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (event.getScriptId() == 902 || event.getScriptId() == 914)
		{
			// Update the logout stone
			updateStoneSprite();
			// Prevent the X button from showing up again
			Widget stoneSource = client.getWidget(InterfaceID.ToplevelPreEoc.STONE10);
			Widget iconSource = client.getWidget(InterfaceID.ToplevelPreEoc.ICON10);
			stoneSource.setHidden(true);
			iconSource.setHidden(true);
		}
	}

	private void updateUIComponents()
	{
		if (client.getGameState() != GameState.LOGGED_IN) return;
		// Get widgets
		Widget sideStatic = client.getWidget(InterfaceID.ToplevelPreEoc.SIDE_STATIC_LAYER);
		Widget stoneSource = client.getWidget(InterfaceID.ToplevelPreEoc.STONE10);
		Widget iconSource = client.getWidget(InterfaceID.ToplevelPreEoc.ICON10);
		if (sideStatic == null || stoneSource == null || iconSource == null) return;
		// Hide X button
		stoneSource.setHidden(true);
		iconSource.setHidden(true);
		// Prevent duplicate logout buttons
		if (sideStatic.getChildren() != null && sideStatic.getChildren().length > 0) return;
		// Create new widgets
		newStone = sideStatic.createChild(-1, stoneSource.getType());
		newIcon = sideStatic.createChild(-1, iconSource.getType());
		// Setup Stone
		if (newStone != null)
		{
			newStone.setSpriteId(stoneSource.getSpriteId());
			newStone.setOriginalWidth(33);
			newStone.setOriginalHeight(36);
			newStone.setOriginalX(0);
			newStone.setOriginalY(0);
			// Copy logout functionality
			newStone.setOnOpListener((JavaScriptCallback) ev -> clientThread.invokeLater(() -> updateStoneSprite()));
			newStone.setHasListener(true);
			newStone.setOnOpListener(stoneSource.getOnOpListener());
			newStone.setAction(0, stoneSource.getActions()[0]);
			newStone.revalidate();
		}
		// Setup icon
		if (newIcon != null)
		{
			newIcon.setSpriteId(SpriteID.SideIcons._10);
			newIcon.setOriginalWidth(33);
			newIcon.setOriginalHeight(36);
			newIcon.setOriginalX(0);
			newIcon.setOriginalY(0);
			newIcon.revalidate();
		}
	}

	private void updateStoneSprite() 
	{
		if (newStone == null) return;
		// Check if the logoutPanel is open
		Widget logoutPanel = client.getWidget(InterfaceID.ToplevelPreEoc.SIDE10);
		boolean isOpen = logoutPanel != null && !logoutPanel.isHidden();
		// Set the correct sprite
		int targetSprite = isOpen ? SpriteID.PreEocStones._1 : -1;
		if (newStone.getSpriteId() != targetSprite) 
		{
			newStone.setSpriteId(targetSprite);
		}
	}
}
