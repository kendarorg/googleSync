package org.old;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

@Configuration
@SpringBootApplication
public class GoogleSyncApplication implements CommandLineRunner {

    public static void main(String[] args) throws Exception {
        //String path = StringUtils.stripEnd(new File(".").getAbsolutePath(),".\\/");
        //String fullPath =path+ File.separator+"log.properties";
        //Files.writeFile("org.hsqldb.persist=SEVERE", new File(fullPath));
        //System.setProperty("java.util.logging.config.file",fullPath);
        /*System.setProperty("hsqldb.reconfig_logging", "false");
        System.setProperty("hsqldb.applog", "0");
        System.setProperty("hsqldb.log_data", "false");
        System.setProperty("org.hsqldb.persist", "SEVERE");*/
        SpringApplication.run(GoogleSyncApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Quickstart.mainStart(args);
    }
}
