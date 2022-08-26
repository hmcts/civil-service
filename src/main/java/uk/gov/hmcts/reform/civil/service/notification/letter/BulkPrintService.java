package uk.gov.hmcts.reform.civil.service.notification.letter;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.sendletter.api.Document;
import uk.gov.hmcts.reform.sendletter.api.Letter;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "send-letter", name = "url")
public class BulkPrintService {

    public static final String XEROX_TYPE_PARAMETER = "CMC001";

    private final SendLetterApi sendLetterApi;
    private final DocumentManagementService documentManagementService;
    private final AuthTokenGenerator authTokenGenerator;

    @Retryable(
        value = RuntimeException.class,
        backoff = @Backoff(delay = 200)
    )
    public SendLetterResponse printLetter(Map<String, Object> letterParams, String letterDocumentPath){
        String authorisation = authTokenGenerator.generate();
        Letter letter = generateLetter(authorisation, letterParams, letterDocumentPath);
        return sendLetterApi.sendLetter(authorisation, letter);
    }

    private Letter generateLetter(String authorisation, Map<String, Object> letterParams, String letterDocumentPath){
        String templateLetter = Base64.getEncoder().encodeToString(documentManagementService.downloadDocument(authorisation, letterDocumentPath));
        Document document = new Document(templateLetter, letterParams);
        return new Letter(List.of(document), XEROX_TYPE_PARAMETER);
    }
}
