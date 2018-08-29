package com.mytaxi.core;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringPropertyRepositoriesExampleApplicationTests
{

    @Autowired
    private FooBarProperties fooBarProperties;


    @Test
    public void propertiesLoadCorrectly()
    {
        Assertions.assertThat(fooBarProperties.getHello()).isEqualTo("Hello World!");
        Assertions.assertThat(fooBarProperties.getHelloByCountry("germany")).isEqualTo("Hello Germany!");
        Assertions.assertThat(fooBarProperties.getHelloByCountry("england")).isEqualTo("Hello World!");
        Assertions.assertThat(fooBarProperties.getHelloByCityOrCountry("hamburg","germany")).isEqualTo("Hello Hamburg!");
        Assertions.assertThat(fooBarProperties.getHelloByCityOrCountry("berlin","germany")).isEqualTo("Hello Germany!");
        Assertions.assertThat(fooBarProperties.getHelloByCityOrCountry("berlin","england")).isEqualTo("Hello World!");
    }

}
