package uk.gov.hmcts.reform.unspec.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class SystemUpdateUserConfiguration {

    private final String userName;
    private final String password;

    public SystemUpdateUserConfiguration(@Value("${unspecified.system-update.username}") String userName,
        @Value("${unspecified.system-update.password}") String password) {
        this.userName = userName;
        this.password = password;
    }
}
