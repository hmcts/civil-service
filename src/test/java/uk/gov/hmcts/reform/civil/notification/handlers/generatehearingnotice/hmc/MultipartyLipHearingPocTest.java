package uk.gov.hmcts.reform.civil.notification.handlers.generatehearingnotice.hmc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.hearing.PartyType;
import uk.gov.hmcts.reform.civil.helpers.hearingsmappings.HearingsPartyMapper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.hearingvalues.PartyDetailsModel;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.hearings.HearingFeesService;
import uk.gov.hmcts.reform.civil.service.hearingnotice.HearingNoticeCamundaService;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
class MultipartyLipHearingPocTest {

    private static final String DEFENDANT_ROLE = "DEFE";
    private static final String LEGAL_REP_ROLE = "LGRP";
    private static final String RESPONDENT_ONE_PARTY_ID = "res-1-party-id";
    private static final String RESPONDENT_TWO_PARTY_ID = "res-2-party-id";

    enum Recipient { CLAIMANT, DEFENDANT_ONE, DEFENDANT_TWO }

    enum Variant {
        A_LR_V_TWO_LIPS(true, true, false),
        B_LR_V_LIP_ONE_AND_LR_TWO(true, false, false),
        C_LR_V_LR_ONE_AND_LIP_TWO(false, true, false),
        D_LR_V_LIP_INDIVIDUAL_AND_LIP_COMPANY(true, true, true);

        private final boolean defendantOneLip;
        private final boolean defendantTwoLip;
        private final boolean defendantTwoCompany;

        Variant(boolean defendantOneLip, boolean defendantTwoLip, boolean defendantTwoCompany) {
            this.defendantOneLip = defendantOneLip;
            this.defendantTwoLip = defendantTwoLip;
            this.defendantTwoCompany = defendantTwoCompany;
        }

        int representedSides() {
            return 1 + (defendantOneLip ? 0 : 1) + (defendantTwoLip ? 0 : 1);
        }
    }

    @Mock
    private OrganisationService organisationService;
    @Mock
    private NotificationsProperties notificationsProperties;
    @Mock
    private HearingNoticeCamundaService camundaService;
    @Mock
    private HearingFeesService hearingFeesService;

    private Map<Recipient, List<EmailDTOGenerator<CaseData>>> hearingNoticeEmailGeneratorsByRecipient;

    @BeforeEach
    void setUp() {
        lenient().when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(new Organisation().setName("Legal Rep Org")));

        hearingNoticeEmailGeneratorsByRecipient = Map.of(
            Recipient.CLAIMANT, List.of(
                new GenerateHearingNoticeHMCAppSolEmailDTOGenerator(
                    notificationsProperties, organisationService, camundaService, hearingFeesService),
                new GenerateHearingNoticeHMCClaimantEmailDTOGenerator(notificationsProperties, camundaService)),
            Recipient.DEFENDANT_ONE, List.of(
                new GenerateHearingNoticeHMCRespSolOneEmailDTOGenerator(
                    notificationsProperties, camundaService, organisationService),
                new GenerateHearingNoticeHMCDefendantEmailDTOGenerator(notificationsProperties, camundaService)),
            Recipient.DEFENDANT_TWO, List.of(
                new GenerateHearingNoticeHMCRespSolTwoEmailDTOGenerator(
                    notificationsProperties, camundaService, organisationService))
        );
    }

    @ParameterizedTest
    @EnumSource(Variant.class)
    void hearingPayloadShouldSendBothDefendantsWithoutLegalRepOrgsForLips(Variant variant) {
        CaseData caseData = buildCase(variant);

        List<PartyDetailsModel> parties =
            HearingsPartyMapper.buildPartyObjectForHearingPayload(caseData, organisationService);

        List<String> defendantPartyIds = parties.stream()
            .filter(party -> DEFENDANT_ROLE.equals(party.getPartyRole()))
            .map(PartyDetailsModel::getPartyID)
            .toList();
        assertThat(defendantPartyIds).contains(RESPONDENT_ONE_PARTY_ID, RESPONDENT_TWO_PARTY_ID);

        long legalRepOrgs = parties.stream()
            .filter(party -> LEGAL_REP_ROLE.equals(party.getPartyRole()) && PartyType.ORG == party.getPartyType())
            .count();
        assertThat(legalRepOrgs).isEqualTo(variant.representedSides());

        if (variant.defendantTwoCompany) {
            PartyDetailsModel defendantTwo = parties.stream()
                .filter(party -> RESPONDENT_TWO_PARTY_ID.equals(party.getPartyID()))
                .findFirst()
                .orElseThrow();
            assertThat(defendantTwo.getPartyType()).isEqualTo(PartyType.ORG);
        }
    }

    @ParameterizedTest
    @EnumSource(Variant.class)
    void hearingNoticeEmailShouldReachEveryParty(Variant variant) {
        CaseData caseData = buildCase(variant);

        Set<Recipient> notified = hearingNoticeEmailGeneratorsByRecipient.entrySet().stream()
            .filter(entry -> entry.getValue().stream().anyMatch(generator -> generator.getShouldNotify(caseData)))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());

        assertThat(notified)
            .as("hearing notice email recipients for %s", variant)
            .containsExactlyInAnyOrderElementsOf(EnumSet.allOf(Recipient.class));
    }

    private CaseData buildCase(Variant variant) {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoDefendantSolicitors()
            .atStateApplicantRespondToDefenceAndProceed(ONE_V_TWO_TWO_LEGAL_REP)
            .build();
        caseData.getRespondent1().setPartyID(RESPONDENT_ONE_PARTY_ID).setPartyEmail("defendant1@example.com");
        caseData.getRespondent2().setPartyID(RESPONDENT_TWO_PARTY_ID).setPartyEmail("defendant2@example.com");

        if (variant.defendantOneLip) {
            caseData.setRespondent1Represented(NO)
                .setRespondent1OrgRegistered(null)
                .setRespondentSolicitor1EmailAddress(null)
                .setRespondent1OrganisationPolicy(
                    new OrganisationPolicy().setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]"));
        }
        if (variant.defendantTwoLip) {
            caseData.setRespondent2Represented(NO)
                .setRespondent2OrgRegistered(null)
                .setRespondentSolicitor2EmailAddress(null)
                .setRespondent2OrganisationPolicy(
                    new OrganisationPolicy().setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]"));
        }
        if (variant.defendantTwoCompany) {
            caseData.setRespondent2(new Party()
                                        .setType(Party.Type.COMPANY)
                                        .setCompanyName("Defendant Two Ltd")
                                        .setPartyID(RESPONDENT_TWO_PARTY_ID)
                                        .setPartyEmail("defendant2@example.com"));
        }
        return caseData;
    }
}
