package DeathDotter;

import com.google.common.annotations.VisibleForTesting;
import javax.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Renderable;
import net.runelite.api.WorldType;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import java.util.List;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

@PluginDescriptor
(
        name = "Death Dotter",
        description = "Allows you to switch rendered entities when players occupy the same tiles",
        tags = {"pvp", "player", "player vs player", "death dotter", "death dot", "death dotting",
                "entity", "entity hider", "wilderness", "emir's", "emir's arena", "pvp arena", "pvp world"}
)

public class DeathDotterPlugin extends Plugin 
{
    @Inject
    private Client client;

    @Inject
    private Hooks hooks;

    @Inject
    private DeathDotterConfig config;

    private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;

    private boolean disableWhileInPvpZone;

    @Provides
    DeathDotterConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(DeathDotterConfig.class);
    }

    @Override
    protected void startUp()
    {
        if (config.disableOutsidePvp() && !isInPvpZone())
        {
            return;
        }
        hooks.registerRenderableDrawListener(drawListener);
    }

    @Override
    protected void shutDown()
    {
        hooks.unregisterRenderableDrawListener(drawListener);
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
        disableWhileInPvpZone = config.disableOutsidePvp();
    }

    private boolean isInPvpZone()
    {
        if (client == null || client.getLocalPlayer() == null)
        {
            return false; // Client or player is not initialized
        }

        // Check if the current world is a PvP world
        boolean isPvpWorld = client.getWorldType().contains(WorldType.PVP);
        boolean isArenaWorld = client.getWorldType().contains(WorldType.PVP_ARENA);

        // Check if the player is in the Wilderness
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        boolean isInWilderness = isInWilderness(playerLocation);

        // Return true if either condition is true
        return isPvpWorld || isInWilderness || isArenaWorld;
    }

    private boolean isInWilderness(WorldPoint location)
    {
        return location != null && location.getY() >= 3520;
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

        if (config.disableOutsidePvp() && !isInPvpZone())
        {
            return true; // Always draw everything if outside PvP zones
        }

        // this should only be run on the client thread
        if (!client.isClientThread())
        {
            return true;
        }

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
