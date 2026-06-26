package com.projeto.ticket_wave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TicketWaveApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketWaveApplication.class, args);
	}

}
