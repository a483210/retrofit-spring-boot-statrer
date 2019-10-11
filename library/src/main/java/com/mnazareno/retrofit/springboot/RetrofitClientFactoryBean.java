package com.mnazareno.retrofit.springboot;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Optional;

import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class RetrofitClientFactoryBean
		implements FactoryBean, ApplicationContextAware, InitializingBean {

	private Class<?> type;

	private String name;

	private String baseUrl;

	private ApplicationContext ctx;

	public RetrofitClientFactoryBean() {
	}

	@Override
	public Object getObject() throws Exception {
		RetrofitClientContext retrofitClientContext = this.ctx
				.getBean(RetrofitClientContext.class);

		Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
		retrofitBuilder.baseUrl(this.baseUrl);

		OkHttpClient okHttpClient = Optional
				.ofNullable(
						retrofitClientContext.getInstance(this.name, OkHttpClient.class))
				.orElse(new OkHttpClient());
		retrofitBuilder.client(okHttpClient);

		Optional.ofNullable(
				retrofitClientContext.getInstances(this.name, Converter.Factory.class))
				.ifPresent(a -> a.values().forEach(retrofitBuilder::addConverterFactory));

		Optional.ofNullable(
				retrofitClientContext.getInstances(this.name, CallAdapter.Factory.class))
				.ifPresent(instances -> instances.values()
						.forEach(retrofitBuilder::addCallAdapterFactory));

		return retrofitBuilder.build().create(this.type);
	}

	@Override
	public Class<?> getObjectType() {
		return this.type;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.ctx = applicationContext;
	}

	public Class<?> getType() {
		return type;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public ApplicationContext getCtx() {
		return ctx;
	}

	public void setCtx(ApplicationContext ctx) {
		this.ctx = ctx;
	}
}
