package com.mytaxi.spring.repository;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

public class PropertyRepositoryRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, BeanClassLoaderAware, EnvironmentAware
{
    private static final Logger LOG = LoggerFactory.getLogger(PropertyRepositoryRegistrar.class);
    private ClassLoader classLoader;
    private Environment environment;
    private ResourceLoader resourceLoader;


    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry)
    {
        final ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);

        final AnnotationTypeFilter includeFilter = new AnnotationTypeFilter(PropertyRepository.class);
        scanner.addIncludeFilter(includeFilter);
        final Set<String> basePackages = getBasePackages(metadata);

        for (final String basePackage : basePackages)
        {
            final Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);

            for (final BeanDefinition candidateComponent : candidateComponents)
            {
                if (candidateComponent instanceof AnnotatedBeanDefinition)
                {
                    final AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    final AnnotationMetadata annotationMetadata = annotatedBeanDefinition.getMetadata();
                    Assert.isTrue(annotationMetadata.isInterface(), "@PropertyRepository can only be specified on an interface");

                    final String className = annotationMetadata.getClassName();
                    BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(PropertyRepositoryFactoryBean.class);
                    definition.addPropertyValue("type", className);
                    definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                    AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();
                    beanDefinition.setPrimary(true);

                    BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className, new String[] {className});
                    BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
                }
            }
        }
    }


    private Set<String> getBasePackages(AnnotationMetadata importingClassMetadata)
    {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnablePropertyRepositories.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();
        if (attributes != null)
        {
            for (String pkg : (String[]) attributes.get("value"))
            {
                if (StringUtils.hasText(pkg))
                {
                    basePackages.add(pkg);
                }
            }
            for (String pkg : (String[]) attributes.get("basePackages"))
            {
                if (StringUtils.hasText(pkg))
                {
                    basePackages.add(pkg);
                }
            }
            for (Class<?> clazz : (Class[]) attributes.get("basePackageClasses"))
            {
                basePackages.add(ClassUtils.getPackageName(clazz));
            }

        }

        if (basePackages.isEmpty())
        {
            basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }


    private ClassPathScanningCandidateComponentProvider getScanner()
    {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment)
        {

            @Override
            protected boolean isCandidateComponent(
                AnnotatedBeanDefinition beanDefinition)
            {
                if (beanDefinition.getMetadata().isIndependent())
                {
                    if (beanDefinition.getMetadata().isInterface()
                        && beanDefinition.getMetadata()
                        .getInterfaceNames().length == 1
                        && Annotation.class.getName().equals(beanDefinition
                        .getMetadata().getInterfaceNames()[0]))
                    {
                        try
                        {
                            Class<?> target = ClassUtils.forName(
                                beanDefinition.getMetadata().getClassName(),
                                PropertyRepositoryRegistrar.this.classLoader);
                            return !target.isAnnotation();
                        }
                        catch (Exception ex)
                        {
                            LOG.error(
                                "Could not load target class: "
                                    + beanDefinition.getMetadata().getClassName(),
                                ex);

                        }
                    }
                    return true;
                }
                return false;

            }
        };
    }


    @Override
    public void setBeanClassLoader(ClassLoader classLoader)
    {

        this.classLoader = classLoader;
    }


    @Override
    public void setEnvironment(Environment environment)
    {

        this.environment = environment;
    }


    @Override
    public void setResourceLoader(ResourceLoader resourceLoader)
    {

        this.resourceLoader = resourceLoader;
    }


    /**
     * Helper class to create a {@link TypeFilter} that matches if all the delegates
     * match.
     *
     * @author Oliver Gierke
     */
    private static class AllTypeFilter implements TypeFilter
    {

        private final List<TypeFilter> delegates;


        /**
         * Creates a new {@link AllTypeFilter} to match if all the given delegates match.
         *
         * @param delegates must not be {@literal null}.
         */
        AllTypeFilter(List<TypeFilter> delegates)
        {

            Assert.notNull(delegates, "delegates should not be null");
            this.delegates = delegates;
        }


        @Override
        public boolean match(
            MetadataReader metadataReader,
            MetadataReaderFactory metadataReaderFactory) throws IOException
        {

            for (TypeFilter filter : this.delegates)
            {
                if (!filter.match(metadataReader, metadataReaderFactory))
                {
                    return false;
                }
            }

            return true;
        }
    }
}
