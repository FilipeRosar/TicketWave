package com.projeto.ticket_wave;

import com.projeto.ticket_wave.infrastructure.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(JwtProperties.class)
@EnableJpaAuditing
public class TicketWaveApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketWaveApplication.class, args);
	}

}
