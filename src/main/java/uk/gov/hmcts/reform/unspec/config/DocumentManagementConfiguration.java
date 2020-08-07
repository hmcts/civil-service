package uk.gov.hmcts.reform.unspec.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
public class DocumentManagementConfiguration {

    private final List<String> userRoles;

    public DocumentManagementConfiguration(@Value("${document_management.userRoles}") List<String> userRoles) {
        this.userRoles = userRoles;
    }
}
