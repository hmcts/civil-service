package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RespondToQueryRespSolTwoEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private RespondToQueryHelper respondToQueryHelper;

    @InjectMocks
    private RespondToQueryRespSolTwoEmailDTOGenerator generator;

    @Test
    void shouldReturnSolicitorTemplateId() {
        String templateId = "template-id";
        when(notificationsProperties.getQueryLrPublicResponseReceived()).thenReturn(templateId);

        assertThat(generator.getEmailTemplateId(CaseDataBuilder.builder().atStateClaimIssued().build())).isEqualTo(templateId);
    }

    @Test
    void shouldReturnReferenceTemplate() {
        assertThat(generator.getReferenceTemplate()).isEqualTo("response-to-query-notification-%s");
    }

    @Test
    void shouldPopulateOrganisationNameForRespondentTwo() {
        CaseData caseData = createBaseCaseData();
        caseData.setRespondent2OrganisationPolicy(organisationPolicy());
        uk.gov.hmcts.reform.civil.prd.model.Organisation organisation =
            new uk.gov.hmcts.reform.civil.prd.model.Organisation();
        organisation.setName("Resp 2 Org");
        when(organisationService.findOrganisationById(anyString())).thenReturn(Optional.of(organisation));
        Map<String, String> properties = new HashMap<>();

        when(respondToQueryHelper.addCustomProperties(properties, caseData, "Resp 2 Org", false))
            .thenReturn(properties);

        generator.addCustomProperties(properties, caseData);

        verify(respondToQueryHelper).addCustomProperties(properties, caseData, "Resp 2 Org", false);
        verify(respondToQueryHelper).addQueryDateProperty(properties, caseData);
    }

    private CaseData createBaseCaseData() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setApplicant1(createParty("Applicant"));
        caseData.setRespondent1(createParty("Respondent One"));
        caseData.setRespondent2(createParty("Respondent Two"));
        StatementOfTruth statementOfTruth = new StatementOfTruth();
        statementOfTruth.setName("Signer");
        caseData.setApplicantSolicitor1ClaimStatementOfTruth(statementOfTruth);
        return caseData;
    }

    private Party createParty(String name) {
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        party.setIndividualFirstName(name);
        party.setIndividualLastName("Test");
        party.setPartyName(name);
        return party;
    }

    private OrganisationPolicy organisationPolicy() {
        Organisation organisation = new Organisation().setOrganisationID("RESP-2");
        return new OrganisationPolicy().setOrganisation(organisation);
    }
}
