package wxdgaming.mariadb;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 处理器
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2024-07-15 17:51
 **/
@Getter
public class ReflectAction {

    public static ReflectAction of() {
        return new ReflectAction();
    }

    private ReflectAction() {}

    private final Set<Class<?>> notConstructor0List = new HashSet<>();

    public void action(Class<?> cls, String packageName) {
        if (cls == Object.class) return;
        if (cls.getSuperclass() != null) action(cls.getSuperclass(), packageName);
        if (StringUtils.isNotBlank(packageName) && !cls.getName().startsWith(packageName))
            return;

        actionField(cls);
        actionMethod(cls);
    }

    public void actionMethod(Class<?> cls) {
        {

            Constructor<?> defaultConstructor1 = null;
            try {
                defaultConstructor1 = cls.getDeclaredConstructor();
            } catch (NoSuchMethodException ignore) {}
            if (defaultConstructor1 == null) {
                notConstructor0List.add(cls);
            }
            Constructor<?>[] declaredConstructors = cls.getDeclaredConstructors();
            for (Constructor<?> declaredConstructor : declaredConstructors) {
                try {
                    Constructor<?> findMethod = cls.getDeclaredConstructor(declaredConstructor.getParameterTypes());
                    System.out.printf("reflectActionConstructor: %s\n", findMethod);
                    findMethod.setAccessible(true);
                    findMethod.newInstance(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
                } catch (Throwable ignore) {}
            }
        }
        {
            Method[] declaredMethods = cls.getDeclaredMethods();
            for (Method method : declaredMethods) {
                try {
                    Method findMethod = cls.getDeclaredMethod(method.getName(), method.getParameterTypes());
                    System.out.printf("reflectActionDeclaredMethod: %s\n", findMethod);
                    findMethod.setAccessible(true);
                    findMethod.invoke(null, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
                } catch (Throwable ignore) {}
            }
        }
        {
            Method[] declaredMethods = cls.getMethods();
            for (Method method : declaredMethods) {
                try {
                    Method findMethod = cls.getMethod(method.getName(), method.getParameterTypes());
                    System.out.printf("reflectActionMethod: %s\n", findMethod);
                    findMethod.setAccessible(true);
                    findMethod.invoke(null, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
                } catch (Throwable ignore) {}
            }
        }
    }

    public void actionField(Class<?> cls) {
        {
            Field[] declaredFields = cls.getDeclaredFields();
            for (Field field : declaredFields) {
                try {
                    System.out.printf("reflectActionDeclaredField: %s\n", cls.getDeclaredField(field.getName()));
                    field.setAccessible(true);
                    field.get(null);
                } catch (Throwable ignore) {}
            }
        }
        {
            Field[] declaredFields = cls.getFields();
            for (Field field : declaredFields) {
                try {
                    System.out.printf("reflectActionField: %s\n", cls.getField(field.getName()));
                    field.setAccessible(true);
                    field.get(null);
                } catch (Throwable ignore) {}
            }
        }
    }

    /** 获取所有资源 <br>如果传入的目录本地文件夹没有，<br>会查找本地目录config目录，<br>如果还没有查找jar包内资源 TODO 开发者模式会有点点路径文件 */
    public static Stream<GraalvmUtil.Tuple<String, InputStream>> resourceStreams(final String path) {
        return resourceStreams(Thread.currentThread().getContextClassLoader(), path);
    }

    /** 获取所有资源 <br>如果传入的目录本地文件夹没有，<br>会查找本地目录config目录，<br>如果还没有查找jar包内资源  TODO 开发者模式会有点点路径文件 */
    public static Stream<GraalvmUtil.Tuple<String, InputStream>> resourceStreams(ClassLoader classLoader, final String path) {
        try {
            String findPath = path;
            boolean fileExists = true;
            if (!new File(path).exists()) {
                fileExists = false;
                if (!path.startsWith("/")) {
                    if (!path.startsWith("config/")) {
                        if (new File("config/" + path).exists()) {
                            findPath = "config/" + path;
                            fileExists = true;
                        }
                    }
                }
            }
            if (!fileExists) {/*当本地文件不存在才查找资源文件*/
                URL resource = classLoader.getResource(path);
                if (resource != null) {
                    findPath = URLDecoder.decode(resource.getPath(), StandardCharsets.UTF_8);
                    if (findPath.startsWith("/")) {
                        findPath = findPath.substring(1);
                    }
                    if (findPath.contains(".zip!") || findPath.contains(".jar!")) {
                        findPath = findPath.substring(5, findPath.indexOf("!/"));
                        try (FileInputStream fileInputStream = new FileInputStream(findPath);
                             ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);) {
                            List<GraalvmUtil.Tuple<String, InputStream>> tmps = new ArrayList<>();
                            ZipEntry nextEntry = null;
                            while ((nextEntry = zipInputStream.getNextEntry()) != null) {
                                if (nextEntry.isDirectory()) continue;
                                String name = nextEntry.getName();
                                String replace = name.replace("\\", "/");
                                if (replace.startsWith(path)) {
                                    /* todo 读取的资源字节可以做解密操作 */
                                    byte[] extra = new byte[(int) nextEntry.getSize()];
                                    zipInputStream.read(extra, 0, (int) nextEntry.getSize());
                                    tmps.add(new GraalvmUtil.Tuple<>(nextEntry.getName(), new ByteArrayInputStream(extra)));
                                }
                            }
                            return tmps.stream();
                        }
                    }
                }
            }

            return listFileStream(findPath, 999, null).map(file -> {
                try {
                    return new GraalvmUtil.Tuple<>(file.getPath(), Files.newInputStream(file.toPath()));
                } catch (Exception e) {
                    throw new RuntimeException("resources:" + file, e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("resources:" + path, e);
        }
    }

    public static Stream<File> listFileStream(String path, int maxDepth, Predicate<File> test) {
        return listFileStream(Paths.get(path), maxDepth, test);
    }

    public static Stream<File> listFileStream(Path path, int maxDepth, Predicate<File> test) {
        try {
            Stream<File> fileStream = Files.walk(path, maxDepth)
                    .map(Path::toFile).filter(File::isFile);
            if (test != null) {
                fileStream = fileStream.filter(test);
            }
            return fileStream;
        } catch (Exception e) {
            throw new RuntimeException(path.toString(), e);
        }
    }

}
