package com.mnazareno.retrofit.springboot.sample.config;

import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * retrofit适配工厂
 *
 * @author Created by gold on 2019-08-26 17:25
 */
public final class RetrofitCallAdapterFactory extends CallAdapter.Factory {
    public static RetrofitCallAdapterFactory create() {
        return new RetrofitCallAdapterFactory();
    }

    private RetrofitCallAdapterFactory() {
    }

    @Override
    public @Nullable
    CallAdapter<?, ?> get(
            Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(returnType) != RetrofitFuture.class) {
            return null;
        }
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalStateException("RetrofitFuture return type must be parameterized"
                    + " as RetrofitFuture<Foo> or RetrofitFuture<? extends Foo>");
        }
        Type innerType = getParameterUpperBound(0, (ParameterizedType) returnType);

        if (getRawType(innerType) != Response.class) {
            // Generic type is not Response<T>. Use it for body-only adapter.
            return new BodyCallAdapter<>(innerType);
        }

        // Generic type is Response<T>. Extract T and create the Response version of the adapter.
        if (!(innerType instanceof ParameterizedType)) {
            throw new IllegalStateException("Response must be parameterized"
                    + " as Response<Foo> or Response<? extends Foo>");
        }
        Type responseType = getParameterUpperBound(0, (ParameterizedType) innerType);
        return new ResponseCallAdapter<>(responseType);
    }

    private static final class BodyCallAdapter<R> implements CallAdapter<R, RetrofitFuture<R>> {
        private final Type responseType;

        BodyCallAdapter(Type responseType) {
            this.responseType = responseType;
        }

        @Override
        public Type responseType() {
            return responseType;
        }

        @Override
        public RetrofitFuture<R> adapt(final Call<R> call) {
            final RetrofitFuture<R> future = new RetrofitFuture<R>() {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    if (mayInterruptIfRunning) {
                        call.cancel();
                    }
                    return super.cancel(mayInterruptIfRunning);
                }
            };

            enqueue(call, future);

            return future;
        }

        private void enqueue(Call<R> call, RetrofitFuture<R> future) {
            call.enqueue(new Callback<R>() {
                @Override
                public void onResponse(Call<R> call, Response<R> response) {
                    if (response.isSuccessful()) {
                        future.complete(response.body());
                    } else {
                        completeExceptionally(new HttpException(response));
                    }
                }

                @Override
                public void onFailure(Call<R> call, Throwable t) {
                    completeExceptionally(t);
                }

                private void completeExceptionally(Throwable t) {
                    if (future.hasRetry()) {
                        enqueue(call.clone(), future);
                    } else {
                        future.completeExceptionally(t);
                    }
                }
            });
        }
    }

    private static final class ResponseCallAdapter<R>
            implements CallAdapter<R, RetrofitFuture<Response<R>>> {
        private final Type responseType;

        ResponseCallAdapter(Type responseType) {
            this.responseType = responseType;
        }

        @Override
        public Type responseType() {
            return responseType;
        }

        @Override
        public RetrofitFuture<Response<R>> adapt(final Call<R> call) {
            final RetrofitFuture<Response<R>> future = new RetrofitFuture<Response<R>>() {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    if (mayInterruptIfRunning) {
                        call.cancel();
                    }
                    return super.cancel(mayInterruptIfRunning);
                }
            };

            enqueue(call, future);

            return future;
        }

        private void enqueue(Call<R> call, RetrofitFuture<Response<R>> future) {
            call.enqueue(new Callback<R>() {
                @Override
                public void onResponse(Call<R> call, Response<R> response) {
                    future.complete(response);
                }

                @Override
                public void onFailure(Call<R> call, Throwable t) {
                    if (future.hasRetry()) {
                        enqueue(call.clone(), future);
                    } else {
                        future.completeExceptionally(t);
                    }
                }
            });
        }
    }

}