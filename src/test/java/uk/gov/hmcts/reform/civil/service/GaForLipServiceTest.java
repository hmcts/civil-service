package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GaForLipServiceTest {

    @Mock
    FeatureToggleService featureToggleService;
    @InjectMocks
    GaForLipService gaForLipService;

    @BeforeEach
    void setUp() {
        given(featureToggleService.isGaForLipsEnabled()).willReturn(true);
    }

    @Test
    void shouldReturnApplicantEmailWhenUserDetailsIsNotPresent() {
        CaseData civilCaseData = CaseData.builder()
            .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                .id("123")
                                                .email("applicantEmail@test.com")
                                                .build()).build();
        assertEquals("applicantEmail@test.com", gaForLipService.getApplicant1Email(civilCaseData));
    }

    @Test
    void shouldReturnApplicantEmailWhenUserDetailsIsPresent() {
        CaseData civilCaseData = CaseData.builder()
            .claimantUserDetails(IdamUserDetails.builder()
                                                .id("123")
                                                .email("applicantEmail@test.com")
                                                .build()).build();
        assertEquals("applicantEmail@test.com", gaForLipService.getApplicant1Email(civilCaseData));
    }

    @Test
    void shouldReturnNullApplicantEmailWhenUserDetailsAndSolicitor1UserDetailsAreNotPresent() {
        CaseData civilCaseData = CaseData.builder().build();
        assertNull(gaForLipService.getApplicant1Email(civilCaseData));
    }

    @Test
    void shouldReturnDefendantEmailWhenUserDetailsIsNotPresent() {
        CaseData civilCaseData = CaseData.builder()
            .respondentSolicitor1EmailAddress("defendantEmail@test.com").build();
        assertEquals("defendantEmail@test.com", gaForLipService.getDefendant1Email(civilCaseData));
    }

    @Test
    void shouldReturnDefendantEmailWhenUserDetailsIsPresent() {
        CaseData civilCaseData = CaseData.builder()
            .defendantUserDetails(IdamUserDetails.builder()
                                     .id("123")
                                     .email("defendantEmail@test.com")
                                     .build()).build();
        assertEquals("defendantEmail@test.com", gaForLipService.getDefendant1Email(civilCaseData));
    }

    @Test
    void shouldReturnNullForDefendantEmailWhenUserDetailsAndSolicitor1UserDetailsAreNotPresent() {
        CaseData civilCaseData = CaseData.builder().build();
        assertNull(gaForLipService.getDefendant1Email(civilCaseData));
    }

    @Test
    void shouldReturnAnyTrue_app_is_welsh() {
        CaseData caseData =
            CaseData.builder().parentClaimantIsApplicant(YesOrNo.YES).applicantBilingualLanguagePreferenceGA(YesOrNo.YES)
                .build();
        assertThat(gaForLipService.anyWelsh(caseData)).isTrue();
    }

    @Test
    void shouldReturnAnyTrue_resp_is_welsh() {
        CaseData caseData =
            CaseData.builder().parentClaimantIsApplicant(YesOrNo.YES).respondentBilingualLanguagePreferenceGA(YesOrNo.YES)
                .build();
        assertThat(gaForLipService.anyWelsh(caseData)).isTrue();
    }

    @Test
    void shouldReturnAnyFalse_nobody_is_welsh() {
        CaseData caseData = CaseData.builder().parentClaimantIsApplicant(YesOrNo.YES).build();
        assertThat(gaForLipService.anyWelsh(caseData)).isFalse();
    }

    @Test
    void shouldReturnNoticeTrue_app_is_welsh() {
        CaseData caseData = CaseData.builder()
            .applicantBilingualLanguagePreferenceGA(YesOrNo.YES)
            .parentClaimantIsApplicant(YesOrNo.YES)
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YesOrNo.YES).build())
            .build();
        assertThat(gaForLipService.anyWelshNotice(caseData)).isTrue();
    }

    @Test
    void shouldReturnNoticeTrue_resp_is_welsh() {
        CaseData caseData = CaseData.builder()
            .parentClaimantIsApplicant(YesOrNo.YES)
            .respondentBilingualLanguagePreferenceGA(YesOrNo.YES)
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YesOrNo.YES).build())
            .build();
        assertThat(gaForLipService.anyWelshNotice(caseData)).isTrue();
    }

    @Test
    void shouldReturnNoticeFalse_nobody_is_welsh() {
        CaseData caseData = CaseData.builder()
            .parentClaimantIsApplicant(YesOrNo.YES)
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YesOrNo.YES).build())
            .build();
        assertThat(gaForLipService.anyWelshNotice(caseData)).isFalse();
    }

    @Test
    void shouldReturnWithoutNoticeFalse_resp_is_welsh() {
        CaseData caseData = CaseData.builder()
            .parentClaimantIsApplicant(YesOrNo.YES)
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YesOrNo.NO).build())
            .build();
        assertThat(gaForLipService.anyWelshNotice(caseData)).isFalse();
    }
}
