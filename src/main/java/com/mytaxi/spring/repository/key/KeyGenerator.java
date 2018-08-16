package com.mytaxi.spring.repository.key;

import java.util.List;

public interface KeyGenerator
{

    void generateKey(StringBuilder keyBuilder, List<String> propertyKeys);
}
