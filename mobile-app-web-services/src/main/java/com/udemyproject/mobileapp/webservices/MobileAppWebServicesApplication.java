package com.udemyproject.mobileapp.webservices;

import com.udemyproject.mobileapp.webservices.security.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class MobileAppWebServicesApplication {

	public static void main(String[] args) {
		SpringApplication.run(MobileAppWebServicesApplication.class, args);
	}

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder(){
		return new BCryptPasswordEncoder();
	}
	@Bean
	public SpringApplicationContext springApplicationContext(){
		return new SpringApplicationContext();
	}
	@Bean(name = "AppProperties")
	public AppProperties getAppProperties(){
		return new AppProperties();
	}
}
