package uk.gov.hmcts.reform.civil.notification.handlers.notifyhwfoutcomeparties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NotifyHwFOutcomePartiesClaimantEmailDTOGeneratorTest {

    private static final String REFERENCE_TEMPLATE = "hwf-outcome-notification-%s";
    private static final String TEMPLATE_ID_BILINGUAL = "65b3524b-b58c-4cdb-b8b5-5fc2cec505a5";
    private static final String TEMPLATE_ID_DEFAULT = "a7c206b5-8a27-4419-83ff-61351cbc69fb";

    private NotifyHwFOutcomePartiesClaimantEmailDTOGenerator generator;
    private NotifyHwFOutcomePartiesHelper helper;

    @BeforeEach
    void setUp() {
        helper = mock(NotifyHwFOutcomePartiesHelper.class);
        generator = new NotifyHwFOutcomePartiesClaimantEmailDTOGenerator(helper);
    }

    @Test
    void shouldReturnTrue_whenEventIsNotFullRemissionAndApplicantIsLiP() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getHwFEvent()).thenReturn(CaseEvent.MORE_INFORMATION_HWF);
        when(caseData.isApplicantLiP()).thenReturn(true);

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalse_whenEventIsFullRemission() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getHwFEvent()).thenReturn(CaseEvent.FULL_REMISSION_HWF);
        when(caseData.isApplicantLiP()).thenReturn(true);

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnFalse_whenApplicantIsNotLiP() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getHwFEvent()).thenReturn(CaseEvent.MORE_INFORMATION_HWF);
        when(caseData.isApplicantLiP()).thenReturn(false);

        Boolean result = generator.getShouldNotify(caseData);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnBilingualTemplateId_whenClaimantIsBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isClaimantBilingual()).thenReturn(true);
        when(caseData.getHwFEvent()).thenReturn(CaseEvent.MORE_INFORMATION_HWF);
        when(helper.getTemplateBilingual(CaseEvent.MORE_INFORMATION_HWF)).thenReturn(TEMPLATE_ID_BILINGUAL);

        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo(TEMPLATE_ID_BILINGUAL);
    }

    @Test
    void shouldReturnDefaultTemplateId_whenClaimantIsNotBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isClaimantBilingual()).thenReturn(false);
        when(caseData.getHwFEvent()).thenReturn(CaseEvent.NO_REMISSION_HWF);
        when(helper.getTemplate(CaseEvent.NO_REMISSION_HWF)).thenReturn(TEMPLATE_ID_DEFAULT);

        String result = generator.getEmailTemplateId(caseData);

        assertThat(result).isEqualTo(TEMPLATE_ID_DEFAULT);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String result = generator.getReferenceTemplate();

        assertThat(result).isEqualTo(REFERENCE_TEMPLATE);
    }

    @Test
    void shouldAddPropertiesForNoRemissionHwfEvent() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getHwFEvent()).thenReturn(CaseEvent.NO_REMISSION_HWF);
        Map<String, String> commonProperties = Map.of("key1", "value1");
        Map<String, String> noRemissionProperties = Map.of("reason", "No remission reason");
        when(helper.getCommonProperties(caseData)).thenReturn(commonProperties);
        when(helper.getFurtherProperties(caseData)).thenReturn(noRemissionProperties);

        Map<String, String> result = generator.addCustomProperties(Map.of(), caseData);

        assertThat(result).containsAllEntriesOf(commonProperties);
        assertThat(result).containsAllEntriesOf(noRemissionProperties);
    }

    @Test
    void shouldAddPropertiesForMoreInformationHwfEvent() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getHwFEvent()).thenReturn(CaseEvent.MORE_INFORMATION_HWF);
        Map<String, String> commonProperties = Map.of("key1", "value1");
        Map<String, String> moreInfoProperties = Map.of("info", "More information required");
        when(helper.getCommonProperties(caseData)).thenReturn(commonProperties);
        when(helper.getFurtherProperties(caseData)).thenReturn(moreInfoProperties);

        Map<String, String> result = generator.addCustomProperties(Map.of(), caseData);

        assertThat(result).containsAllEntriesOf(commonProperties);
        assertThat(result).containsAllEntriesOf(moreInfoProperties);
    }

    @Test
    void shouldAddPropertiesForPartialRemissionHwfEvent() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getHwFEvent()).thenReturn(CaseEvent.PARTIAL_REMISSION_HWF_GRANTED);
        Map<String, String> commonProperties = Map.of("key1", "value1");
        Map<String, String> partialRemissionProperties = Map.of("amount", "100");
        when(helper.getCommonProperties(caseData)).thenReturn(commonProperties);
        when(helper.getFurtherProperties(caseData)).thenReturn(partialRemissionProperties);

        Map<String, String> result = generator.addCustomProperties(Map.of(), caseData);

        assertThat(result).containsAllEntriesOf(commonProperties);
        assertThat(result).containsAllEntriesOf(partialRemissionProperties);
    }
}
