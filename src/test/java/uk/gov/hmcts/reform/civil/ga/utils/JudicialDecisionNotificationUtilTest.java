package uk.gov.hmcts.reform.civil.ga.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.ga.utils.NotificationCriterion.JUDGE_DISMISSED_APPLICATION;

public class JudicialDecisionNotificationUtilTest {

    @Test
    public void shouldGetJudicialDismissalNotificationCriterion() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder().setMakeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION))
            .businessProcess(BusinessProcess.ready(CaseEvent.MAKE_DECISION))
            .build();

        NotificationCriterion notificationCriterion = JudicialDecisionNotificationUtil.notificationCriterion(caseData);

        assertThat(notificationCriterion).isEqualTo(JUDGE_DISMISSED_APPLICATION);
    }

    @Test
    public void shouldGetJudicialDismissalNotificationCriterionAfterTranslationUpload() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder().setMakeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION))
            .businessProcess(BusinessProcess.ready(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION))
            .build();

        NotificationCriterion notificationCriterion = JudicialDecisionNotificationUtil.notificationCriterion(caseData);

        assertThat(notificationCriterion).isEqualTo(JUDGE_DISMISSED_APPLICATION);
    }
}
