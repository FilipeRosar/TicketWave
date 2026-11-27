package com.projeto.ticket_wave;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.security.SecureRandom;
import java.util.Base64;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	DynamicPropertyRegistrar testCredentials() {
		byte[] signingKey = new byte[64];
		new SecureRandom().nextBytes(signingKey);
		String jwtSecret = Base64.getEncoder().encodeToString(signingKey);
		return registry -> {
			registry.add("security.jwt.secret", () -> jwtSecret);
			registry.add("storage.access-key", () -> "test");
			registry.add("storage.secret-key", () -> "test");
		};
	}

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));
	}

	@Bean
	@ServiceConnection(name = "redis")
	GenericContainer<?> redisContainer() {
		return new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);
	}

}
