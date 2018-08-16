package com.mytaxi.spring.repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.aopalliance.intercept.MethodInvocation;

public class PropertyKeyCache
{
    private static PropertyKeyCache ourInstance = new PropertyKeyCache();

    private final Map<CacheKey, String> cache = new HashMap<>();


    public static PropertyKeyCache getInstance()
    {
        return ourInstance;
    }


    private PropertyKeyCache()
    {
    }


    public void clear()
    {
        cache.clear();
    }


    public String getKey(MethodInvocation methodInvocation)
    {
        return cache.get(new CacheKey(methodInvocation));
    }


    public void addKey(MethodInvocation methodInvocation, String key)
    {
        cache.put(new CacheKey(methodInvocation), key);
    }


    private class CacheKey
    {
        private String methodName;
        private Object[] args;
        private String className;


        CacheKey(MethodInvocation methodInvocation)
        {
            className = methodInvocation.getMethod().getDeclaringClass().getName();
            methodName = methodInvocation.getMethod().getName();
            args = methodInvocation.getArguments();
        }


        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(methodName, cacheKey.methodName) &&
                Arrays.equals(args, cacheKey.args) &&
                Objects.equals(className, cacheKey.className);
        }


        @Override
        public int hashCode()
        {
            int result = Objects.hash(methodName, className);
            result = 31 * result + Arrays.hashCode(args);
            return result;
        }
    }
}
