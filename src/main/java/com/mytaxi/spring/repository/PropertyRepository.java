package com.mytaxi.spring.repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

@Component
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyRepository
{
    /**
     * When adding a prefix the keys are loaded directly with the configured prefix in front of it.
     *
     * @return key prefix
     */
    String prefix() default "";
}
