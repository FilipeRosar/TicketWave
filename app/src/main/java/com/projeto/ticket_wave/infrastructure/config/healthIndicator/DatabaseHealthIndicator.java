package com.projeto.ticket_wave.infrastructure.config.healthIndicator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health getHealth(boolean includeDetails) {
        return HealthIndicator.super.getHealth(includeDetails);
    }

    @Override
    public Health health() {
       try(Connection connection = dataSource.getConnection()){
           if (connection.isValid(1)){
               return Health.up()
                       .withDetail("Database", connection.getMetaData().getDatabaseProductName())
                       .build();
           }
       } catch (Exception e) {
            return Health.down(e)
                    .withDetail("Database", e.getMessage())
                    .build();
       }
       return Health.down().build();
    }
}
