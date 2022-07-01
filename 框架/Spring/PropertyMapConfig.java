@Configuration
public class PropertyMapConfig {

    private final Environment env;

    public PropertyMapConfig(Environment env) {
        this.env = env;
        map = map();
    }

    private Map<String, Map<String, Object>> map;

    private Map<String, Map<String, Object>> map() {
        Map<String, Map<String, Object>> map = new HashMap<>();
        for (PropertySource<?> source : ((AbstractEnvironment)env).getPropertySources()) {
            if (source instanceof MapPropertySource) {
                map.put(source.getName(), ((MapPropertySource)source).getSource());
            }
        }
        return map;
    }

    public Map<String, String> map(String fileName) {
        Optional<String> first = map.keySet().stream().filter(key -> key.contains(fileName)).findFirst();
        if (first.isPresent()) {
            final Map<String, String> result = new HashMap<>();
            map.get(first.get()).forEach((key, value) -> result.put(key, value.toString()));
            return result;
        }
        return Collections.emptyMap();
    }
}