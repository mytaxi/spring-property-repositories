[![Download](https://api.bintray.com/packages/mytaxi/oss/Spring-Property-Repositories/images/download.svg) ](https://bintray.com/mytaxi/oss/Spring-Property-Repositories/_latestVersion) [![Build Status](https://travis-ci.org/freenowtech/spring-property-repositories.svg?branch=master)](https://travis-ci.org/freenowtech/spring-property-repositories)

# Property Repositories for Spring Framework applications

## Pre Requirements

If you are using yaml files, describing your properties in a unflatten way and using any ids(numbers) as 
part of the property key, please make sure you are using Spring version _>= 2.0.0_.
 
The reason is that the property keys with ids in the name will be mapped using brackets and you will not be able to load it afterward.

ie:

```yaml
hello:
  countries:
    1: Ola!
    2: Servus!
    3: Hello!
```
Internally, it will be mapped to:

```properties
"hello.country[1]" -> "Ola!"
"hello.country[2]" -> "Servus!"
"hello.country[3]" -> "Hello!"
```
If you have this use case, make sure the Spring version is _>= 2.0.0_ 

## How to enable the feature

**Add `@EnablePropertyRepositories` annotation to one of your configuration classes.**
```java
@SpringBootApplication
@EnablePropertyRepositories
public class MyPropertyApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(MyPropertyApplication.class, args);
    }
}
```

**Create a `PropertyRepository`**

Create a new Interface with `PropertyRepository` Annotation
```java
@PropertyRepository
public interface MyProperties {
    
}
```

**Load Properties**
    
**Default properties loading**

Properties loading is done by a method in the `PropertyRepository` interface. 
A method represents a key in the spring `environment`. The key itself is parsed out of the method name , 
where all camel humps of a Method representing a `.` in the property key.
    
```java
@PropertyRepository
public interface MyProperties {
    
    String getPropertyCustomConfig();
}
```

In this example the method `getPropertyCustomConfig` represents the property key `property.custom.config`
    
_Hint: The prefixes 'get' and 'is' are ignored by convention._         

**Customized properties keys**

You can customize the property key by adding `@Property` to the method and overriding the parameter `value` or `key`

```java
@PropertyRepository
public interface MyProperties {

    @Property("custom.config")    
    String getPropertyCustomConfig();
}
```
      
_Hint: Property key with {} are placeholders for parameter values._

```java
@PropertyRepository
public interface MyProperties {
 
 @Property("custom.config.{}.{}")
 String getPropertyCustomConfig(String group, String label);
}
```

_Hint: You can specify a key prefix in the `PropertyRepository` annotation._

```java
@PropertyRepository("key.prefix")
public interface MyProperties {

 @Property
 String getPropertyCustomConfig();
}
```

Which produces `key.prefix.property.custom.config`
 
**Default property behaviour**

**defaultValue** 

`@Property` does have a field called `defaultValue`. With this field you can add a default value that is taken if
no other value is available for the parsed key in the spring `environment`.
   
```java
@PropertyRepository
public interface MyProperties {

    @Property(defaultValue = "default")    
    String getPropertyCustomConfig();
}  
```   

**`By` and `Or` Keyword in method name**

Once you add By to your method name you get a quiet different behaviour.

```java
@PropertyRepository
public interface MyProperties {
    
    String getPropertyCustomConfigByGroup(String group);
}
```
    
_Hint: In first place the value of the argument is used as it comes. If the value is upper case the key will be upper case. As a fallback if no value was found the hole key 
is translated to lowercase to try to find a value._

This Method now creates two property keys. 

- property.custom.config.group.{} ({} a placeholder for any value in parameter group)
- property.custom.config

When you call the method with a group lets say `developers`
The `PropertyRepository` now searches first for the first key `property.custom.config.group.developers`. If this 
key returns a null value the 'default' key `property.custom.config` gets consulted.

```java
@PropertyRepository
public interface MyProperties {
    
    String getPropertyCustomConfigByUsernameOrGroup(String username, String group);
}
```
    
By adding the keyword `Or` you just add another layer of key structure.

- property.custom.config.username.{} ({} a placeholder for any value in parameter username)
- property.custom.config.group.{} ({} a placeholder for any value in parameter group)
- property.custom.config

The loading order is always first come first serve. Means that the username iteration is consulted first, then 
group and than the key without any enhancements.

```java
@PropertyRepository
public interface MyProperties {
    
    @Property(defaultValue = "default")
    String getPropertyCustomConfigByUsernameOrGroup(String username, String group);
}
```
    
By adding `@Property` with a default value the method returns the configured default value if nothing else 
returns a value.
   
## Examples

```java
@PropertyRepository
public interface TestRepository 
{
    // asks for key
    // test.property
    String testProperty();
    
    // asks for keys 
    // 1. test.property.office.{office}
    // 2. test.property.office.{office.lowerCase}
    // 3. test.property
    String testPropertyByOffice(String Office);
    
    // In this example the order of the keys is quite relevant
    // asks for keys 
    // 1. test.property.office.{office}
    // 2. test.property.office.{office.lowerCase}
    // 3. test.property.country.{country}
    // 4. test.property.country.{country.lowerCase}
    // 5. test.property
    String testPropertyByOfficeOrCountry(String Office, String country);
    
    // asks for key 
    // 1. test.property.office.{office}.country.{country}
    // 2. test.property.office.{office.lowerCase}.country.{country.lowerCase}
    String getTestPropertyByOfficeAndCountry(String Office, String country);
   
    // asks for keys
    // 1. test.property.office.{office}.test.{test}
    // 2. test.property.office.{office.lowerCase}.test.{test.lowerCase}
    // 3. test.property.country.{country}
    // 4. test.property.country.{country.lowerCase}
    // 5. test.property
    String testPropertyByOfficeAndTestOrCountry(String Office, String test, String country);

    // asks for key 
    // 1. testclass1
    @Property(key = "testclass1")
    String duplicateProperty();

    // returns the configured defaultValue 42 if no value is configured for key in environment
    // asks for key 
    // 1. test.property.default
    @Property(defaultValue = "42")
    String testPropertyDefault();
    
    // returns the configured defaultValue if no value is configured in environment
    // asks for key
    // 1. property.default.test.{test}
    // 2. property.default.test.{test.lowerCase}
    // 3. property.default 
    @Property(defaultValue = "defaultValueByTest")
    String getPropertyDefaultByTest(String test);
  
    // returns the configured defaultValue if no value is configured in environment
    // asks for key
    // 1. test.property
    @Property(key = "test.property", defaultValue = "defaultValueWithKey")
    String testPropertyKeyAndPropertyDefault();
    
    // asks for key
    // 1. test.PROPERTY_KEY
    // 2. test.property_key 
    @Property(key = "test.PROPERTY_KEY")
    String testPropertyKeyWithCamelCase();
    
    // asks for key
    // 1. test.property.{first}.{second}
    // 2. test.property.{first.lowerCase}.{second.lowerCase}
    @Property(key = "test.property.{}.{}")
    String testProperty(String first, String second);
    
    // Produces a exception at runtime, because no by was found.
    String testProperty(String Office);
    
    // List of generic types, as long as springs DefaultConversionService can convert them.
    List<Long> listOfGenerics();
}
```

## License

The Property Repositories Project is released under version 2.0 of the
[Apache License](http://www.apache.org/licenses/LICENSE-2.0).
