package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class CrossAccessUserConfiguration {

    private final String userName;
    private final String password;

    public CrossAccessUserConfiguration(@Value("${civil.cross-access.username}") String userName,
                                        @Value("${civil.cross-access.password}") String password) {
        this.userName = userName;
        this.password = password;
    }
}
