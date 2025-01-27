package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.integrationtests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.config.ExitSurveyConfiguration;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.handler.callback.user.RespondToClaimSpecCallbackHandler;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.RespondToClaimSpecCallbackHandlerTestConfig;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.dq.ExpertDetails;
import uk.gov.hmcts.reform.civil.sampledata.AddressBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.service.citizenui.responsedeadline.DeadlineExtensionCalculatorService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.simplegrammar.SimpleStateFlowBuilder;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.civil.utils.CaseFlagsInitialiser;
import uk.gov.hmcts.reform.civil.utils.CourtLocationUtils;
import uk.gov.hmcts.reform.civil.utils.DQResponseDocumentUtils;
import uk.gov.hmcts.reform.civil.utils.FrcDocumentsUtils;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PaymentDateValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;
import uk.gov.hmcts.reform.civil.validation.UnavailableDateValidator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.CaseRole.RESPONDENTSOLICITORTWO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        RespondToClaimSpecCallbackHandler.class,
        ExitSurveyConfiguration.class,
        ExitSurveyContentService.class,
        JacksonAutoConfiguration.class,
        ValidationAutoConfiguration.class,
        DateOfBirthValidator.class,
        UnavailableDateValidator.class,
        CaseDetailsConverter.class,
        LocationReferenceDataService.class,
        CourtLocationUtils.class,
        SimpleStateFlowEngine.class,
        SimpleStateFlowBuilder.class,
        AssignCategoryId.class,
        FrcDocumentsUtils.class,
        RespondToClaimSpecCallbackHandlerTestConfig.class
})
public class RespondToClaimSpecCallbackHandlerAboutToSubmitTest extends BaseCallbackHandlerTest {

    @Autowired
    private RespondToClaimSpecCallbackHandler handler;

    @MockBean
    private PaymentDateValidator validator;

    @MockBean
    private PostcodeValidator postcodeValidator;

    @MockBean
    private FeatureToggleService toggleService;

    @MockBean
    private LocationReferenceDataService locationRefDataService;

    @MockBean
    private SimpleStateFlowBuilder simpleStateFlowBuilder;

    @MockBean
    private CoreCaseUserService coreCaseUserService;

    @MockBean
    private DQResponseDocumentUtils dqResponseDocumentUtils;

    @MockBean
    private CaseFlagsInitialiser caseFlagsInitialiser;

    @MockBean
    private DeadlinesCalculator deadlinesCalculator;

    @MockBean
    private DeadlineExtensionCalculatorService deadlineExtensionCalculatorService;

    @MockBean
    private Time time;

    @Mock
    private StateFlow mockedStateFlow;

    @MockBean
    private SimpleStateFlowEngine stateFlowEngine;

    @BeforeEach
    void setUp() {
        when(userService.getUserInfo(anyString())).thenReturn(UserInfo.builder().uid("uid").build());
        when(mockedStateFlow.isFlagSet(any())).thenReturn(true);
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(mockedStateFlow);
        when(deadlinesCalculator.calculateApplicantResponseDeadlineSpec(any()))
                .thenReturn(LocalDateTime.now());
    }

    @Test
    void updateRespondent1AddressWhenUpdated() {
        Address changedAddress = AddressBuilder.maximal().build();

        CaseData caseData = CaseDataBuilder.builder()
                .respondent1(PartyBuilder.builder().individual().build())
                .atStateApplicantRespondToDefenceAndProceed()
                .atSpecAoSApplicantCorrespondenceAddressRequired(NO)
                .atSpecAoSApplicantCorrespondenceAddressDetails(AddressBuilder.maximal().build())
                .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

        assertThat(response.getData())
                .extracting("respondent1").extracting("primaryAddress")
                .extracting("AddressLine1").isEqualTo(changedAddress.getAddressLine1());
        assertThat(response.getData())
                .extracting("respondent1").extracting("primaryAddress")
                .extracting("AddressLine2").isEqualTo(changedAddress.getAddressLine2());
        assertThat(response.getData())
                .extracting("respondent1").extracting("primaryAddress")
                .extracting("AddressLine3").isEqualTo(changedAddress.getAddressLine3());
    }

