package com.cts.user_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        // Use H2 in-memory DB — no real MySQL needed for tests
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "jwt.secret=your_256bit_super_secret_key_here_make_it_long",
        "jwt.expiration=86400000",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "server.port=0"
})
class UserServiceApplicationTests {

    @Test
    void contextLoads() {
        // verifies app context loads correctly
    }
}