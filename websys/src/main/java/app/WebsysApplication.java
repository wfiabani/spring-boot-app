package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableScheduling
public class WebsysApplication {

	private static ConfigurableApplicationContext ctx;

	public static void main(String[] args) {

		ctx = SpringApplication.run(WebsysApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/rest/authenticate").allowedOrigins("http://localhost:4200");
			}
		};
	}

	public static void destroy(){
		if( ctx != null ) {
			ctx.close();
		}
	}
}
