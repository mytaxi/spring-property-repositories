package com.mytaxi.core;

import com.mytaxi.spring.repository.PropertyRepository;

@PropertyRepository
public interface FooBarProperties
{
    String getHello();
    String getHelloByCountry(String country);
    String getHelloByCityOrCountry(String city, String country);
}
