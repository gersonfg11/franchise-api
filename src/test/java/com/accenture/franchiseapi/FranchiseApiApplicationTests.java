package com.accenture.franchiseapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "aws.access-key=local",
        "aws.secret-key=local",
        "aws.dynamodb.endpoint=http://localhost:8000"
})
class FranchiseApiApplicationTests {

    @Test
    void contextLoads() {
    }
}
