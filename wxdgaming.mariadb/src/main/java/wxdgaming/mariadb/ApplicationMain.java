package wxdgaming.mariadb;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import wxdgaming.mariadb.server.DBFactory;
import wxdgaming.mariadb.server.WebService;
import wxdgaming.tailfn.ConsoleApplication;
import wxdgaming.tailfn.ConsoleController;
import wxdgaming.tailfn.ViewConfig;

import java.net.URL;
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
                initGraalvm();
                DbConfig.loadYaml();
                startDb(true);

                consoleController.addMenuItem(0, "备份数据库", () -> {
                    DBFactory.getIns().getMyDB().bakSql();
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

    public static void startDb(boolean checked) {
        try {
            GraalvmUtil.write(1, "启动中");
            DbConfig.loadYaml();
            WebService.getIns().start(DbConfig.ins.getWebPort());
            Thread.sleep(500);
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