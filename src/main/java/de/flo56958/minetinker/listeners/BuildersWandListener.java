package de.flo56958.minetinker.listeners;

import de.flo56958.minetinker.MineTinker;
import de.flo56958.minetinker.data.Lists;
import de.flo56958.minetinker.modifiers.ModManager;
import de.flo56958.minetinker.utils.ChatWriter;
import de.flo56958.minetinker.utils.ConfigurationManager;
import de.flo56958.minetinker.utils.LanguageManager;
import de.flo56958.minetinker.utils.PlayerInfo;
import de.flo56958.minetinker.utils.data.DataHandler;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Slab;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class BuildersWandListener implements Listener {

	private static final ModManager modManager;
	private static final ArrayList<ItemStack> wands = new ArrayList<>();
	private static FileConfiguration config;

	static {
		modManager = ModManager.instance();
		config = ConfigurationManager.getConfig("BuildersWand.yml");
		config.options().copyDefaults(true);

		config.addDefault("enabled", true);
		config.addDefault("Description", "%WHITE%MineTinker-Builderswand");
		config.addDefault("useDurability", true);

		List<String> list = new ArrayList<>();
		list.add("bannedExample1");
		list.add("bannedExample2");
		config.addDefault("BannedWorlds", list); //#Worlds where MineTinker-Builderswands can't be used

		String recipe = "Recipes.Wood";
		config.addDefault(recipe + ".Top", "  W");
		config.addDefault(recipe + ".Middle", " S ");
		config.addDefault(recipe + ".Bottom", "S  ");
		config.addDefault(recipe + ".Materials.S", "STICK");
		config.addDefault(recipe + ".Materials.W", "LEGACY_WOOD");

		recipe = "Recipes.Stone";
		config.addDefault(recipe + ".Top", "  C");
		config.addDefault(recipe + ".Middle", " S ");
		config.addDefault(recipe + ".Bottom", "S  ");
		config.addDefault(recipe + ".Materials.C", "COBBLESTONE");
		config.addDefault(recipe + ".Materials.S", "STICK");

		recipe = "Recipes.Iron";
		config.addDefault(recipe + ".Top", "  I");
		config.addDefault(recipe + ".Middle", " S ");
		config.addDefault(recipe + ".Bottom", "S  ");
		config.addDefault(recipe + ".Materials.I", "IRON_INGOT");
		config.addDefault(recipe + ".Materials.S", "STICK");

		recipe = "Recipes.Gold";
		config.addDefault(recipe + ".Top", "  G");
		config.addDefault(recipe + ".Middle", " S ");
		config.addDefault(recipe + ".Bottom", "S  ");
		config.addDefault(recipe + ".Materials.G", "GOLD_INGOT");
		config.addDefault(recipe + ".Materials.S", "STICK");

		recipe = "Recipes.Diamond";
		config.addDefault(recipe + ".Top", "  D");
		config.addDefault(recipe + ".Middle", " S ");
		config.addDefault(recipe + ".Bottom", "S  ");
		config.addDefault(recipe + ".Materials.D", "DIAMOND");
		config.addDefault(recipe + ".Materials.S", "STICK");

		recipe = "Recipes.Netherite";
		config.addDefault(recipe + ".Top", "  N");
		config.addDefault(recipe + ".Middle", " S ");
		config.addDefault(recipe + ".Bottom", "S  ");
		config.addDefault(recipe + ".Materials.N", "NETHERITE_INGOT");
		config.addDefault(recipe + ".Materials.S", "STICK");

		ConfigurationManager.saveConfig(config);
	}

	@SuppressWarnings("EmptyMethod")
	public static void init() {/*class must be called once*/}

	public static void reload() {
		config = ConfigurationManager.getConfig("BuildersWand.yml");

		wands.clear();
		wands.add(buildersWandCreator(Material.WOODEN_SHOVEL, LanguageManager.getString("Builderswand.Name.Wood")));
		wands.add(buildersWandCreator(Material.STONE_SHOVEL, LanguageManager.getString("Builderswand.Name.Stone")));
		wands.add(buildersWandCreator(Material.IRON_SHOVEL, LanguageManager.getString("Builderswand.Name.Iron")));
		wands.add(buildersWandCreator(Material.GOLDEN_SHOVEL, LanguageManager.getString("Builderswand.Name.Gold")));
		wands.add(buildersWandCreator(Material.DIAMOND_SHOVEL, LanguageManager.getString("Builderswand.Name.Diamond")));
		if (MineTinker.is16compatible) wands.add(buildersWandCreator(Material.NETHERITE_SHOVEL, LanguageManager.getString("Builderswand.Name.Netherite")));

		registerBuildersWands();
	}

	private static ItemStack buildersWandCreator(Material m, String name) { //TODO: Modify to implement Modifiers
		ItemStack wand = new ItemStack(m, 1);
		ItemMeta meta = wand.getItemMeta();

		if (meta != null) {
			ArrayList<String> lore = new ArrayList<>();
			lore.add(LanguageManager.getString("Builderswand.Description"));
			meta.setLore(lore);

			meta.setDisplayName(ChatWriter.addColors(name));
			meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
			wand.setItemMeta(meta);
		}

		//TODO: DataHandler.setStringList(wand, "CanDestroy", true, "minecraft:air");
		DataHandler.setTag(wand, "identifier_builderswand", 0, PersistentDataType.INTEGER, false);

		return wand;
	}

	/**
	 * tries to register the Builderswand recipes
	 */
	private static void registerBuildersWands() {
		try {
			NamespacedKey nkey = new NamespacedKey(MineTinker.getPlugin(), "Builderswand_Wood");
			ShapedRecipe newRecipe = new ShapedRecipe(nkey, wands.get(0)); //init recipe
			String top = config.getString("Recipes.Wood.Top");
			String middle = config.getString("Recipes.Wood.Middle");
			String bottom = config.getString("Recipes.Wood.Bottom");
			ConfigurationSection materials = config.getConfigurationSection("Recipes.Wood.Materials");

			// TODO: Make safe
			newRecipe.shape(top, middle, bottom); //makes recipe

			for (String key : materials.getKeys(false)) {
				newRecipe.setIngredient(key.charAt(0), Material.getMaterial(materials.getString(key)));
			}

			MineTinker.getPlugin().getServer().addRecipe(newRecipe); //adds recipe
			ModManager.instance().recipe_Namespaces.add(nkey);
		} catch (Exception e) {
			ChatWriter.logError(LanguageManager.getString("Builderswand.Error").replaceAll("%wand", wands.get(0).getItemMeta().getDisplayName())); //executes if the recipe could not initialize
			e.printStackTrace();
		}
		try {
			NamespacedKey nkey = new NamespacedKey(MineTinker.getPlugin(), "Builderswand_Stone");
			ShapedRecipe newRecipe = new ShapedRecipe(nkey, wands.get(1)); //init recipe
			String top = config.getString("Recipes.Stone.Top");
			String middle = config.getString("Recipes.Stone.Middle");
			String bottom = config.getString("Recipes.Stone.Bottom");
			ConfigurationSection materials = config.getConfigurationSection("Recipes.Stone.Materials");

			// TODO: Make safe
			newRecipe.shape(top, middle, bottom); //makes recipe

			for (String key : materials.getKeys(false)) {
				newRecipe.setIngredient(key.charAt(0), Material.getMaterial(materials.getString(key)));
			}

			MineTinker.getPlugin().getServer().addRecipe(newRecipe); //adds recipe
			ModManager.instance().recipe_Namespaces.add(nkey);
		} catch (Exception e) {
			ChatWriter.logError(LanguageManager.getString("Builderswand.Error").replaceAll("%wand", wands.get(1).getItemMeta().getDisplayName())); //executes if the recipe could not initialize
			e.printStackTrace();
		}
		try {
			NamespacedKey nkey = new NamespacedKey(MineTinker.getPlugin(), "Builderswand_Iron");
			ShapedRecipe newRecipe = new ShapedRecipe(nkey, wands.get(2)); //init recipe
			String top = config.getString("Recipes.Iron.Top");
			String middle = config.getString("Recipes.Iron.Middle");
			String bottom = config.getString("Recipes.Iron.Bottom");
			ConfigurationSection materials = config.getConfigurationSection("Recipes.Iron.Materials");

			newRecipe.shape(top, middle, bottom); //makes recipe

			for (String key : materials.getKeys(false)) {
				newRecipe.setIngredient(key.charAt(0), Material.getMaterial(materials.getString(key)));
			}

			MineTinker.getPlugin().getServer().addRecipe(newRecipe); //adds recipe
			ModManager.instance().recipe_Namespaces.add(nkey);
		} catch (Exception e) {
			ChatWriter.logError(LanguageManager.getString("Builderswand.Error").replaceAll("%wand", wands.get(2).getItemMeta().getDisplayName())); //executes if the recipe could not initialize
			e.printStackTrace();
		}
		try {
			NamespacedKey nkey = new NamespacedKey(MineTinker.getPlugin(), "Builderswand_Gold");
			ShapedRecipe newRecipe = new ShapedRecipe(nkey, wands.get(3)); //init recipe
			String top = config.getString("Recipes.Gold.Top");
			String middle = config.getString("Recipes.Gold.Middle");
			String bottom = config.getString("Recipes.Gold.Bottom");
			ConfigurationSection materials = config.getConfigurationSection("Recipes.Gold.Materials");

			// TODO: Make safe
			newRecipe.shape(top, middle, bottom); //makes recipe

			for (String key : materials.getKeys(false)) {
				newRecipe.setIngredient(key.charAt(0), Material.getMaterial(materials.getString(key)));
			}

			MineTinker.getPlugin().getServer().addRecipe(newRecipe); //adds recipe
			ModManager.instance().recipe_Namespaces.add(nkey);
		} catch (Exception e) {
			ChatWriter.logError(LanguageManager.getString("Builderswand.Error").replaceAll("%wand", wands.get(3).getItemMeta().getDisplayName())); //executes if the recipe could not initialize
			e.printStackTrace();
		}
		try {
			NamespacedKey nkey = new NamespacedKey(MineTinker.getPlugin(), "Builderswand_Diamond");
			ShapedRecipe newRecipe = new ShapedRecipe(nkey, wands.get(4)); //init recipe
			String top = config.getString("Recipes.Diamond.Top");
			String middle = config.getString("Recipes.Diamond.Middle");
			String bottom = config.getString("Recipes.Diamond.Bottom");
			ConfigurationSection materials = config.getConfigurationSection("Recipes.Diamond.Materials");

			newRecipe.shape(top, middle, bottom); //makes recipe

			for (String key : materials.getKeys(false)) {
				newRecipe.setIngredient(key.charAt(0), Material.getMaterial(materials.getString(key)));
			}

			MineTinker.getPlugin().getServer().addRecipe(newRecipe); //adds recipe
		} catch (Exception e) {
			ChatWriter.logError(LanguageManager.getString("Builderswand.Error").replaceAll("%wand", wands.get(4).getItemMeta().getDisplayName())); //executes if the recipe could not initialize
			e.printStackTrace();
		}
		if (MineTinker.is16compatible) {
			try {
				NamespacedKey nkey = new NamespacedKey(MineTinker.getPlugin(), "Builderswand_Netherite");
				ShapedRecipe newRecipe = new ShapedRecipe(nkey, wands.get(5)); //init recipe
				String top = config.getString("Recipes.Netherite.Top");
				String middle = config.getString("Recipes.Netherite.Middle");
				String bottom = config.getString("Recipes.Netherite.Bottom");
				ConfigurationSection materials = config.getConfigurationSection("Recipes.Netherite.Materials");

				newRecipe.shape(top, middle, bottom); //makes recipe

				for (String key : materials.getKeys(false)) {
					newRecipe.setIngredient(key.charAt(0), Material.getMaterial(materials.getString(key)));
				}

				MineTinker.getPlugin().getServer().addRecipe(newRecipe); //adds recipe
			} catch (Exception e) {
				ChatWriter.logError(LanguageManager.getString("Builderswand.Error").replaceAll("%wand", wands.get(5).getItemMeta().getDisplayName())); //executes if the recipe could not initialize
				e.printStackTrace();
			}
		}
	}

	public static ArrayList<ItemStack> getWands() {
		return wands;
	}

	@EventHandler(ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event) {
		if (Lists.WORLDS_BUILDERSWANDS.contains(event.getPlayer().getWorld().getName())) {
			return;
		}

		ItemStack wand = event.getPlayer().getInventory().getItemInMainHand();
		event.setCancelled(modManager.isWandViable(wand));
	}

	@EventHandler(ignoreCancelled = true)
	public void onClick(PlayerInteractEvent event) {
		if (Lists.WORLDS_BUILDERSWANDS.contains(event.getPlayer().getWorld().getName())) {
			return;
		}

		ItemStack wand = event.getPlayer().getInventory().getItemInMainHand();

		if (!modManager.isWandViable(wand)) {
			return;
		}

		if (!event.getPlayer().hasPermission("minetinker.builderswands.use")) {
			return;
		}

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			if (event.getAction() != Action.PHYSICAL) {
				event.setCancelled(true);
			}
			return;
		}

		event.setCancelled(true);

		int _u = 0;
		int _w = 0;

		Player player = event.getPlayer();

		if (!player.isSneaking()) {
			switch (wand.getType()) {  //TODO: custom Builderswand sizes
				case STONE_SHOVEL:
					_w = 1;
					break;
				case IRON_SHOVEL:
					_u = 1;
					_w = 1;
					break;
				case GOLDEN_SHOVEL:
					_u = 1;
					_w = 2;
					break;
				case DIAMOND_SHOVEL:
					_u = 2;
					_w = 2;
					break;
			}
			if (MineTinker.is16compatible) {
				switch (wand.getType()) {
					case NETHERITE_SHOVEL:
						_u = 3;
						_w = 3;
				}
			}
		}

		Block block = event.getClickedBlock();
		BlockFace face = event.getBlockFace();
		ItemStack[] inv = player.getInventory().getContents();

		Vector u = new Vector(0, 0, 0);
		Vector v = new Vector(0, 0, 0);
		Vector w = new Vector(0, 0, 0);

		if (face.equals(BlockFace.UP) || face.equals(BlockFace.DOWN)) {
			if (face.equals(BlockFace.UP)) {
				v = new Vector(0, 1, 0);
			} else {
				v = new Vector(0, -1, 0);
			}

			switch (PlayerInfo.getFacingDirection(player)) {
				case NORTH:
					w = new Vector(-1, 0, 0);
					break;
				case EAST:
					w = new Vector(0, 0, -1);
					break;
				case SOUTH:
					w = new Vector(1, 0, 0);
					break;
				case WEST:
				default:
					w = new Vector(0, 0, 1);
					break;
			}

			u = v.getCrossProduct(w);
		} else if (face.equals(BlockFace.NORTH)) {
			v = new Vector(0, 0, -1);
			w = new Vector(-1, 0, 0);
			u = new Vector(0, -1, 0);
		} else if (face.equals(BlockFace.EAST)) {
			v = new Vector(1, 0, 0);
			w = new Vector(0, 0, -1);
			u = new Vector(0, 1, 0);
		} else if (face.equals(BlockFace.SOUTH)) {
			v = new Vector(0, 0, 1);
			w = new Vector(1, 0, 0);
			u = new Vector(0, 1, 0);
		} else if (face.equals(BlockFace.WEST)) {
			v = new Vector(-1, 0, 0);
			w = new Vector(0, 0, 1);
			u = new Vector(0, -1, 0);
		}
		if ((player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) && block != null) {
			for (ItemStack current : inv) {
				if (current == null) {
					continue;
				}

				if (!current.getType().equals(block.getType())) {
					continue;
				}

				if (current.hasItemMeta()) {
					continue;
				}

				loop:
				for (int i = -_w; i <= _w; i++) {
					for (int j = -_u; j <= _u; j++) {
						Location l = block.getLocation().clone();

						l.subtract(w.clone().multiply(i));
						l.subtract(u.clone().multiply(j));

						Location loc = l.clone().subtract(v.clone().multiply(-1));

						if (wand.getItemMeta() instanceof Damageable) {
							Damageable damageable = (Damageable) wand.getItemMeta();

							if (!wand.getItemMeta().isUnbreakable()) {
								if (wand.getType().getMaxDurability() - damageable.getDamage() <= 1) {
									break loop;
								}
							}

							if (!placeBlock(block, player, l, loc, current, v)) {
								continue;
							}

							int amountPlaced = 1;
							BlockData behindData = block.getWorld().getBlockAt(loc.subtract(v)).getBlockData();

							if (behindData instanceof Slab && ((Slab) behindData).getType().equals(Slab.Type.DOUBLE)) {
								amountPlaced = 2; //Special case for slabs as you place two slabs at once
							}

							current.setAmount(current.getAmount() - amountPlaced);

							if (config.getBoolean("useDurability")) {
								//TODO: Add Modifiers to the Builderswand (Self-Repair, Reinforced, XP)
								if (!wand.getItemMeta().isUnbreakable()) {
									damageable.setDamage(damageable.getDamage() + 1);
								}
							}

							wand.setItemMeta((ItemMeta) damageable);

							if (current.getAmount() == 0) {
								//TODO: Add Exp gain for Builderswands
								break loop;
							}
							event.setCancelled(true);
						}
					}
				}

				break;
			}
		} else if (player.getGameMode() == GameMode.CREATIVE && block != null) {
			for (int i = -_w; i <= _w; i++) {
				for (int j = -_u; j <= _u; j++) {
					Location l = block.getLocation().clone();

					l.subtract(w.clone().multiply(i));
					l.subtract(u.clone().multiply(j));

					Location loc = l.clone().subtract(v.clone().multiply(-1));

					placeBlock(block, player, l, loc, new ItemStack(block.getType(), 64), v);
				}
			}
		}
	}

	private boolean placeBlock(Block b, Player player, Location l, Location loc, ItemStack item, Vector vector) {
		if (!b.getWorld().getBlockAt(l).getType().equals(b.getType())) {
			return false;
		}

		Material type = b.getWorld().getBlockAt(loc).getType();

		if (!(type == Material.AIR || type == Material.CAVE_AIR ||
				type == Material.WATER || type == Material.BUBBLE_COLUMN ||
				type == Material.LAVA || type == Material.GRASS)) {

			return false;
		}

		//TODO: Transfer to DataHandler
		//triggers a pseudoevent to find out if the Player can build
		Block block = b.getWorld().getBlockAt(loc);

		BlockPlaceEvent placeEvent = new BlockPlaceEvent(block, block.getState(), b, item, player, true, EquipmentSlot.HAND);
		Bukkit.getPluginManager().callEvent(placeEvent);

		//check the pseudoevent
		if (!placeEvent.canBuild() || placeEvent.isCancelled()) {
			return false;
		}

		Block nb = b.getWorld().getBlockAt(loc);
		Block behind = nb.getWorld().getBlockAt(loc.clone().subtract(vector));
		if (behind.getBlockData() instanceof Slab) {
			if (((Slab) behind.getBlockData()).getType().equals(Slab.Type.DOUBLE)) {
				if (item.getAmount() - 2 < 0) {
					return false;
				}
			}
		}

		nb.setType(item.getType());
		BlockData bd = nb.getBlockData();

		if (bd instanceof Directional) {
			((Directional) bd).setFacing(((Directional) behind.getBlockData()).getFacing());
		}

		if (bd instanceof Slab) {
			((Slab) bd).setType(((Slab) behind.getBlockData()).getType());
		}

		nb.setBlockData(bd);

		return true;
	}
}
