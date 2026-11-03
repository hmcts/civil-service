package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPDATE_GA_CASE_DATA;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class GeneralApplicationCaseNameServiceTest {

    private static final String ORIGINAL_NAME = "Claimant v Original Defendant";
    private static final String NEW_NAME = "Claimant v New Defendant";

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @InjectMocks
    private GeneralApplicationCaseNameService service;

    @Test
    void shouldUpdateEveryLinkedGeneralApplication_whenCaseNameChanged() {
        CaseData saved = caseData(NEW_NAME, generalApplication("111"), generalApplication("222"));

        service.updateGeneralApplicationsIfCaseNameChanged(saved, caseData(ORIGINAL_NAME));

        verify(coreCaseDataService).triggerGeneralApplicationEvent(
            111L, UPDATE_GA_CASE_DATA, Map.of("caseNameGaInternal", NEW_NAME));
        verify(coreCaseDataService).triggerGeneralApplicationEvent(
            222L, UPDATE_GA_CASE_DATA, Map.of("caseNameGaInternal", NEW_NAME));
    }

    @Test
    void shouldNotUpdateGeneralApplications_whenCaseNameUnchanged() {
        CaseData saved = caseData(ORIGINAL_NAME, generalApplication("111"));

        service.updateGeneralApplicationsIfCaseNameChanged(saved, caseData(ORIGINAL_NAME));

        verifyNoInteractions(coreCaseDataService);
    }

    @Test
    void shouldNotUpdateGeneralApplications_whenSavedCaseNameMissing() {
        CaseData saved = caseData(null, generalApplication("111"));

        service.updateGeneralApplicationsIfCaseNameChanged(saved, caseData(ORIGINAL_NAME));

        verifyNoInteractions(coreCaseDataService);
    }

    @Test
    void shouldUpdateGeneralApplications_whenNoCaseDataBefore() {
        CaseData saved = caseData(NEW_NAME, generalApplication("111"));

        service.updateGeneralApplicationsIfCaseNameChanged(saved, null);

        verify(coreCaseDataService).triggerGeneralApplicationEvent(
            111L, UPDATE_GA_CASE_DATA, Map.of("caseNameGaInternal", NEW_NAME));
    }

    @Test
    void shouldSkipGeneralApplicationsWithoutCaseLink() {
        CaseData saved = caseData(NEW_NAME, new GeneralApplication());

        service.updateGeneralApplicationsIfCaseNameChanged(saved, caseData(ORIGINAL_NAME));

        verifyNoInteractions(coreCaseDataService);
    }

    @Test
    void shouldDoNothing_whenNoGeneralApplications() {
        service.updateGeneralApplicationsIfCaseNameChanged(caseData(NEW_NAME), caseData(ORIGINAL_NAME));

        verifyNoInteractions(coreCaseDataService);
    }

    @Test
    void shouldContinueWithRemainingGeneralApplications_whenOneUpdateFails() {
        CaseData saved = caseData(NEW_NAME, generalApplication("111"), generalApplication("222"));
        doThrow(new IllegalStateException("CCD unavailable"))
            .when(coreCaseDataService).triggerGeneralApplicationEvent(eq(111L), eq(UPDATE_GA_CASE_DATA), eq(Map.of("caseNameGaInternal", NEW_NAME)));

        service.updateGeneralApplicationsIfCaseNameChanged(saved, caseData(ORIGINAL_NAME));

        verify(coreCaseDataService).triggerGeneralApplicationEvent(
            222L, UPDATE_GA_CASE_DATA, Map.of("caseNameGaInternal", NEW_NAME));
    }

    private static CaseData caseData(String caseName, GeneralApplication... generalApplications) {
        CaseData caseData = CaseDataBuilder.builder().ccdCaseReference(1L).build();
        caseData.setCaseNameHmctsInternal(caseName);
        if (generalApplications.length > 0) {
            caseData.setGeneralApplications(wrapElements(generalApplications));
        }
        return caseData;
    }

    private static GeneralApplication generalApplication(String caseReference) {
        return new GeneralApplication().setCaseLink(new CaseLink(caseReference));
    }
}
