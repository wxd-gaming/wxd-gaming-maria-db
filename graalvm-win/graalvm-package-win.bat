@echo off
C:\\java\\graalvm-jdk-21.0.3+7.1\\bin\\java -agentlib:native-image-agent=config-merge-dir=graalvm-win/config -Dfile.encoding=UTF-8 -Dlogback.configurationFile=logback-gbk.xml -cp "target/mysql-server.jar;target/lib/*" wxdgaming.mariadb.winfm.ApplicationMain 1