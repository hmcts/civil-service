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
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.referencedata.LocationRefDataService;
import uk.gov.hmcts.reform.civil.utils.CaseMigratonUtility;

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

    private final CoreCaseDataService coreCaseDataService;
    private final LocationRefDataService locationRefDataService;

    @Override
    protected Map<String, Callback> callbacks() {
        return new ImmutableMap.Builder<String, Callback>()
            .put(callbackKey(ABOUT_TO_SUBMIT), this::migrateCaseData)
            .put(callbackKey(SUBMITTED), this::migrateSuppmentryData)
            .build();
    }

    private CallbackResponse migrateCaseData(CallbackParams callbackParams) {
        CaseData oldCaseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = oldCaseData.toBuilder();

        log.info("Migrating data for case: {}", oldCaseData.getCcdCaseReference());
        String authToken = callbackParams.getParams().get(BEARER_TOKEN).toString();
        log.info("After getting the access token Type : {}", oldCaseData.getCaseAccessCategory());
        CaseLocation caseLocation = CaseLocation.builder().baseLocation("420219").region("2").build();
        if (CaseCategory.SPEC_CLAIM.equals(oldCaseData.getCaseAccessCategory())) {
            log.info("Inside IF SPEC CLAIM ");
            CaseMigratonUtility.migrateGS(oldCaseData, caseDataBuilder
            );

            CaseMigratonUtility.migrateCaseManagementLocation(caseDataBuilder, caseLocation);
        } else {
            log.info("Inside ELSE UNSPEC CLAIM ");
            caseLocation = CaseLocation.builder().baseLocation("192280").region("4").build();
            CaseMigratonUtility.migrateCaseManagementLocation(caseDataBuilder, caseLocation);
            CaseMigratonUtility.migrateGS(oldCaseData, caseDataBuilder);

            CaseMigratonUtility.migrateUnspecCoutLocation(authToken, oldCaseData, caseDataBuilder,
                                                          locationRefDataService
            );

        }
        log.info("Update respondent and applicant DQ ");
        caseLocation = CaseLocation.builder().baseLocation("420219").region("2").build();
        CaseMigratonUtility.migrateRespondentAndApplicantDQUnSpec(authToken, oldCaseData, caseDataBuilder,
                                                                  locationRefDataService, caseLocation
        );
        log.info("Update migration ID ");
        caseDataBuilder.migrationId(MIGRATION_ID_VALUE);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse migrateSuppmentryData(CallbackParams callbackParams) {
        CaseData oldCaseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = oldCaseData.toBuilder();
        if (CaseCategory.SPEC_CLAIM.equals(oldCaseData.getCaseAccessCategory())) {
            CaseMigratonUtility.setSupplementaryData(oldCaseData.getCcdCaseReference(), coreCaseDataService,
                                                     "AAA6");
        } else {
            CaseMigratonUtility.setSupplementaryData(oldCaseData.getCcdCaseReference(), coreCaseDataService,
                                                     "AAA7");

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
