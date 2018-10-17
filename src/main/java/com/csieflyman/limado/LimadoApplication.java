package com.csieflyman.limado;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author csieflyman
 */
@SpringBootApplication
public class LimadoApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(LimadoApplication.class, args);
    }
}
