package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.hearingvalues.IndividualDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.OrganisationDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.PartyDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.RelatedPartiesModel;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.hearings.EntityRoleService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyType.IND;
import static uk.gov.hmcts.reform.civil.enums.hearing.PartyType.ORG;
import static uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingsPartyMapper.buildPartyObjectForHearingPayload;

@ExtendWith(SpringExtension.class)
public class HearingsPartyMapperTest {

    private static final String CLAIMANT_ROLE = "CLAI";
    private static final String DEFENDANT_ROLE = "DEFE";
    private static final String LITIGATION_FRIEND_ROLE = "LIFR";
    private static final String LEGAL_REP_ROLE = "LGRP";
    private static final String EXPERT_ROLE = "EXPR";
    private static final String WITNESS_ROLE = "WITN";

    private static final String FULL_NAME = "%s %s";

    private static final String APPLICANT_ORG_ID = "QWERTY A";
    private static final String RESPONDENT_ONE_ORG_ID = "QWERTY R";
    private static final String RESPONDENT_TWO_ORG_ID = "QWERTY R2";

    private static final String APPLICANT_ORG_NAME = "Applicant Org name";
    private static final String RESPONDENT_ONE_ORG_NAME = "Respondent 1 Org name";
    private static final String RESPONDENT_TWO_ORG_NAME = "Respondent 2 Org name";

    @Mock
    private OrganisationService organisationService;

    @Mock
    private EntityRoleService entityRoleService;

    private final String auth = "";

    @BeforeEach
    void setUp() {
        when(organisationService.findOrganisationById(APPLICANT_ORG_ID))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(APPLICANT_ORG_NAME)
                                        .build()));
        when(organisationService.findOrganisationById(RESPONDENT_ONE_ORG_ID))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(RESPONDENT_ONE_ORG_NAME)
                                        .build()));
        when(organisationService.findOrganisationById(RESPONDENT_TWO_ORG_ID))
            .thenReturn(Optional.of(Organisation.builder()
                                        .name(RESPONDENT_TWO_ORG_NAME)
                                        .build()));
    }

    @Test
    void shouldBuildIndividualDetails_whenClaimantIsIndividualRespondentSoleTrader() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued()
            .build();

        PartyDetailsModel applicantPartyDetails = buildExpectedIndividualPartyDetails(
            caseData.getApplicant1().getIndividualFirstName(),
            caseData.getApplicant1().getIndividualLastName(),
            caseData.getApplicant1().getPartyName(),
            CLAIMANT_ROLE,
            caseData.getApplicant1().getPartyEmail(),
            caseData.getApplicant1().getPartyPhone()
        );

        PartyDetailsModel applicantSolicitorParty = buildExpectedOrganisationPartyObject(
            APPLICANT_ORG_NAME,
            LEGAL_REP_ROLE,
            APPLICANT_ORG_ID
        );

        PartyDetailsModel respondentPartyDetails = buildExpectedIndividualPartyDetails(
            caseData.getRespondent1().getSoleTraderFirstName(),
            caseData.getRespondent1().getSoleTraderLastName(),
            caseData.getRespondent1().getPartyName(),
            DEFENDANT_ROLE,
            caseData.getRespondent1().getPartyEmail(),
            caseData.getRespondent1().getPartyPhone()
        );

        PartyDetailsModel respondentSolicitorParty = buildExpectedOrganisationPartyObject(
            RESPONDENT_ONE_ORG_NAME,
            LEGAL_REP_ROLE,
            RESPONDENT_ONE_ORG_ID
        );

        List<PartyDetailsModel> expected = new ArrayList<>();
        expected.add(applicantPartyDetails);
        expected.add(applicantSolicitorParty);
        expected.add(respondentPartyDetails);
        expected.add(respondentSolicitorParty);

        List<PartyDetailsModel> actualPartyDetailsModel = buildPartyObjectForHearingPayload(
            caseData,
            organisationService,
            entityRoleService,
            auth
        );
        assertThat(actualPartyDetailsModel).isEqualTo(expected);
    }

    private PartyDetailsModel buildExpectedIndividualPartyDetails(String firstName, String lastName,
                                                                  String partyName, String partyRole,
                                                                  String email, String phone) {
        IndividualDetailsModel individualDetails = IndividualDetailsModel.builder()
            .firstName(firstName)
            .lastName(lastName)
            .interpreterLanguage(null)
            .reasonableAdjustments(null)
            .vulnerableFlag(false)
            .vulnerabilityDetails(null)
            .hearingChannelEmail(List.of(email))
            .hearingChannelPhone(List.of(phone))
            .relatedParties(List.of(RelatedPartiesModel.builder().build()))
            .custodyStatus(null)
            .build();

        return PartyDetailsModel.builder()
            .partyID("")
            .partyType(IND)
            .partyName(partyName)
            .partyRole(partyRole)
            .individualDetails(individualDetails)
            .organisationDetails(null)
            .unavailabilityDOW(null)
            .unavailabilityRange(null)
            .hearingSubChannel(null)
            .build();
    }

    private PartyDetailsModel buildExpectedOrganisationPartyObject(String name,
                                                                 String partyRole,
                                                                 String cftOrganisationID) {
        OrganisationDetailsModel organisationDetails = OrganisationDetailsModel.builder()
            .name(name)
            .organisationType(null)
            .cftOrganisationID(cftOrganisationID)
            .build();

        return PartyDetailsModel.builder()
            .partyID("")
            .partyType(ORG)
            .partyName(name)
            .partyRole(partyRole)
            .individualDetails(null)
            .organisationDetails(organisationDetails)
            .unavailabilityDOW(null)
            .unavailabilityRange(null)
            .hearingSubChannel(null)
            .build();
    }
}
