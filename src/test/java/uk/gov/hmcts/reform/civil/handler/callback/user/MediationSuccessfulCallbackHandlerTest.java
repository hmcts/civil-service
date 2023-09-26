package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    MediationSuccessfulCallbackHandler.class,
    JacksonAutoConfiguration.class,
})
class MediationSuccessfulCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private MediationSuccessfulCallbackHandler handler;

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
