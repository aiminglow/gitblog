package com.aiminglow.gitblog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@MapperScan("com.aiminglow.gitblog.dao")
@EnableTransactionManagement
@SpringBootApplication
public class GitBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitBlogApplication.class, args);
    }

}
