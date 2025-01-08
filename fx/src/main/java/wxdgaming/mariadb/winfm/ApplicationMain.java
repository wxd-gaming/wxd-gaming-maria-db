package wxdgaming.mariadb.winfm;

import javafx.application.Application;
import wxdgaming.mariadb.server.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class ApplicationMain {

    public static Properties properties = null;

    public static String javaClassPath() {
        return System.getProperty("java.class.path");
    }

    public static void main(String[] args) throws Exception {

        properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(Paths.get("my.ini"));
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            properties.load(inputStreamReader);
            HelloApplication.__title = String.valueOf(properties.getOrDefault("title", HelloApplication.__title));
        }

        LogbackResetTimeFilter.out = true;

        CompletableFuture.runAsync(() -> Application.launch(HelloApplication.class))
                .exceptionally(t -> {
                    t.printStackTrace(System.out);
                    return null;
                });
        startDb(true);
    }

    public static void initGraalvm() {
        try {
            if (javaClassPath() != null && javaClassPath().contains(".jar")) {
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                List<String> strings = GraalvmUtil.jarResources();
                for (String string : strings) {
                    URL resource = contextClassLoader.getResource(string);
                    System.out.println(string + " - " + resource);
                }

                ReflectAction reflectAction = ReflectAction.of();
                reflectAction.action(MyDB.class, false);
                reflectAction.action(DBFactory.class, false);
                reflectAction.action(HelloApplication.class, false);
                reflectAction.action(HelloController.class, false);
            }
        } catch (Throwable e) {
            e.printStackTrace(System.out);
        }
    }

    public static void startDb(boolean checked) {
        try {
            Thread.sleep(500);
            if (checked) {
                int webPort = Integer.parseInt(properties.getProperty("web-port"));

                try (HttpClient client = HttpClient.newHttpClient()) {

                    HttpRequest build = HttpRequest.newBuilder().GET().uri(URI.create("http://localhost:" + webPort + "/api/db/show")).build();
                    HttpResponse<byte[]> send = client.send(build, HttpResponse.BodyHandlers.ofByteArray());
                    /*正常访问说明已经打开过，退出当前程序，*/
                    System.exit(0);
                    return;
                } catch (Exception ignore) {
                    /*如果访问报错说没有启动过；*/
                }
                System.out.println("可以正常开启");

                WebService.getIns().setPort(webPort);
            }
            DBFactory.getIns().init(
                    properties.getProperty("database"),
                    Integer.parseInt(properties.getProperty("port")),
                    properties.getProperty("user"),
                    properties.getProperty("pwd")
            );


            DBFactory.getIns().print();
            WebService.getIns().start();
            WebService.getIns().initShow();

        } catch (Throwable e) {
            e.printStackTrace(System.out);
            System.out.println("启动异常了！");
            System.out.println("启动异常了！");
            System.out.println("启动异常了！");
            System.out.println("启动异常了！");
        }
    }
}