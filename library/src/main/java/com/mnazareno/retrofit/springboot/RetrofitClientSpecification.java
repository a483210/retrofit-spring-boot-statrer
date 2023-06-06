package com.mnazareno.retrofit.springboot;

import org.springframework.cloud.context.named.NamedContextFactory;

public class RetrofitClientSpecification implements NamedContextFactory.Specification {

    private final String name;

    private final Class<?>[] configs;

    public RetrofitClientSpecification(String name, Class<?>[] configs) {
        this.name = name;
        this.configs = configs;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Class<?>[] getConfiguration() {
        return this.configs;
    }
}
