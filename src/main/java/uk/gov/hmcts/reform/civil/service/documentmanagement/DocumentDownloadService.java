package uk.gov.hmcts.reform.civil.service.documentmanagement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.config.SystemUpdateUserConfiguration;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DownloadedDocumentResponse;
import uk.gov.hmcts.reform.civil.service.UserService;

@Service
@RequiredArgsConstructor
public class DocumentDownloadService {

    private final UserService userService;
    private final SystemUpdateUserConfiguration userConfig;
    private final DocumentManagementService documentManagementService;

    public DownloadedDocumentResponse downloadDocument(String documentId) {
        String authorisation = userService.getAccessToken(userConfig.getUserName(), userConfig.getPassword());
        String documentPath = String.format("documents/%s", documentId);

        return documentManagementService.downloadDocumentWithMetaData(authorisation, documentPath);
    }
}
