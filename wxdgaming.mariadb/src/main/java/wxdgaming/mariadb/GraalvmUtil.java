package wxdgaming.mariadb;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class GraalvmUtil {

    public static final File ok = new File("db-ok.txt");

    public static void write(int state, String msg) {
        String json = """
                {"PID": %s, "states": %s, "web-port": %s, "msg": "%s"}
                """.formatted(fetchProcessId(), state, DbConfig.ins.getWebPort(), msg);
        writeFile(ok, json);
    }

    public static void writeFile(File file, String content) {
        try {
            Files.writeString(
                    file.toPath(),
                    content,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public static String fetchProcessId() {
        try {
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            String name = runtime.getName();
            return name.substring(0, name.indexOf("@"));

        } catch (Exception ignore) {
            return "-1";
        }
    }

    public static String javaClassPath() {
        return System.getProperty("java.class.path");
    }

    public static List<Class<?>> jarClasses(String... packageNames) throws Exception {
        List<String> strings = jarResources();
        List<Class<?>> classes = new ArrayList<>();
        Predicate<String> predicate = string -> {
            for (String packageName : packageNames) {
                if (string.startsWith(packageName)) {
                    return true;
                }
            }
            return false;
        };
        for (String string : strings) {
            if (string.endsWith(".class")) {
                String substring = string.substring(0, string.length() - 6);
                String replace = substring.replace('/', '.');
                replace = replace.replace('\\', '.');
                try {
                    if (predicate.test(replace)) {
                        Class<?> aClass = GraalvmUtil.class.getClassLoader().loadClass(replace);
                        classes.add(aClass);
                    }
                } catch (Throwable ignored) {}
            }
        }
        return classes;
    }

    public static List<String> jarResources() throws Exception {
        List<String> resourcesPath = new ArrayList<>();
        String x = javaClassPath();
        String[] split = x.split(File.pathSeparator);
        List<String> collect = Arrays.stream(split).sorted().toList();
        for (String string : collect) {
            Path start = Paths.get(string);
            if (!string.endsWith(".jar") && !string.endsWith(".war") && !string.endsWith(".zip")) {
                if (string.endsWith("classes")) {
                    try (Stream<Path> stream = Files.walk(start)) {
                        String target = start.toString();
                        stream
                                .map(Path::toString)
                                .filter(s -> s.startsWith(target) && s.length() > target.length())
                                .map(s -> s.substring(target.length() + 1))
                                .forEach(resourcesPath::add);
                    }
                    continue;
                }
                continue;
            }

            try (JarFile jarFile = new JarFile(string)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    String entryName = jarEntry.getName();
                    resourcesPath.add(entryName);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        Collections.sort(resourcesPath);
        return resourcesPath;
    }

    public static class Tuple<F, S> {

        public final F f;
        public final S s;

        public Tuple(F f, S s) {
            this.f = f;
            this.s = s;
        }

    }
}
