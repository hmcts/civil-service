package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
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
class OtherPartyQueryResponseRespSolTwoEmailDTOGeneratorTest {

    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private OtherPartyQueryResponseHelper helper;

    @InjectMocks
    private OtherPartyQueryResponseRespSolTwoEmailDTOGenerator generator;

    @Test
    void shouldReturnTemplateAndReference() {
        when(notificationsProperties.getQueryLrPublicResponseReceived()).thenReturn("template-id");

        assertThat(generator.getEmailTemplateId(CaseDataBuilder.builder().atStateClaimIssued().build()))
            .isEqualTo("template-id");
        assertThat(generator.getReferenceTemplate())
            .isEqualTo("other-party-response-to-query-notification-%s");
    }

    @Test
    void shouldPopulateRespondentTwoOrganisation() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        caseData.setRespondent2(createParty("Respondent Two"));
        Organisation organisation = new Organisation().setOrganisationID("RESP2");
        caseData.setRespondent2OrganisationPolicy(new OrganisationPolicy().setOrganisation(organisation));
        uk.gov.hmcts.reform.civil.prd.model.Organisation prdOrganisation =
            new uk.gov.hmcts.reform.civil.prd.model.Organisation();
        prdOrganisation.setName("Respondent 2 Org");
        when(organisationService.findOrganisationById(anyString())).thenReturn(Optional.of(prdOrganisation));
        Map<String, String> properties = new HashMap<>();

        generator.addCustomProperties(properties, caseData);

        verify(helper).addCustomProperties(properties, caseData, "Respondent 2 Org", false);
    }

    private Party createParty(String name) {
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        party.setIndividualFirstName(name);
        party.setIndividualLastName("Test");
        party.setPartyName(name);
        return party;
    }
}
