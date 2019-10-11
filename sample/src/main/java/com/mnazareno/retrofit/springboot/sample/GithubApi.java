package com.mnazareno.retrofit.springboot.sample;

import com.mnazareno.retrofit.springboot.RetrofitClient;
import com.mnazareno.retrofit.springboot.sample.config.RetrofitFuture;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.Path;

@RetrofitClient(name = "github", baseUrl = "https://api.github.com")
public interface GithubApi {

    @GET("/repos/{owner}/{repo}/contributors")
    RetrofitFuture<List<Contributor>> contributors(@Path("owner") String owner, @Path("repo") String repo);
}
