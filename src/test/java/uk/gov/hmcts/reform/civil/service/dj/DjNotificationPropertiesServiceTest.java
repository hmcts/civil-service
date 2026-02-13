package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class DjNotificationPropertiesServiceTest {

    @InjectMocks
    private DjNotificationPropertiesService service;

    @Mock
    private OrganisationService organisationService;
    @Mock
    private NotificationsSignatureConfiguration configuration;
    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        when(featureToggleService.isPublicQueryManagementEnabled(org.mockito.ArgumentMatchers.any()))
            .thenReturn(true);
        when(configuration.getHmctsSignature()).thenReturn("HMCTS");
        when(configuration.getPhoneContact()).thenReturn("0300");
        when(configuration.getOpeningHours()).thenReturn("9am-5pm");
        when(configuration.getWelshHmctsSignature()).thenReturn("Llys");
        when(configuration.getWelshPhoneContact()).thenReturn("0301");
        when(configuration.getWelshOpeningHours()).thenReturn("9yb-5yh");
        when(configuration.getRaiseQueryLr()).thenReturn("lr-query@justice.gov.uk");
        when(configuration.getSpecUnspecContact()).thenReturn("spec-unspec@justice.gov.uk");
        when(configuration.getLipContactEmail()).thenReturn("lip@justice.gov.uk");
        when(configuration.getLipContactEmailWelsh()).thenReturn("lip-cy@justice.gov.uk");
        when(configuration.getRaiseQueryLip()).thenReturn("lip-query@justice.gov.uk");
        when(configuration.getRaiseQueryLipWelsh()).thenReturn("lip-query-cy@justice.gov.uk");
        when(configuration.getCnbcContact()).thenReturn("cnbc@justice.gov.uk");
    }

    @Test
    void shouldBuildClaimantPropertiesUsingOrganisationName() {
        uk.gov.hmcts.reform.ccd.model.Organisation ccdOrganisation = new uk.gov.hmcts.reform.ccd.model.Organisation();
        ccdOrganisation.setOrganisationID("Org1");
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .ccdCaseReference(1594901956117591L)
            .legacyCaseReference("000DC001")
            .applicant1(PartyBuilder.builder().individual().build())
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(ccdOrganisation))
            .build();
        Organisation applicantOrganisation = new Organisation();
        applicantOrganisation.setName("Applicant Org");
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(applicantOrganisation));

        Map<String, String> properties = service.buildClaimantProperties(caseData);

        assertThat(properties)
            .containsEntry("LegalOrgName", "Applicant Org")
            .containsEntry("claimnumber", caseData.getCcdCaseReference().toString())
            .containsEntry("casemanRef", caseData.getLegacyCaseReference())
            .containsEntry("hmctsSignature", "HMCTS");
    }

    @Test
    void shouldFallbackToPartyNameWhenOrganisationMissing() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .ccdCaseReference(1594901956117591L)
            .legacyCaseReference("000DC001")
            .respondent1(PartyBuilder.builder().individual().build())
            .build();

        Map<String, String> properties = service.buildDefendant1Properties(caseData);

        assertThat(properties).containsEntry("LegalOrgName", caseData.getRespondent1().getPartyName());
    }

    @Test
    void shouldBuildDefendant2PropertiesUsingOrganisationPolicy() {
        uk.gov.hmcts.reform.ccd.model.Organisation ccdOrganisation = new uk.gov.hmcts.reform.ccd.model.Organisation();
        ccdOrganisation.setOrganisationID("Org2");
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .ccdCaseReference(1594901956117591L)
            .legacyCaseReference("000DC001")
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent2OrganisationPolicy(new OrganisationPolicy().setOrganisation(ccdOrganisation))
            .build();
        Organisation respondentOrganisation = new Organisation();
        respondentOrganisation.setName("Def Two Org");
        when(organisationService.findOrganisationById(anyString()))
            .thenReturn(Optional.of(respondentOrganisation));

        Map<String, String> properties = service.buildDefendant2Properties(caseData);

        assertThat(properties).containsEntry("LegalOrgName", "Def Two Org");
    }

    @Test
    void shouldFallbackToRespondent1OrganisationWhenSameSolicitor() {
        uk.gov.hmcts.reform.ccd.model.Organisation ccdOrganisation = new uk.gov.hmcts.reform.ccd.model.Organisation();
        ccdOrganisation.setOrganisationID("OrgShared");
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .ccdCaseReference(1594901956117591L)
            .legacyCaseReference("000DC001")
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent2(PartyBuilder.builder().individual().build())
            .respondent1OrganisationPolicy(new OrganisationPolicy().setOrganisation(ccdOrganisation))
            .build();

        Organisation sharedOrganisation = new Organisation();
        sharedOrganisation.setName("Shared Org");
        when(organisationService.findOrganisationById("OrgShared"))
            .thenReturn(Optional.of(sharedOrganisation));

        Map<String, String> properties = service.buildDefendant2Properties(caseData);

        assertThat(properties).containsEntry("LegalOrgName", "Shared Org");
    }

}
