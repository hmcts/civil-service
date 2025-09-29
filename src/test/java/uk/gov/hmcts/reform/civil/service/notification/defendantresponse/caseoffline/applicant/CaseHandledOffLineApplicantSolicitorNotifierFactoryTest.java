package uk.gov.hmcts.reform.civil.service.notification.defendantresponse.caseoffline.applicant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CaseHandledOffLineApplicantSolicitorNotifierFactoryTest {

    @Mock
    private CaseHandledOfflineApplicantSolicitorUnspecNotifier caseHandledOfflineApplicantSolicitorUnspecNotifier;
    @Mock
    private CaseHandledOfflineApplicantSolicitorSpecNotifier caseHandledOfflineApplicantSolicitorSpecNotifier;

    @InjectMocks
    private CaseHandledOffLineApplicantSolicitorNotifierFactory caseHandledOffLineApplicantSolicitorNotifierFactory;

    @Test
    void whenSpecCase_shouldReturnSpecNotifier() {
        CaseData data = CaseData.builder().caseAccessCategory(CaseCategory.SPEC_CLAIM).build();

        var notifier = caseHandledOffLineApplicantSolicitorNotifierFactory.getCaseHandledOfflineSolicitorNotifier(data);

        assertThat(notifier.getClass()).isEqualTo(CaseHandledOfflineApplicantSolicitorSpecNotifier.class);
    }

    @Test
    void whenUnspecCase_shouldReturnUnspecNotifier() {
        CaseData data = CaseData.builder().caseAccessCategory(CaseCategory.UNSPEC_CLAIM).build();

        var notifier = caseHandledOffLineApplicantSolicitorNotifierFactory.getCaseHandledOfflineSolicitorNotifier(data);

        assertThat(notifier.getClass()).isEqualTo(CaseHandledOfflineApplicantSolicitorUnspecNotifier.class);
    }
}