    @Test
    void updateRespondent1Experts() {
        when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));

        ExpertDetails experts = ExpertDetails.builder()
                .expertName("Mr Expert Defendant")
                .firstName("Expert")
                .lastName("Defendant")
                .phoneNumber("07123456789")
                .emailAddress("test@email.com")
                .fieldofExpertise("Roofing")
                .estimatedCost(new BigDecimal(434))
                .build();

        CaseData caseData = CaseDataBuilder.builder()
                .respondent1(PartyBuilder.builder().individual().build())
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1DQSmallClaimExperts(experts, YES)
                .atSpecAoSApplicantCorrespondenceAddressRequired(NO)
                .atSpecAoSApplicantCorrespondenceAddressDetails(AddressBuilder.maximal().build())
                .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

        assertThat(response.getData())
                .extracting("responseClaimExpertSpecRequired").isEqualTo("Yes");
        assertThat(response.getData()).extracting("respondent1DQExperts").extracting("expertRequired").isEqualTo("Yes");
    }

    @Test
    void updateRespondent1Experts_WhenNoExperts() {
        CaseData caseData = CaseDataBuilder.builder()
                .respondent1(PartyBuilder.builder().individual().build())
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent1DQSmallClaimExperts(null, NO)
                .atSpecAoSApplicantCorrespondenceAddressRequired(NO)
                .atSpecAoSApplicantCorrespondenceAddressDetails(AddressBuilder.maximal().build())
                .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

        assertThat(response.getData())
                .extracting("responseClaimExpertSpecRequired").isEqualTo("No");
        assertThat(response.getData()).extracting("respondent1DQExperts").extracting("expertRequired").isEqualTo("No");
    }

    @Test
    void updateRespondent2Experts() {
        when(time.now()).thenReturn(LocalDateTime.of(2022, 2, 18, 12, 10, 55));
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

        ExpertDetails experts = ExpertDetails.builder()
                .expertName("Mr Expert Defendant")
                .firstName("Expert")
                .lastName("Defendant")
                .phoneNumber("07123456789")
                .emailAddress("test@email.com")
                .fieldofExpertise("Roofing")
                .estimatedCost(new BigDecimal(434))
                .build();

        CaseData caseData = CaseDataBuilder.builder()
                .respondent2(PartyBuilder.builder().individual().build())
                .multiPartyClaimTwoDefendantSolicitors()
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent2DQSmallClaimExperts(experts, YES)
                .atSpecAoSApplicantCorrespondenceAddressRequired(NO)
                .atSpecAoSApplicantCorrespondenceAddressDetails(AddressBuilder.maximal().build())
                .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

        assertThat(response.getData())
                .extracting("responseClaimExpertSpecRequired2").isEqualTo("Yes");
    }

    @Test
    void updateRespondent2Experts_WhenNoExperts() {
        CaseData caseData = CaseDataBuilder.builder()
                .respondent1(PartyBuilder.builder().individual().build())
                .atStateApplicantRespondToDefenceAndProceed()
                .respondent2DQSmallClaimExperts(null, NO)
                .atSpecAoSApplicantCorrespondenceAddressRequired(NO)
                .atSpecAoSApplicantCorrespondenceAddressDetails(AddressBuilder.maximal().build())
                .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

        assertThat(response.getData())
                .extracting("responseClaimExpertSpecRequired2").isEqualTo("No");
    }

    @Test
    void updateRespondent2AddressWhenUpdated() {
        when(coreCaseUserService.userHasCaseRole(any(), any(), eq(RESPONDENTSOLICITORTWO))).thenReturn(true);

        CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .respondent2DQ()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .atSpecAoSApplicantCorrespondenceAddressRequired(YES)
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .atSpecAoSRespondent2HomeAddressRequired(NO)
                .atSpecAoSRespondent2HomeAddressDetails(AddressBuilder.maximal().build())
                .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

        assertThat(response.getData())
                .extracting("respondent2").extracting("primaryAddress")
                .extracting("AddressLine1").isEqualTo("address line 1");
        assertThat(response.getData())
                .extracting("respondent2").extracting("primaryAddress")
                .extracting("AddressLine2").isEqualTo("address line 2");
        assertThat(response.getData())
                .extracting("respondent2").extracting("primaryAddress")
                .extracting("AddressLine3").isEqualTo("address line 3");
    }

    @Test
    void updateRespondent2AddressWhenSpecAoSRespondent2HomeAddressRequiredIsNO() {
        Party partyWithPrimaryAddress = PartyBuilder.builder().individual().build();
        partyWithPrimaryAddress.setPrimaryAddress(AddressBuilder.maximal()
                .addressLine1("address line 1")
                .addressLine2("address line 2")
                .addressLine3("address line 3")
                .build());

        CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .respondent2DQ()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .atSpecAoSRespondent2HomeAddressRequired(NO)
                .atSpecAoSRespondent2HomeAddressDetails(AddressBuilder.maximal()
                        .addressLine1("new address line 1")
                        .build())
                .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

        assertThat(response.getData())
                .extracting("respondent2")
                .extracting("primaryAddress")
                .extracting("AddressLine1")
                .isEqualTo("new address line 1");
    }

    @Test
    void updateRespondent2AddressWhenSpecAoSRespondent2HomeAddressRequiredIsNotNO() {
        Party partyWithPrimaryAddress = PartyBuilder.builder().individual().build();
        partyWithPrimaryAddress.setPrimaryAddress(AddressBuilder.maximal()
                .addressLine1("address line 1")
                .addressLine2("address line 2")
                .addressLine3("address line 3")
                .build());

        CaseData caseData = CaseDataBuilder.builder().atStateApplicantRespondToDefenceAndProceed()
                .respondent2DQ()
                .respondent1Copy(PartyBuilder.builder().individual().build())
                .addRespondent2(YES)
                .respondent2(PartyBuilder.builder().individual().build())
                .respondent2Copy(PartyBuilder.builder().individual().build())
                .atSpecAoSRespondent2HomeAddressRequired(YES)
                .build();

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
                callbackParamsOf(caseData, ABOUT_TO_SUBMIT));

        assertThat(response.getData())
                .extracting("respondent2")
                .extracting("primaryAddress")
                .extracting("AddressLine1")
                .isEqualTo("address line 1");
        assertThat(response.getData())
                .extracting("respondent2")
                .extracting("primaryAddress")
                .extracting("AddressLine2")
                .isEqualTo("address line 2");
        assertThat(response.getData())
                .extracting("respondent2")
                .extracting("primaryAddress")
                .extracting("AddressLine3")
                .isEqualTo("address line 3");
    }
}
