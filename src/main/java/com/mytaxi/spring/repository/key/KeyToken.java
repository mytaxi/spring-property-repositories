package com.mytaxi.spring.repository.key;

import java.util.List;

public class KeyToken implements KeyGenerator
{
    private static final String OR_TOKEN = "or";
    private static final String AND_TOKEN = "and";
    private static final String BY_TOKEN = "by";
    private KeyToken child = null;
    private KeyGenerator relation;
    private String token;


    private KeyToken(String token)
    {
        this.token = token;
    }


    public KeyToken(List<String> tokens, List<Object> arguments)
    {
        if (!tokens.isEmpty())
        {
            String token = tokens.remove(0);
            if (token.equalsIgnoreCase(BY_TOKEN))
            {
                relation = new OrRelation(tokens, arguments);
                return;
            }
            else if (token.equalsIgnoreCase(AND_TOKEN))
            {
                this.token = String.valueOf(arguments.remove(0));
                child = new KeyToken(tokens, arguments);
                return;
            }
            this.token = token;
            if (!tokens.isEmpty())
            {
                if (tokens.get(0).equalsIgnoreCase(OR_TOKEN))
                {
                    tokens.remove(0);
                    child = new KeyToken(String.valueOf(arguments.remove(0)));
                    return;
                }
                child = new KeyToken(tokens, arguments);
            }
            else if (!arguments.isEmpty())
            {
                child = new KeyToken(String.valueOf(arguments.remove(0)));
            }

        }
    }


    @Override
    public void generateKey(StringBuilder keyBuilder, List<String> propertyKeys)
    {

        if (relation != null)
        {
            relation.generateKey(new StringBuilder(keyBuilder.toString()), propertyKeys);
        }
        else if (child != null)
        {
            keyBuilder.append(token);
            if ((child.token != null && !child.token.equals("")) || child.relation != null)
            {
                keyBuilder.append(".");
            }
            child.generateKey(keyBuilder, propertyKeys);
        }

        if (child == null)
        {
            if (token != null)
            {
                keyBuilder.append(token);
            }
            else
            {
                if (keyBuilder.charAt(keyBuilder.length() - 1) == '.')
                {
                    keyBuilder.deleteCharAt(keyBuilder.length() - 1);
                }
            }
            propertyKeys.add(keyBuilder.toString());
        }
    }
}
