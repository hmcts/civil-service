package uk.gov.hmcts.reform.civil.ga.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.SPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.SPEC_UNSPEC_CONTACT;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.WELSH_CONTACT;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.WELSH_HMCTS_SIGNATURE;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.WELSH_OPENING_HOURS;
import static uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA.WELSH_PHONE_CONTACT;
import static uk.gov.hmcts.reform.civil.ga.utils.EmailFooterUtils.RAISE_QUERY_LIP;
import static uk.gov.hmcts.reform.civil.ga.utils.EmailFooterUtils.RAISE_QUERY_LIP_WELSH;
import static uk.gov.hmcts.reform.civil.ga.utils.EmailFooterUtils.RAISE_QUERY_LR;
import static uk.gov.hmcts.reform.civil.ga.utils.EmailFooterUtils.addAllFooterItems;

public class EmailFooterUtilsTest {

    private final NotificationsSignatureConfiguration configuration = mock(NotificationsSignatureConfiguration.class);

    @BeforeEach
    void setUp() {
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        when(configuration.getWelshContact()).thenReturn("E-bost: ymholiadaucymraeg@justice.gov.uk");
        when(configuration.getSpecContact()).thenReturn("Email: contactocmc@justice.gov.uk");
        when(configuration.getWelshHmctsSignature()).thenReturn("Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        when(configuration.getWelshPhoneContact()).thenReturn("Ffôn: 0300 303 5174");
        when(configuration.getWelshOpeningHours()).thenReturn("Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
    }

    @Test
    void shouldAddEmailsToFooterWhenPublicQmNotEnabled() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .build().copy().respondent1Represented(YES).applicant1Represented(YES).build();
        GeneralApplicationCaseData mainCaseData = GeneralApplicationCaseDataBuilder.builder().build();
        Map<String, String> actual = addAllFooterItems(
            caseData,
            mainCaseData,
            new HashMap<>(),
            configuration,
            false
        );
        assertFooterItems(actual, false, false, true);
    }

    @Test
    void shouldAddQueryStringToFooterWhenPublicQmEnabled() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .build().copy().respondent1Represented(YES).applicant1Represented(YES).build();
        GeneralApplicationCaseData mainCaseData = GeneralApplicationCaseDataBuilder.builder().build();
        Map<String, String> actual = addAllFooterItems(
            caseData,
            mainCaseData,
            new HashMap<>(),
            configuration,
            true
        );
        assertFooterItems(actual, true, false, true);
    }

    @Test
    void shouldAddQueryStringToFooterWhenPublicQmEnabledApplicantLip() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .build().copy().respondent1Represented(YES).isGaApplicantLip(YES).build();
        GeneralApplicationCaseData mainCaseData = GeneralApplicationCaseDataBuilder.builder().build();
        Map<String, String> actual = addAllFooterItems(
            caseData,
            mainCaseData,
            new HashMap<>(),
            configuration,
            true
        );
        assertFooterItems(actual, true, true, true);
    }

    @Test
    void shouldAddQueryStringToFooterWhenPublicEnabledRespondentLip() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .ccdState(CaseState.AWAITING_APPLICANT_INTENTION)
            .build().copy().isGaRespondentOneLip(YES).applicant1Represented(YES).build();
        GeneralApplicationCaseData mainCaseData = GeneralApplicationCaseDataBuilder.builder().build();
        Map<String, String> actual = addAllFooterItems(
            caseData,
            mainCaseData,
            new HashMap<>(),
            configuration,
            true
        );
        assertFooterItems(actual, true, true, true);
    }

    @ParameterizedTest()
    @ValueSource(strings = {"PENDING_CASE_ISSUED", "CLOSED", "PROCEEDS_IN_HERITAGE_SYSTEM", "CASE_DISMISSED"})
    void shouldAddSpecAndUnspecContactWhenCaseInQueryNotAllowedState(String caseState) {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .build().copy().respondent1Represented(YES).applicant1Represented(YES)
            .ccdState(Enum.valueOf(CaseState.class, caseState)).build();
        GeneralApplicationCaseData mainCaseData = GeneralApplicationCaseDataBuilder.builder().ccdState(CaseState.valueOf(caseState)).build();
        Map<String, String> actual = addAllFooterItems(
            caseData,
            mainCaseData,
            new HashMap<>(),
            configuration,
            true
        );
        assertFooterItems(actual, true, false, false);
    }

    private void assertFooterItems(Map<String, String> actual, boolean publicQmEnabled, boolean isLipCase, boolean queryAllowedCaseState) {
        assertThat(actual.get(HMCTS_SIGNATURE)).isEqualTo("Online Civil Claims \n HM Courts & Tribunal Service");
        assertThat(actual.get(WELSH_HMCTS_SIGNATURE)).isEqualTo("Hawliadau am Arian yn y Llys Sifil Ar-lein "
                                                                    + "\n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        assertThat(actual.get(PHONE_CONTACT)).isEqualTo("For anything related to hearings, call 0300 123 5577 "
                                                            + "\n For all other matters, call 0300 123 7050");
        assertThat(actual.get(WELSH_PHONE_CONTACT)).isEqualTo("Ffôn: 0300 303 5174");
        assertThat(actual.get(OPENING_HOURS)).isEqualTo("Monday to Friday, 8.30am to 5pm");
        assertThat(actual.get(WELSH_OPENING_HOURS)).isEqualTo("Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        if ((!isLipCase || (isLipCase && publicQmEnabled)) && queryAllowedCaseState) {
            assertThat(actual.get(SPEC_UNSPEC_CONTACT)).isEqualTo(RAISE_QUERY_LR);
        } else {
            assertThat(actual.get(SPEC_UNSPEC_CONTACT)).isEqualTo("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        }

        if (isLipCase && queryAllowedCaseState && publicQmEnabled) {
            assertThat(actual.get(SPEC_CONTACT)).isEqualTo(RAISE_QUERY_LIP);
            assertThat(actual.get(WELSH_CONTACT)).isEqualTo(RAISE_QUERY_LIP_WELSH);
        } else {
            assertThat(actual.get(SPEC_CONTACT)).isEqualTo("Email: contactocmc@justice.gov.uk");
            assertThat(actual.get(WELSH_CONTACT)).isEqualTo("E-bost: ymholiadaucymraeg@justice.gov.uk");
        }
    }
}
