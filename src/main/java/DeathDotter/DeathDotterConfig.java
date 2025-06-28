package DeathDotter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(DeathDotterConfig.GROUP)
public interface DeathDotterConfig extends Config
{

    String GROUP = "DeathDotter";
    @ConfigItem(
            keyName = "disableOutsidePvp",
            name = "Disable Outside PvP Zones",
            description = "Automatically disable the plugin outside PvP zones (Wilderness or PvP world)"
    )
    default boolean disableOutsidePvp()
    {
        return true; // Default to disabling the plugin outside PvP zones
    }
}
