package DeathDotter;

import com.google.common.annotations.VisibleForTesting;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Renderable;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.callback.Hooks;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import java.util.List;

@PluginDescriptor
(
        name = "Death Dotter",
        description = "Allows you to switch rendered entities when players occupy the same tiles",
        tags = {"npc", "player", "deathdotter"}
)

public class DeathDotterPlugin extends Plugin 
{
    @Inject
    private Client client;

    @Inject
    private Hooks hooks;

    private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;

    @Override
    protected void startUp()
    {
        hooks.registerRenderableDrawListener(drawListener);
    }

    @Override
    protected void shutDown()
    {
        hooks.unregisterRenderableDrawListener(drawListener);
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
