package com.plushnode.atlacoremobs;

import com.plushnode.atlacore.game.Game;
import com.plushnode.atlacore.internal.ninja.configurate.ConfigurationOptions;
import com.plushnode.atlacore.internal.ninja.configurate.commented.CommentedConfigurationNode;
import com.plushnode.atlacore.internal.ninja.configurate.hocon.HoconConfigurationLoader;
import com.plushnode.atlacore.internal.ninja.configurate.loader.ConfigurationLoader;
import com.plushnode.atlacoremobs.commands.CommandMultiplexer;
import com.plushnode.atlacoremobs.commands.ReloadConfigCommand;
import com.plushnode.atlacoremobs.compatibility.projectkorra.ProjectKorraHook;
import com.plushnode.atlacoremobs.modules.mobarena.MobArenaGame;
import com.plushnode.atlacoremobs.modules.raid.Raid;
import com.plushnode.atlacoremobs.modules.raid.TownyRaidGame;
import com.plushnode.atlacoremobs.listeners.EntityListener;
import com.plushnode.atlacoremobs.modules.spawn.SpawnManager;
import com.plushnode.atlacoremobs.modules.trainingarena.TrainingArenaModule;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class AtlaCoreMobsPlugin extends JavaPlugin {
    public static AtlaCoreMobsPlugin plugin;

    private ScriptedUserService userService;
    private CommentedConfigurationNode configRoot;
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private MobArenaGame mobArenaGame;
    private TownyRaidGame raidGame;
    private CommandMultiplexer commandMultiplexer;
    private SpawnManager spawnManager;
    private TrainingArenaModule trainingArena;
    private VelocityTrackerService tracker;

    @Override
    public void onEnable() {
        AtlaCoreMobsPlugin.plugin = this;

        loadConfig();

        userService = new ScriptedUserService();

        getServer().getPluginManager().registerEvents(new EntityListener(this), this);

        this.commandMultiplexer = new CommandMultiplexer("acmobs");
        this.getCommand("acmobs").setExecutor(this.commandMultiplexer);

        this.commandMultiplexer.registerCommand(new ReloadConfigCommand(this));

        mobArenaGame = new MobArenaGame(this);
        raidGame = new TownyRaidGame(this);
        spawnManager = new SpawnManager(this);
        trainingArena = new TrainingArenaModule(this);
        tracker = new VelocityTrackerService(5, 3);
        tracker.start();

        if (this.getServer().getPluginManager().getPlugin("ProjectKorra") != null) {
            ProjectKorraHook pkh = new ProjectKorraHook();

            boolean pkCollisions = getConfigRoot().getNode("projectkorra", "collisions").getBoolean(false);

            if (pkCollisions) {
                Game.plugin.createTask(pkh::createAbility, 20);
            }
        }

        try {
            loader.save(configRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public VelocityTrackerService getTrackingService() {
        return tracker;
    }

    @Override
    public void onDisable() {
        if (raidGame != null) {
            Raid raid = raidGame.getRaid();
            if (raid != null) {
                raid.stop();
            }
        }

        spawnManager.destroySpawns();
        trainingArena.stop();
    }

    public void loadConfig() {
        File dataFolder = getDataFolder();

        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                getLogger().warning("Failed to create data folder.");
            }
        }

        File configFile = new File(dataFolder.getPath() + "/atlacore-mobs.conf");
        Path path = configFile.toPath();

        ConfigurationOptions options = ConfigurationOptions.defaults().setShouldCopyDefaults(true);
        loader = HoconConfigurationLoader.builder()
                .setPath(path)
                .setDefaultOptions(options)
                .build();

        try {
            configRoot = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() throws IOException {
        this.mobArenaGame.stop();
        this.raidGame.stop();
        this.trainingArena.stop();

        configRoot = loader.load();

        mobArenaGame = new MobArenaGame(this);
        raidGame = new TownyRaidGame(this);
        trainingArena = new TrainingArenaModule(this);
    }

    public ScriptedUserService getUserService() {
        return userService;
    }

    public TownyRaidGame getRaidGame() {
        return this.raidGame;
    }

    public CommentedConfigurationNode getConfigRoot() {
        return configRoot;
    }

    public CommandMultiplexer getCommandMultiplexer() {
        return this.commandMultiplexer;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }
}
