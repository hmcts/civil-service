package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.docmosis.hearing.HearingNoticeHmcGenerator;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeVariables;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationReferenceDataService;
import uk.gov.hmcts.reform.civil.utils.HmcDataUtils;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingGetResponse;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingRequestDetails;
import uk.gov.hmcts.reform.hmc.model.hearing.HearingResponse;
import uk.gov.hmcts.reform.hmc.model.unnotifiedhearings.HearingDay;
import uk.gov.hmcts.reform.hmc.service.HearingsService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_HEARING_NOTICE_HMC;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_FORM;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_FORM_WELSH;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_NOTICE_HMC;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.HEARING_NOTICE_HMC_WELSH;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;

@ExtendWith(MockitoExtension.class)
class GenerateHearingNoticeHmcHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private GenerateHearingNoticeHmcHandler handler;
    @Mock
    private HearingsService hearingsService;
    @Mock
    private HearingNoticeCamundaService camundaService;
    @Mock
    private HearingNoticeHmcGenerator hearingNoticeHmcGenerator;
    @Mock
    private LocationReferenceDataService locationRefDataService;
    @Mock
    private HearingFeesService hearingFeesService;
    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new GenerateHearingNoticeHmcHandler(camundaService, hearingsService, hearingNoticeHmcGenerator,
                                                      mapper, locationRefDataService, hearingFeesService, featureToggleService);
        mapper.registerModule(new JavaTimeModule());

    }

    private static Long CASE_ID = 1L;
    private static String HEARING_ID = "1234";
    private static String PROCESS_INSTANCE_ID = "process-instance-id";
    private static String EPIMS = "venue-id";
    private static Long VERSION_NUMBER = 1L;
    private static final String REFERENCE_NUMBER = "000DC001";
    private static final String TRIAL_HEARING_TYPE = "AAA7-TRI";

    private static final String fileName_application = String.format(
        HEARING_NOTICE_HMC.getDocumentTitle(), REFERENCE_NUMBER);

    private static final CaseDocument CASE_DOCUMENT;
    private static final CaseDocument CASE_DOCUMENT_WELSH;

    static {
        CaseDocument caseDocument = new CaseDocument();
        caseDocument.setDocumentName(fileName_application);
        caseDocument.setDocumentType(HEARING_FORM);
        CASE_DOCUMENT = caseDocument;

        CaseDocument caseDocumentWelsh = new CaseDocument();
        caseDocumentWelsh.setDocumentName(fileName_application);
        caseDocumentWelsh.setDocumentType(HEARING_FORM_WELSH);
        CASE_DOCUMENT_WELSH = caseDocumentWelsh;
    }

    @Test
    void shouldPopulateCamundaProcessVariables_andReturnExpectedCaseData() {
        CaseData caseData = CaseDataBuilder.builder().build();
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
        caseData.setBusinessProcess(businessProcess);
        caseData.setCcdState(CASE_PROGRESSION);
        caseData.setCaseAccessCategory(SPEC_CLAIM);
        caseData.setResponseClaimTrack("SMALL_CLAIM");
        caseData.setTotalClaimAmount(new BigDecimal(1000));
        caseData.setClaimValue(null);
        caseData.setTotalInterest(BigDecimal.TEN);
        HearingNoticeVariables inputVariables = new HearingNoticeVariables();
        inputVariables.setHearingId(HEARING_ID);
        inputVariables.setCaseId(CASE_ID);

        List<LocationRefData> locations = List.of(new LocationRefData()
                                                      .setEpimmsId(EPIMS));
        when(locationRefDataService.getHearingCourtLocations(anyString()))
            .thenReturn(locations);
        when(camundaService.getProcessVariables(PROCESS_INSTANCE_ID)).thenReturn(inputVariables);
        HearingDay hearingDay = new HearingDay()
            .setHearingStartDateTime(LocalDateTime.of(2023, 01, 01, 0, 0, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 01, 01, 12, 0, 0));
        LocalDateTime hearingResponseDate = LocalDateTime.of(2023, 02, 02, 0, 0, 0);
        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                    List.of(
                        new HearingDaySchedule()
                            .setHearingVenueId(EPIMS)
                            .setHearingStartDateTime(hearingDay.getHearingStartDateTime())
                            .setHearingEndDateTime(hearingDay.getHearingEndDateTime())))
                        .setReceivedDateTime(hearingResponseDate))
            .setRequestDetails(new HearingRequestDetails()
                                .setVersionNumber(VERSION_NUMBER))
            .setHearingDetails(new HearingDetails()
                                .setHearingType(TRIAL_HEARING_TYPE));
        when(hearingsService.getHearingResponse(anyString(), anyString())).thenReturn(hearing);
        when(hearingNoticeHmcGenerator.generate(eq(caseData), eq(hearing), anyString(), anyString(), anyString(), any())).thenReturn(List.of(CASE_DOCUMENT));
        Fee expectedFee = new Fee();
        expectedFee.setCalculatedAmountInPence(new BigDecimal(54500));
        expectedFee.setCode("FEE0441");
        expectedFee.setVersion("1");
        given(hearingFeesService.getFeeForHearingSmallClaims(any())).willReturn(expectedFee);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_NOTICE_HMC.name());

        HearingNoticeVariables updatedVars = new HearingNoticeVariables();
        updatedVars.setCaseId(CASE_ID);
        updatedVars.setHearingId(HEARING_ID);
        updatedVars.setCaseState(CASE_PROGRESSION.name());
        updatedVars.setRequestVersion(VERSION_NUMBER);
        updatedVars.setHearingStartDateTime(hearingDay.getHearingStartDateTime());
        updatedVars.setHearingLocationEpims(EPIMS);
        updatedVars.setResponseDateTime(hearingResponseDate);
        updatedVars.setDays(List.of(hearingDay));
        updatedVars.setHearingType(TRIAL_HEARING_TYPE);
        var actual = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        verify(camundaService).setProcessVariables(
            PROCESS_INSTANCE_ID, updatedVars);

        CaseData updatedData = mapper.convertValue(actual.getData(), CaseData.class);
        assertThat(updatedData.getHearingDocuments()).hasSize(1);
        assertThat(unwrapElements(updatedData.getHearingDocuments()).get(0)).isEqualTo(CASE_DOCUMENT);
        assertThat(updatedData.getHearingDate()).isEqualTo(hearingDay.getHearingStartDateTime().toLocalDate());
        assertThat(updatedData.getHearingDueDate()).isEqualTo(LocalDate.of(2023, 1, 1));
        assertThat(updatedData.getHearingFee()).isEqualTo(expectedFee);
    }

    @Test
    void shouldPopulateCamundaProcessVariables_andReturnExpectedCaseData_BstHearingDate() {
        CaseData caseData = CaseDataBuilder.builder().build();
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
        caseData.setBusinessProcess(businessProcess);
        caseData.setCcdState(CASE_PROGRESSION);
        caseData.setCaseAccessCategory(UNSPEC_CLAIM);
        caseData.setAllocatedTrack(AllocatedTrack.SMALL_CLAIM);
        caseData.setTotalClaimAmount(new BigDecimal(1000));
        caseData.setClaimValue(null);
        caseData.setTotalInterest(BigDecimal.TEN);
        HearingNoticeVariables inputVariables = new HearingNoticeVariables();
        inputVariables.setHearingId(HEARING_ID);
        inputVariables.setCaseId(CASE_ID);

        List<LocationRefData> locations = List.of(new LocationRefData()
                                                      .setEpimmsId(EPIMS));
        when(locationRefDataService.getHearingCourtLocations(anyString()))
            .thenReturn(locations);
        when(camundaService.getProcessVariables(PROCESS_INSTANCE_ID)).thenReturn(inputVariables);
        HearingDay hearingDay = new HearingDay()
            .setHearingStartDateTime(LocalDateTime.of(2023, 07, 01, 9, 0, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 07, 01, 11, 0, 0));
        LocalDateTime hearingResponseDate = LocalDateTime.of(2023, 06, 02, 0, 0, 0);
        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                    List.of(
                        new HearingDaySchedule()
                            .setHearingVenueId(EPIMS)
                            .setHearingStartDateTime(hearingDay.getHearingStartDateTime())
                            .setHearingEndDateTime(hearingDay.getHearingEndDateTime())))
                                 .setReceivedDateTime(hearingResponseDate))
            .setRequestDetails(new HearingRequestDetails()
                                .setVersionNumber(VERSION_NUMBER))
            .setHearingDetails(new HearingDetails()
                                .setHearingType(TRIAL_HEARING_TYPE));
        when(hearingsService.getHearingResponse(anyString(), anyString())).thenReturn(hearing);
        when(hearingNoticeHmcGenerator.generate(eq(caseData), eq(hearing), anyString(), anyString(), anyString(), any())).thenReturn(List.of(CASE_DOCUMENT));
        Fee expectedFee = new Fee();
        expectedFee.setCalculatedAmountInPence(new BigDecimal(54500));
        expectedFee.setCode("FEE0441");
        expectedFee.setVersion("1");
        given(hearingFeesService.getFeeForHearingSmallClaims(any())).willReturn(expectedFee);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_NOTICE_HMC.name());
        var expectedHearingDays = List.of(
            new HearingDay()
                .setHearingStartDateTime(LocalDateTime.of(2023, 07, 01, 10, 0, 0))
                .setHearingEndDateTime(LocalDateTime.of(2023, 07, 01, 12, 0, 0))
        );

        HearingNoticeVariables updatedVars = new HearingNoticeVariables();
        updatedVars.setCaseId(CASE_ID);
        updatedVars.setHearingId(HEARING_ID);
        updatedVars.setCaseState(CASE_PROGRESSION.name());
        updatedVars.setRequestVersion(VERSION_NUMBER);
        updatedVars.setHearingStartDateTime(hearingDay.getHearingStartDateTime().plusHours(1));
        updatedVars.setHearingLocationEpims(EPIMS);
        updatedVars.setResponseDateTime(hearingResponseDate);
        updatedVars.setDays(expectedHearingDays);
        updatedVars.setHearingType(TRIAL_HEARING_TYPE);
        var actual = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        verify(camundaService).setProcessVariables(PROCESS_INSTANCE_ID, updatedVars);

        CaseData updatedData = mapper.convertValue(actual.getData(), CaseData.class);
        assertThat(updatedData.getHearingDocuments()).hasSize(1);
        assertThat(unwrapElements(updatedData.getHearingDocuments()).get(0)).isEqualTo(CASE_DOCUMENT);
        assertThat(updatedData.getHearingDate()).isEqualTo(hearingDay.getHearingStartDateTime().toLocalDate());
        assertThat(updatedData.getHearingDueDate()).isEqualTo(LocalDate.of(2023, 7, 1));
        assertThat(updatedData.getHearingFee()).isEqualTo(expectedFee);

    }

    @Test
    void shouldNotReturnHearingFee_WhenCaseProgressionFeatureToggleIsDisabled() {
        CaseData caseData = CaseDataBuilder.builder().build();
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
        caseData.setBusinessProcess(businessProcess);
        caseData.setCcdState(CASE_PROGRESSION);
        caseData.setCaseAccessCategory(UNSPEC_CLAIM);
        caseData.setAllocatedTrack(AllocatedTrack.SMALL_CLAIM);
        caseData.setTotalClaimAmount(new BigDecimal(1000));
        caseData.setClaimValue(null);
        caseData.setTotalInterest(BigDecimal.TEN);
        HearingNoticeVariables inputVariables = new HearingNoticeVariables();
        inputVariables.setHearingId(HEARING_ID);
        inputVariables.setCaseId(CASE_ID);

        List<LocationRefData> locations = List.of(new LocationRefData()
                                                      .setEpimmsId(EPIMS));
        when(locationRefDataService.getHearingCourtLocations(anyString()))
            .thenReturn(locations);
        when(camundaService.getProcessVariables(PROCESS_INSTANCE_ID)).thenReturn(inputVariables);
        HearingDay hearingDay = new HearingDay()
            .setHearingStartDateTime(LocalDateTime.of(2023, 07, 01, 9, 0, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 07, 01, 11, 0, 0));
        LocalDateTime hearingResponseDate = LocalDateTime.of(2023, 06, 02, 0, 0, 0);
        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                    List.of(
                        new HearingDaySchedule()
                            .setHearingVenueId(EPIMS)
                            .setHearingStartDateTime(hearingDay.getHearingStartDateTime())
                            .setHearingEndDateTime(hearingDay.getHearingEndDateTime())))
                                 .setReceivedDateTime(hearingResponseDate))
            .setRequestDetails(new HearingRequestDetails()
                                .setVersionNumber(VERSION_NUMBER))
            .setHearingDetails(new HearingDetails()
                                .setHearingType(TRIAL_HEARING_TYPE));
        when(hearingsService.getHearingResponse(anyString(), anyString())).thenReturn(hearing);
        when(hearingNoticeHmcGenerator.generate(eq(caseData), eq(hearing), anyString(), anyString(), anyString(), any())).thenReturn(List.of(CASE_DOCUMENT));

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_NOTICE_HMC.name());

        var actual = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData updatedData = mapper.convertValue(actual.getData(), CaseData.class);
        assertThat(updatedData.getHearingDocuments()).hasSize(1);
        assertThat(updatedData.getHearingFee()).isNull();

    }

    @Test
    void shouldCreateWelshDocument_whenConditionsAreMetOnDefendantLip() {
        RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
        respondentLiPResponse.setRespondent1ResponseLanguage("BOTH");
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimantFullDefence().build();
        caseData.setCaseDataLiP(caseDataLiP);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
        caseData.setBusinessProcess(businessProcess);
        caseData.setCcdState(CASE_PROGRESSION);
        caseData.setCaseAccessCategory(UNSPEC_CLAIM);
        caseData.setAllocatedTrack(AllocatedTrack.SMALL_CLAIM);
        caseData.setTotalClaimAmount(new BigDecimal(1000));
        caseData.setClaimValue(null);
        caseData.setTotalInterest(BigDecimal.TEN);
        caseData.setRespondent1Represented(YesOrNo.NO);

        HearingNoticeVariables inputVariables = new HearingNoticeVariables();
        inputVariables.setHearingId(HEARING_ID);
        inputVariables.setCaseId(CASE_ID);

        List<LocationRefData> locations = List.of(new LocationRefData()
                                                      .setEpimmsId(EPIMS));
        when(locationRefDataService.getHearingCourtLocations(anyString()))
            .thenReturn(locations);
        when(camundaService.getProcessVariables(PROCESS_INSTANCE_ID)).thenReturn(inputVariables);
        HearingDay hearingDay = new HearingDay()
            .setHearingStartDateTime(LocalDateTime.of(2023, 07, 01, 9, 0, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 07, 01, 11, 0, 0));
        LocalDateTime hearingResponseDate = LocalDateTime.of(2023, 06, 02, 0, 0, 0);
        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                    List.of(
                        new HearingDaySchedule()
                            .setHearingVenueId(EPIMS)
                            .setHearingStartDateTime(hearingDay.getHearingStartDateTime())
                            .setHearingEndDateTime(hearingDay.getHearingEndDateTime())))
                                 .setReceivedDateTime(hearingResponseDate))
            .setRequestDetails(new HearingRequestDetails()
                                .setVersionNumber(VERSION_NUMBER))
            .setHearingDetails(new HearingDetails()
                                .setHearingType(TRIAL_HEARING_TYPE));
        when(hearingsService.getHearingResponse(anyString(), anyString())).thenReturn(hearing);
        when(hearingNoticeHmcGenerator.generate(eq(caseData), eq(hearing), anyString(), anyString(), anyString(), eq(HEARING_NOTICE_HMC)))
            .thenReturn(List.of(CASE_DOCUMENT));
        when(hearingNoticeHmcGenerator.generate(eq(caseData), eq(hearing), anyString(), anyString(), anyString(), eq(HEARING_NOTICE_HMC_WELSH)))
            .thenReturn(List.of(CASE_DOCUMENT_WELSH));

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_NOTICE_HMC.name());

        var actual = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData updatedData = mapper.convertValue(actual.getData(), CaseData.class);
        assertThat(updatedData.getHearingDocuments()).hasSize(1);
        assertThat(updatedData.getHearingDocumentsWelsh()).hasSize(1);

    }

    @Test
    void shouldCreateWelshDocument_whenConditionsAreMetOnClaimantLip() {
        RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
        respondentLiPResponse.setRespondent1ResponseLanguage("BOTH");
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimantFullDefence().build();
        caseData.setCaseDataLiP(caseDataLiP);
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
        caseData.setBusinessProcess(businessProcess);
        caseData.setCcdState(CASE_PROGRESSION);
        caseData.setCaseAccessCategory(UNSPEC_CLAIM);
        caseData.setAllocatedTrack(AllocatedTrack.SMALL_CLAIM);
        caseData.setTotalClaimAmount(new BigDecimal(1000));
        caseData.setClaimValue(null);
        caseData.setTotalInterest(BigDecimal.TEN);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setClaimantBilingualLanguagePreference(Language.BOTH.toString());

        HearingNoticeVariables inputVariables = new HearingNoticeVariables();
        inputVariables.setHearingId(HEARING_ID);
        inputVariables.setCaseId(CASE_ID);

        List<LocationRefData> locations = List.of(new LocationRefData()
                                                      .setEpimmsId(EPIMS));
        when(locationRefDataService.getHearingCourtLocations(anyString()))
            .thenReturn(locations);
        when(camundaService.getProcessVariables(PROCESS_INSTANCE_ID)).thenReturn(inputVariables);
        HearingDay hearingDay = new HearingDay()
            .setHearingStartDateTime(LocalDateTime.of(2023, 07, 01, 9, 0, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 07, 01, 11, 0, 0));
        LocalDateTime hearingResponseDate = LocalDateTime.of(2023, 06, 02, 0, 0, 0);
        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(
                    List.of(
                        new HearingDaySchedule()
                            .setHearingVenueId(EPIMS)
                            .setHearingStartDateTime(hearingDay.getHearingStartDateTime())
                            .setHearingEndDateTime(hearingDay.getHearingEndDateTime())))
                                 .setReceivedDateTime(hearingResponseDate))
            .setRequestDetails(new HearingRequestDetails()
                                .setVersionNumber(VERSION_NUMBER))
            .setHearingDetails(new HearingDetails()
                                .setHearingType(TRIAL_HEARING_TYPE));
        when(hearingsService.getHearingResponse(anyString(), anyString())).thenReturn(hearing);
        when(hearingNoticeHmcGenerator.generate(eq(caseData), eq(hearing), anyString(), anyString(), anyString(), eq(HEARING_NOTICE_HMC)))
            .thenReturn(List.of(CASE_DOCUMENT));
        when(hearingNoticeHmcGenerator.generate(eq(caseData), eq(hearing), anyString(), anyString(), anyString(), eq(HEARING_NOTICE_HMC_WELSH)))
            .thenReturn(List.of(CASE_DOCUMENT_WELSH));

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_NOTICE_HMC.name());

        var actual = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData updatedData = mapper.convertValue(actual.getData(), CaseData.class);
        assertThat(updatedData.getHearingDocuments()).hasSize(1);
        assertThat(updatedData.getHearingDocumentsWelsh()).hasSize(1);

    }

    @Test
    void shouldCreateWelshDocument_whenWelshFeatureEnabledAndAppendExistingDocuments() {
        CaseDocument existingEnglish = new CaseDocument();
        existingEnglish.setDocumentName("existing-hearing-notice");
        existingEnglish.setDocumentType(HEARING_FORM);
        CaseDocument existingWelsh = new CaseDocument();
        existingWelsh.setDocumentName("existing-hearing-notice-welsh");
        existingWelsh.setDocumentType(HEARING_FORM_WELSH);

        RespondentLiPResponse respondentLiPResponse = new RespondentLiPResponse();
        respondentLiPResponse.setRespondent1ResponseLanguage("BOTH");
        CaseDataLiP caseDataLiP = new CaseDataLiP();
        caseDataLiP.setRespondent1LiPResponse(respondentLiPResponse);
        CaseData caseData = CaseDataBuilder.builder().atStateClaimantFullDefence().build();
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
        caseData.setBusinessProcess(businessProcess);
        caseData.setCcdState(CASE_PROGRESSION);
        caseData.setCaseAccessCategory(UNSPEC_CLAIM);
        caseData.setAllocatedTrack(AllocatedTrack.SMALL_CLAIM);
        caseData.setTotalClaimAmount(new BigDecimal(1000));
        caseData.setClaimValue(null);
        caseData.setTotalInterest(BigDecimal.TEN);
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setClaimantBilingualLanguagePreference(Language.BOTH.toString());
        caseData.setCaseDataLiP(caseDataLiP);
        caseData.setHearingDocuments(List.of(element(existingEnglish)));
        caseData.setHearingDocumentsWelsh(List.of(element(existingWelsh)));

        HearingNoticeVariables inputVariables = new HearingNoticeVariables();
        inputVariables.setHearingId(HEARING_ID);
        inputVariables.setCaseId(CASE_ID);

        List<LocationRefData> locations = List.of(new LocationRefData().setEpimmsId(EPIMS));
        when(locationRefDataService.getHearingCourtLocations(anyString())).thenReturn(locations);
        when(camundaService.getProcessVariables(PROCESS_INSTANCE_ID)).thenReturn(inputVariables);
        HearingDay hearingDay = new HearingDay()
            .setHearingStartDateTime(LocalDateTime.of(2023, 7, 1, 9, 0, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 7, 1, 11, 0, 0));
        LocalDateTime hearingResponseDate = LocalDateTime.of(2023, 6, 2, 0, 0, 0);
        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(List.of(
                    new HearingDaySchedule()
                        .setHearingVenueId(EPIMS)
                        .setHearingStartDateTime(hearingDay.getHearingStartDateTime())
                        .setHearingEndDateTime(hearingDay.getHearingEndDateTime())))
                                 .setReceivedDateTime(hearingResponseDate))
            .setRequestDetails(new HearingRequestDetails()
                                .setVersionNumber(VERSION_NUMBER))
            .setHearingDetails(new HearingDetails()
                                .setHearingType(TRIAL_HEARING_TYPE));
        when(hearingsService.getHearingResponse(anyString(), anyString())).thenReturn(hearing);
        when(hearingNoticeHmcGenerator.generate(any(), eq(hearing), anyString(), nullable(String.class), anyString(), eq(HEARING_NOTICE_HMC)))
            .thenReturn(List.of(CASE_DOCUMENT));
        when(hearingNoticeHmcGenerator.generate(any(), eq(hearing), anyString(), nullable(String.class), anyString(), eq(HEARING_NOTICE_HMC_WELSH)))
            .thenReturn(List.of(CASE_DOCUMENT_WELSH));
        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_NOTICE_HMC.name());

        AboutToStartOrSubmitCallbackResponse actual = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        CaseData updatedData = mapper.convertValue(actual.getData(), CaseData.class);
        assertThat(updatedData.getHearingDocuments()).hasSize(2);
        assertThat(updatedData.getHearingDocumentsWelsh()).hasSize(2);
        assertThat(unwrapElements(updatedData.getHearingDocuments()).get(0)).isEqualTo(CASE_DOCUMENT);
        assertThat(unwrapElements(updatedData.getHearingDocuments()).get(1)).isEqualTo(existingEnglish);
        assertThat(unwrapElements(updatedData.getHearingDocumentsWelsh()).get(0)).isEqualTo(CASE_DOCUMENT_WELSH);
        assertThat(unwrapElements(updatedData.getHearingDocumentsWelsh()).get(1)).isEqualTo(existingWelsh);
    }

    @Test
    void shouldReturnNullHearingLocationWhenReferenceDataUnavailable() {
        CaseDocument existingEnglish = new CaseDocument();
        existingEnglish.setDocumentName("existing-hearing-notice");
        existingEnglish.setDocumentType(HEARING_FORM);

        CaseData caseData = CaseDataBuilder.builder().atStateClaimantFullDefence().build();
        BusinessProcess businessProcess = new BusinessProcess();
        businessProcess.setProcessInstanceId(PROCESS_INSTANCE_ID);
        caseData.setBusinessProcess(businessProcess);
        caseData.setCcdState(CASE_PROGRESSION);
        caseData.setCaseAccessCategory(SPEC_CLAIM);
        caseData.setResponseClaimTrack("SMALL_CLAIM");
        caseData.setTotalClaimAmount(new BigDecimal(1000));
        caseData.setClaimValue(null);
        caseData.setTotalInterest(BigDecimal.TEN);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setHearingDocuments(List.of(element(existingEnglish)));

        HearingNoticeVariables inputVariables = new HearingNoticeVariables();
        inputVariables.setHearingId(HEARING_ID);
        inputVariables.setCaseId(CASE_ID);

        when(camundaService.getProcessVariables(PROCESS_INSTANCE_ID)).thenReturn(inputVariables);
        HearingDay hearingDay = new HearingDay()
            .setHearingStartDateTime(LocalDateTime.of(2023, 7, 1, 9, 0, 0))
            .setHearingEndDateTime(LocalDateTime.of(2023, 7, 1, 11, 0, 0));
        LocalDateTime hearingResponseDate = LocalDateTime.of(2023, 6, 2, 0, 0, 0);
        HearingGetResponse hearing = new HearingGetResponse()
            .setHearingResponse(new HearingResponse().setHearingDaySchedule(List.of(
                    new HearingDaySchedule()
                        .setHearingVenueId(EPIMS)
                        .setHearingStartDateTime(hearingDay.getHearingStartDateTime())
                        .setHearingEndDateTime(hearingDay.getHearingEndDateTime())))
                                 .setReceivedDateTime(hearingResponseDate))
            .setRequestDetails(new HearingRequestDetails()
                                .setVersionNumber(VERSION_NUMBER))
            .setHearingDetails(new HearingDetails()
                                .setHearingType(TRIAL_HEARING_TYPE));
        when(hearingsService.getHearingResponse(anyString(), anyString())).thenReturn(hearing);
        when(hearingNoticeHmcGenerator.generate(any(), eq(hearing), anyString(), nullable(String.class), anyString(), eq(HEARING_NOTICE_HMC)))
            .thenReturn(List.of(CASE_DOCUMENT));

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
        params.getRequest().setEventId(GENERATE_HEARING_NOTICE_HMC.name());

        try (MockedStatic<HmcDataUtils> utils = Mockito.mockStatic(HmcDataUtils.class, Answers.CALLS_REAL_METHODS)) {
            utils.when(() -> HmcDataUtils.getLocationRefData(HEARING_ID, EPIMS, "BEARER_TOKEN", locationRefDataService))
                .thenReturn(null);

            AboutToStartOrSubmitCallbackResponse actual = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            CaseData updatedData = mapper.convertValue(actual.getData(), CaseData.class);
            assertThat(updatedData.getHearingLocation().getValue().getLabel()).isNull();
            assertThat(updatedData.getHearingDocuments()).hasSize(2);
            assertThat(unwrapElements(updatedData.getHearingDocuments()).get(0)).isEqualTo(CASE_DOCUMENT);
            assertThat(unwrapElements(updatedData.getHearingDocuments()).get(1)).isEqualTo(existingEnglish);
        }
    }
}
