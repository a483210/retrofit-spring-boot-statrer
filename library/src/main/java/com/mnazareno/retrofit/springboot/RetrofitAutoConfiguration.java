package com.mnazareno.retrofit.springboot;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Converter;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.List;
import java.util.Optional;

@Configuration(proxyBeanMethods = false)
public class RetrofitAutoConfiguration {

    @Bean
    public RetrofitClientContext retrofitContext(
            Optional<List<RetrofitClientSpecification>> specs) {
        RetrofitClientContext retrofitClientContext = new RetrofitClientContext();
        specs.ifPresent(retrofitClientContext::setConfigurations);
        return retrofitClientContext;
    }

    @Configuration(proxyBeanMethods = false)
    @EnableRetrofitClients
    static class RetrofitClientConfiguration {
    }

    @Configuration(proxyBeanMethods = false)
    static class DefaultRetrofitClientConfiguration {

        @Bean
        public Converter.Factory jacksonConverter(ObjectMapper objectMapper) {
            return JacksonConverterFactory.create(objectMapper);
        }

        @Bean
        @ConditionalOnMissingBean
        public ObjectMapper objectMapper() {
            ObjectMapper om = new ObjectMapper();
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return om;
        }
    }
}
