package com.mnazareno.retrofit.springboot.sample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.CallAdapter;

/**
 * retrofit配置
 *
 * @author Created by gold on 2019-08-26 17:26
 */
@Configuration
public class RetrofitConfig {

    public static final long DEFAULT_TIMEOUT = 15 * 1000;

    @Bean
    public OkHttpClient createOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS)
                .addInterceptor(new HttpLoggingInterceptor())
                .build();
    }

    @Bean
    public CallAdapter.Factory createCallAdapter() {
        return RetrofitCallAdapterFactory.create();
    }
}
