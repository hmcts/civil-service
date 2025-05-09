package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.*;

class GenerateDJFormHelperTest {

    private GenerateDJFormHelper generateDJFormHelper;

    @BeforeEach
    void setUp() {
        generateDJFormHelper = new GenerateDJFormHelper();
    }

    @Test
    void shouldReturnTrue_whenRespondent1MatchesDefendantDetails() {
        CaseData caseData = mock(CaseData.class);
        Party respondent1 = mock(Party.class);
        DynamicList defendantDetails = mock(DynamicList.class);
        DynamicListElement selectedElement = mock(DynamicListElement.class);

        when(caseData.getDefendantDetails()).thenReturn(defendantDetails);
        when(defendantDetails.getValue()).thenReturn(selectedElement);
        when(selectedElement.getLabel()).thenReturn("Respondent 1");
        when(caseData.getRespondent1()).thenReturn(respondent1);
        when(respondent1.getPartyName()).thenReturn("Respondent 1");

        boolean result = generateDJFormHelper.checkDefendantRequested(caseData, true);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_whenRespondent1DoesNotMatchDefendantDetails() {
        CaseData caseData = mock(CaseData.class);
        Party respondent1 = mock(Party.class);
        DynamicList defendantDetails = mock(DynamicList.class);
        DynamicListElement selectedElement = mock(DynamicListElement.class);

        when(caseData.getDefendantDetails()).thenReturn(defendantDetails);
        when(defendantDetails.getValue()).thenReturn(selectedElement);
        when(selectedElement.getLabel()).thenReturn("Defendant");
        when(caseData.getRespondent1()).thenReturn(respondent1);
        when(respondent1.getPartyName()).thenReturn("Respondent 1");

        boolean result = generateDJFormHelper.checkDefendantRequested(caseData, true);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalse_whenRespondent1IsNull() {
        CaseData caseData = mock(CaseData.class);
        DynamicList defendantDetails = mock(DynamicList.class);
        DynamicListElement selectedElement = mock(DynamicListElement.class);

        when(caseData.getDefendantDetails()).thenReturn(defendantDetails);
        when(defendantDetails.getValue()).thenReturn(selectedElement);
        when(selectedElement.getLabel()).thenReturn("Respondent 1");
        when(caseData.getRespondent1()).thenReturn(null);

        boolean result = generateDJFormHelper.checkDefendantRequested(caseData, true);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrue_whenRespondent2MatchesDefendantDetails() {
        CaseData caseData = mock(CaseData.class);
        Party respondent2 = mock(Party.class);
        DynamicList defendantDetails = mock(DynamicList.class);
        DynamicListElement selectedElement = mock(DynamicListElement.class);

        when(caseData.getDefendantDetails()).thenReturn(defendantDetails);
        when(defendantDetails.getValue()).thenReturn(selectedElement);
        when(selectedElement.getLabel()).thenReturn("Respondent 2");
        when(caseData.getRespondent2()).thenReturn(respondent2);
        when(respondent2.getPartyName()).thenReturn("Respondent 2");

        boolean result = generateDJFormHelper.checkDefendantRequested(caseData, false);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_whenRespondent2DoesNotMatchDefendantDetails() {
        CaseData caseData = mock(CaseData.class);
        Party respondent2 = mock(Party.class);
        DynamicList defendantDetails = mock(DynamicList.class);
        DynamicListElement selectedElement = mock(DynamicListElement.class);

        when(caseData.getDefendantDetails()).thenReturn(defendantDetails);
        when(defendantDetails.getValue()).thenReturn(selectedElement);
        when(selectedElement.getLabel()).thenReturn("Defendant");
        when(caseData.getRespondent1()).thenReturn(respondent2);
        when(respondent2.getPartyName()).thenReturn("Respondent 2");

        boolean result = generateDJFormHelper.checkDefendantRequested(caseData, false);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalse_whenDefendantDetailsIsNull() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.getDefendantDetails()).thenReturn(null);

        boolean result = generateDJFormHelper.checkDefendantRequested(caseData, true);

        assertThat(result).isFalse();
    }

    @Test
    void shouldUpdateRespondent2Properties() {
        CaseData caseData = mock(CaseData.class);
        Party respondent2 = mock(Party.class);
        Map<String, String> properties = new HashMap<>();

        when(caseData.getRespondent2()).thenReturn(respondent2);
        when(respondent2.getPartyName()).thenReturn("Respondent 2");

        Map<String, String> result = generateDJFormHelper.updateRespondent2Properties(properties, caseData);

        assertThat(result).containsEntry(DEFENDANT_NAME_INTERIM, "Respondent 2");
    }

    @Test
    void shouldReturnFalse_whenNotBothDefendants() {
        CaseData caseData = mock(CaseData.class);
        DynamicList defendantDetails = mock(DynamicList.class);
        DynamicListElement selectedElement = mock(DynamicListElement.class);

        when(caseData.getDefendantDetails()).thenReturn(defendantDetails);
        when(defendantDetails.getValue()).thenReturn(selectedElement);
        when(selectedElement.getLabel()).thenReturn("Defendant 1");

        boolean result = generateDJFormHelper.checkIfBothDefendants(caseData);

        assertThat(result).isFalse();
    }

}
