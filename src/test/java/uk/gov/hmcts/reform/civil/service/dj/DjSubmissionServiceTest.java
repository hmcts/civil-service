package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderCaseProgressionService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.STANDARD_DIRECTION_ORDER_DJ;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.HearingUtils.getHearingNotes;

@ExtendWith(MockitoExtension.class)
class DjSubmissionServiceTest {

    private static final String AUTH_TOKEN = "auth-token";

    @Mock
    private AssignCategoryId assignCategoryId;
    @Mock
    private DirectionsOrderCaseProgressionService caseProgressionService;

    private DjSubmissionService service;

    @BeforeEach
    void setUp() {
        service = new DjSubmissionService(assignCategoryId, caseProgressionService);
    }

    @Test
    void shouldRemovePreviewDocumentAndAssignCategories() {
        uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument caseDocument =
            uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument.builder()
                .documentLink(Document.builder().documentUrl("url").build())
                .build();

        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .orderSDODocumentDJ(Document.builder().documentUrl("url").build())
            .orderSDODocumentDJCollection(List.of(element(caseDocument)))
            .build();

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
        verify(caseProgressionService).applyCaseProgressionRouting(caseData, AUTH_TOKEN, false, false);
    }

    @Test
    void shouldApplyHelperMutationsToResult() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("110").build())
            .build();

        doAnswer(invocation -> {
            caseData.setEaCourtLocation(YesOrNo.YES);
            return null;
        }).when(caseProgressionService).applyCaseProgressionRouting(eq(caseData), eq(AUTH_TOKEN), eq(false), eq(false));

        CaseData result = service.prepareSubmission(caseData, AUTH_TOKEN);

        assertThat(result.getEaCourtLocation()).isEqualTo(YesOrNo.YES);
        verify(caseProgressionService).applyCaseProgressionRouting(caseData, AUTH_TOKEN, false, false);
    }

    @Test
    void shouldDelegateWaUpdatesToHelperOnly() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("220").build())
            .build();

        service.prepareSubmission(caseData, AUTH_TOKEN);

        verify(caseProgressionService).applyCaseProgressionRouting(caseData, AUTH_TOKEN, false, false);
        verifyNoMoreInteractions(caseProgressionService);
    }
}
