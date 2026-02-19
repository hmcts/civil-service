package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.QUERY_DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getQueryById;
import static uk.gov.hmcts.reform.civil.utils.CaseQueriesUtil.getUserRoleForQuery;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isApplicantSolicitor;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorOne;
import static uk.gov.hmcts.reform.civil.utils.UserRoleUtils.isRespondentSolicitorTwo;

@Component
@RequiredArgsConstructor
public class RespondToQueryHelper {

    static final String QUERY_NOT_FOUND = "Matching parent query not found.";

    private final QueryManagementCamundaService runtimeService;
    private final CoreCaseUserService coreCaseUserService;

    public Map<String, String> addCustomProperties(Map<String, String> properties,
                                                   CaseData caseData,
                                                   String legalOrgNameOrPartyName,
                                                   boolean isLipOtherParty) {
        if (isLipOtherParty) {
            properties.put(PARTY_NAME, legalOrgNameOrPartyName);
            properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
        } else {
            properties.put(CLAIM_LEGAL_ORG_NAME_SPEC, legalOrgNameOrPartyName);
            properties.put(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString());
            properties.put(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData));
            properties.put(CASEMAN_REF, caseData.getLegacyCaseReference());
        }
        return properties;
    }

    public void addQueryDateProperty(Map<String, String> properties, CaseData caseData) {
        getOriginalQueryCreatedDate(caseData)
            .map(date -> formatLocalDate(date, DATE))
            .ifPresent(formattedDate -> properties.put(QUERY_DATE, formattedDate));
    }

    private Optional<LocalDate> getOriginalQueryCreatedDate(CaseData caseData) {
        if (caseData.getBusinessProcess() == null
            || caseData.getBusinessProcess().getProcessInstanceId() == null) {
            return Optional.empty();
        }

        QueryManagementVariables processVariables = runtimeService.getProcessVariables(
            caseData.getBusinessProcess().getProcessInstanceId());
        if (processVariables == null || processVariables.getQueryId() == null) {
            return Optional.empty();
        }

        CaseMessage responseQuery = getQueryById(caseData, processVariables.getQueryId());
        String parentQueryId = responseQuery.getParentId();
        CaseMessage parentQuery = getQueryById(caseData, parentQueryId);
        List<String> roles = getUserRoleForQuery(caseData, coreCaseUserService, parentQueryId);

        return Optional.ofNullable(getOriginalQueryCreatedDate(caseData, responseQuery, roles, parentQuery));
    }

    private LocalDate getOriginalQueryCreatedDate(CaseData caseData, CaseMessage responseQuery, List<String> roles,
                                                  CaseMessage parentQuery) {
        if (caseData.getQueries() != null) {
            return getLastRelatedQueryRaisedBySolicitorDate(caseData.getQueries(),
                                                            parentQuery, responseQuery
            );
        }
        if (isApplicantSolicitor(roles)) {
            return getLastRelatedQueryRaisedBySolicitorDate(caseData.getQmApplicantSolicitorQueries(),
                                                            parentQuery, responseQuery
            );
        } else if (isRespondentSolicitorOne(roles)) {
            return getLastRelatedQueryRaisedBySolicitorDate(caseData.getQmRespondentSolicitor1Queries(),
                                                            parentQuery, responseQuery
            );
        } else if (isRespondentSolicitorTwo(roles)) {
            return getLastRelatedQueryRaisedBySolicitorDate(caseData.getQmRespondentSolicitor2Queries(),
                                                            parentQuery, responseQuery
            );
        }
        return null;
    }

    private LocalDate getLastRelatedQueryRaisedBySolicitorDate(CaseQueriesCollection solicitorQueries,
                                                               CaseMessage parentQuery, CaseMessage responseQuery) {
        List<CaseMessage> caseMessages = unwrapElements(solicitorQueries.getCaseMessages());
        List<CaseMessage> queriesByUserWithMatchingParentId = caseMessages.stream()
            .filter(m -> responseQuery.getParentId().equals(m.getParentId())
                && m.getCreatedBy().equals(parentQuery.getCreatedBy())
                && m.getCreatedOn().isBefore(responseQuery.getCreatedOn()))
            .toList();
        CaseMessage latestQuery;
        if (!queriesByUserWithMatchingParentId.isEmpty()) {
            latestQuery =
                queriesByUserWithMatchingParentId.stream().max(Comparator.comparing(CaseMessage::getCreatedOn))
                    .orElse(null);
        } else {
            latestQuery = parentQuery;
        }
        if (latestQuery != null) {
            return latestQuery.getCreatedOn().toLocalDate();
        } else {
            throw new IllegalArgumentException(QUERY_NOT_FOUND);
        }
    }
}
