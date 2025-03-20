package DeathDotter;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Renderable;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import java.util.List;

@PluginDescriptor
(
        name = "Death Dotter",
        description = "Allows you to switch rendered entities when players occupy the same tiles",
        tags = {"npc", "player", "deathdotter"},
        enabledByDefault = false
)

public class DeathDotterPlugin extends Plugin 
{
    @Inject
    private Client client;

    @Inject
    private Hooks hooks;

    @Inject
    private DeathDotterConfig config;

    private boolean hideLocalPlayer;

    private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;

    @Provides
    DeathDotterConfig provideConfig(ConfigManager configManager) 
    {
        return configManager.getConfig(DeathDotterConfig.class);
    }

    @Override
    protected void startUp() 
    {
        updateConfig();

        hooks.registerRenderableDrawListener(drawListener);
    }

    @Override
    protected void shutDown() 
    {
        hooks.unregisterRenderableDrawListener(drawListener);
    }

    @Subscribe
    public void onGameTick(GameTick event) 
    {
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer == null) 
        {
            return;
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged e) 
    {
        if (e.getGroup().equals(DeathDotterConfig.GROUP)) 
        {
            updateConfig();
        }
    }

    private void updateConfig() 
    {

        hideLocalPlayer = config.hideLocalPlayer();

    }
    
    private boolean areModelsOverlapping(Player localPlayer, Player otherPlayer) 
    {
        if (localPlayer == otherPlayer) 
        {
            return false;
        }
        LocalPoint localLoc1 = localPlayer.getLocalLocation();
        LocalPoint localLoc2 = otherPlayer.getLocalLocation();

        // Calculate squared distance between the two players
        int dx = localLoc1.getX() - localLoc2.getX();
        int dy = localLoc1.getY() - localLoc2.getY();

        int distanceSquared = dx * dx + dy * dy;

        // Get radii and calculate radius sum
        int localRadius = 1;
        int otherRadius = 1;
        int radiusSum = localRadius + otherRadius;

        // Check for overlap
        return distanceSquared <= radiusSum;
    }

    @VisibleForTesting
    boolean shouldDraw(Renderable renderable, boolean drawingUi) 
    {
        if (renderable instanceof Player) 
        {
            Player local = client.getLocalPlayer();

            // Check if the renderable is the local player
            if (renderable == local) 
            {
                List<Player> players = client.getPlayers();

                for (Player otherPlayer : players) {

                    if (areModelsOverlapping(local, otherPlayer)) 
                    {
                        return false; // Hide local player
                    }
                }
            }
        }

        return true;
    }
}
