package com.pix.pixelmonfix.config;

import com.pix.pixelmonfix.PixelmonFixMain;
import net.neoforged.fml.loading.FMLPaths;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.inspector.TagInspector;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;

public class ConfigManager {

    private static final File CONFIG_FILE = new File(FMLPaths.CONFIGDIR.get().toFile(), "pixelmon_fixes.yml");
    public static FixConfig CONFIG = new FixConfig();

    public static void loadConfig() {
        if (CONFIG_FILE.exists()) {
            try (InputStream inputStream = new FileInputStream(CONFIG_FILE)) {
                
                LoaderOptions loaderOptions = new LoaderOptions();
                TagInspector tagInspector = tag -> tag.getClassName().equals(FixConfig.class.getName());
                loaderOptions.setTagInspector(tagInspector);

                Yaml yaml = new Yaml(new Constructor(FixConfig.class, loaderOptions));
                CONFIG = yaml.loadAs(inputStream, FixConfig.class);

                if (CONFIG == null) {
                    CONFIG = new FixConfig();
                }
                PixelmonFixMain.LOGGER.info("Successfully loaded pixelmon_fixes.yml");
            } catch (Exception e) {
                PixelmonFixMain.LOGGER.error("Failed to read YAML config file!", e);
            }
        } else {
            saveConfig();
            PixelmonFixMain.LOGGER.info("Generated default pixelmon_fixes.yml");
        }
    }

    public static void saveConfig() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        
        Representer representer = new Representer(options);
        representer.addClassTag(FixConfig.class, Tag.MAP);

        Yaml yaml = new Yaml(representer, options);

        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            yaml.dump(CONFIG, writer);
        } catch (Exception e) {
            PixelmonFixMain.LOGGER.error("Failed to save YAML config file!", e);
        }
    }
}