package com.mytaxi.spring.repository.key;

import java.util.ArrayList;
import java.util.List;

public class OrRelation implements KeyGenerator
{
    private List<KeyToken> orBranches = new ArrayList<>();


    OrRelation(List<String> tokens, List<Object> arguments)
    {
        while (!tokens.isEmpty())
        {
            orBranches.add(new KeyToken(tokens, arguments));
        }
    }


    @Override
    public void generateKey(StringBuilder keyBuilder, List<String> propertyKeys)
    {
        for (KeyToken keyToken : orBranches)
        {
            keyToken.generateKey(new StringBuilder(keyBuilder.toString()), propertyKeys);
        }
    }
}
