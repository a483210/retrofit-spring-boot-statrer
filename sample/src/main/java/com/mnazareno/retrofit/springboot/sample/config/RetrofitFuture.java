package com.mnazareno.retrofit.springboot.sample.config;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * retrofit java8返回逻辑封装
 *
 * @author Created by gold on 2019-08-26 17:25
 */
public class RetrofitFuture<T> extends CompletableFuture<T> {

    private int maxRetries;

    /**
     * 返回参数，如果请求错误会返回空
     */
    @Nullable
    public T ofNullable() {
        try {
            return get();
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 返回optional
     */
    @NonNull
    public Optional<T> optional() {
        return Optional.ofNullable(ofNullable());
    }

    /**
     * 重试
     *
     * @param maxRetries 次数
     */
    public RetrofitFuture<T> retry(int maxRetries) {
        if (maxRetries <= 0) {
            return this;
        }

        this.maxRetries = maxRetries;

        return this;
    }

    boolean hasRetry() {
        return maxRetries-- > 0;
    }

}