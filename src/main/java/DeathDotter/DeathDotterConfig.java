package DeathDotter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(DeathDotterConfig.GROUP)
public interface DeathDotterConfig extends Config
{
	String GROUP = "deathdotter ";
	@ConfigItem(
			keyName = "hideLocalPlayer",
			name = "Hide Local Player",
			description = "Configures whether the local player should be hidden when another player is on the same tile"
	)
	default boolean hideLocalPlayer()
	{
		return true;
	}


}