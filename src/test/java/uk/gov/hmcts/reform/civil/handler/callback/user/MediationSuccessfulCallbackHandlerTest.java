package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.MediationAgreementDocument;
import uk.gov.hmcts.reform.civil.model.MediationSuccessful;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
class MediationSuccessfulCallbackHandlerTest extends BaseCallbackHandlerTest {

    private MediationSuccessfulCallbackHandler handler;

    @BeforeEach
    void setup() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new MediationSuccessfulCallbackHandler(objectMapper);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldCallSubmitSuccessfulMediationUponAboutToSubmit() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStatePendingClaimIssued()
                .build()
                .builder()
                .mediation(Mediation
                               .builder()
                               .mediationSuccessful(MediationSuccessful.builder()
                                                        .mediationSettlementAgreedAt(LocalDate.now())
                                                        .mediationAgreement(MediationAgreementDocument.builder()
                                                                                .document(Document.builder().build())
                                                                                .documentType(DocumentType.MEDIATION_AGREEMENT)
                                                                                .name("Mediation Agreement")
                                                                                .build())
                                                        .build())
                               .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getData()).extracting("mediationSettlementAgreedAt").isNotNull();
            assertThat(response.getData()).extracting("mediationAgreement").isNotNull();
            assertThat(response.getData()).extracting("manageDocuments").isNotNull();
            assertThat(response.getState())
                .isEqualTo(CaseState.CASE_STAYED.name());
        }
    }
}
