package wxdgaming.mariadb;


import lombok.extern.slf4j.Slf4j;

/**
 * 异步执行
 *
 * @author: wxd-gaming(無心道, 15388152619)
 * @version: 2025-02-12 09:27
 **/
@Slf4j
public class RunAsync {


    public static void async(PlatformRunnable runnable) {
        Thread thread = new Thread(() -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                log.error(runnable.toString(), e);
            }
        });
        thread.start();
    }

}
