package uk.gov.hmcts.reform.civil.handler.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.repositories.DashboardNotificationsRepository;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FixDashboardParamsTextTaskTest {

    private static final String CASE_ID = "1234567890";
    private static final String NOTIFICATION_NAME = "Notice.AAA6.ClaimantIntent.ClaimSettled.Defendant";

    @Mock
    private DashboardNotificationsRepository dashboardNotificationsRepository;
    @InjectMocks
    private FixDashboardParamsTextTask task;

    @Test
    void shouldReplacePlaceholderWithCorrectParamValue() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("applicant1ClaimSettledDateEn", "30 March 2026");
        params.put("applicant1ClaimSettledDateCy", "30 Mawrth 2026");

        DashboardNotificationsEntity notification = buildNotification(
            "<p class=\"govuk-body\">The claimant has confirmed that this case was settled on ${claimSettledDateEn}.</p>"
                + "<p class=\"govuk-body\">If you do not agree that the case is settled, please outline your objections"
                + " in writing within 19 days of the settlement date, to the Civil National Business Centre"
                + " using the email address at {cmcCourtEmailId}</p>",
            "<p class=\"govuk-body\">The claimant has confirmed that this case was settled on ${claimSettledDateEn}.</p>"
                + "<p class=\"govuk-body\">If you do not agree that the case is settled, please outline your objections"
                + " in writing within 19 days of the settlement date, to the Civil National Business Centre"
                + " using the email address at {cmcCourtEmailId}</p>",
            params
        );

        when(dashboardNotificationsRepository.findByReferenceAndCitizenRoleAndName(CASE_ID, "DEFENDANT", NOTIFICATION_NAME))
            .thenReturn(List.of(notification));

        CaseReference caseRef = new CaseReference();
        caseRef.setCaseReference(CASE_ID);
        task.migrateCaseData(CaseData.builder().build(), caseRef);

        ArgumentCaptor<DashboardNotificationsEntity> captor = ArgumentCaptor.forClass(DashboardNotificationsEntity.class);
        verify(dashboardNotificationsRepository).save(captor.capture());

        DashboardNotificationsEntity saved = captor.getValue();
        assertThat(saved.getDescriptionEn()).contains("settled on 30 March 2026.");
        assertThat(saved.getDescriptionEn()).doesNotContain("${claimSettledDateEn}");
        assertThat(saved.getDescriptionEn()).contains("{cmcCourtEmailId}");
        assertThat(saved.getDescriptionCy()).contains("settled on 30 March 2026.");
        assertThat(saved.getUpdatedBy()).isEqualTo("FixDashboardParamsTextTask");
    }

    @Test
    void shouldNotSaveWhenNoNotificationFound() {
        when(dashboardNotificationsRepository.findByReferenceAndCitizenRoleAndName(CASE_ID, "DEFENDANT", NOTIFICATION_NAME))
            .thenReturn(List.of());

        CaseReference caseRef = new CaseReference();
        caseRef.setCaseReference(CASE_ID);
        task.migrateCaseData(CaseData.builder().build(), caseRef);

        verify(dashboardNotificationsRepository, never()).save(any());
    }

    @Test
    void shouldSkipWhenParamsAreNull() {
        DashboardNotificationsEntity notification = buildNotification(
            "text with ${claimSettledDateEn}",
            "text with ${claimSettledDateEn}",
            null
        );

        when(dashboardNotificationsRepository.findByReferenceAndCitizenRoleAndName(CASE_ID, "DEFENDANT", NOTIFICATION_NAME))
            .thenReturn(List.of(notification));

        CaseReference caseRef = new CaseReference();
        caseRef.setCaseReference(CASE_ID);
        task.migrateCaseData(CaseData.builder().build(), caseRef);

        verify(dashboardNotificationsRepository, never()).save(any());
    }

    @Test
    void shouldSkipWhenMappedParamValueNotInStoredParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("someOtherParam", "value");

        DashboardNotificationsEntity notification = buildNotification(
            "text with ${claimSettledDateEn}",
            "text with ${claimSettledDateEn}",
            params
        );

        when(dashboardNotificationsRepository.findByReferenceAndCitizenRoleAndName(CASE_ID, "DEFENDANT", NOTIFICATION_NAME))
            .thenReturn(List.of(notification));

        CaseReference caseRef = new CaseReference();
        caseRef.setCaseReference(CASE_ID);
        task.migrateCaseData(CaseData.builder().build(), caseRef);

        verify(dashboardNotificationsRepository, never()).save(any());
    }

    @Test
    void shouldReturnCorrectTaskName() {
        assertThat(task.getTaskName()).isEqualTo("FixDashboardParamsTextTask");
    }

    private DashboardNotificationsEntity buildNotification(String descEn, String descCy,
                                                            HashMap<String, Object> params) {
        DashboardNotificationsEntity entity = new DashboardNotificationsEntity();
        entity.setId(UUID.randomUUID());
        entity.setReference(CASE_ID);
        entity.setName(NOTIFICATION_NAME);
        entity.setCitizenRole("DEFENDANT");
        entity.setTitleEn("The claim is settled");
        entity.setDescriptionEn(descEn);
        entity.setTitleCy("Mae'r hawliad wedi'i setlo");
        entity.setDescriptionCy(descCy);
        entity.setParams(params);
        entity.setCreatedAt(OffsetDateTime.now());
        return entity;
    }
}
