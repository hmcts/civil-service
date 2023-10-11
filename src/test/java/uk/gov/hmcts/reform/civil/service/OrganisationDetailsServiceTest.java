package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class OrganisationDetailsServiceTest {

    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private OrganisationDetailsService organisationDetailsService;

    private final Organisation organisation = Organisation.builder().name("test org").build();

    @Test
    void shouldReturnApplicantOrgNameWhenOrgNameExist() {
        when(organisationService.findOrganisationById(any())).thenReturn(Optional.ofNullable(organisation));
        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDraft()
            .legacyCaseReference("100MC001")
            .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                              .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                .organisationID("test org")
                                                                .build())
                                              .build())
            .build();

        caseData.getApplicantSolicitor1ClaimStatementOfTruth().setName("OrgClaimName");
        //When
        String organisationID = organisationDetailsService.getApplicantLegalOrganizationName(caseData);
        //Then
        assertThat(organisationID).isEqualTo("test org");
    }

    @Test
    void shouldGetApplicantSolicitor1ClaimStatementOfTruthName_whenNoOrgFound() {
        when(organisationService.findOrganisationById(any())).thenReturn(Optional.empty());
        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDraft()
            .legacyCaseReference("100MC001")
            .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                              .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                .organisationID("test org")
                                                                .build())
                                              .build())
            .build();
        caseData.getApplicantSolicitor1ClaimStatementOfTruth().setName("OrgClaimName");

        //When
        String organisationID = organisationDetailsService.getApplicantLegalOrganizationName(caseData);

        //Then
        assertThat(organisationID).isEqualTo("OrgClaimName");
    }

    @Test
    void shouldReturnRespondent1OrgNameWhenOrgNameExist() {
        when(organisationService.findOrganisationById(any())).thenReturn(Optional.ofNullable(organisation));
        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference("100MC001")
            .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                              .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                .organisationID("test org")
                                                                .build())
                                              .build())
            .build();
        //When
        String organisationID = organisationDetailsService.getRespondentLegalOrganizationName(caseData);
        //Then
        assertThat(organisationID).isEqualTo("test org");
    }

    @Test
    void shouldReturnNullForRespondent1OrgName_whenNoOrgFound() {
        when(organisationService.findOrganisationById(any())).thenReturn(Optional.empty());
        //Given
        CaseData caseData = CaseDataBuilder.builder()
            .legacyCaseReference("100MC001")
            .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                              .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder()
                                                                .organisationID("test org")
                                                                .build())
                                              .build())
            .build();
        //When
        String organisationID = organisationDetailsService.getRespondentLegalOrganizationName(caseData);
        //Then
        assertNull(organisationID);
    }
}
