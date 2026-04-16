package uk.gov.hmcts.reform.civil.notification.handlers.standarddirectionorderdj;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class StandardDirectionOrderDJNotificationHelperTest {

    private StandardDirectionOrderDJNotificationHelper helper;
    private MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic;

    @BeforeEach
    void setUp() {
        helper = new StandardDirectionOrderDJNotificationHelper();
        multiPartyScenarioMockedStatic = mockStatic(MultiPartyScenario.class);
    }

    @AfterEach
    void tearDown() {
        if (multiPartyScenarioMockedStatic != null) {
            multiPartyScenarioMockedStatic.close();
        }
    }

    @Test
    void shouldNotifyApplicantSolicitor_whenApplicantIsRepresented() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isApplicant1NotRepresented()).thenReturn(false);

        assertThat(helper.shouldNotifyApplicantSolicitor(caseData)).isTrue();
    }

    @Test
    void shouldNotNotifyApplicantSolicitor_whenApplicantIsNotRepresented() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isApplicant1NotRepresented()).thenReturn(true);

        assertThat(helper.shouldNotifyApplicantSolicitor(caseData)).isFalse();
    }

    @Test
    void shouldNotifyRespondentSolicitorOne_whenRespondent1IsRepresented() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isRespondent1NotRepresented()).thenReturn(false);

        assertThat(helper.shouldNotifyRespondentSolicitorOne(caseData)).isTrue();
    }

    @Test
    void shouldNotNotifyRespondentSolicitorOne_whenRespondent1IsNotRepresented() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isRespondent1NotRepresented()).thenReturn(true);

        assertThat(helper.shouldNotifyRespondentSolicitorOne(caseData)).isFalse();
    }

    @Test
    void shouldNotifyRespondentSolicitorTwo_whenAllConditionsMet() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getAddRespondent2()).thenReturn(YesOrNo.YES);
        when(caseData.isRespondent2NotRepresented()).thenReturn(false);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(true);

        assertThat(helper.shouldNotifyRespondentSolicitorTwo(caseData)).isTrue();
    }

    @Test
    void shouldNotNotifyRespondentSolicitorTwo_whenAddRespondent2IsNo() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getAddRespondent2()).thenReturn(YesOrNo.NO);

        assertThat(helper.shouldNotifyRespondentSolicitorTwo(caseData)).isFalse();
    }

    @Test
    void shouldNotNotifyRespondentSolicitorTwo_whenNotOneVTwoTwoLegalRep() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getAddRespondent2()).thenReturn(YesOrNo.YES);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(false);

        assertThat(helper.shouldNotifyRespondentSolicitorTwo(caseData)).isFalse();
    }

    @Test
    void shouldNotNotifyRespondentSolicitorTwo_whenRespondent2IsNotRepresented() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getAddRespondent2()).thenReturn(YesOrNo.YES);
        when(caseData.isRespondent2NotRepresented()).thenReturn(true);
        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(true);

        assertThat(helper.shouldNotifyRespondentSolicitorTwo(caseData)).isFalse();
    }
}
