package com.projeto.ticket_wave;

import org.springframework.boot.SpringApplication;

public class TestTicketWaveApplication {

	public static void main(String[] args) {
		SpringApplication.from(TicketWaveApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
