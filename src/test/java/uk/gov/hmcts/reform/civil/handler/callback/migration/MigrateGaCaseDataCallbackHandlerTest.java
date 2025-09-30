package uk.gov.hmcts.reform.civil.handler.callback.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.migrateCase;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.RELIEF_FROM_SANCTIONS;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    MigrateGaCaseDataCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})

public class MigrateGaCaseDataCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MigrateGaCaseDataCallbackHandler handler;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    private static final String STRING_CONSTANT = "STRING_CONSTANT";
    private static final Long CHILD_CCD_REF = 1646003133062762L;
    private static final Long PARENT_CCD_REF = 1645779506193000L;
    private CallbackParams params;

    @Test
    public void shouldReturnCorrectEvent() {
        CaseData caseData = CaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                getGeneralApplication())
            .toBuilder().ccdCaseReference(CHILD_CCD_REF).build();
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThat(handler.handledEvents()).contains(migrateCase);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        public void shouldNotThrowError_WhenMigrateCaseDataSuccessfully() {
            CaseData caseData = CaseDataBuilder.builder().buildCaseDateBaseOnGeneralApplication(
                    getGeneralApplication())
                .toBuilder().ccdCaseReference(CHILD_CCD_REF).build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
        }
    }

    private GeneralApplication getGeneralApplication() {
        return GeneralApplication.builder()
            .generalAppType(GAApplicationType.builder().types(List.of(RELIEF_FROM_SANCTIONS)).build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
            .generalAppPBADetails(GAPbaDetails.builder().build())
            .generalAppDetailsOfOrder(STRING_CONSTANT)
            .generalAppReasonsOfOrder(STRING_CONSTANT)
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
            .generalAppStatementOfTruth(GAStatementOfTruth.builder().build())
            .generalAppHearingDetails(GAHearingDetails.builder().build())
            .generalAppRespondentSolicitors(wrapElements(GASolicitorDetailsGAspec.builder()
                                                             .email("abc@gmail.com").build()))
            .isMultiParty(NO)
            .parentClaimantIsApplicant(YES)
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(PARENT_CCD_REF.toString()).build())
            .build();
    }

}
