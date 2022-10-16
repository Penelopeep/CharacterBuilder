package pene.gc.charbuild;

import emu.grasscutter.plugin.Plugin;
import pene.gc.charbuild.commands.Builder;

import java.io.*;
import java.util.stream.Collectors;

/**
 * The Grasscutter plugin template.
 * This is the main class for the plugin.
 */
public final class CharacterBuilder extends Plugin {
    /* Turn the plugin into a singleton. */
    private static CharacterBuilder instance;

    /**
     * Gets the plugin instance.
     * @return A plugin singleton.
     */
    public static CharacterBuilder getInstance() {
        return instance;
    }
    
    /**
     * This method is called immediately after the plugin is first loaded into system memory.
     */
    @Override public void onLoad() {
        // Set the plugin instance.
        instance = this;
        
        // Get the configuration file.
        File config = new File(this.getDataFolder(), "builds.json");
        
        // Load the configuration.
        try {
            if(!config.exists()) {
                try (FileWriter writer = new FileWriter(config)) {
                    InputStream configStream = this.getResource("builds.json");
                    if(configStream == null) {
                        this.getLogger().error("Failed to save default builds file.");
                    } else {
                        writer.write(new BufferedReader(
                                new InputStreamReader(configStream)).lines().collect(Collectors.joining("\n"))
                        ); writer.close();

                        this.getLogger().info("Saved default builds file.");
                    }
                }
            }

        } catch (IOException exception) {
            this.getLogger().error("Failed to create builds file.", exception);
        }
    }

    /**
     * This method is called before the servers are started, or when the plugin enables.
     */
    @Override public void onEnable() {
        // Register commands.
        this.getHandle().registerCommand(new Builder());

        // Log a plugin status message.
        this.getLogger().info("The CharacterBuilder plugin has been enabled.");
    }

    /**
     * This method is called when the plugin is disabled.
     */
    @Override public void onDisable() {
        // Log a plugin status message.
        this.getLogger().info("The CharacterBuilder plugin has been disabled.");
    }
}
