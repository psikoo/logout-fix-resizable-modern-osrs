package com.LogoutFixResizableModern;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.inject.Provides;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpriteID;

import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(name = "Logout Fix Resizable Modern")
public class LogoutFixResizableModernPlugin extends Plugin
{
	private Map<String, Widget> stones = new HashMap<>();
	private Map<String, Widget> icons = new HashMap<>();
	private Map<String, Integer> sides = new HashMap<>();
	private Map<String, Widget> nStones = new HashMap<>();
	private Map<String, Widget> nIcons = new HashMap<>();

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private LogoutFixResizableModernConfig config;

	@Provides
	LogoutFixResizableModernConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(LogoutFixResizableModernConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		clientThread.invokeLater(() -> startPlugin());
	}

	@Override
	protected void shutDown() throws Exception
	{
		stopPlugin();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (!configChanged.getGroup().equals("LogoutFixResizableModern")) return;
		stopPlugin();
		clientThread.invokeLater(() -> startPlugin());
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		// Start plugin when on modern resizable
		if (event.getGroupId() == InterfaceID.TOPLEVEL_PRE_EOC) clientThread.invokeLater(() -> startPlugin());
		// Hide stones and icons while bank is open
		if (event.getGroupId() == InterfaceID.BANKMAIN) nSetHiddenAllStonesAndIcons(true);
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		// Stop plugin when not on modern resizable
		if (event.getGroupId() == InterfaceID.TOPLEVEL_PRE_EOC) clientThread.invokeLater(() -> stopPlugin());
		// Show stones and icons when the bank closes
		if (event.getGroupId() == InterfaceID.BANKMAIN) nSetHiddenAllStonesAndIcons(false);
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		// Prevent the old stones and icons from showing up
		if (event.getScriptId() == 903)
		{
			setHiddenAllStonesAndIcons(true);
		}
		// Update the logout stone sprite
		// Open/change/close side tab events (905 = fkey)
		if (event.getScriptId() == 902 || event.getScriptId() == 905 || event.getScriptId() == 914)
		{
			updateStoneSprite();
			setHiddenAllStonesAndIcons(true);
		}
		// Change menu inside side menu
		//                                    friends, ignore, grouping, sailing, cc,   clan
		Set<Integer> ICON_CHANGE_IDS = Set.of(123,     127,    432,      489,     1656, 4395);
		if (ICON_CHANGE_IDS.contains(event.getScriptId())) {
			clientThread.invokeLater(() -> startPlugin());
		}
	}

