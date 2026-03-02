package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementCamundaService;
import uk.gov.hmcts.reform.civil.service.querymanagement.QueryManagementVariables;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.QUERY_DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
class RespondToQueryHelperTest {

    @Mock
    private QueryManagementCamundaService runtimeService;
    @Mock
    private CoreCaseUserService coreCaseUserService;
    @Mock
    private OrganisationService organisationService;

    private RespondToQueryHelper helper;

    @BeforeEach
    void setUp() {
        helper = new RespondToQueryHelper(runtimeService, coreCaseUserService, organisationService);
    }

    @Test
    void shouldAddLipProperties() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        Map<String, String> properties = new HashMap<>();

        helper.addCustomProperties(properties, caseData, "John Smith", true);

        assertThat(properties)
            .containsEntry(PARTY_NAME, "John Smith")
            .containsEntry(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString())
            .doesNotContainKey(CLAIM_LEGAL_ORG_NAME_SPEC);
    }

    @Test
    void shouldAddLegalRepProperties() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        Map<String, String> properties = new HashMap<>();

        helper.addCustomProperties(properties, caseData, "Signer Name", false);

        assertThat(properties)
            .containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, "Signer Name")
            .containsEntry(CLAIM_REFERENCE_NUMBER, caseData.getCcdCaseReference().toString())
            .containsEntry(PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData))
            .containsEntry(CASEMAN_REF, caseData.getLegacyCaseReference())
            .doesNotContainKey(PARTY_NAME);
    }

    @Test
    void shouldAddLatestFollowUpQueryDate() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseData caseData = createCaseData(now);
        QueryManagementVariables queryManagementVariables = new QueryManagementVariables();
        queryManagementVariables.setQueryId("response-id");
        when(runtimeService.getProcessVariables("process-id"))
            .thenReturn(queryManagementVariables);
        when(coreCaseUserService.getUserCaseRoles(caseData.getCcdCaseReference().toString(), "LR"))
            .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.toString()));

        Map<String, String> properties = new HashMap<>();

        helper.addQueryDateProperty(properties, caseData);

        assertThat(properties).containsEntry(QUERY_DATE, formatLocalDate(now.minusDays(1).toLocalDate(), DATE));
    }

    @Test
    void shouldFallbackToParentQueryDateWhenNoFollowUp() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseQueriesCollection caseQueries = new CaseQueriesCollection();
        caseQueries.setCaseMessages(wrapElements(
            createCaseMessage("parent-id", "LR", now.minusDays(3), null),
            createCaseMessage("response-id", "admin", now, "parent-id")
        ));

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setQueries(caseQueries);
        caseData.setBusinessProcess(new BusinessProcess().setProcessInstanceId("process-id"));
        caseData.setCcdCaseReference(1234567890L);
        QueryManagementVariables queryManagementVariables = new QueryManagementVariables();
        queryManagementVariables.setQueryId("response-id");

        when(runtimeService.getProcessVariables("process-id"))
            .thenReturn(queryManagementVariables);
        when(coreCaseUserService.getUserCaseRoles(any(), any()))
            .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.toString()));

        Map<String, String> properties = new HashMap<>();

        helper.addQueryDateProperty(properties, caseData);

        assertThat(properties).containsEntry(QUERY_DATE, formatLocalDate(now.minusDays(3).toLocalDate(), DATE));
    }

    @Test
    void shouldIdentifyApplicantSolicitorAsRecipient() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseData caseData = createCaseData(now);
        QueryManagementVariables queryManagementVariables = new QueryManagementVariables();
        queryManagementVariables.setQueryId("response-id");
        when(runtimeService.getProcessVariables("process-id"))
            .thenReturn(queryManagementVariables);
        when(coreCaseUserService.getUserCaseRoles(caseData.getCcdCaseReference().toString(), "LR"))
            .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.toString()));

        assertThat(helper.shouldNotifyApplicantSolicitor(caseData)).isTrue();
        assertThat(helper.shouldNotifyRespondentSolicitorOne(caseData)).isFalse();
    }

    @Test
    void shouldIdentifyRespondentSolicitorOneAsRecipient() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseData caseData = createCaseData(now);
        QueryManagementVariables queryManagementVariables = new QueryManagementVariables();
        queryManagementVariables.setQueryId("response-id");
        when(runtimeService.getProcessVariables("process-id"))
            .thenReturn(queryManagementVariables);
        when(coreCaseUserService.getUserCaseRoles(caseData.getCcdCaseReference().toString(), "LR"))
            .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.toString()));

        assertThat(helper.shouldNotifyRespondentSolicitorOne(caseData)).isTrue();
        assertThat(helper.shouldNotifyApplicantSolicitor(caseData)).isFalse();
    }

    @Test
    void shouldIdentifyRespondentSolicitorTwoAsRecipient() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseData caseData = createCaseData(now);
        QueryManagementVariables queryManagementVariables = new QueryManagementVariables();
        queryManagementVariables.setQueryId("response-id");
        when(runtimeService.getProcessVariables("process-id"))
            .thenReturn(queryManagementVariables);
        when(coreCaseUserService.getUserCaseRoles(caseData.getCcdCaseReference().toString(), "LR"))
            .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORTWO.toString()));

        assertThat(helper.shouldNotifyRespondentSolicitorTwo(caseData)).isTrue();
        assertThat(helper.shouldNotifyApplicantSolicitor(caseData)).isFalse();
    }

    @Test
    void shouldIdentifyLipPartiesAsRecipients() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseData caseData = createCaseData(now);
        QueryManagementVariables queryManagementVariables = new QueryManagementVariables();
        queryManagementVariables.setQueryId("response-id");
        when(runtimeService.getProcessVariables("process-id"))
            .thenReturn(queryManagementVariables);

        when(coreCaseUserService.getUserCaseRoles(caseData.getCcdCaseReference().toString(), "LR"))
            .thenReturn(List.of(CaseRole.CLAIMANT.toString()));
        assertThat(helper.shouldNotifyLipClaimant(caseData)).isTrue();
        assertThat(helper.shouldNotifyLipDefendant(caseData)).isFalse();

        when(coreCaseUserService.getUserCaseRoles(caseData.getCcdCaseReference().toString(), "LR"))
            .thenReturn(List.of(CaseRole.DEFENDANT.toString()));
        assertThat(helper.shouldNotifyLipDefendant(caseData)).isTrue();
        assertThat(helper.shouldNotifyLipClaimant(caseData)).isFalse();
    }

    @Test
    void shouldNotifyLipClaimantWhenOtherPartyIsClaimant() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseData caseData = createCaseData(now);
        caseData.getApplicant1().setPartyEmail("claimant@example.com");
        caseData.getRespondent1().setPartyEmail("defendant@example.com");
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);
        QueryManagementVariables queryManagementVariables = new QueryManagementVariables();
        queryManagementVariables.setQueryId("response-id");
        when(runtimeService.getProcessVariables("process-id"))
            .thenReturn(queryManagementVariables);
        when(coreCaseUserService.getUserCaseRoles(caseData.getCcdCaseReference().toString(), "LR"))
            .thenReturn(List.of(CaseRole.DEFENDANT.toString()));

        assertThat(helper.shouldNotifyOtherPartyLipClaimant(caseData)).isTrue();
        assertThat(helper.shouldNotifyOtherPartyLipDefendant(caseData)).isFalse();
    }

    @Test
    void shouldNotifyLipDefendantWhenOtherPartyIsDefendant() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseData caseData = createCaseData(now);
        caseData.getApplicant1().setPartyEmail("claimant@example.com");
        caseData.getRespondent1().setPartyEmail("defendant@example.com");
        caseData.setApplicant1Represented(YesOrNo.NO);
        caseData.setRespondent1Represented(YesOrNo.NO);
        QueryManagementVariables queryManagementVariables = new QueryManagementVariables();
        queryManagementVariables.setQueryId("response-id");
        when(runtimeService.getProcessVariables("process-id"))
            .thenReturn(queryManagementVariables);
        when(coreCaseUserService.getUserCaseRoles(caseData.getCcdCaseReference().toString(), "LR"))
            .thenReturn(List.of(CaseRole.CLAIMANT.toString()));

        assertThat(helper.shouldNotifyOtherPartyLipDefendant(caseData)).isTrue();
        assertThat(helper.shouldNotifyOtherPartyLipClaimant(caseData)).isFalse();
    }

    @Test
    void shouldNotifyOtherPartyApplicantSolicitorWhenRespondentSolicitorRaisedQuery() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseData caseData = createCaseData(now);
        caseData.setApplicantSolicitor1UserDetails(new IdamUserDetails("appsol@example.com", "1"));
        caseData.setRespondentSolicitor1EmailAddress("resp1@example.com");
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1Represented(YesOrNo.YES);
        QueryManagementVariables variables = new QueryManagementVariables();
        variables.setQueryId("response-id");
        when(runtimeService.getProcessVariables("process-id"))
            .thenReturn(variables);
        when(coreCaseUserService.getUserCaseRoles(caseData.getCcdCaseReference().toString(), "LR"))
            .thenReturn(List.of(CaseRole.RESPONDENTSOLICITORONE.toString()));
        when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(new Organisation().setName("Org")));

        assertThat(helper.shouldNotifyOtherPartyApplicantSolicitor(caseData)).isTrue();
        assertThat(helper.shouldNotifyOtherPartyRespondentSolicitorOne(caseData)).isFalse();
    }

    @Test
    void shouldNotifyOtherPartyRespondentSolicitorsWhenApplicantRaisedQuery() {
        OffsetDateTime now = OffsetDateTime.now();
        CaseData caseData = createCaseData(now);
        caseData.setApplicantSolicitor1UserDetails(new IdamUserDetails("appsol@example.com", "1"));
        caseData.setRespondentSolicitor1EmailAddress("resp1@example.com");
        caseData.setRespondentSolicitor2EmailAddress("resp2@example.com");
        caseData.setRespondent2(PartyBuilder.builder().individual().build());
        caseData.setAddRespondent2(YesOrNo.YES);
        caseData.setRespondent2SameLegalRepresentative(YesOrNo.NO);
        caseData.setApplicant1Represented(YesOrNo.YES);
        caseData.setRespondent1Represented(YesOrNo.YES);
        QueryManagementVariables variables = new QueryManagementVariables();
        variables.setQueryId("response-id");
        when(runtimeService.getProcessVariables("process-id"))
            .thenReturn(variables);
        when(coreCaseUserService.getUserCaseRoles(caseData.getCcdCaseReference().toString(), "LR"))
            .thenReturn(List.of(CaseRole.APPLICANTSOLICITORONE.toString()));
        when(organisationService.findOrganisationById(any())).thenReturn(Optional.of(new Organisation().setName("Org")));

        assertThat(helper.shouldNotifyOtherPartyRespondentSolicitorOne(caseData)).isTrue();
        assertThat(helper.shouldNotifyOtherPartyRespondentSolicitorTwo(caseData)).isTrue();
        assertThat(helper.shouldNotifyOtherPartyApplicantSolicitor(caseData)).isFalse();
    }

    private CaseData createCaseData(OffsetDateTime now) {
        CaseQueriesCollection caseQueries = new CaseQueriesCollection();
        caseQueries.setCaseMessages(wrapElements(
            createCaseMessage("parent-id", "LR", now.minusDays(2), null),
            createCaseMessage("response-id", "admin", now, "parent-id"),
            createCaseMessage("follow-up", "LR", now.minusDays(1), "parent-id")
        ));

        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setQueries(caseQueries);
        caseData.setBusinessProcess(new BusinessProcess().setProcessInstanceId("process-id"));
        caseData.setCcdCaseReference(1234567890L);
        return caseData;
    }

    private CaseMessage createCaseMessage(String id, String createdBy, OffsetDateTime createdOn, String parentId) {
        CaseMessage caseMessage = new CaseMessage();
        caseMessage.setId(id);
        caseMessage.setCreatedBy(createdBy);
        caseMessage.setCreatedOn(createdOn);
        caseMessage.setParentId(parentId);
        return caseMessage;
    }
}
