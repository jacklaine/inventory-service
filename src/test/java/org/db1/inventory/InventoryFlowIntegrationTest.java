package org.db1.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.db1.inventory.support.IntegrationTestContainersResource;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(IntegrationTestContainersResource.class)
class InventoryFlowIntegrationTest {

    @Inject
    DataSource dataSource;

    @ConfigProperty(name = "kafka.bootstrap.servers", defaultValue = "localhost:9092")
    String kafkaBootstrapServers;

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement delete = connection.prepareStatement("DELETE FROM inventory_item")) {
            delete.executeUpdate();
        }
    }

    @Test
    void shouldReserveStockAndPublishOrderConfirmed() throws Exception {
        insertInventoryItem("SKU-100", 10, 2);

        String orderId = "order-" + UUID.randomUUID();
        publishOrderCreated(orderId, "SKU-100", 3);

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
            .untilAsserted(() -> assertEquals(7, getQuantity("SKU-100")));

        JsonObject confirmedEvent = awaitEventByType("orders.v1.events", "OrderConfirmed", Duration.ofSeconds(15));
        assertNotNull(confirmedEvent);
        assertEquals(orderId, confirmedEvent.getJsonObject("payload").getString("orderId"));
    }

    @Test
    void shouldPublishLowStockNotificationWhenThresholdIsReached() throws Exception {
        insertInventoryItem("SKU-LOW", 5, 3);

        publishOrderCreated("order-" + UUID.randomUUID(), "SKU-LOW", 2);

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
            .untilAsserted(() -> assertEquals(3, getQuantity("SKU-LOW")));

        JsonObject lowStockEvent = awaitFirstEvent("inventory.low-stock", Duration.ofSeconds(15));
        assertNotNull(lowStockEvent);
        assertEquals("SKU-LOW", lowStockEvent.getString("sku"));
        assertEquals(3, lowStockEvent.getInteger("currentQuantity"));
        assertEquals(3, lowStockEvent.getInteger("threshold"));
    }

    private void insertInventoryItem(String sku, int quantity, int lowStockThreshold) throws SQLException {
        String sql = "INSERT INTO inventory_item (id, sku, quantity, low_stock_threshold) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, UUID.randomUUID());
            statement.setString(2, sku);
            statement.setInt(3, quantity);
            statement.setInt(4, lowStockThreshold);
            statement.executeUpdate();
        }
    }

    private int getQuantity(String sku) throws SQLException {
        String sql = "SELECT quantity FROM inventory_item WHERE sku = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sku);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        throw new IllegalStateException("Item not found for sku " + sku);
    }

    private void publishOrderCreated(String orderId, String sku, int quantity) throws Exception {
        JsonObject item = new JsonObject()
                .put("sku", sku)
                .put("quantity", quantity)
                .put("unitPrice", 10.0);

        JsonObject payload = new JsonObject()
                .put("orderId", orderId)
                .put("customerId", "customer-1")
                .put("items", new JsonArray().add(item))
                .put("createdAt", Instant.now().toString());

        JsonObject envelope = new JsonObject()
                .put("type", "OrderCreated")
                .put("payload", payload);

        Properties producerProperties = new Properties();
        producerProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        producerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProperties)) {
            producer.send(new ProducerRecord<>("orders.v1.events", envelope.encode())).get();
        }
    }

    private JsonObject awaitEventByType(String topic, String expectedType, Duration timeout) {
        return awaitFirstEvent(topic, timeout, expectedType);
    }

    private JsonObject awaitFirstEvent(String topic, Duration timeout) {
        return awaitFirstEvent(topic, timeout, null);
    }

    private JsonObject awaitFirstEvent(String topic, Duration timeout, String expectedType) {
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "it-" + UUID.randomUUID());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        long deadline = System.currentTimeMillis() + timeout.toMillis();

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProperties)) {
            consumer.subscribe(List.of(topic));
            while (System.currentTimeMillis() < deadline) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                for (ConsumerRecord<String, String> consumerRecord : records) {
                    JsonObject event = new JsonObject(consumerRecord.value());
                    if (expectedType == null || expectedType.equals(event.getString("type"))) {
                        return event;
                    }
                }
            }
        }

        return null;
    }
}
