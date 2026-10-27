package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertaskstest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks.PopulateRespondentTabDetails.updateDataForClaimDetailsTab;

class PopulateRespondentTabDetailsTest {

    private final ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldSkipMissingRespondents(boolean respondent2CopyNeeded) {
        CaseData caseData = CaseDataBuilder.builder().respondent1(null).respondent2(null).build();

        updateDataForClaimDetailsTab(caseData, objectMapper, respondent2CopyNeeded);

        assertThat(caseData.getRespondent1DetailsForClaimDetailsTab()).isNull();
        assertThat(caseData.getRespondent2DetailsForClaimDetailsTab()).isNull();
        assertThat(caseData.getRespondent2Copy()).isNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldCopyRespondent1WithoutFlagsWhenRespondent2Missing(boolean respondent2CopyNeeded) {
        Flags flags = new Flags().setPartyName("Defendant one");
        Party respondent = new Party().setType(Party.Type.COMPANY).setCompanyName("Defendant Ltd").setFlags(flags);
        CaseData caseData = CaseDataBuilder.builder().respondent1(respondent).respondent2(null).build();

        updateDataForClaimDetailsTab(caseData, objectMapper, respondent2CopyNeeded);

        assertThat(caseData.getRespondent1DetailsForClaimDetailsTab())
            .isNotSameAs(respondent);
        Party tabCopy = caseData.getRespondent1DetailsForClaimDetailsTab();
        assertThat(tabCopy.getCompanyName()).isEqualTo("Defendant Ltd");
        assertThat(tabCopy.getFlags()).isNull();
        assertThat(respondent.getFlags()).isSameAs(flags);
        assertThat(caseData.getRespondent2DetailsForClaimDetailsTab()).isNull();
        assertThat(caseData.getRespondent2Copy()).isNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldCopyRespondent2WhenRespondent1Missing(boolean respondent2CopyNeeded) {
        Flags flags = new Flags().setPartyName("Defendant two");
        Party respondent = new Party().setType(Party.Type.COMPANY).setCompanyName("Defendant Ltd").setFlags(flags);
        CaseData caseData = CaseDataBuilder.builder().respondent1(null).respondent2(respondent).build();

        updateDataForClaimDetailsTab(caseData, objectMapper, respondent2CopyNeeded);

        assertThat(caseData.getRespondent1DetailsForClaimDetailsTab()).isNull();
        assertThat(caseData.getRespondent2DetailsForClaimDetailsTab())
            .isNotSameAs(respondent);
        Party tabCopy = caseData.getRespondent2DetailsForClaimDetailsTab();
        assertThat(tabCopy.getCompanyName()).isEqualTo("Defendant Ltd");
        assertThat(tabCopy.getFlags()).isNull();
        assertThat(respondent.getFlags()).isSameAs(flags);
        assertThat(caseData.getRespondent2Copy()).isEqualTo(respondent2CopyNeeded ? respondent : null);
    }
}
