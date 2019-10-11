package com.mnazareno.retrofit.springboot.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import java.util.List;

@SpringBootApplication
public class RetrofitSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(RetrofitSpringBootApplication.class, args);
    }

    @Component
    class RetrofitSampleRunner implements CommandLineRunner {

        @Autowired
        private GithubApi githubApi;

        @Override
        public void run(String... args) {
            List<Contributor> list = githubApi
                    .contributors("square", "retrofit")
                    .retry(1)
                    .ofNullable();

            System.out.println(list);

//            Response<List<Contributor>> response = this.githubApi.contributors("square", "retrofit").execute();
//            System.out.println(response);
//            System.out.println(response.headers());
//            System.out.println(response.body());
        }
    }
}
