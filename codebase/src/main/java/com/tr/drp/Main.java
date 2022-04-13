package com.tr.drp;

import com.tr.drp.common.model.DFIRequest;
import com.tr.drp.service.dfi.DFIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.AbstractEnvironment;

import javax.annotation.PostConstruct;
import java.nio.file.Paths;

@SpringBootApplication
public class Main implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static ConfigurableApplicationContext applicationContext;

    @Autowired
    private AbstractEnvironment env;

    @Autowired
    private DFIService dfiService;

    public static void main(String... args) {
        log.info("STARTING APPLICATION");
        applicationContext = SpringApplication.run(Main.class, args);
        log.info("APPLICATION FINISHED");
        applicationContext.close();
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("RUN");
        dfiService.processRequest(DFIRequest.builder()
                .csvFile(Paths.get("/abc"))
                .id("0x5")
                .propertiesFile(Paths.get("/p"))
                .build());
    }

    @PostConstruct
    public void init() {
        env.setIgnoreUnresolvableNestedPlaceholders(true);
    }
}
