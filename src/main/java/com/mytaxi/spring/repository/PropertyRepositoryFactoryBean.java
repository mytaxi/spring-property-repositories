package com.mytaxi.spring.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

public class PropertyRepositoryFactoryBean implements FactoryBean<Object>, InitializingBean, EnvironmentAware
{

    private Class<?> type;
    private static final Logger LOG = LoggerFactory.getLogger(PropertyRepositoryFactoryBean.class);
    private Environment environment;


    @Override
    public Object getObject()
    {
        try
        {
            final ProxyFactory proxyFactory = new ProxyFactory();
            proxyFactory.setInterfaces(type);
            proxyFactory.addAdvice(new PropertyRepositoryMethodInterceptor(environment));
            return proxyFactory.getProxy();
        }
        catch (AopConfigException e)
        {
            LOG.error("unable to proxy {}", type.getSimpleName(), e);
            throw e;
        }
    }


    @Override
    public Class<?> getObjectType()
    {
        return type;
    }


    @Override
    public boolean isSingleton()
    {
        return true;
    }


    @Override
    public void afterPropertiesSet() throws Exception
    {
        LOG.info(type.getSimpleName() + " initialized");
    }


    public void setType(Class<?> type)
    {
        this.type = type;
    }


    @Override
    public void setEnvironment(Environment environment)
    {

        this.environment = environment;
    }
}
