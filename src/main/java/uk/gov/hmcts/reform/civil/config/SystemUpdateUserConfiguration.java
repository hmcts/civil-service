package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class SystemUpdateUserConfiguration {

    private final String userName;
    private final String password;

    public SystemUpdateUserConfiguration(@Value("${civil.system-update.username}") String userName,
        @Value("${civil.system-update.password}") String password) {
        this.userName = userName;
        this.password = password;
    }
}
