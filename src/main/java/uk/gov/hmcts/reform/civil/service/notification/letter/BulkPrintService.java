package uk.gov.hmcts.reform.civil.service.notification.letter;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.sendletter.api.Document;
import uk.gov.hmcts.reform.sendletter.api.Letter;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "send-letter", name = "url")
public class BulkPrintService {

    public static final String XEROX_TYPE_PARAMETER = "CMC001";
    private final SendLetterApi sendLetterApi;
    private final AuthTokenGenerator authTokenGenerator;

    public SendLetterResponse printLetter(Map<String, Object> letterParams, String template){
        Letter letter = generateLetter(letterParams, template);
        return sendLetterApi.sendLetter(authTokenGenerator.generate(), letter);
    }

    private Letter generateLetter(Map<String, Object> letterParams, String template){
        Document document = new Document(template, letterParams);
        return new Letter(List.of(document), XEROX_TYPE_PARAMETER);
    }
}
