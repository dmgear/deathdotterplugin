package DeathDotter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(DeathDotterConfig.GROUP)
public interface DeathDotterConfig extends Config
{
	String GROUP = "deathdotter ";
	@ConfigItem
	(
		keyName = "hideLocalPlayer",
		name = "Hide Local Player",
		description = "Hides local player when other players occupy the same location"
	)
	default boolean hideLocalPlayer()
	{
		return true;
	}
}
