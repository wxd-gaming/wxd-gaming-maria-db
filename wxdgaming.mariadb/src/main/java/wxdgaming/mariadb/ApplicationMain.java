package wxdgaming.mariadb;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.mariadb.server.DBFactory;
import wxdgaming.mariadb.server.WebService;
import wxdgaming.tailfn.ConsoleApplication;
import wxdgaming.tailfn.ConsoleController;
import wxdgaming.tailfn.ViewConfig;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

@Slf4j
public class ApplicationMain {

    public static void main(String[] args) {

        RunAsync.async(() -> {
            ViewConfig.loadYaml("target/db-view.yml");
            ViewConfig.ins.tailFNPath = "target/logs/db.log";
            ConsoleApplication.__Title = "数据库引擎";
            ConsoleApplication.__iconName = "db-icon.png";

            ConsoleController.initEndCallback = (consoleController) -> {

                consoleController.addMenuItem(0, "备份数据库", () -> {
                    DBFactory.getIns().getMyDB().bakSql();
                });

                consoleController.addSeparatorMenuItem(consoleController.menu_file.getItems().size() - 1);
                consoleController.addMenuItem(consoleController.menu_file.getItems().size() - 1, "清档数据库", () -> {
                    RunAsync.async(() -> {
                        DBFactory.getIns().getMyDB().bakSql();
                        DBFactory.getIns().stop();
                        clearFile("data-base/data");
                        startDb();
                    });
                });
                consoleController.addSeparatorMenuItem(consoleController.menu_file.getItems().size() - 1);

                RunAsync.async(() -> {
                    try {
                        DbConfig.loadYaml();
                        WebService.getIns().start(DbConfig.ins.getWebPort());
                        Thread.sleep(500);
                        startDb();
                        initGraalvm();
                        Runtime.getRuntime().addShutdownHook(new Thread(() -> DBFactory.getIns().stop()));
                    } catch (Throwable throwable) {
                        log.info("数据库启动异常", throwable);
                        GraalvmUtil.write(99, "启动异常：" + throwable.toString());
                        Runtime.getRuntime().exit(99);
                    }
                });
            };
            Application.launch(ConsoleApplication.class);
        });
    }

    public static void initGraalvm() {
        try {
            if (!"true".equalsIgnoreCase(System.getProperty("build.graalvm"))) {
                return;
            }
            System.setProperty("build.graalvm", "");
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            List<String> strings = GraalvmUtil.jarResources();
            for (String string : strings) {
                URL resource = contextClassLoader.getResource(string);
                System.out.printf("%s - %s %n", string, resource);
            }

            ReflectAction reflectAction = ReflectAction.of();

            List<Class<?>> classes = GraalvmUtil.jarClasses("wxdgaming");
            for (Class<?> cls : classes) {
                reflectAction.action(cls, cls.getPackageName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void clearFile(String path) {
        try {
            Path start = Paths.get(path);
            if (Files.exists(start)) {
                Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        log.info("清理文件：" + file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        log.info("清理文件：" + dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } else {
                log.info(path + " 文件夹不存在");
            }
        } catch (Exception e) {
            log.error("清档异常", e);
        }
        log.info("清档完成，需要手动启动");
    }

    public static void startDb() {
        try {
            GraalvmUtil.write(1, "启动中");
            boolean initResult = DBFactory.getIns().init(
                    DbConfig.ins.getDataBases(),
                    DbConfig.ins.getPort(),
                    DbConfig.ins.getUser(),
                    DbConfig.ins.getPwd()
            );
            if (!initResult) return;
            DBFactory.getIns().print();
        } catch (Throwable e) {
            log.error("start failed ", e);
            log.info("数据库启动异常");
            log.info("数据库启动异常");
            log.info("数据库启动异常");
            GraalvmUtil.write(99, "启动异常：" + e.toString());
            Runtime.getRuntime().exit(99);
        }
    }

}