package uk.gov.hmcts.reform.civil.ga.handler.callback.migration;

import static org.assertj.core.api.Assertions.assertThat;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.migrateCase;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.RELIEF_FROM_SANCTIONS;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
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
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class MigrateGaCaseDataCallbackHandlerTest
        extends GeneralApplicationBaseCallbackHandlerTest {

    @Spy private ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @InjectMocks private MigrateGaCaseDataCallbackHandler handler;

    @Mock private CoreCaseDataService coreCaseDataService;

    @Spy private CaseDetailsConverter caseDetailsConverter = new CaseDetailsConverter(objectMapper);

    private static final String STRING_CONSTANT = "STRING_CONSTANT";
    private static final Long CHILD_CCD_REF = 1646003133062762L;
    private static final Long PARENT_CCD_REF = 1645779506193000L;
    private CallbackParams params;

    @Test
    public void shouldReturnCorrectEvent() {
        GeneralApplicationCaseData caseData =
                GeneralApplicationCaseDataBuilder.builder()
                        .buildCaseDateBaseOnGeneralApplication(getGeneralApplication())
                        .copy()
                        .ccdCaseReference(CHILD_CCD_REF)
                        .build();
        params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        assertThat(handler.handledEvents()).contains(migrateCase);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        public void shouldNotThrowError_WhenMigrateCaseDataSuccessfully() {
            GeneralApplicationCaseData caseData =
                    GeneralApplicationCaseDataBuilder.builder()
                            .buildCaseDateBaseOnGeneralApplication(getGeneralApplication())
                            .copy()
                            .ccdCaseReference(CHILD_CCD_REF)
                            .build();
            params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            assertThat(response.getErrors()).isNull();
        }
    }

    private GeneralApplication getGeneralApplication() {
        return new GeneralApplication()
                .setGeneralAppType(new GAApplicationType().setTypes(List.of(RELIEF_FROM_SANCTIONS)))
                .setGeneralAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(NO))
                .setGeneralAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(NO))
                .setGeneralAppPBADetails(new GAPbaDetails())
                .setGeneralAppDetailsOfOrder(STRING_CONSTANT)
                .setGeneralAppReasonsOfOrder(STRING_CONSTANT)
                .setGeneralAppUrgencyRequirement(
                        new GAUrgencyRequirement().setGeneralAppUrgency(NO))
                .setGeneralAppStatementOfTruth(new GAStatementOfTruth())
                .setGeneralAppHearingDetails(new GAHearingDetails())
                .setGeneralAppRespondentSolicitors(
                        wrapElements(new GASolicitorDetailsGAspec().setEmail("abc@gmail.com")))
                .setIsMultiParty(NO)
                .setParentClaimantIsApplicant(YES)
                .setGeneralAppParentCaseLink(
                        new GeneralAppParentCaseLink().setCaseReference(PARENT_CCD_REF.toString()));
    }
}
