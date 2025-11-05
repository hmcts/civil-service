package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.service.sdo.SdoFeatureToggleService;
import uk.gov.hmcts.reform.civil.service.sdo.SdoLocationService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.Document.builder;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getHearingNotes;

@ExtendWith(MockitoExtension.class)
class DjSubmissionServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private AssignCategoryId assignCategoryId;
    @Mock
    private SdoFeatureToggleService featureToggleService;
    @Mock
    private SdoLocationService sdoLocationService;

    private DjSubmissionService service;

    @BeforeEach
    void setUp() {
        service = new DjSubmissionService(assignCategoryId, featureToggleService, sdoLocationService);
    }

    @Test
    void shouldRemovePreviewDocumentAndAssignCategories() {
        uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument caseDocument =
            uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument.builder()
                .documentLink(builder().documentUrl("url").build())
                .build();

        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .orderSDODocumentDJ(builder().documentUrl("url").build())
            .orderSDODocumentDJCollection(List.of(element(caseDocument)))
            .build();

        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        CaseData result = service.prepareSubmission(caseData, AUTH_TOKEN);

        assertThat(result.getOrderSDODocumentDJ()).isNull();
        BusinessProcess businessProcess = result.getBusinessProcess();
        assertThat(businessProcess.getStatus()).isEqualTo(BusinessProcessStatus.READY);
        assertThat(businessProcess.getCamundaEvent()).isEqualTo(STANDARD_DIRECTION_ORDER_DJ.name());
        assertThat(result.getHearingNotes()).isEqualTo(getHearingNotes(caseData));
        verify(assignCategoryId).assignCategoryIdToCollection(
            eq(caseData.getOrderSDODocumentDJCollection()),
            any(),
            eq("caseManagementOrders")
        );
        verify(sdoLocationService, never()).updateWaLocationsIfRequired(any(), any(), any());
    }

    @Test
    void shouldSetEaCourtLocationYesForSpecNonLip() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1Represented(YesOrNo.YES)
            .respondent1Represented(YesOrNo.YES)
            .respondent2Represented(YesOrNo.YES)
            .build();

        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        CaseData result = service.prepareSubmission(caseData, AUTH_TOKEN);

        assertThat(result.getEaCourtLocation()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldRespectWelshToggleForEaCourtLocation() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("110").build())
            .build();

        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(true);
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        CaseData result = service.prepareSubmission(caseData, AUTH_TOKEN);

        assertThat(result.getEaCourtLocation()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldCallWaUpdateWhenEnabled() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("220").build())
            .build();

        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(true);

        service.prepareSubmission(caseData, AUTH_TOKEN);

        ArgumentCaptor<CaseData.CaseDataBuilder<?, ?>> captor =
            ArgumentCaptor.forClass(CaseData.CaseDataBuilder.class);
        verify(sdoLocationService).updateWaLocationsIfRequired(eq(caseData), captor.capture(), eq(AUTH_TOKEN));
        assertThat(captor.getValue()).isNotNull();
    }

    @Test
    void shouldSetEaCourtLocationNoWhenLipAndNotWhitelisted() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1Represented(YesOrNo.NO)
            .respondent1Represented(YesOrNo.NO)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("330").build())
            .build();

        when(featureToggleService.isWelshEnabledForMainCase()).thenReturn(false);
        when(featureToggleService.isCaseProgressionEnabledAndLocationWhiteListed("330")).thenReturn(false);
        when(featureToggleService.isMultiOrIntermediateTrackEnabled(caseData)).thenReturn(false);

        CaseData result = service.prepareSubmission(caseData, AUTH_TOKEN);

        assertThat(result.getEaCourtLocation()).isEqualTo(YesOrNo.NO);
    }
}
