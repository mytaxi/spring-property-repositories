package com.mytaxi.spring.repository;

import com.mytaxi.spring.repository.key.Key;
import java.lang.reflect.Method;
import java.util.Collections;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.env.Environment;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class PropertyRepositoryMethodInterceptorInvokeMethodTest
{
    private final Environment environment = mock(Environment.class);
    private final PropertyRepositoryMethodInterceptor propertyRepositoryMethodInterceptor = new PropertyRepositoryMethodInterceptor(environment);

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();


    @After
    public void tearDown()
    {
        PropertyKeyCache.getInstance().clear();
    }


    @Test
    public void invokeMethodWithBySeparatorButWithoutArguments()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testProperty", String.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {"test"});

        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Method arguments available but no 'By' specified key token. Use this phrase 'testPropertyByOffice'");

        propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        verify(environment, VerificationModeFactory.noMoreInteractions());
    }


    @Test
    public void invokeMethodWithoutArguments()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testProperty");
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[0]);

        when(environment.getProperty("test.property", String.class)).thenReturn("testResult");

        final String property = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        assertThat(property, is("testResult"));
    }


    @Test
    public void invokeMethodWithBySeparatorAndWithArguments()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testPropertyByOffice", String.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {"Test"});

        propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        verify(environment).getProperty("test.property.office.Test", String.class);
    }


    @Test
    public void invokeMethodWithBySeparatorAndWithNullArguments()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testPropertyByOffice", String.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {null});
        when(environment.getProperty("test.property", String.class)).thenReturn("default");

        final String property = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);

        verify(environment, times(2)).getProperty("test.property.office.null", String.class);
        assertThat(property, is("default"));
    }


    @Test
    public void invokeMethodWithBySeparatorOrSeparatorWithArguments()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testPropertyByOfficeOrCountry", String.class, String.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {"TestOffice", "TestCountry"});

        when(environment.getProperty("test.property.office.TestOffice", String.class)).thenReturn("office");
        when(environment.getProperty("test.property.country.TestCountry", String.class)).thenReturn("country");

        final String property = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        assertThat(property, is("office"));
    }


    @Test
    public void invokeMethodWithBySeparatorOrSeparatorWithNullArguments()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testPropertyByOfficeOrCountry", String.class, String.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {null, null});

        when(environment.getProperty("test.property", String.class)).thenReturn("default");

        final String property = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);

        verify(environment, times(2)).getProperty("test.property.office.null", String.class);
        verify(environment, times(2)).getProperty("test.property.country.null", String.class);
        assertThat(property, is("default"));
    }


    @Test
    public void invokeMethodWithBySeparatorOrSeparatorWithArgumentsFirstNull()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testPropertyByOfficeOrCountry", String.class, String.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {"TestOffice", "TestCountry"});

        when(environment.getProperty("test.property.office.TestOffice", String.class)).thenReturn(null);
        when(environment.getProperty("test.property.country.TestCountry", String.class)).thenReturn("country");

        final String property = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        assertThat(property, is("country"));
    }


    @Test
    public void invokeMethodWithBySeparatorOrSeparatorWithArgumentsAllNull()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testPropertyByOfficeOrCountry", String.class, String.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {"TestOffice", "TestCountry"});

        when(environment.getProperty("test.property.office.TestOffice", String.class)).thenReturn(null);
        when(environment.getProperty("test.property.country.TestCountry", String.class)).thenReturn(null);
        when(environment.getProperty("test.property", String.class)).thenReturn("default");

        final String property = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        assertThat(property, is("default"));
    }


    @Test
    public void invokeMethodWithArgumentReplacer()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testProperty", String.class, String.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {"blah", "blubb"});
        when(environment.getProperty("test.property.blah.blubb", String.class)).thenReturn("works");

        final String property = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        assertThat(property, is("works"));
    }


    @Test
    public void invokeMethodWithBySeparatorOrSeparatorAndSeparator()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testPropertyByOfficeAndTestOrCountry", String.class, String.class, String.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {"TestOffice", "testProp", "TestCountry"});

        when(environment.getProperty("test.property.office.TestOffice.test.testProp", String.class)).thenReturn("testProp");
        when(environment.getProperty("test.property.country.TestCountry", String.class)).thenReturn(null);
        when(environment.getProperty("test.property", String.class)).thenReturn("default");

        final String property = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        assertThat(property, is("testProp"));
    }


    @Test
    public void invokeMethodWithBySeparatorOrSeparatorAndSeparatorFirstNull()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testPropertyByOfficeAndTestOrCountry", String.class, String.class, String.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {"TestOffice", "testProp", "TestCountry"});

        when(environment.getProperty("test.property.office.TestOffice.test.testProp", String.class)).thenReturn(null);
        when(environment.getProperty("test.property.country.TestCountry", String.class)).thenReturn("country");
        when(environment.getProperty("test.property", String.class)).thenReturn("default");

        final String property = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        assertThat(property, is("country"));
    }


    @Test
    public void invokeMethodWithBySeparatorAndSeparator()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "getTestPropertyByOfficeAndCountry", String.class, String.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {"TestOffice", "TestCountry"});

        when(environment.getProperty("test.property.office.TestOffice.country.TestCountry", String.class)).thenReturn("officeCountry");

        final String property = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        assertThat(property, is("officeCountry"));
    }


    @Test
    public void cacheMethodKeysWithoutArguments()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testProperty");
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[0]);

        when(environment.getProperty("test.property", String.class)).thenReturn("testResult");

        final String property = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        final String result = PropertyKeyCache.getInstance().getKey(methodInvocation);
        assertThat(result, is("test.property"));
    }


    @Test
    public void cacheMethodKeysWithArguments()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testPropertyByOffice", String.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {"TestOffice"});

        when(environment.getProperty("test.property.office.TestOffice", String.class)).thenReturn("testResult");

        final String property = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        final String result = PropertyKeyCache.getInstance().getKey(methodInvocation);
        assertThat(result, is("test.property.office.TestOffice"));
    }


    @Test
    public void cacheMethodKeysWithVaryingArgsArrayInstance()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testPropertyByOffice", String.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {"TestOffice"});
        final MethodInvocation methodInvocation2 = mock(MethodInvocation.class);
        when(methodInvocation2.getMethod()).thenReturn(method);
        when(methodInvocation2.getArguments()).thenReturn(new Object[] {"TestOffice"});

        when(environment.getProperty("test.property.office.TestOffice", String.class)).thenReturn("testResult");

        final String property = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        final String result = PropertyKeyCache.getInstance().getKey(methodInvocation2);
        assertThat(result, is("test.property.office.TestOffice"));
    }


    @Test
    public void cacheMethodKeysWithDefaultValues()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testPropertyDefault");
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {});

        propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        String result = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        assertThat(result, is("defaultValue"));
    }


    @Test
    public void cacheMethodKeysWithMultipleClasses()
    {
        final MethodInvocation methodInvocationClass1 = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "duplicateProperty");
        when(methodInvocationClass1.getMethod()).thenReturn(method);
        when(methodInvocationClass1.getArguments()).thenReturn(new Object[] {});
        when(environment.getProperty("testclass1", String.class)).thenReturn("testclass1");

        final MethodInvocation methodInvocationClass2 = mock(MethodInvocation.class);
        final Method method2 = BeanUtils.findMethod(TestClass2.class, "duplicateProperty");
        when(methodInvocationClass2.getMethod()).thenReturn(method2);
        when(methodInvocationClass2.getArguments()).thenReturn(new Object[] {});
        when(environment.getProperty("testclass2", String.class)).thenReturn("testclass2");

        String result = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocationClass1);
        assertThat(result, is("testclass1"));

        final String result2 = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocationClass2);
        assertThat(result2, is("testclass2"));
    }


    @Test
    public void defaultValueWithKeyInPropertyAnnotation()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testPropertyKeyAndPropertyDefault");
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {});

        String result = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        assertThat(result, is("defaultValueWithKey"));
    }


    @Test
    public void keyWithCamelCase()
    {
        final Method method = BeanUtils.findMethod(TestClass.class, "testPropertyKeyWithCamelCase");
        final Key key = new Key("", method.getName(), method.getAnnotation(Property.class).key(), Collections.emptyList());
        assertThat(key.getKeys().get(0), is("test.PROPERTY_KEY"));
    }


    @Test
    public void defaultValueWithByClause()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "getPropertyDefaultByTest", String.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {"test"});

        final String result2 = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        assertThat(result2, is("defaultValueByTest"));

    }


    @Test
    public void findKeyCaseSensitiveAndCaseInsensitive()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testPropertyByOffice", String.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {"Test"});

        propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        verify(environment).getProperty("test.property.office.Test", String.class);
        verify(environment).getProperty("test.property.office.test", String.class);
    }


    @Test
    public void storeTheCorrectKeyIfOnlyLowerCaseKeyFoundAResult()
    {
        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass.class, "testPropertyByOffice", String.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {"Test"});
        when(environment.getProperty("test.property.office.test", String.class)).thenReturn("lowercase");

        String result = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        verify(environment).getProperty("test.property.office.Test", String.class);
        verify(environment).getProperty("test.property.office.test", String.class);

        assertThat(result, is("lowercase"));

        reset(environment);
        when(environment.getProperty("test.property.office.test", String.class)).thenReturn("lowercase");

        result = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);
        verify(environment, only()).getProperty("test.property.office.test", String.class);
        verifyNoMoreInteractions(environment);

        assertThat(result, is("lowercase"));
    }


    @Test
    public void repositoriesWithPrefixesAddThemToTheKey()
    {

        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass3.class, "property");
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {});

        when(environment.getProperty("test.class.3.property", String.class)).thenReturn("testClass3Property");

        String result = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);

        assertThat(result, is("testClass3Property"));
    }


    @Test
    public void repositoriesWithPrefixesAddThemToTheCustomKey()
    {

        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass3.class, "keyProperty");
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {});

        when(environment.getProperty("test.class.3.custom.key", String.class)).thenReturn("testClass3CustomProperty");

        String result = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);

        assertThat(result, is("testClass3CustomProperty"));
    }


    @Test
    public void repositoriesWithPrefixesAddThemToTheKeyByArgument()
    {

        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass3.class, "propertyByOffice", Long.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {1L});

        when(environment.getProperty("test.class.3.property.office.1", String.class)).thenReturn("testClass3PropertyByOffice");

        String result = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);

        assertThat(result, is("testClass3PropertyByOffice"));
    }


    @Test
    public void repositoriesWithPrefixesAddThemToTheAndRemoveTheMethodPrefixesLikeGet()
    {

        final MethodInvocation methodInvocation = mock(MethodInvocation.class);
        final Method method = BeanUtils.findMethod(TestClass3.class, "getPropertyByOffice", Long.class);
        when(methodInvocation.getMethod()).thenReturn(method);
        when(methodInvocation.getArguments()).thenReturn(new Object[] {1L});

        when(environment.getProperty("test.class.3.property.office.1", String.class)).thenReturn("testClass3GetPropertyByOffice");

        String result = (String) propertyRepositoryMethodInterceptor.invoke(methodInvocation);

        assertThat(result, is("testClass3GetPropertyByOffice"));
    }


    @PropertyRepository
    private interface TestClass
    {
        // generates key
        // test.property
        String testProperty();

        // generates keys
        // 1. test.property.office.{office}
        // 2. test.property.office.{office.lowerCase}
        // 3. test.property
        String testPropertyByOffice(String Office);

        // In this example the order of the keys is quite relevant
        // generates keys
        // 1. test.property.office.{office}
        // 2. test.property.office.{office.lowerCase}
        // 3. test.property.country.{country}
        // 4. test.property.country.{country.lowerCase}
        // 5. test.property
        String testPropertyByOfficeOrCountry(String Office, String country);

        // generates key
        // 1. test.property.office.{office}.country.{country}
        // 2. test.property.office.{office.lowerCase}.country.{country.lowerCase}
        String getTestPropertyByOfficeAndCountry(String Office, String country);

        // generates keys
        // 1. test.property.office.{office}.test.{test}
        // 2. test.property.office.{office.lowerCase}.test.{test.lowerCase}
        // 3. test.property.country.{country}
        // 4. test.property.country.{country.lowerCase}
        // 5. test.property
        String testPropertyByOfficeAndTestOrCountry(String Office, String test, String country);

        // generates key
        // 1. testclass1
        @Property(key = "testclass1")
        String duplicateProperty();

        // uses the configured defaultValue if no value is configured in environment
        // generates key
        // 1. test.property.default
        @Property(defaultValue = "defaultValue")
        String testPropertyDefault();

        // uses the configured defaultValue if no value is configured in environment
        // generates key
        // 1. property.default.test.{test}
        // 2. property.default.test.{test.lowerCase}
        // 3. property.default
        @Property(defaultValue = "defaultValueByTest")
        String getPropertyDefaultByTest(String test);

        // uses the configured defaultValue if no value is configured in environment
        // generates key
        // 1. test.property
        @Property(key = "test.property", defaultValue = "defaultValueWithKey")
        String testPropertyKeyAndPropertyDefault();

        // generates key
        // 1. test.PROPERTY_KEY
        // 2. test.property_key
        @Property(key = "test.PROPERTY_KEY")
        String testPropertyKeyWithCamelCase();

        // generates key
        // 1. test.property.{first}.{second}
        // 1. test.property.{first.lowerCase}.{second.lowerCase}
        @Property(key = "test.property.{}.{}")
        String testProperty(String first, String second);

        // Produces a exception at runtime, because no by was found.
        String testProperty(String Office);
    }

    @PropertyRepository
    private interface TestClass2
    {
        @Property(key = "testclass2")
        String duplicateProperty();

    }

    @PropertyRepository(prefix = "test.class.3")
    private interface TestClass3
    {
        String property();

        @Property(key = "custom.key")
        String keyProperty();

        String propertyByOffice(Long office);

        String getPropertyByOffice(Long office);
    }
}