package uk.gov.hmcts.reform.civil.service.ga;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class GaInitiateGeneralApplicationServiceTest {

    private static final String AUTH = "auth";

    @Mock
    private InitiateGeneralApplicationService delegate;
    @Mock
    private GaInitiateGeneralApplicationHelper helper;

    private GaInitiateGeneralApplicationService service;
    private List<Element<GeneralApplication>> applications;

    @BeforeEach
    void setUp() {
        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(new Jdk8Module())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        service = new GaInitiateGeneralApplicationService(delegate, helper, mapper);
        applications = List.of(element(GeneralApplication.builder().build()));
        lenient().when(helper.ensureDefaults(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(helper.buildApplications(any())).thenReturn(applications);
        lenient().doAnswer(invocation -> {
            Map<String, Object> map = invocation.getArgument(0);
            GeneralApplicationCaseData data = invocation.getArgument(1);
            applyLipFlag(map, "applicant1Represented", data.getIsGaApplicantLip());
            applyLipFlag(map, "respondent1Represented", data.getIsGaRespondentOneLip());
            applyLipFlag(map, "specRespondent1Represented", data.getIsGaRespondentOneLip());
            applyLipFlag(map, "respondent2Represented", data.getIsGaRespondentTwoLip());
            applyLipFlag(map, "specRespondent2Represented", data.getIsGaRespondentTwoLip());
            return null;
        }).when(helper).applyLipFlags(anyMap(), any(GeneralApplicationCaseData.class));
    }

    private void applyLipFlag(Map<String, Object> map, String key, YesOrNo lipFlag) {
        if (lipFlag == null) {
            return;
        }
        YesOrNo represented = lipFlag == YesOrNo.YES ? YesOrNo.NO : YesOrNo.YES;
        map.put(key, represented);
    }

    @Test
    void caseContainsLip_shouldReturnTrueWhenFlagsPresent() {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withIsGaApplicantLip(YesOrNo.YES)
            .build();
        when(helper.hasExplicitLipFlag(gaCaseData)).thenReturn(true);

        assertThat(service.caseContainsLip(gaCaseData)).isTrue();
    }

    @Test
    void caseContainsLip_shouldFallbackToDelegateWhenFlagsMissing() {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withIsGaApplicantLip(null)
            .withIsGaRespondentOneLip(null)
            .withIsGaRespondentTwoLip(null)
            .build();
        when(helper.hasExplicitLipFlag(gaCaseData)).thenReturn(false);
        when(delegate.caseContainsLiP(any())).thenReturn(true);

        assertThat(service.caseContainsLip(gaCaseData)).isTrue();
        verify(delegate).caseContainsLiP(any());
    }

    @Test
    void respondentAssigned_shouldDelegateToHelper() {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder().build();
        when(helper.respondentAssigned(gaCaseData)).thenReturn(true);

        assertThat(service.respondentAssigned(gaCaseData)).isTrue();
        verify(helper).respondentAssigned(gaCaseData);
    }

    @Test
    void isGaApplicantSameAsParentCaseClaimant_shouldDelegateToHelper() {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder().build();
        when(helper.isGaApplicantSameAsParentCaseClaimant(gaCaseData, AUTH)).thenReturn(true);

        assertThat(service.isGaApplicantSameAsParentCaseClaimant(gaCaseData, AUTH)).isTrue();
        verify(helper).isGaApplicantSameAsParentCaseClaimant(gaCaseData, AUTH);
    }

    @Test
    void asCaseData_shouldFlipLipFlagsToRepresentedFields() {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder()
            .withIsGaApplicantLip(YesOrNo.YES)
            .withIsGaRespondentOneLip(YesOrNo.NO)
            .withIsGaRespondentTwoLip(YesOrNo.YES)
            .build();

        CaseData converted = service.asCaseData(gaCaseData);

        assertThat(converted.getApplicant1Represented()).isEqualTo(YesOrNo.NO);
        assertThat(converted.getRespondent1Represented()).isEqualTo(YesOrNo.YES);
        assertThat(converted.getRespondent2Represented()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void buildCaseData_shouldOverlayDelegateResponseOntoGaDto() {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder()
            .build();
        CaseData delegateInput = CaseDataBuilder.builder().build();
        CaseData delegateOutput = delegateInput.toBuilder()
            .generalApplications(wrapElements(GeneralApplication.builder().build()))
            .build();
        when(delegate.buildCaseData(any(), any(), any(), any())).thenReturn(delegateOutput);
        UserDetails userDetails = UserDetails.builder().id("user").email("user@test.com").build();

        GeneralApplicationCaseData result = service.buildCaseData(gaCaseData, userDetails, AUTH);

        assertThat(result.getGeneralApplications()).isEqualTo(applications);
        ArgumentCaptor<CaseData> caseDataCaptor = ArgumentCaptor.forClass(CaseData.class);
        verify(delegate).buildCaseData(any(), caseDataCaptor.capture(), any(), any());
        assertThat(caseDataCaptor.getValue().getGeneralApplications()).isEmpty();
    }

    @Test
    void ensureDefaults_shouldDelegateToHelper() {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder().build();
        assertThat(service.ensureDefaults(gaCaseData)).isSameAs(gaCaseData);
        verify(helper).ensureDefaults(gaCaseData);
    }

}
