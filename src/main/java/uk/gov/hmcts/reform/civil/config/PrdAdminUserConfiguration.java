package uk.gov.hmcts.reform.civil.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class PrdAdminUserConfiguration {

    private final String username;
    private final String password;

    public PrdAdminUserConfiguration(@Value("${civil.prd-admin.username}") String username,
                                     @Value("${civil.prd-admin.password}") String password) {
        this.username = username;
        this.password = password;
    }
}
