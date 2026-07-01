package uk.gov.hmcts.reform.civil.ga.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.ga.utils.NotificationCriterion.JUDGE_DISMISSED_APPLICATION;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

class JudicialDecisionNotificationUtilTest {

    @Test
    void shouldGetJudicialDismissalNotificationCriterion() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder().setMakeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION))
            .businessProcess(BusinessProcess.ready(CaseEvent.MAKE_DECISION))
            .build();

        NotificationCriterion notificationCriterion = JudicialDecisionNotificationUtil.notificationCriterion(caseData);

        assertThat(notificationCriterion).isEqualTo(JUDGE_DISMISSED_APPLICATION);
    }

    @Test
    void shouldGetJudicialDismissalNotificationCriterionAfterTranslationUpload() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder().setMakeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION))
            .businessProcess(BusinessProcess.ready(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION))
            .build();

        NotificationCriterion notificationCriterion = JudicialDecisionNotificationUtil.notificationCriterion(caseData);

        assertThat(notificationCriterion).isEqualTo(JUDGE_DISMISSED_APPLICATION);
    }

    @Test
    void areRespondentSolicitorsPresent_shouldNotThrowNPE_whenEmailIsNull() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalAppRespondentSolicitors(List.of(
                element(new GASolicitorDetailsGAspec().setEmail(null))
            ))
            .build();

        boolean result = JudicialDecisionNotificationUtil.areRespondentSolicitorsPresent(caseData);

        assertThat(result).isFalse();
    }

    @Test
    void areRespondentSolicitorsPresent_shouldReturnTrue_whenEmailIsPresent() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalAppRespondentSolicitors(List.of(
                element(new GASolicitorDetailsGAspec().setEmail("solicitor@example.com"))
            ))
            .build();

        boolean result = JudicialDecisionNotificationUtil.areRespondentSolicitorsPresent(caseData);

        assertThat(result).isTrue();
    }

    @Test
    void areRespondentSolicitorsPresent_shouldReturnFalse_whenEmailIsEmpty() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .generalAppRespondentSolicitors(List.of(
                element(new GASolicitorDetailsGAspec().setEmail("")),
                element(new GASolicitorDetailsGAspec().setEmail(" "))
            ))
            .build();

        boolean result = JudicialDecisionNotificationUtil.areRespondentSolicitorsPresent(caseData);

        assertThat(result).isFalse();
    }
}