	private void startPlugin() 
	{
		if (client.getGameState() != GameState.LOGGED_IN) return;
		// Delete any existing widgets
		Widget sideStatic = client.getWidget(InterfaceID.ToplevelPreEoc.SIDE_STATIC_LAYER);
		if (sideStatic != null) sideStatic.deleteAllChildren();
		// Ensure stones, icons and sides are empty
		stones.clear();
		icons.clear();
		sides.clear();
		// Populate stones and icons array
		stones.put("10", client.getWidget(InterfaceID.ToplevelPreEoc.STONE10));
		icons.put("10", client.getWidget(InterfaceID.ToplevelPreEoc.ICON10));
		sides.put("10", InterfaceID.ToplevelPreEoc.SIDE10);
		if(config.reorderStones()) 
		{
			stones.put("7", client.getWidget(InterfaceID.ToplevelPreEoc.STONE7));
			stones.put("8", client.getWidget(InterfaceID.ToplevelPreEoc.STONE8));
			stones.put("9", client.getWidget(InterfaceID.ToplevelPreEoc.STONE9));
			stones.put("11", client.getWidget(InterfaceID.ToplevelPreEoc.STONE11));
			stones.put("12", client.getWidget(InterfaceID.ToplevelPreEoc.STONE12));
			stones.put("13", client.getWidget(InterfaceID.ToplevelPreEoc.STONE13));
			icons.put("7", client.getWidget(InterfaceID.ToplevelPreEoc.ICON7));
			icons.put("8", client.getWidget(InterfaceID.ToplevelPreEoc.ICON8));
			icons.put("9", client.getWidget(InterfaceID.ToplevelPreEoc.ICON9));
			icons.put("11", client.getWidget(InterfaceID.ToplevelPreEoc.ICON11));
			icons.put("12", client.getWidget(InterfaceID.ToplevelPreEoc.ICON12));
			icons.put("13", client.getWidget(InterfaceID.ToplevelPreEoc.ICON13));
			sides.put("7", InterfaceID.ToplevelPreEoc.SIDE7);
			sides.put("8", InterfaceID.ToplevelPreEoc.SIDE8);
			sides.put("9", InterfaceID.ToplevelPreEoc.SIDE9);
			sides.put("11", InterfaceID.ToplevelPreEoc.SIDE11);
			sides.put("12", InterfaceID.ToplevelPreEoc.SIDE12);
			sides.put("13", InterfaceID.ToplevelPreEoc.SIDE13);
		}
		// Hide stones and icons
		setHiddenAllStonesAndIcons(true);
		// Ensure nStones and nIcons are empty
		nStones.clear();
		nIcons.clear();
		// Populate all nStones and nIcons array
		if(!config.reorderStones()) 
		{
			nStones.put("10", cloneWidget(sideStatic, stones.get("10"),                     -1, 33, 36, 0, 0));
			nIcons.put("10",  cloneWidget(sideStatic, icons.get("10") , SpriteID.SideIcons._10, 33, 36, 0, 0));
		}
		else
		{
			nStones.put("10", cloneWidget(sideStatic, stones.get("10"),                     -1, 33, 36, 33*3, 0));
			nIcons.put("10",  cloneWidget(sideStatic, icons.get("10") , SpriteID.SideIcons._10, 33, 36, 33*3, 0));
			nStones.put("7",  cloneWidget(sideStatic, stones.get("7"),  -2, 33, 36, 33*0, 0));
			nIcons.put("7",   cloneWidget(sideStatic, icons.get("7"),   -2, 33, 36, 33*0, 0));
			nStones.put("9",  cloneWidget(sideStatic, stones.get("9"),  -2, 33, 36, 33*1, 0));
			nIcons.put("9",   cloneWidget(sideStatic, icons.get("9"),   -2, 33, 36, 33*1, 0));
			nStones.put("8",  cloneWidget(sideStatic, stones.get("8"),  -2, 33, 36, 33*2, 0));
			nIcons.put("8",   cloneWidget(sideStatic, icons.get("8"),   -2, 33, 36, 33*2, 0));
			nStones.put("11", cloneWidget(sideStatic, stones.get("11"), -2, 33, 36, 33*4, 0));
			nIcons.put("11",  cloneWidget(sideStatic, icons.get("11"),  -2, 33, 36, 33*4, 0));
			nStones.put("12", cloneWidget(sideStatic, stones.get("12"), -2, 33, 36, 33*5, 0));
			nIcons.put("12",  cloneWidget(sideStatic, icons.get("12"),  -2, 33, 36, 33*5, 0));
			nStones.put("13", cloneWidget(sideStatic, stones.get("13"), -2, 33, 36, 33*6, 0));
			nIcons.put("13",  cloneWidget(sideStatic, icons.get("13"),  -2, 33, 36, 33*6, 0));
		}
		updateStoneSprite();
	}

	private void stopPlugin() 
	{
		if (client.getGameState() != GameState.LOGGED_IN) return;
		// Show the all the original stones
		setHiddenAllStonesAndIcons(false);
		// Remove the custom stones
		Widget sideStatic = client.getWidget(InterfaceID.ToplevelPreEoc.SIDE_STATIC_LAYER);
		if (sideStatic != null) sideStatic.deleteAllChildren();
	}

	private Widget cloneWidget(Widget parent, Widget source, int spriteID, int width, int height, int originalX, int originalY) 
	{
		Widget nWidget = parent.createChild(-1, source.getType());
		nWidget.setSpriteId(spriteID != -2? spriteID : source.getSpriteId());
		nWidget.setOriginalWidth(width);
		nWidget.setOriginalHeight(height);
		nWidget.setOriginalX(originalX);
		nWidget.setOriginalY(originalY);
		nWidget.setHasListener(source.hasListener());
		nWidget.setOnOpListener(source.getOnOpListener());
		// Copy all actions if any
		String[] actions = source.getActions();
		if (actions != null) 
		{
			for (int i = 0; i < actions.length; i++) nWidget.setAction(i, actions[i]);
		}
		nWidget.revalidate();
		return nWidget;
	}
	
	private void updateStoneSprite() 
	{
		// Check if any sides are open
		sides.forEach((key, value) -> {
			Widget side = client.getWidget(value);
			boolean isOpen = side != null && !side.isHidden();
			int targetSprite = isOpen ? SpriteID.PreEocStones._1 : -1;
			if (nStones.get(key).getSpriteId() != targetSprite) 
			{
				nStones.get(key).setSpriteId(targetSprite);
			}
		});
	}

	private void setHiddenAllStonesAndIcons(boolean hidden) 
	{
		for (Widget stone : stones.values()) stone.setHidden(hidden);
		for (Widget icon : icons.values()) icon.setHidden(hidden);
	}

	private void nSetHiddenAllStonesAndIcons(boolean hidden) 
	{
		for (Widget stone : nStones.values()) stone.setHidden(hidden);
		for (Widget icon : nIcons.values()) icon.setHidden(hidden);
	}
}
