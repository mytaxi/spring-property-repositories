package com.mytaxi.spring.repository;

import com.mytaxi.spring.repository.key.Key;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@SuppressWarnings("Duplicates")
class PropertyRepositoryMethodInterceptor implements MethodInterceptor
{

    private static final List<String> PREFIXES = Arrays.asList("get", "is");
    private static final String CAMEL_CASE_REGEXP = "(?=[A-Z])";
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyRepositoryMethodInterceptor.class);
    private final Environment environment;
    private volatile DefaultConversionService conversionService = new DefaultConversionService();


    PropertyRepositoryMethodInterceptor(Environment environment)
    {
        this.environment = environment;
    }


    @SuppressWarnings("ConstantConditions")
    @Override
    public Object invoke(MethodInvocation methodInvocation)
    {
        final Method method = methodInvocation.getMethod();
        final Property propertyAnnotation = method.getAnnotation(Property.class);
        final String methodName = method.getName();
        final String declaringClass = method.getDeclaringClass().getCanonicalName();

        final String cachedKey = PropertyKeyCache.getInstance().getKey(methodInvocation);
        if (!StringUtils.isEmpty(cachedKey))
        {
            Object property = environment.getProperty(cachedKey, method.getReturnType());
            if (property != null)
            {
                LOGGER.trace("Method '{}' mapped to key '{}' from '{}' has value '{}'", methodName, cachedKey, declaringClass, property);
                return convertProperty(property, method);
            }
            if (propertyAnnotation != null)
            {
                return getDefaultValueFromProperty(method, propertyAnnotation);
            }
            return null;
        }

        if (methodInvocation.getArguments().length > 0 && !method.getName().contains("By") && propertyAnnotation == null)
        {
            throw new IllegalArgumentException("Method arguments available but no 'By' specified key token. Use this phrase 'testPropertyByOffice'");
        }
        else if (propertyAnnotation != null)
        {
            final String key = StringUtils.isEmpty(propertyAnnotation.value()) ? propertyAnnotation.key() : propertyAnnotation.value();
            if (!StringUtils.isEmpty(key) && StringUtils.countOccurrencesOf(key, "{}") != methodInvocation.getArguments().length)
            {
                throw new IllegalArgumentException("Method arguments arguments do not match replacement chars");
            }
        }

        final PropertyRepository propertyRepository = methodInvocation.getMethod().getDeclaringClass().getAnnotation(PropertyRepository.class);
        String annotationKey = null;
        if (propertyAnnotation != null && propertyAnnotation.value() != null)
        {
            annotationKey = propertyAnnotation.key();
        }
        final List<String> keys = new Key(
            propertyRepository.prefix(),
            methodInvocation.getMethod().getName(),
            annotationKey,
            new ArrayList<>(Arrays.asList(methodInvocation.getArguments()))).getKeys();
        for (String key : keys)
        {
            final Object property = getProperty(methodInvocation, key);
            if (property != null)
            {
                return property;
            }
        }

        if (propertyAnnotation != null)
        {
            return getDefaultValueFromProperty(method, propertyAnnotation);
        }
        return null;
    }


    private Object getDefaultValueFromProperty(final Method method, final Property propertyAnnotation)
    {
        Object property = null;
        String defaultValue = propertyAnnotation.defaultValue();
        if (!StringUtils.isEmpty(defaultValue))
        {
            property = convertProperty(defaultValue, method);
        }
        return property;
    }


    @SuppressWarnings("ConstantConditions")
    private Object getProperty(MethodInvocation methodInvocation, String key)
    {
        Object property = environment.getProperty(key, String.class);
        if (property != null)
        {
            PropertyKeyCache.getInstance().addKey(methodInvocation, key);
            return convertProperty(property, methodInvocation.getMethod());
        }
        property = environment.getProperty(key.toLowerCase(), methodInvocation.getMethod().getReturnType());
        if (property != null)
        {
            PropertyKeyCache.getInstance().addKey(methodInvocation, key.toLowerCase());
            return convertProperty(property, methodInvocation.getMethod());
        }
        return null;
    }


    private Object convertProperty(Object property, Method method)
    {
        if (property != null)
        {
            if (method.getReturnType().isAssignableFrom(List.class))
            {
                return conversionService.convert(property, TypeDescriptor.collection(
                    method.getReturnType(),
                    TypeDescriptor.valueOf((Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0])));
            }
            return conversionService.convert(property, method.getReturnType());
        }
        return null;
    }


}
