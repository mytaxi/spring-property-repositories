package com.mytaxi.spring.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property
{
    /**
     * The same as key
     *
     * @return the key
     */
    String value() default "";

    /**
     * The property key
     *
     * @return the key
     */
    String key() default "";

    /**
     * The default value if no property found
     *
     * @return the default value
     */
    String defaultValue() default "";
}
