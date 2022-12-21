package uk.gov.hmcts.reform.civil.handler.callback.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.referencedata.response.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.CaseMigrationUtility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@SpringBootTest(classes = {
    MigrateCaseDataCallbackHandler.class,
    CaseMigrationUtility.class,
    JacksonAutoConfiguration.class})
public class MigrateCaseDataCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MigrateCaseDataCallbackHandler handler;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private  LocationRefDataService locationRefDataService;

    private static final String USER_AUTH_TOKEN = "Bearer user-xyz";

    @Test
    void shouldReturnNoError_whenAboutToSubmitIsInvoked_UNSpec() {
        //CourtLocation location = CourtLocation.builder().applicantPreferredCourt("123").build();
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .courtLocation()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent1DQWithLocation()
            .applicant1DQWithLocation()
            .build();
        caseData = caseData.toBuilder().caseAccessCategory(CaseCategory.SPEC_CLAIM).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        when(locationRefDataService.getCourtLocation("BEARER_TOKEN", "444")).thenReturn(
            LocationRefData.builder().epimmsId("1234").region("1").build());
        when(locationRefDataService.getCourtLocation("BEARER_TOKEN", "court4")).thenReturn(
            LocationRefData.builder().epimmsId("1234").region("1").build());
        when(locationRefDataService.getCourtLocation("BEARER_TOKEN", "127")).thenReturn(
            LocationRefData.builder().epimmsId("1234").region("1").build());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnNoError_whenAboutToSubmitIsInvoked_SpecNoCourtCode() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .courtLocation()
            .respondent1DQWithLocation()
            .respondent2DQWithLocation()
            .applicant1DQWithLocation()
            .applicant2DQWithLocation()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant2DQWithLocation()
            .build();
        caseData = caseData.toBuilder().caseAccessCategory(CaseCategory.SPEC_CLAIM).build();
        caseData = caseData.toBuilder().respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                                          .respondent1DQRequestedCourt(
                                                              caseData.getRespondent1DQ()
                                                                  .getRespondent1DQRequestedCourt()
                                                                  .toBuilder()
                                                                  .responseCourtCode(null).build())
                                                          .respondent1DQStatementOfTruth(new StatementOfTruth("name", "role"))
                                                          .build()).build();
        caseData = caseData.toBuilder()
            .respondent2DQ(caseData.getRespondent2DQ().toBuilder()
                               .respondent2DQRequestedCourt(
                                   caseData.getRespondent2DQ()
                                       .getRespondent2DQRequestedCourt()
                                       .toBuilder()
                                       .responseCourtCode(null).build())
                               .respondent2DQExperts(Experts.builder()
                                                                   .expertRequired(YES)
                                                                   .build())
                               .respondent2DQStatementOfTruth(new StatementOfTruth("name", "role"))
                               .build()).build();

        caseData = caseData.toBuilder().applicant1DQ(caseData.getApplicant1DQ().toBuilder()
                                                         .applicant1DQRequestedCourt(
                                                             caseData.getApplicant1DQ()
                                                                 .getApplicant1DQRequestedCourt()
                                                                 .toBuilder()
                                                                 .responseCourtCode(null).build())
                                                         .applicant1DQStatementOfTruth(new StatementOfTruth("name", "role"))
                                                         .applicant1DQExperts(Experts.builder()
                                                                                    .expertRequired(YES)
                                                                                    .build())
                                                         .build()).build();

        caseData = caseData.toBuilder().applicant2DQ(caseData.getApplicant2DQ().toBuilder()
                                                         .applicant2DQRequestedCourt(
                                                             caseData.getApplicant2DQ()
                                                                 .getApplicant2DQRequestedCourt()
                                                                 .toBuilder()
                                                                 .responseCourtCode(null).build())
                                                         .build()).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // params.getParams().put(BEARER_TOKEN,USER_AUTH_TOKEN);
        when(locationRefDataService.getCourtLocation("BEARER_TOKEN", "444")).thenReturn(
            LocationRefData.builder().epimmsId("1234").region("1").build());
        when(locationRefDataService.getCourtLocation("BEARER_TOKEN", "court4")).thenReturn(
            LocationRefData.builder().epimmsId("1234").region("1").build());
        when(locationRefDataService.getCourtLocation("BEARER_TOKEN", "127")).thenReturn(
            LocationRefData.builder().epimmsId("1234").region("1").build());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnNoError_whenAboutToSubmitIsInvoked_Spec() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .courtLocation()
            .respondent1DQWithLocation()
            .respondent2DQWithLocation()
            .applicant1DQWithLocation()
            .applicant2DQWithLocation()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant2DQWithLocation()
            .build();
        caseData = caseData.toBuilder().caseAccessCategory(CaseCategory.SPEC_CLAIM).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // params.getParams().put(BEARER_TOKEN,USER_AUTH_TOKEN);
        when(locationRefDataService.getCourtLocation("BEARER_TOKEN", "444")).thenReturn(
            LocationRefData.builder().epimmsId("1234").region("1").build());
        when(locationRefDataService.getCourtLocation("BEARER_TOKEN", "court4")).thenReturn(
            LocationRefData.builder().epimmsId("1234").region("1").build());
        when(locationRefDataService.getCourtLocation("BEARER_TOKEN", "127")).thenReturn(
            LocationRefData.builder().epimmsId("1234").region("1").build());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnNoError_whenAboutToSubmitIsInvoked_UnSpecNoCourtCode() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .courtLocation()
            .respondent1DQWithLocation()
            .respondent2DQWithLocation()
            .applicant1DQWithLocation()
            .applicant2DQWithLocation()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .applicant2DQWithLocation()
            .build();
        caseData = caseData.toBuilder().caseAccessCategory(CaseCategory.UNSPEC_CLAIM).build();
        caseData = caseData.toBuilder().respondent1DQ(caseData.getRespondent1DQ().toBuilder()
                                                          .respondent1DQRequestedCourt(
                                                              caseData.getRespondent1DQ()
                                                                  .getRespondent1DQRequestedCourt()
                                                                  .toBuilder()
                                                                  .responseCourtCode(null).build())
                                                          .respondent1DQStatementOfTruth(new StatementOfTruth("name", "role"))
                                                          .respondent1DQExperts(Experts.builder()
                                                                                     .expertRequired(YES)
                                                                                     .build())
                                                          .build()).build();
        caseData = caseData.toBuilder()
            .respondent2DQ(caseData.getRespondent2DQ().toBuilder()
                               .respondent2DQRequestedCourt(
                                   caseData.getRespondent2DQ()
                                       .getRespondent2DQRequestedCourt()
                                       .toBuilder()
                                       .responseCourtCode(null).build())
                               .respondent2DQStatementOfTruth(new StatementOfTruth("name", "role"))
                               .build()).build();

        caseData = caseData.toBuilder().applicant1DQ(caseData.getApplicant1DQ().toBuilder()
                                                         .applicant1DQRequestedCourt(
                                                             caseData.getApplicant1DQ()
                                                                 .getApplicant1DQRequestedCourt()
                                                                 .toBuilder()
                                                                 .responseCourtCode(null).build())
                                                         .applicant1DQStatementOfTruth(new StatementOfTruth("name", "role"))
                                                         .build()).build();

        caseData = caseData.toBuilder().applicant2DQ(caseData.getApplicant2DQ().toBuilder()
                                                         .applicant2DQRequestedCourt(
                                                             caseData.getApplicant2DQ()
                                                                 .getApplicant2DQRequestedCourt()
                                                                 .toBuilder()
                                                                 .responseCourtCode(null).build())
                                                         .build()).build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // params.getParams().put(BEARER_TOKEN,USER_AUTH_TOKEN);
        when(locationRefDataService.getCourtLocation("BEARER_TOKEN", "444")).thenReturn(
            LocationRefData.builder().epimmsId("1234").region("1").build());
        when(locationRefDataService.getCourtLocation("BEARER_TOKEN", "court4")).thenReturn(
            LocationRefData.builder().epimmsId("1234").region("1").build());
        when(locationRefDataService.getCourtLocation("BEARER_TOKEN", "127")).thenReturn(
            LocationRefData.builder().epimmsId("1234").region("1").build());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnNoError_whenAboutToSubmitIsInvoked_UnSpec() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .courtLocation()
            .respondent1DQWithLocation()
            .respondent2DQWithLocation()
            .applicant1DQWithLocation()
            .applicant2DQWithLocation()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .applicant2DQWithLocation()
            .build();
        caseData = caseData.toBuilder().caseAccessCategory(CaseCategory.UNSPEC_CLAIM).build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        // params.getParams().put(BEARER_TOKEN,USER_AUTH_TOKEN);
        when(locationRefDataService.getCourtLocation("BEARER_TOKEN", "444")).thenReturn(
            LocationRefData.builder().epimmsId("1234").region("1").build());
        when(locationRefDataService.getCourtLocation("BEARER_TOKEN", "court4")).thenReturn(
            LocationRefData.builder().epimmsId("1234").region("1").build());
        when(locationRefDataService.getCourtLocation("BEARER_TOKEN", "127")).thenReturn(
            LocationRefData.builder().epimmsId("1234").region("1").build());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnNoError_whenSubmittedIsInvoked_UNSpec() {
        //CourtLocation location = CourtLocation.builder().applicantPreferredCourt("123").build();
        CaseData caseData = CaseDataBuilder.builder()
            .atStatePendingClaimIssued()
            .build();

        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

        SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler
            .handle(params);
    }
}
