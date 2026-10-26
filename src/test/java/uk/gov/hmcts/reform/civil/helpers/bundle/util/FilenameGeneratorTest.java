package uk.gov.hmcts.reform.civil.helpers.bundle.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FilenameGeneratorTest {

    private final FilenameGenerator generator = new FilenameGenerator();

    @ParameterizedTest
    @CsvSource({
        "2026-10-19, Smith v Jones-19102026",
        ", Smith v Jones"
    })
    void shouldGenerateIndividualPartyFilename(LocalDate hearingDate, String expected) {
        CaseData caseData = CaseData.builder()
            .applicant1(new Party().setType(Party.Type.INDIVIDUAL).setIndividualLastName("Smith"))
            .respondent1(new Party().setType(Party.Type.INDIVIDUAL).setIndividualLastName("Jones"))
            .hearingDate(hearingDate)
            .build();

        assertThat(generator.generateBundleFilenamePrefix(caseData)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
        "2026-10-19, Applicant Ltd v Respondent Ltd-19102026",
        ", Applicant Ltd v Respondent Ltd"
    })
    void shouldGenerateCompanyPartyFilename(LocalDate hearingDate, String expected) {
        CaseData caseData = CaseData.builder()
            .applicant1(new Party().setType(Party.Type.COMPANY).setCompanyName("Applicant Ltd"))
            .respondent1(new Party().setType(Party.Type.COMPANY).setCompanyName("Respondent Ltd"))
            .hearingDate(hearingDate)
            .build();

        assertThat(generator.generateBundleFilenamePrefix(caseData)).isEqualTo(expected);
    }
}
