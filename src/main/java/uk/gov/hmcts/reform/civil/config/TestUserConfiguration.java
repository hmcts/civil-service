package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class TestUserConfiguration {

    private final String username;
    private final String password;

    public TestUserConfiguration(@Value("${civil.test-user.username}") String username,
                                 @Value("${civil.test-user.password}") String password) {
        this.username = username;
        this.password = password;
    }
}
