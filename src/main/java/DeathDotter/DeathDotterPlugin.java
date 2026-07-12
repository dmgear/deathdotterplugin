package DeathDotter;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.inject.Inject;
import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Renderable;
import net.runelite.api.WorldView;
import net.runelite.api.WorldType;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.callback.RenderCallback;
import net.runelite.client.callback.RenderCallbackManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.api.gameval.VarbitID;

@PluginDescriptor(name = "Death Dotter", description = "Allows you to switch rendered entities when players occupy the same tiles", tags = {
    "pvp", "player", "player vs player", "death dotter", "death dot", "death dotting",
    "entity", "entity hider", "wilderness", "emir's", "emir's arena", "pvp arena", "pvp world" })

public class DeathDotterPlugin extends Plugin {

  private static final Set<Integer> LMS_REGIONS = ImmutableSet.of(12344, 12600, 13658, 13659, 13660, 13914, 13915,
      13916, 13918,
      13919, 13920,
      14174, 14175,
      14176, 14430,
      14431, 14432);

  @Inject
  private Client client;

  @Inject
  private RenderCallbackManager renderCallbackManager;

  @Inject
  private DeathDotterConfig config;

  private boolean activeInWilderness;
  private boolean activeInPvpWorlds;
  private boolean activeInPvpArena;
  private boolean activeInLms;
  private boolean activeInDeadman;
  private boolean alwaysActive;
  private boolean hide2D;

  private final RenderCallback drawListener = new RenderCallback() {
    @Override
    public boolean addEntity(Renderable renderable, boolean ui) {
      return shouldDraw(renderable, ui);
    }
  };

  @Provides
  DeathDotterConfig provideConfig(ConfigManager configManager) {
    return configManager.getConfig(DeathDotterConfig.class);
  }

  @Override
  protected void startUp() {
    updateConfig();
    renderCallbackManager.register(drawListener);
  }

  @Override
  protected void shutDown() {
    renderCallbackManager.unregister(drawListener);
  }

  @Subscribe
  public void onConfigChanged(ConfigChanged e) {
    if (e.getGroup().equals(DeathDotterConfig.GROUP)) {
      updateConfig();
    }
  }

  private void updateConfig() {
    alwaysActive = config.alwaysActive();
    activeInWilderness = config.activeInWilderness();
    activeInPvpWorlds = config.activeInPvpWorlds();
    activeInPvpArena = config.activeInPvpArena();
    activeInLms = config.activeInLms();
    activeInDeadman = config.activeInDeadman();
    hide2D = config.hide2D();
  }

  /**
   * Determines if the plugin should be active based on the current location and
   * configuration.
   *
   * @return true if the plugin should be active, false otherwise
   */
  private boolean shouldPluginBeActive() {
    // If always active is enabled, plugin works everywhere
    if (alwaysActive) {
      return true;
    }

    // Check if we're in any of the configured active areas
    if (activeInWilderness && isInWilderness()) {
      return true;
    }

    if (activeInPvpWorlds && isPvpWorld()) {
      return true;
    }

    if (activeInPvpArena && isPvpArena()) {
      return true;
    }

    if (activeInLms && isLms()) {
      return true;
    }

    if (activeInDeadman && isDeadman()) {
      return true;
    }

    return false;
  }

  private boolean isPvpWorld() {
    return client.getWorldType().contains(WorldType.PVP);
  }

  private boolean isPvpArena() {
    return client.getWorldType().contains(WorldType.PVP_ARENA);
  }

  private boolean isLms() {
    Player localPlayer = client.getLocalPlayer();
    if (localPlayer == null) {
      return false;
    }

    WorldView worldView = localPlayer.getWorldView();
    if (worldView == null) {
      return false;
    }

    final int[] mapRegions = worldView.getMapRegions();

    for (int region : mapRegions) {
      if (LMS_REGIONS.contains(region)) {
        return true;
      }
    }

    return false;
  }

  private boolean isDeadman() {
    return client.getWorldType().contains(WorldType.DEADMAN);
  }

  private boolean isInWilderness() {
    if (!client.isClientThread()) {
      return false; // Assume not in Wilderness if not on the client thread
    }
    return client.getVarbitValue(VarbitID.INSIDE_WILDERNESS) == 1;
  }

  private boolean areModelsOverlapping(Player localPlayer, Player otherPlayer) {
    if (localPlayer == null || otherPlayer == null) {
      return false;
    }

    if (localPlayer == otherPlayer) {
      return false;
    }

    if (localPlayer.getWorldView() != otherPlayer.getWorldView()) {
      return false;
    }

    LocalPoint localLoc1 = localPlayer.getLocalLocation();
    LocalPoint localLoc2 = otherPlayer.getLocalLocation();
    if (localLoc1 == null || localLoc2 == null) {
      return false;
    }

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
  boolean shouldDraw(Renderable renderable, boolean drawingUi) {
    // this should only be run on the client thread
    if (!client.isClientThread()) {
      return true;
    }

    // Draw everything when plugin shouldn't be active
    if (!shouldPluginBeActive()) {
      return true;
    }

    if (renderable instanceof Player) {
      Player local = client.getLocalPlayer();
      if (local == null) {
        return true;
      }

      WorldView worldView = local.getWorldView();
      if (worldView == null) {
        return true;
      }

      // Check if the renderable is the local player
      if (renderable == local) {
        for (Player otherPlayer : worldView.players()) {
          if (areModelsOverlapping(local, otherPlayer)) {
            // Hide local player and hide 2D elements if hide2D is enabled
            return !(drawingUi ? hide2D : true);
          }
        }
      }
    }
    return true;
  }
}
