package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.testsupport.mockito.MockitoBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.DiscontinuanceTypeList;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

@SpringBootTest(classes = {
    DismissCaseCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class DismissCaseCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private DismissCaseCallbackHandler handler;

    @MockitoBean
    private FeatureToggleService toggleService;

    @Test
    void aboutToSubmitShouldReadyCamundaProcess() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimSubmitted2v1RespondentUnrepresented().build();
        caseData.setTypeOfDiscontinuance(DiscontinuanceTypeList.FULL_DISCONTINUANCE);
        DynamicListElement dynamicListElement = new DynamicListElement();
        dynamicListElement.setLabel("Both");
        DynamicList dynamicList = new DynamicList();
        dynamicList.setValue(dynamicListElement);
        caseData.setClaimantWhoIsDiscontinuing(dynamicList);
        caseData.setCourtPermissionNeeded(SettleDiscontinueYesOrNoList.NO);
        caseData.setHearingDate(LocalDate.now());
        caseData.setHearingDueDate(LocalDate.now());
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(((Map<?, ?>)response.getData().get("businessProcess")).get("camundaEvent"))
            .isEqualTo(CaseEvent.DISMISS_CASE.name());
        assertThat(response.getData()).extracting("hearingDate").isNull();
        assertThat(response.getData()).extracting("hearingDueDate").isNull();
    }

    @Test
    void submittedShouldHaveConfirmationScreen() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setIsPermissionGranted(SettleDiscontinueYesOrNoList.YES);
        caseData.setCourtPermissionNeeded(SettleDiscontinueYesOrNoList.YES);
        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

        SubmittedCallbackResponse response =
            (SubmittedCallbackResponse) handler.handle(params);

        Assertions.assertNotNull(response.getConfirmationHeader());
        Assertions.assertNotNull(response.getConfirmationBody());
    }
}
