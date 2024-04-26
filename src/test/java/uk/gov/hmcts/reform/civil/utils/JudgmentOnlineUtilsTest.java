package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.applicant2Present;
import static uk.gov.hmcts.reform.civil.utils.JudgmentOnlineUtils.areRespondentLegalOrgsEqual;
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
        if(applicant2Present) {
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
        if(respondent2Present) {
            caseDataBuilder.addRespondent2(YesOrNo.YES);
        } else {
            caseDataBuilder.build();
        }

        assertThat(respondent2Present(caseDataBuilder.build())).isEqualTo(respondent2Present);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testShouldCheckAreRespondentLegalOrgsEqual(boolean sameLegalOrgs) {

        organisationService = mock(OrganisationService.class);

        uk.gov.hmcts.reform.civil.prd.model.Organisation testOrg1 = uk.gov.hmcts.reform.civil.prd.model.Organisation.builder().organisationIdentifier("123").build();
        uk.gov.hmcts.reform.civil.prd.model.Organisation testOrg2 = uk.gov.hmcts.reform.civil.prd.model.Organisation.builder().organisationIdentifier("123").build();

        when(organisationService.findOrganisationById("1234"))
            .thenReturn(Optional.of(testOrg1));

        when(organisationService.findOrganisationById("3456"))
            .thenReturn(Optional.of(testOrg2));

        OrganisationPolicy organisation1Policy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("1234").build()).build();

        OrganisationPolicy organisation2Policy = OrganisationPolicy.builder()
            .organisation(Organisation.builder().organisationID("3456").build()).build();

        when(getOrganisationByPolicy(organisation1Policy, organisationService))
            .thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder().organisationIdentifier("1234").build()));

        when(getOrganisationByPolicy(organisation2Policy, organisationService))
            .thenReturn(Optional.of(uk.gov.hmcts.reform.civil.prd.model.Organisation.builder().organisationIdentifier("3456").build()));

        CaseDataBuilder caseDataBuilder = CaseDataBuilder.builder()
            .atStateClaimIssued1v2AndBothDefendantsDefaultJudgment()
            .respondent1OrganisationPolicy(organisation1Policy)
            .respondent2OrganisationPolicy(sameLegalOrgs ? organisation1Policy : organisation2Policy);


        assertThat(areRespondentLegalOrgsEqual(caseDataBuilder.build(), organisationService)).isEqualTo(sameLegalOrgs);
    }


}
