package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.prd.model.ContactInformation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.applicant2Present;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.areRespondentLegalOrgsEqual;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getAddress;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.getOrganisationByPolicy;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.respondent2Present;

class JudgmentOnlineUtilsTest {

    private OrganisationService organisationService;

    @Test
    void testShouldGetOrganisationByPolicy() {
        organisationService = mock(OrganisationService.class);

        uk.gov.hmcts.reform.civil.prd.model.Organisation testOrg = uk.gov.hmcts.reform.civil.prd.model.Organisation.builder().organisationIdentifier("123").build();

        when(organisationService.findOrganisationById("1234"))
            .thenReturn(Optional.of(testOrg));

        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("1234").build()).build();

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
            .respondent1OrganisationPolicy(organisationPolicy)
            .build();

        assertThat(getOrganisationByPolicy(caseData.getRespondent1OrganisationPolicy(), organisationService).get()).isEqualTo(testOrg);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testShouldCheckApplicant2Present(boolean applicant2Present) {
        CaseDataBuilder caseDataBuilder = CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment();
        if (applicant2Present) {
            caseDataBuilder.addApplicant2(YesOrNo.YES);
        } else {
            caseDataBuilder.build();
        }

        assertThat(applicant2Present(caseDataBuilder.build())).isEqualTo(applicant2Present);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testShouldCheckRespondent2Present(boolean respondent2Present) {
        CaseDataBuilder caseDataBuilder = CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment();
        if (respondent2Present) {
            caseDataBuilder.addRespondent2(YesOrNo.YES);
        } else {
            caseDataBuilder.build();
        }

        assertThat(respondent2Present(caseDataBuilder.build())).isEqualTo(respondent2Present);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testShouldCheckAreRespondentLegalOrgsEqual(boolean sameLegalOrgs) {
        OrganisationPolicy organisation1Policy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("1234").build()).build();

        OrganisationPolicy organisation2Policy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("3456").build()).build();

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
            .respondent2(Party.builder().partyName("Respondent2 name").type(Party.Type.INDIVIDUAL).build())
            .respondent1OrganisationPolicy(organisation1Policy)
            .respondent2OrganisationPolicy(sameLegalOrgs ? organisation1Policy : organisation2Policy)
            .build();

        assertThat(areRespondentLegalOrgsEqual(caseData)).isEqualTo(sameLegalOrgs);
    }

    @Test
    void testShouldHandleNullValues() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
            .addApplicant2(null)
            .addRespondent2(null)
            .respondent1OrganisationPolicy(null)
            .respondent2OrganisationPolicy(null)
            .build();

        assertThat(applicant2Present(caseData)).isFalse();
        assertThat(respondent2Present(caseData)).isFalse();
        assertThat(areRespondentLegalOrgsEqual(caseData)).isFalse();
    }

    @Test
    void testShouldReturnAddress() {

        ContactInformation contact =  ContactInformation.builder().addressLine1("Test").country(
            "Test").build();
        Address address = Address.builder().addressLine1("Test").country("Test").build();
        assertThat(getAddress(contact).getAddressLine1()).isEqualTo(address.getAddressLine1());
        assertThat(getAddress(contact).getCountry()).isEqualTo(address.getCountry());
    }
}
