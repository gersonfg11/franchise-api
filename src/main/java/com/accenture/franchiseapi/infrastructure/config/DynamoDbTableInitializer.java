package com.accenture.franchiseapi.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamoDbTableInitializer {

    private final DynamoDbAsyncClient dynamoDbAsyncClient;

    @Value("${aws.dynamodb.table-name:franchises}")
    private String tableName;

    @EventListener(ApplicationReadyEvent.class)
    public void createTableIfNotExists() {
        dynamoDbAsyncClient.describeTable(r -> r.tableName(tableName))
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        createTable();
                    } else {
                        log.info("DynamoDB table '{}' already exists", tableName);
                    }
                });
    }

    private void createTable() {
        dynamoDbAsyncClient.createTable(r -> r
                .tableName(tableName)
                .keySchema(KeySchemaElement.builder()
                        .attributeName("id")
                        .keyType(KeyType.HASH)
                        .build())
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName("id")
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST))
                .whenComplete((result, ex) -> {
                    if (ex != null && !(ex.getCause() instanceof ResourceInUseException)) {
                        log.error("Error creating DynamoDB table '{}': {}", tableName, ex.getMessage());
                    } else {
                        log.info("DynamoDB table '{}' created successfully", tableName);
                    }
                });
    }
}
