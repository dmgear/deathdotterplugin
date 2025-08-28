package DeathDotter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(DeathDotterConfig.GROUP)
public interface DeathDotterConfig extends Config {

  String GROUP = "DeathDotter";

  @ConfigSection(name = "Active Areas", description = "Configure which areas the plugin should be active in", position = 1)
  String activeAreasSection = "activeAreas";

  @ConfigItem(keyName = "alwaysActive", name = "Always Active", description = "Keep the plugin active in all areas (overrides other settings)", section = activeAreasSection, position = 0)
  default boolean alwaysActive() {
    return false;
  }

  @ConfigItem(keyName = "activeInWilderness", name = "Active in Wilderness", description = "Enable the plugin in the Wilderness", section = activeAreasSection, position = 1)
  default boolean activeInWilderness() {
    return true;
  }

  @ConfigItem(keyName = "activeInPvpWorlds", name = "Active in PvP Worlds", description = "Enable the plugin in PvP worlds", section = activeAreasSection, position = 2)
  default boolean activeInPvpWorlds() {
    return true;
  }

  @ConfigItem(keyName = "activeInEmirsArena", name = "Active in Emir's Arena", description = "Enable the plugin in Emir's Arena (PVP Arena)", section = activeAreasSection, position = 3)
  default boolean activeInPvpArena() {
    return true;
  }

  @ConfigItem(keyName = "activeInLms", name = "Active in LMS", description = "Enable the plugin in Last Man Standing", section = activeAreasSection, position = 4)
  default boolean activeInLms() {
    return true;
  }

  @ConfigItem(keyName = "activeInDeadman", name = "Active in Deadman", description = "Enable the plugin in Deadman mode", section = activeAreasSection, position = 5)
  default boolean activeInDeadman() {
    return true;
  }
}
