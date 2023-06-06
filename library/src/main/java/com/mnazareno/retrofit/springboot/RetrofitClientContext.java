package com.mnazareno.retrofit.springboot;

import org.springframework.cloud.context.named.NamedContextFactory;

public class RetrofitClientContext extends NamedContextFactory<RetrofitClientSpecification> {
    public RetrofitClientContext() {
        super(RetrofitAutoConfiguration.DefaultRetrofitClientConfiguration.class, "retrofit", "retrofit.client.name");
    }
}
