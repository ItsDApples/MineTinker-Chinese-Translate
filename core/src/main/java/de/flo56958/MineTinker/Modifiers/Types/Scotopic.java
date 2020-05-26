package de.flo56958.MineTinker.Modifiers.Types;

import de.flo56958.MineTinker.Data.ToolType;
import de.flo56958.MineTinker.Main;
import de.flo56958.MineTinker.Modifiers.Modifier;
import de.flo56958.MineTinker.Utilities.ChatWriter;
import de.flo56958.MineTinker.Utilities.ConfigurationManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scotopic extends Modifier implements Listener {

	private static Scotopic instance;

	private int requiredLightLevel;
	private int durationPerLevel;
	private int cooldownInSeconds;
	private double cooldownReductionPerLevel;
	private final HashMap<String, Long> cooldownTracker = new HashMap<>();

	private Scotopic() {
		super(Main.getPlugin());
		customModelData = 10_053;
	}

	public static Scotopic instance() {
		synchronized (Scotopic.class) {
			if (instance == null) {
				instance = new Scotopic();
			}
		}

		return instance;
	}

	@Override
	public String getKey() {
		return "Scotopic";
	}

	@Override
	public List<ToolType> getAllowedTools() {
		return Collections.singletonList(ToolType.HELMET);
	}

	@Override
	public void reload() {
		FileConfiguration config = getConfig();
		config.options().copyDefaults(true);

		config.addDefault("Allowed", true);
		config.addDefault("Name", "Scotopic");
		config.addDefault("ModifierItemName", "Nightly Eye");
		config.addDefault("Description", "Grants %amount seconds of Night-Vision per level if the light level drops below %light! Cooldown: %cmax to %cmin seconds (depends on level)");
		config.addDefault("DescriptionModifierItem", "%WHITE%Modifier-Item for the Scotopic-Modifier");
		config.addDefault("Color", "%YELLOW%");
		config.addDefault("MaxLevel", 3);
		config.addDefault("SlotCost", 2);
		config.addDefault("RequiredLightLevel", 6);
		config.addDefault("CooldownInSeconds", 120); //in seconds
		config.addDefault("DurationPerLevel", 100); //in ticks
		config.addDefault("CooldownReductionPerLevel", 0.65);
		config.addDefault("OverrideLanguagesystem", false);

		config.addDefault("EnchantCost", 10);
		config.addDefault("Enchantable", false);

		config.addDefault("Recipe.Enabled", true);
		config.addDefault("Recipe.Top", "S S");
		config.addDefault("Recipe.Middle", " F ");
		config.addDefault("Recipe.Bottom", "S S");

		Map<String, String> recipeMaterials = new HashMap<>();
		recipeMaterials.put("S", Material.SPIDER_EYE.name());
		recipeMaterials.put("F", Material.FERMENTED_SPIDER_EYE.name());

		config.addDefault("Recipe.Materials", recipeMaterials);

		ConfigurationManager.saveConfig(config);
		ConfigurationManager.loadConfig("Modifiers" + File.separator, getFileName());

		init(Material.FERMENTED_SPIDER_EYE);
		this.requiredLightLevel = config.getInt("RequiredLightLevel", 6);
		this.durationPerLevel = config.getInt("DurationPerLevel", 100);
		this.cooldownInSeconds = config.getInt("CooldownInSeconds", 120);
		this.cooldownReductionPerLevel = config.getDouble("CooldownReductionPerLevel", 0.65);

		this.description = this.description
				.replaceAll("%amount", String.valueOf(this.durationPerLevel / 20.0))
				.replaceAll("%light", String.valueOf(this.requiredLightLevel))
				.replaceAll("%cmax", String.valueOf(this.cooldownInSeconds))
				.replaceAll("%cmin", String.valueOf(Math.round(this.cooldownInSeconds * Math.pow(1.0 - this.cooldownReductionPerLevel, this.getMaxLvl() - 1))));
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (!player.hasPermission("minetinker.modifiers.scotopic.use")) return;

		ItemStack helmet = player.getInventory().getHelmet();
		if (!modManager.isArmorViable(helmet)) return;
		int level = modManager.getModLevel(helmet, this);
		if (level <= 0) return;

		//cooldown checker
		Long time = System.currentTimeMillis();
		long cooldownTime = (long) (this.cooldownInSeconds * 1000 * Math.pow(1.0 - this.cooldownReductionPerLevel, level - 1));
		if (this.cooldownInSeconds > 0) {
			Long cd = cooldownTracker.get(player.getUniqueId().toString());
			if (cd != null) { //was on cooldown
				if (time - cd > cooldownTime || player.getGameMode() == GameMode.CREATIVE) {
					cooldownTracker.remove(player.getUniqueId().toString());
				} else {
					return; //still on cooldown
				}
			}
		}

		Location loc = event.getTo();
		byte lightlevel = player.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).getLightLevel();

		if (lightlevel <= this.requiredLightLevel) {
			int duration = this.durationPerLevel * level;
			player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, duration, level, false, false, false));
			cooldownTracker.put(player.getUniqueId().toString(), time);
			ChatWriter.logModifier(player, event, this, helmet,
					String.format("Cooldown(%ds)", cooldownTime / 1000),
					String.format("LightLevel(%d/%d)", lightlevel, this.requiredLightLevel));
		}
	}
}
