package uk.gov.hmcts.reform.civil.model.citizenui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
public class RespondentLiPResponseTest {

    @Test
    void when_RespondentResponseLanguage_Is_Welsh_and_English() {
        CaseData caseData = CaseDataBuilder.builder()
            .caseDataLip(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder()
                             .respondent1ResponseLanguage(Language.BOTH.toString()).build())
                             .build())
            .build();

        boolean isBilingualResponse = caseData.isRespondentResponseBilingual();
        assertThat(isBilingualResponse).isEqualTo(true);
    }

    @Test
    void when_RespondentResponseLanguage_Is_English() {
        CaseData caseData = CaseDataBuilder.builder()
            .caseDataLip(CaseDataLiP.builder()
                             .respondent1LiPResponse(RespondentLiPResponse.builder()
                             .respondent1ResponseLanguage(Language.ENGLISH.toString()).build())
                             .build())
            .build();

        boolean isBilingualResponse = caseData.isRespondentResponseBilingual();
        assertThat(isBilingualResponse).isEqualTo(false);
    }

    @Test
    void when_RespondentResponseLanguage_Is_NotSet() {
        CaseData caseData = CaseDataBuilder.builder()
            .caseDataLip(null)
            .build();

        boolean isBilingualResponse = caseData.isRespondentResponseBilingual();
        assertThat(isBilingualResponse).isEqualTo(false);
    }
}
