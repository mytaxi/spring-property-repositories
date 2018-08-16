package com.mytaxi.spring.repository;

import java.lang.reflect.Method;
import java.util.List;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.core.env.Environment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class PropertyRepositoryMethodInterceptorTypeConversionTest
{
    private final Environment environment = mock(Environment.class);
    private final PropertyRepositoryMethodInterceptor propertyRepositoryMethodInterceptor = new PropertyRepositoryMethodInterceptor(environment);


    @After
    public void tearDown()
    {
        PropertyKeyCache.getInstance().clear();
    }


    @Test
    public void convertToListOfLong()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "listOfLongTest");
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[0]);
        final List<?> result = (List<?>) propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        final Object aLong = result.get(0);
        Assert.assertTrue("property not converted to Long", aLong instanceof Long);
    }


    @PropertyRepository
    private interface TestClass
    {
        @Property(defaultValue = "1,2,3")
        List<Long> listOfLongTest();
    }
}
