package com.audioviking.distractedcitizens;

import com.google.inject.Provides;
import java.io.BufferedInputStream;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import javax.inject.Inject;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Notification;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Distracted Citizens",
	description = "Highlights distracted wealthy citizens in Varlamore and announces them",
	tags = {"varlamore", "thieving", "wealthy", "citizen", "notification", "accessibility"}
)
public class DistractedCitizensPlugin extends Plugin
{
	private static final String WEALTHY_CITIZEN = "Wealthy citizen";

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DistractedCitizensOverlay overlay;

	@Inject
	private DistractedCitizensConfig config;

	@Inject
	private Notifier notifier;

	@Getter
	private final Set<NPC> distractedCitizens = Collections.newSetFromMap(new IdentityHashMap<>());

	/*
	 * Wealthy citizens transform when the street urchin distraction starts and
	 * transform back when it ends. NpcChanged fires for both transitions, so we
	 * toggle that individual NPC's state.
	 */
	@Subscribe
	public void onNpcChanged(NpcChanged event)
	{
		NPC npc = event.getNpc();
		if (!isWealthyCitizen(npc))
		{
			distractedCitizens.remove(npc);
			return;
		}

		if (distractedCitizens.remove(npc))
		{
			return;
		}

		distractedCitizens.add(npc);
		announce();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		// Treat newly loaded citizens as normal. Their next transform marks distraction.
		distractedCitizens.remove(event.getNpc());
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		distractedCitizens.remove(event.getNpc());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN || event.getGameState() == GameState.HOPPING)
		{
			distractedCitizens.clear();
		}
	}

	private boolean isWealthyCitizen(NPC npc)
	{
		return npc != null && WEALTHY_CITIZEN.equalsIgnoreCase(npc.getName());
	}

	private void announce()
	{
		if (config.desktopNotification())
		{
			notifier.notify(Notification.ON, "A wealthy citizen is distracted!");
		}

		if (config.voiceAlert())
		{
			playVoiceAlert();
		}
	}

	private void playVoiceAlert()
	{
		Thread audioThread = new Thread(() ->
		{
			try (AudioInputStream stream = AudioSystem.getAudioInputStream(
				new BufferedInputStream(getClass().getResourceAsStream("/distracted-citizen.wav"))))
			{
				Clip clip = AudioSystem.getClip();
				clip.open(stream);
				clip.addLineListener(event ->
				{
					if (event.getType() == LineEvent.Type.STOP)
					{
						clip.close();
					}
				});
				clip.start();
			}
			catch (Exception ex)
			{
				log.warn("Unable to play distracted citizen voice alert", ex);
			}
		}, "distracted-citizen-audio");

		audioThread.setDaemon(true);
		audioThread.start();
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		distractedCitizens.clear();
	}

	@Provides
	DistractedCitizensConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(DistractedCitizensConfig.class);
	}
}
