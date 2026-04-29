package uk.gov.hmcts.reform.civil.handler.migration;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.bulkupdate.csv.CaseReference;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.dashboard.entities.DashboardNotificationsEntity;
import uk.gov.hmcts.reform.dashboard.repositories.DashboardNotificationsRepository;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class FixDashboardParamsTextTask extends MigrationTask<CaseReference> {

    private static final String NOTIFICATION_NAME = "Notice.AAA6.ClaimantIntent.ClaimSettled.Defendant";
    private static final String DEFENDANT = "DEFENDANT";

    private static final Map<String, String> PARAM_KEY_MAPPING = Map.of(
        "claimSettledDateEn", "applicant1ClaimSettledDateEn",
        "claimSettledDateCy", "applicant1ClaimSettledDateCy"
    );

    private final DashboardNotificationsRepository dashboardNotificationsRepository;

    public FixDashboardParamsTextTask(DashboardNotificationsRepository dashboardNotificationsRepository) {
        super(CaseReference.class);
        this.dashboardNotificationsRepository = dashboardNotificationsRepository;
    }

    @Override
    protected String getTaskName() {
        return "FixDashboardParamsTextTask";
    }

    @Override
    protected String getEventSummary() {
        return "Fix claim settled defendant notification with unresolved param placeholders";
    }

    @Override
    protected String getEventDescription() {
        return "Replaces ${claimSettledDateEn} with the correct applicant1ClaimSettledDateEn value from stored params";
    }

    @Override
    protected CaseData migrateCaseData(CaseData caseData, CaseReference caseReference) {
        String caseId = caseReference.getCaseReference();

        List<DashboardNotificationsEntity> notifications =
            dashboardNotificationsRepository.findByReferenceAndCitizenRoleAndName(caseId, DEFENDANT, NOTIFICATION_NAME);

        if (notifications.isEmpty()) {
            log.info("No {} notification found for case {}", NOTIFICATION_NAME, caseId);
            return caseData;
        }

        for (DashboardNotificationsEntity notification : notifications) {
            HashMap<String, Object> params = notification.getParams();
            if (params == null || params.isEmpty()) {
                log.warn("Notification {} for case {} has no stored params, skipping", NOTIFICATION_NAME, caseId);
                continue;
            }

            HashMap<String, Object> mappedParams = buildMappedParams(params);
            if (mappedParams.isEmpty()) {
                log.warn("No matching param values found for case {}, skipping", caseId);
                continue;
            }

            StringSubstitutor substitutor = new StringSubstitutor(mappedParams);
            notification.setDescriptionEn(substitutor.replace(notification.getDescriptionEn()));
            notification.setDescriptionCy(substitutor.replace(notification.getDescriptionCy()));
            notification.setUpdatedOn(OffsetDateTime.now());
            notification.setUpdatedBy("FixDashboardParamsTextTask");

            dashboardNotificationsRepository.save(notification);
            log.info("Fixed notification for case {} - descEn: [{}], descCy: [{}]",
                caseId, notification.getDescriptionEn(), notification.getDescriptionCy());
        }

        return caseData;
    }

    private HashMap<String, Object> buildMappedParams(HashMap<String, Object> storedParams) {
        HashMap<String, Object> mapped = new HashMap<>();
        for (Map.Entry<String, String> entry : PARAM_KEY_MAPPING.entrySet()) {
            String templateKey = entry.getKey();
            String storedKey = entry.getValue();
            Object value = storedParams.get(storedKey);
            if (value != null) {
                mapped.put(templateKey, value);
            }
        }
        return mapped;
    }
}
