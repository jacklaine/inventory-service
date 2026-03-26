package org.db1.inventory.support;

import java.util.Map;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class IntegrationTestContainersResource implements QuarkusTestResourceLifecycleManager {

    private PostgreSQLContainer<?> postgres;
    private ConfluentKafkaContainer kafka;

    @Override
    @SuppressWarnings("resource")
    public Map<String, String> start() {
        postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                .withDatabaseName("inventory")
                .withUsername("inventory")
                .withPassword("inventory");

        kafka = new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

        postgres.start();
        kafka.start();

        return Map.of(
                "quarkus.datasource.jdbc.url", postgres.getJdbcUrl(),
                "quarkus.datasource.username", postgres.getUsername(),
                "quarkus.datasource.password", postgres.getPassword(),
                "kafka.bootstrap.servers", kafka.getBootstrapServers(),
                "quarkus.datasource.devservices.enabled", "false",
                "quarkus.kafka.devservices.enabled", "false");
    }

    @Override
    public void stop() {
        if (kafka != null) {
            kafka.stop();
        }
        if (postgres != null) {
            postgres.stop();
        }
    }
}
