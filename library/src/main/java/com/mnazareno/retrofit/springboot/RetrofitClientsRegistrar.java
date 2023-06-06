package com.mnazareno.retrofit.springboot;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RetrofitClientsRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata,
                                        BeanDefinitionRegistry registry) {

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(
                false) {
            @Override
            protected boolean isCandidateComponent(
                    AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isIndependent()
                        && !beanDefinition.getMetadata().isAnnotation();
            }
        };
        scanner.addIncludeFilter(new AnnotationTypeFilter(RetrofitClient.class));

        Set<String> basePackages = getBasePackages(metadata, registry);

        basePackages.stream()
                .map(scanner::findCandidateComponents)
                .flatMap(Set::stream)
                .forEach(candidateComponent -> {
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    Map<String, Object> attributes = beanDefinition.getMetadata()
                            .getAnnotationAttributes(
                                    RetrofitClient.class.getCanonicalName());
                    if (attributes == null) {
                        return;
                    }

                    String name = resolve((String) attributes.get("name"));
                    String baseUrl = resolve((String) attributes.get("baseUrl"));

                    BeanDefinitionBuilder retrofitClientFactoryBeanBuilder = BeanDefinitionBuilder
                            .genericBeanDefinition(RetrofitClientFactoryBean.class);
                    retrofitClientFactoryBeanBuilder.addPropertyValue("type",
                            beanDefinition.getBeanClassName());
                    retrofitClientFactoryBeanBuilder.addPropertyValue("name", name);
                    retrofitClientFactoryBeanBuilder.addPropertyValue("baseUrl", baseUrl);

                    registerClientConfiguration(registry, name,
                            attributes.get("configuration"));
                    registry.registerBeanDefinition(name,
                            retrofitClientFactoryBeanBuilder.getBeanDefinition());
                });
    }

    private String resolve(String value) {
        if (StringUtils.hasText(value)) {
            return this.environment.resolvePlaceholders(value);
        }
        return value;
    }

    private void registerClientConfiguration(BeanDefinitionRegistry registry, Object name,
                                             Object configuration) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(RetrofitClientSpecification.class);
        builder.addConstructorArgValue(name);
        builder.addConstructorArgValue(configuration);
        registry.registerBeanDefinition(
                name + "." + RetrofitClientSpecification.class.getSimpleName(),
                builder.getBeanDefinition());
    }

    private Set<String> getBasePackages(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(EnableRetrofitClients.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();
        if (attributes == null) {
            return basePackages;
        }

        for (String pkg : (String[]) attributes.get("value")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (String pkg : (String[]) attributes.get("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (Class<?> clazz : (Class[]) attributes.get("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }

        if (basePackages.isEmpty()) {
            String applicationPackage = getApplicationPackage(registry);
            if (!StringUtils.isEmpty(applicationPackage)) {
                basePackages.add(applicationPackage);
            } else {
                basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
            }
        }

        return basePackages;
    }

    private String getApplicationPackage(BeanDefinitionRegistry registry) {
        for (String beanName : registry.getBeanDefinitionNames()) {
            BeanDefinition obj = registry.getBeanDefinition(beanName);
            if (!(obj instanceof AbstractBeanDefinition)) {
                continue;
            }
            AbstractBeanDefinition beanDef = ((AbstractBeanDefinition) obj);
            Class<?> cls = beanDef.getBeanClass();
            SpringBootApplication springBootApplication = cls.getAnnotation(SpringBootApplication.class);
            if (springBootApplication != null) {
                return cls.getPackage().getName();
            }
        }

        return null;
    }
}
