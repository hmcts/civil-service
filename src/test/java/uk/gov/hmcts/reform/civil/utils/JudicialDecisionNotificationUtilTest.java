package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.dq.GAJudgeMakeAnOrderOption;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialMakeAnOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.utils.NotificationCriterion.JUDGE_DISMISSED_APPLICATION;

public class JudicialDecisionNotificationUtilTest {

    @Test
    public void shouldGetJudicialDismissalNotificationCriterion() {
        CaseData caseData = CaseData.builder()
            .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder().makeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION).build())
            .businessProcess(BusinessProcess.ready(CaseEvent.MAKE_DECISION))
            .build();

        NotificationCriterion notificationCriterion = JudicialDecisionNotificationUtil.notificationCriterion(caseData);

        assertThat(notificationCriterion).isEqualTo(JUDGE_DISMISSED_APPLICATION);
    }

    @Test
    public void shouldGetJudicialDismissalNotificationCriterionAfterTranslationUpload() {
        CaseData caseData = CaseData.builder()
            .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder().makeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION).build())
            .businessProcess(BusinessProcess.ready(CaseEvent.UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION))
            .build();

        NotificationCriterion notificationCriterion = JudicialDecisionNotificationUtil.notificationCriterion(caseData);

        assertThat(notificationCriterion).isEqualTo(JUDGE_DISMISSED_APPLICATION);
    }
}
