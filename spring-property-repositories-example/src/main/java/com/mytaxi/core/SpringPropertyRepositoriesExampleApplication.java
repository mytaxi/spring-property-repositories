package com.mytaxi.core;

import com.mytaxi.spring.repository.EnablePropertyRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringPropertyRepositoriesExampleApplication implements ApplicationRunner
{

    private static final Logger log = LoggerFactory.getLogger(SpringPropertyRepositoriesExampleApplication.class);
    private final FooBarProperties fooBarProperties;


    public SpringPropertyRepositoriesExampleApplication(FooBarProperties fooBarProperties)
    {
        this.fooBarProperties = fooBarProperties;
    }


    public static void main(String[] args)
    {
        SpringApplication.run(SpringPropertyRepositoriesExampleApplication.class, args);
    }


    @Override
    public void run(ApplicationArguments args) throws Exception
    {

        log.info(fooBarProperties.getHello());
        log.info(fooBarProperties.getHelloByCountry("germany"));
        log.info(fooBarProperties.getHelloByCountry("england"));
        log.info(fooBarProperties.getHelloByCityOrCountry("hamburg","germany"));
        log.info(fooBarProperties.getHelloByCityOrCountry("berlin","germany"));
        log.info(fooBarProperties.getHelloByCityOrCountry("berlin","england"));
    }
}
