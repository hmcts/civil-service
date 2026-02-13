package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class GaForLipServiceTest {

    @InjectMocks
    private GaForLipService gaForLipService;

    @Test
    void shouldReturnApplicantEmailWhenUserDetailsIsNotPresent() {
        GeneralApplicationCaseData civilCaseData = GeneralApplicationCaseData.builder()
            .applicantSolicitor1UserDetails(IdamUserDetails.builder()
                                                .id("123")
                                                .email("applicantEmail@test.com")
                                                .build()).build();
        assertEquals("applicantEmail@test.com", gaForLipService.getApplicant1Email(civilCaseData));
    }

    @Test
    void shouldReturnApplicantEmailWhenUserDetailsIsPresent() {
        GeneralApplicationCaseData civilCaseData = GeneralApplicationCaseData.builder()
            .claimantUserDetails(IdamUserDetails.builder()
                                     .id("123")
                                     .email("applicantEmail@test.com")
                                     .build()).build();
        assertEquals("applicantEmail@test.com", gaForLipService.getApplicant1Email(civilCaseData));
    }

    @Test
    void shouldReturnNullApplicantEmailWhenUserDetailsAndSolicitor1UserDetailsAreNotPresent() {
        GeneralApplicationCaseData civilCaseData = GeneralApplicationCaseData.builder().build();
        assertNull(gaForLipService.getApplicant1Email(civilCaseData));
    }

    @Test
    void shouldReturnDefendantEmailWhenUserDetailsIsNotPresent() {
        GeneralApplicationCaseData civilCaseData = GeneralApplicationCaseData.builder()
            .respondentSolicitor1EmailAddress("defendantEmail@test.com").build();
        assertEquals("defendantEmail@test.com", gaForLipService.getDefendant1Email(civilCaseData));
    }

    @Test
    void shouldReturnDefendantEmailWhenUserDetailsIsPresent() {
        GeneralApplicationCaseData civilCaseData = GeneralApplicationCaseData.builder()
            .defendantUserDetails(IdamUserDetails.builder()
                                      .id("123")
                                      .email("defendantEmail@test.com")
                                      .build()).build();
        assertEquals("defendantEmail@test.com", gaForLipService.getDefendant1Email(civilCaseData));
    }

    @Test
    void shouldReturnNullForDefendantEmailWhenUserDetailsAndSolicitor1UserDetailsAreNotPresent() {
        GeneralApplicationCaseData civilCaseData = GeneralApplicationCaseData.builder().build();
        assertNull(gaForLipService.getDefendant1Email(civilCaseData));
    }

    @Test
    void shouldReturnAnyTrue_app_is_welsh() {
        GeneralApplicationCaseData caseData =
            GeneralApplicationCaseData.builder().parentClaimantIsApplicant(YesOrNo.YES).applicantBilingualLanguagePreference(
                    YesOrNo.YES)
                .build();
        assertThat(gaForLipService.anyWelsh(caseData)).isTrue();
    }

    @Test
    void shouldReturnAnyTrue_resp_is_welsh() {
        GeneralApplicationCaseData caseData =
            GeneralApplicationCaseData.builder().parentClaimantIsApplicant(YesOrNo.YES).respondentBilingualLanguagePreference(
                    YesOrNo.YES)
                .build();
        assertThat(gaForLipService.anyWelsh(caseData)).isTrue();
    }

    @Test
    void shouldReturnAnyFalse_nobody_is_welsh() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder().parentClaimantIsApplicant(YesOrNo.YES).build();
        assertThat(gaForLipService.anyWelsh(caseData)).isFalse();
    }

    @Test
    void shouldReturnNoticeTrue_app_is_welsh() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .parentClaimantIsApplicant(YesOrNo.YES)
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YesOrNo.YES).build())
            .build();
        assertThat(gaForLipService.anyWelshNotice(caseData)).isTrue();
    }

    @Test
    void shouldReturnNoticeTrue_resp_is_welsh() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .parentClaimantIsApplicant(YesOrNo.YES)
            .respondentBilingualLanguagePreference(YesOrNo.YES)
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YesOrNo.YES).build())
            .build();
        assertThat(gaForLipService.anyWelshNotice(caseData)).isTrue();
    }

    @Test
    void shouldReturnNoticeFalse_nobody_is_welsh() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .parentClaimantIsApplicant(YesOrNo.YES)
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YesOrNo.YES).build())
            .build();
        assertThat(gaForLipService.anyWelshNotice(caseData)).isFalse();
    }

    @Test
    void shouldReturnWithoutNoticeFalse_resp_is_welsh() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseData.builder()
            .parentClaimantIsApplicant(YesOrNo.YES)
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YesOrNo.NO).build())
            .build();
        assertThat(gaForLipService.anyWelshNotice(caseData)).isFalse();
    }
}
