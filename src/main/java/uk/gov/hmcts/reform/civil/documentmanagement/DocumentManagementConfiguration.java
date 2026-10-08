package uk.gov.hmcts.reform.civil.documentmanagement;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import java.util.List;

@Data
@Configuration
public class DocumentManagementConfiguration {

    private final List<String> userRoles;
    private final DataSize maxDownloadSize;

    public DocumentManagementConfiguration(@Value("${document_management.userRoles}") List<String> userRoles,
                                           @Value("${document_management.maxDownloadSize:100MB}") DataSize maxDownloadSize) {
        this.userRoles = userRoles;
        this.maxDownloadSize = maxDownloadSize;
    }
}
