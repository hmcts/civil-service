package uk.gov.hmcts.reform.unspec.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class CrossAccessUserConfiguration {

    private final String userName;
    private final String password;

    public CrossAccessUserConfiguration(@Value("${unspecified.cross-access.username}") String userName,
                                        @Value("${unspecified.cross-access.password}") String password) {
        this.userName = userName;
        this.password = password;
    }
}
