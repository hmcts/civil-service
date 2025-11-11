package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.dj.CaseManagementOrderAdditional;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingBundleType;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingAddNewDirectionsDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingBundleDJ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.dj.DisposalAndTrialHearingDJToggle.SHOW;

class DjTemplateFieldServiceTest {

    private final DjTemplateFieldService service = new DjTemplateFieldService();

    @Nested
    class BuildBundleInfo {

        @Test
        void shouldReturnText_whenAllThreeTypesSelected() {
            CaseData caseData = caseDataWithBundle(List.of(
                DisposalHearingBundleType.DOCUMENTS,
                DisposalHearingBundleType.ELECTRONIC,
                DisposalHearingBundleType.SUMMARY
            ));

            String expectedText = "an indexed bundle of documents, with each page clearly numbered"
                + " / an electronic bundle of digital documents"
                + " / a case summary containing no more than 500 words";

            assertThat(service.buildBundleInfo(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenTwoTypesSelected() {
            CaseData caseData = caseDataWithBundle(List.of(
                DisposalHearingBundleType.DOCUMENTS,
                DisposalHearingBundleType.SUMMARY
            ));

            String expectedText = "an indexed bundle of documents, with each page clearly numbered"
                + " / a case summary containing no more than 500 words";

            assertThat(service.buildBundleInfo(caseData)).isEqualTo(expectedText);
        }

        @Test
        void shouldReturnText_whenSingleTypeSelected() {
            CaseData caseData = caseDataWithBundle(List.of(DisposalHearingBundleType.ELECTRONIC));

            assertThat(service.buildBundleInfo(caseData))
                .isEqualTo("an electronic bundle of digital documents");
        }

        @Test
        void shouldReturnEmpty_whenNoBundlePresent() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build();

            assertThat(service.buildBundleInfo(caseData)).isEmpty();
        }

        private CaseData caseDataWithBundle(List<DisposalHearingBundleType> bundleTypes) {
            DisposalHearingBundleDJ bundle = DisposalHearingBundleDJ.builder()
                .type(bundleTypes)
                .build();

            return CaseDataBuilder.builder()
                .atStateClaimDraft()
                .build()
                .toBuilder()
                .disposalHearingBundleDJ(bundle)
                .build();
        }
    }

    @Test
    void shouldIdentifyToggleEnabled() {
        assertThat(service.isToggleEnabled(List.of(SHOW))).isTrue();
        assertThat(service.isToggleEnabled(null)).isFalse();
    }

    @Test
    void shouldIdentifyAdditionalDirections() {
        CaseData caseData = CaseData.builder()
            .disposalHearingAddNewDirectionsDJ(List.of(
                Element.<DisposalHearingAddNewDirectionsDJ>builder()
                    .value(DisposalHearingAddNewDirectionsDJ.builder().build())
                    .build()
            ))
            .build();

        assertThat(service.hasAdditionalDirections(caseData)).isTrue();
    }

    @Test
    void shouldDetectEmployerLiability() {
        assertThat(service.hasEmployerLiability(
            List.of(CaseManagementOrderAdditional.OrderTypeTrialAdditionalDirectionsEmployersLiability)
        )).isTrue();
    }

    @Test
    void shouldDetectJudgeRole() {
        UserDetails userDetails = UserDetails.builder()
            .forename("Judge")
            .roles(List.of("caseworker-civil-judge"))
            .build();

        assertThat(service.isJudge(userDetails)).isTrue();
    }

}
