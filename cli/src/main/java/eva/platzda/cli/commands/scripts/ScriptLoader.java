package eva.platzda.cli.commands.scripts;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Loads scripts.
 *
 */
public class ScriptLoader {
    private Map<String, List<String>> scripts = new HashMap<>();

    /**
     *
     * Reads content of resources/scripts.yml
     *
     */
    public ScriptLoader() {
        Yaml yaml = new Yaml();

        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("scripts.yml")) {
            if (in == null) throw new IOException("File not found");

            Map<String, Object> data = yaml.load(in);
            scripts = (Map<String, List<String>>) data.get("scripts");

        } catch (Exception e) {
            System.out.println("/resources/scripts.yml not found - Scripts are not available.");
        }
    }

    /**
     *
     * Returns list of commands of script with given name.
     *
     * @param name
     * @return
     */
    public List<String> getScript(String name) {
        return scripts == null ? null : scripts.get(name);
    }

    /**
     *
     * Request existence of script with name
     *
     * @param name name of script
     * @return true if script exists
     */
    public boolean hasScript(String name) {
        return scripts != null && scripts.containsKey(name);
    }

    /**
     *
     * Returns list of names of available scripts
     *
     * @return
     */
    public List<String> listScripts() {
        if(scripts == null || scripts.isEmpty()) return List.of("No scripts found.");
        return scripts.keySet().stream().toList();
    }
}

