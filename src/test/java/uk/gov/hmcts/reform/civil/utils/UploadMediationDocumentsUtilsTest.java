package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.model.common.DynamicListElement.dynamicElementFromCode;

class UploadMediationDocumentsUtilsTest {

    @Test
    void shouldAddApplicantOptions_when1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        List<DynamicListElement> dynamicListOptions = new ArrayList<>();
        UploadMediationDocumentsUtils.addApplicantOptions(dynamicListOptions, caseData);

        DynamicListElement expected = dynamicElementFromCode("CLAIMANT_1", "Claimant 1: Mr. John Rambo");

        assertThat(dynamicListOptions).containsExactly(expected);
    }

    @Test
    void shouldAddApplicantOptions_when2v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .multiPartyClaimTwoApplicants()
            .build();
        List<DynamicListElement> dynamicListOptions = new ArrayList<>();
        UploadMediationDocumentsUtils.addApplicantOptions(dynamicListOptions, caseData);

        DynamicListElement expectedApplicant1 = dynamicElementFromCode("CLAIMANT_1", "Claimant 1: Mr. John Rambo");
        DynamicListElement expectedApplicant2 = dynamicElementFromCode("CLAIMANT_2", "Claimant 2: Mr. Jason Rambo");
        DynamicListElement expectedBothApplicants = dynamicElementFromCode("CLAIMANTS", "Claimants 1 and 2");

        assertThat(dynamicListOptions).containsExactly(expectedApplicant1, expectedApplicant2, expectedBothApplicants);
    }

    @Test
    void addDefendant1Option_when1v1() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        List<DynamicListElement> dynamicListOptions = new ArrayList<>();
        UploadMediationDocumentsUtils.addDefendant1Option(dynamicListOptions, caseData);

        DynamicListElement expected = dynamicElementFromCode("DEFENDANT_1", "Defendant 1: Mr. Sole Trader");

        assertThat(dynamicListOptions).containsExactly(expected);
    }

    @Test
    void addDefendant2Option_when1v2() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .multiPartyClaimTwoDefendantSolicitors()
            .build();
        List<DynamicListElement> dynamicListOptions = new ArrayList<>();
        UploadMediationDocumentsUtils.addDefendant2Option(dynamicListOptions, caseData);

        DynamicListElement expected = dynamicElementFromCode("DEFENDANT_2", "Defendant 2: Mr. John Rambo");

        assertThat(dynamicListOptions).containsExactly(expected);
    }

    @Test
    void addSameSolicitorDefendantOptions() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
            .multiPartyClaimOneDefendantSolicitor()
            .build();
        List<DynamicListElement> dynamicListOptions = new ArrayList<>();
        UploadMediationDocumentsUtils.addSameSolicitorDefendantOptions(dynamicListOptions, caseData);

        DynamicListElement expected1 = dynamicElementFromCode("DEFENDANT_1", "Defendant 1: Mr. Sole Trader");
        DynamicListElement expected2 = dynamicElementFromCode("DEFENDANT_2", "Defendant 2: Mr. John Rambo");
        DynamicListElement expected3 = dynamicElementFromCode("DEFENDANTS", "Defendants 1 and 2");

        assertThat(dynamicListOptions).containsExactly(expected1, expected2, expected3);
    }
}
