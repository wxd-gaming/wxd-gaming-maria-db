@echo off
C:\\java\\graalvm-jdk-21.0.3+7.1\\bin\\java -agentlib:native-image-agent=config-merge-dir=target/console/config -Dlogback.configurationFile=logback-gbk.xml -cp "target/console/mysql-server.jar;target/console/lib/*" wxdgaming.mariadb.console.ConsoleMain 1