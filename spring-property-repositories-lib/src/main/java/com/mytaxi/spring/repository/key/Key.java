package com.mytaxi.spring.repository.key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.isEmpty;

public class Key
{
    private static final List<String> PREFIXES = Arrays.asList("get", "is");
    private static final Pattern CAMEL_CASE_REGEXP = Pattern.compile("(?=[A-Z])");
    private KeyToken parent;


    public Key(String prefix, String methodName, String key, List<Object> arguments)
    {
        parseKey(prefix, methodName, key, arguments);
    }


    public Key(String prefix, String methodName, List<Object> arguments)
    {
        parseKey(prefix, methodName, null, arguments);
    }


    public List<String> getKeys()
    {

        final ArrayList<String> keys = new ArrayList<>();
        parent.generateKey(new StringBuilder(), keys);
        return keys;
    }


    private void parseKey(String prefix, String methodName, String key, List<Object> arguments)
    {
        List<String> tokens = isEmpty(prefix) ? new ArrayList<>() : new ArrayList<>(Arrays.asList(prefix.split("\\.")));
        if (key != null)
        {
            for (Object arg : arguments)
            {
                key = key.replaceFirst("\\{}", String.valueOf(arg));
            }
            arguments.clear();
            tokens.addAll(Arrays.asList(key.split("\\.")));
        }
        else
        {

            final List<String> methodTokens = Stream.of(CAMEL_CASE_REGEXP.split(methodName)).map(String::toLowerCase).collect(Collectors.toList());

            if (PREFIXES.contains(methodTokens.get(0)))
            {
                methodTokens.remove(0);
            }
            tokens.addAll(methodTokens);
        }
        parent = new KeyToken(tokens, arguments);
    }



}
