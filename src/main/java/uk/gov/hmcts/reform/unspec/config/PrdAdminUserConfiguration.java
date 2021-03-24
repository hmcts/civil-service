package uk.gov.hmcts.reform.unspec.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class PrdAdminUserConfiguration {

    private final String username;
    private final String password;

    public PrdAdminUserConfiguration(@Value("${unspecified.prd-admin.username}") String username,
                                     @Value("${unspecified.prd-admin.password}") String password) {
        this.username = username;
        this.password = password;
    }
}
