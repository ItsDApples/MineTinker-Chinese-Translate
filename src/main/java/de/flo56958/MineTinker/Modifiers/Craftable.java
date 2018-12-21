package de.flo56958.MineTinker.Modifiers;

import de.flo56958.MineTinker.Main;
import de.flo56958.MineTinker.Utilities.ChatWriter;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.PluginManager;

public interface Craftable {
    void registerCraftingRecipe();
    
    static final ModManager modManager = ModManager.instance();
    static final PluginManager pluginManager = Bukkit.getPluginManager();

    default void _registerCraftingRecipe(FileConfiguration config, Modifier mod, String name, String keyName) {
        if (config.getBoolean(name + ".Recipe.Enabled")) {
            try {
                ShapedRecipe newRecipe = new ShapedRecipe(new NamespacedKey(Main.getPlugin(), keyName), mod.getModItem()); //init recipe
                String top = config.getString(name + ".Recipe.Top");
                String middle = config.getString(name + ".Recipe.Middle");
                String bottom = config.getString(name + ".Recipe.Bottom");
                ConfigurationSection materials = config.getConfigurationSection(name + ".Recipe.Materials");
                newRecipe.shape(top, middle, bottom); //makes recipe
                for (String key : materials.getKeys(false)) {
                    newRecipe.setIngredient(key.charAt(0), Material.getMaterial(materials.getString(key)));
                }
                Main.getPlugin().getServer().addRecipe(newRecipe); //adds recipe
            } catch (Exception e) {
                ChatWriter.logError("Could not register recipe for the " + name + "-Modifier!"); //executes if the recipe could not initialize
            }
        }
    }
}
