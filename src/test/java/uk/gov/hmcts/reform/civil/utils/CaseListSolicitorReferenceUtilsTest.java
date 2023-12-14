package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllDefendantSolicitorReferences;
import static uk.gov.hmcts.reform.civil.utils.CaseListSolicitorReferenceUtils.getAllOrganisationPolicyReferences;

class CaseListSolicitorReferenceUtilsTest {

    @Nested
    class OrganisationReference {
        @Nested
        class OneDefendant {
            @Test
            void shouldReturnAllOrganisationReference() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimSubmitted()
                    .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                                      .orgPolicyReference("CLAIMANTREF1")
                                                      .build())
                    .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                       .orgPolicyReference("DEFENDANTREF1")
                                                       .build())
                    .build();

                assertThat(getAllOrganisationPolicyReferences(caseData))
                    .isEqualTo("CLAIMANTREF1, DEFENDANTREF1");
            }

            @Test
            void shouldReturnAllOrganisationReference_whenNoClaimantOrgReference() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimSubmitted()
                    .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                       .orgPolicyReference("DEFENDANTREF1")
                                                       .build())
                    .build();

                assertThat(getAllOrganisationPolicyReferences(caseData))
                    .isEqualTo("DEFENDANTREF1");
            }

            @Test
            void shouldReturnAllOrganisationReference_whenNoOrgReference() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimSubmitted()
                    .build();

                assertThat(getAllOrganisationPolicyReferences(caseData))
                    .isEqualTo("");
            }
        }

        @Nested
        class TwoDefendant {
            @Test
            void shouldReturnAllOrganisationReference() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimSubmitted()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .applicant1OrganisationPolicy(OrganisationPolicy.builder()
                                                      .orgPolicyReference("CLAIMANTREF1")
                                                      .build())
                    .respondent1OrganisationPolicy(OrganisationPolicy.builder()
                                                       .orgPolicyReference("DEFENDANTREF1")
                                                       .build())
                    .respondent2OrganisationPolicy(OrganisationPolicy.builder()
                                                       .orgPolicyReference("DEFENDANTREF2")
                                                       .build())
                    .build();

                assertThat(getAllOrganisationPolicyReferences(caseData))
                    .isEqualTo("CLAIMANTREF1, DEFENDANTREF1, DEFENDANTREF2");
            }

            @Test
            void shouldReturnAllOrganisationReference_whenNoOrgPolicy() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimSubmitted()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .build();

                assertThat(getAllOrganisationPolicyReferences(caseData))
                    .isEqualTo("");
            }

            @Test
            void shouldReturnAllOrganisationReference_whenUnregisteredAndNoOrgPolicy() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimDraft()
                    .multiPartyClaimTwoDefendantSolicitorsUnregistered()
                    .build();

                assertThat(getAllOrganisationPolicyReferences(caseData))
                    .isEqualTo("");
            }
        }
    }

    @Nested
    class DefendantSolicitorReference {
        @Nested
        class OneDefendant {
            @Test
            void shouldReturnAllDefendantSolicitorReference() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimSubmitted().build();

                assertThat(getAllDefendantSolicitorReferences(caseData))
                    .isEqualTo("6789");
            }
        }

        @Nested
        class TwoDefendant {
            @Test
            void shouldReturnAllDefendantSolicitorReference() {
                CaseData caseData = CaseDataBuilder.builder()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .atStateClaimSubmitted().build();

                assertThat(getAllDefendantSolicitorReferences(caseData))
                    .isEqualTo("6789, 01234");
            }

            @Test
            void shouldReturnAllDefendantSolicitorReference_whenNoReferencesExists() {
                CaseData caseData = CaseDataBuilder.builder()
                    .atStateClaimSubmitted()
                    .multiPartyClaimTwoDefendantSolicitors()
                    .removeSolicitorReferences().build();

                assertThat(getAllDefendantSolicitorReferences(caseData))
                    .isEqualTo("");
            }

            @Test
            void shouldReturnAllDefendantSolicitorReference_whenOneReference() {
                assertThat(getAllDefendantSolicitorReferences(null, "6789"))
                    .isEqualTo("6789");
            }

            @Test
            void shouldReturnAllDefendantSolicitorReference_whenNoReferences() {
                assertThat(getAllDefendantSolicitorReferences(null, ""))
                    .isEqualTo("");
            }
        }

    }

}
