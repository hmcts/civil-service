package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.Time;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    EvidenceUploadRespondentHandler.class,
    JacksonAutoConfiguration.class
})
class EvidenceUploadRespondentHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private EvidenceUploadRespondentHandler handler;

    @MockBean
    private Time time;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    private final UploadEvidenceExpert uploadEvidenceDate = new UploadEvidenceExpert();
    private final UploadEvidenceWitness uploadEvidenceDate2 = new UploadEvidenceWitness();

    private static final String PAGE_ID = "validateValuesRespondent";

}

