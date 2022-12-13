package uk.gov.hmcts.reform.civil.handler.callback.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocation;
import uk.gov.hmcts.reform.civil.utils.CaseMigrationUtility;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.migrateCase;

@Slf4j
@Service
@RequiredArgsConstructor
public class MigrateCaseDataCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(migrateCase);

    private static final String MIGRATION_ID_VALUE = "GSMigration";

    private final ObjectMapper objectMapper;
    private final CaseMigrationUtility caseMigrationUtility;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_SUBMIT), this::migrateCaseData)
            .put(callbackKey(SUBMITTED), this::migrateSupplementaryData)
            .build();
    }

    private CallbackResponse migrateCaseData(CallbackParams callbackParams) {
        CaseData oldCaseData = callbackParams.getCaseData();
        log.info("Migrating data for case: {}", oldCaseData.getCcdCaseReference());
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = oldCaseData.toBuilder();
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        if (CaseCategory.SPEC_CLAIM.equals(oldCaseData.getCaseAccessCategory())) {
            caseMigrationUtility.migrateGS(oldCaseData, caseDataBuilder
            );

            caseMigrationUtility.migrateCaseManagementLocation(
                caseDataBuilder,
                CaseLocation.builder().baseLocation("420219").region("2").build()
            );
        } else {
            caseMigrationUtility.migrateCaseManagementLocation(
                caseDataBuilder,
                CaseLocation.builder().baseLocation("192280").region("4").build()
            );
            caseMigrationUtility.migrateGS(oldCaseData, caseDataBuilder);
            caseMigrationUtility.migrateUnspecCourtLocation(authToken, oldCaseData, caseDataBuilder);
        }
        caseMigrationUtility.migrateRespondentAndApplicantDQUnSpec(
            authToken,
            oldCaseData,
            caseDataBuilder,
            CaseLocation.builder().baseLocation("420219").region("2").build()
        );

        caseDataBuilder.migrationId(MIGRATION_ID_VALUE);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse migrateSupplementaryData(CallbackParams callbackParams) {
        CaseData oldCaseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = oldCaseData.toBuilder();
        if (CaseCategory.SPEC_CLAIM.equals(oldCaseData.getCaseAccessCategory())) {
            caseMigrationUtility.setSupplementaryData(
                oldCaseData.getCcdCaseReference(),
                "AAA6"
            );
        } else {
            caseMigrationUtility.setSupplementaryData(
                oldCaseData.getCcdCaseReference(),
                "AAA7"
            );

        }
        return SubmittedCallbackResponse.builder().build();
    }

    /*private CallbackResponse migrateCaseData(CallbackParams callbackParams) {
        CaseData oldCaseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = oldCaseData.toBuilder();

        log.info("Migrating data for case: {}", oldCaseData.getCcdCaseReference());

        if (SuperClaimType.SPEC_CLAIM.equals(oldCaseData.getSuperClaimType())) {
            caseDataBuilder.caseAccessCategory(CaseCategory.SPEC_CLAIM);
            updateOrgPolicyCaseRole(oldCaseData, caseDataBuilder);
        } else {
            caseDataBuilder.caseAccessCategory(CaseCategory.UNSPEC_CLAIM);
        }

        caseDataBuilder.migrationId(MIGRATION_ID_VALUE);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void updateOrgPolicyCaseRole(CaseData oldCaseData,
                                         CaseData.CaseDataBuilder<?, ?> caseDataBuilder) {
        OrganisationPolicy applicant1OrganisationPolicy = oldCaseData.getApplicant1OrganisationPolicy();
        caseDataBuilder.applicant1OrganisationPolicy(OrganisationPolicy.builder()
                             .orgPolicyReference(applicant1OrganisationPolicy.getOrgPolicyReference())
                             .orgPolicyCaseAssignedRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName())
                             .organisation(applicant1OrganisationPolicy.getOrganisation())
                             .build());

        OrganisationPolicy respondent1OrganisationPolicy = oldCaseData.getRespondent1OrganisationPolicy();
        if (respondent1OrganisationPolicy != null) {
            caseDataBuilder.respondent1OrganisationPolicy(OrganisationPolicy.builder()
                              .organisation(respondent1OrganisationPolicy.getOrganisation())
                              .orgPolicyReference(respondent1OrganisationPolicy.getOrgPolicyReference())
                              .orgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName())
                              .build());
        }

        OrganisationPolicy respondent2OrganisationPolicy = oldCaseData.getRespondent2OrganisationPolicy();
        if (respondent2OrganisationPolicy != null) {
            caseDataBuilder.respondent2OrganisationPolicy(OrganisationPolicy.builder()
                            .organisation(respondent2OrganisationPolicy.getOrganisation())
                            .orgPolicyReference(respondent2OrganisationPolicy.getOrgPolicyReference())
                            .orgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName())
                            .build());
        }
    }
*/
    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
