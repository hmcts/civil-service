package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
public class JudicialDecisionHelperTest {

    @InjectMocks
    private JudicialDecisionHelper helper;

    @Nested
    class IsApplicationCloakedTests {

        @Test
        void isApplicationCloaked_shouldReturnNoWhenRespondentAgreementIsNull() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().generalAppRespondentAgreement(null).build();
            assertThat(helper.isApplicationCreatedWithoutNoticeByApplicant(caseData)).isEqualTo(NO);
        }

        @Test
        void isApplicationCloaked_shouldReturnNoWhenRespondentAgreementHasAgreed() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().generalAppRespondentAgreement(
                GARespondentOrderAgreement.builder().hasAgreed(YES).build()).build();
            assertThat(helper.isApplicationCreatedWithoutNoticeByApplicant(caseData)).isEqualTo(YES);
        }

        @Test
        void isApplicationCloaked_shouldReturnNoWhenRespondentAgreementHasNotAgreedButNotificationDetailsNotSet() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().generalAppRespondentAgreement(
                    GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(null).build();
            assertThat(helper.isApplicationCreatedWithoutNoticeByApplicant(caseData)).isEqualTo(NO);
        }

        @Test
        void isApplicationCloaked_shouldReturnNoWhenRespondentAgreementHasNotAgreedAndNotified() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().generalAppRespondentAgreement(
                    GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build()).build();
            assertThat(helper.isApplicationCreatedWithoutNoticeByApplicant(caseData)).isEqualTo(NO);
        }

        @Test
        void isApplicationCloaked_shouldReturnNoWhenRespondentAgreementHasNotAgreedAndUnNotified() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().generalAppRespondentAgreement(
                    GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build()).build();
            assertThat(helper.isApplicationCreatedWithoutNoticeByApplicant(caseData)).isEqualTo(YES);
        }

        @Test
        void isLipApplicationCloaked_shouldReturnNoWhenGeneralAppInformOtherPartyIsNull() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().generalAppRespondentAgreement(
                    GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(null).build();
            assertThat(helper.isLipApplicationCreatedWithoutNoticeByApplicant(caseData)).isEqualTo(YES);
        }

        @Test
        void isLipApplicationCloaked_shouldReturnNoWhenGeneralAppInformOtherPartyIsYes() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().generalAppRespondentAgreement(
                    GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build()).build();
            assertThat(helper.isLipApplicationCreatedWithoutNoticeByApplicant(caseData)).isEqualTo(NO);
        }

        @Test
        void isLipApplicationCloaked_shouldReturnNoWhenGeneralAppInformOtherPartyIsNo() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().generalAppRespondentAgreement(
                    GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build()).build();
            assertThat(helper.isLipApplicationCreatedWithoutNoticeByApplicant(caseData)).isEqualTo(YES);
        }

        @Test
        void isLipApplicationCloaked_shouldReturnNoWhenConsentOrder() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().generalAppRespondentAgreement(
                    GARespondentOrderAgreement.builder().hasAgreed(NO).build()).generalAppConsentOrder(YES).build();
            assertThat(helper.isLipApplicationCreatedWithoutNoticeByApplicant(caseData)).isEqualTo(NO);

        }

        @Test
        void isLipApplicationCloaked_shouldReturnNoWhenConsentOrderIsNull() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().generalAppRespondentAgreement(
                GARespondentOrderAgreement.builder().hasAgreed(NO).build()).build();
            assertThat(helper.isLipApplicationCreatedWithoutNoticeByApplicant(caseData)).isEqualTo(YES);

        }
    }

    @Nested
    class IsApplicantAndRespondentLocationPrefSame {

        @Test
        void shouldReturnFalse_whenApplicantHearingDetailsNotProvided() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().build();
            assertThat(helper.isApplicantAndRespondentLocationPrefSame(caseData)).isEqualTo(false);
        }

        @Test
        void shouldReturnFalse_whenRespondentHearingDetailsNotProvided() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData().generalAppHearingDetails(
                GAHearingDetails.builder().build()).build();
            assertThat(helper.isApplicantAndRespondentLocationPrefSame(caseData)).isEqualTo(false);
        }

        @Test
        void shouldReturnFalse_whenLocationSelectedByApplicantAndOneOfTheRespondentIsNotSame() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .generalAppHearingDetails(
                    GAHearingDetails.builder()
                        .hearingPreferredLocation(
                            getDynamicLocationsList("ABCD - RG0 0AL",
                                                    getDynamicLocation("ABCD - RG0 0AL"),
                                                    getDynamicLocation("PQRS - GU0 0EE"),
                                                    getDynamicLocation("WXYZ - EW0 0HE"),
                                                    getDynamicLocation("LMNO - NE0 0BH"))).build())
                .respondentsResponses(
                    wrapElements(
                        new GARespondentResponse()
                            .setGaHearingDetails(
                                GAHearingDetails.builder()
                                    .hearingPreferredLocation(
                                        getDynamicLocationsList("PQRS - GU0 0EE",
                                                                getDynamicLocation("ABCD - RG0 0AL"),
                                                                getDynamicLocation("PQRS - GU0 0EE"),
                                                                getDynamicLocation("WXYZ - EW0 0HE"),
                                                                getDynamicLocation("LMNO - NE0 0BH"))).build()),
                        new GARespondentResponse()
                            .setGaHearingDetails(
                                GAHearingDetails.builder()
                                    .hearingPreferredLocation(
                                        getDynamicLocationsList("ABCD - RG0 0AL",
                                                                getDynamicLocation("ABCD - RG0 0AL"),
                                                                getDynamicLocation("PQRS - GU0 0EE"),
                                                                getDynamicLocation("WXYZ - EW0 0HE"),
                                                                getDynamicLocation("LMNO - NE0 0BH"))).build())

                    )
                )
                .build();
            assertThat(helper.isApplicantAndRespondentLocationPrefSame(caseData)).isEqualTo(false);
        }

        @Test
        void shouldReturnFalse_whenLocationSelectedByApplicantAndBothOfTheRespondentIsNotSame() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .generalAppHearingDetails(
                    GAHearingDetails.builder()
                        .hearingPreferredLocation(
                            getDynamicLocationsList("ABCD - RG0 0AL",
                                                    getDynamicLocation("ABCD - RG0 0AL"),
                                                    getDynamicLocation("PQRS - GU0 0EE"),
                                                    getDynamicLocation("WXYZ - EW0 0HE"),
                                                    getDynamicLocation("LMNO - NE0 0BH"))).build())
                .respondentsResponses(
                    wrapElements(
                        new GARespondentResponse()
                            .setGaHearingDetails(
                                GAHearingDetails.builder()
                                    .hearingPreferredLocation(
                                        getDynamicLocationsList("PQRS - GU0 0EE",
                                                                getDynamicLocation("ABCD - RG0 0AL"),
                                                                getDynamicLocation("PQRS - GU0 0EE"),
                                                                getDynamicLocation("WXYZ - EW0 0HE"),
                                                                getDynamicLocation("LMNO - NE0 0BH"))).build()),
                        new GARespondentResponse()
                            .setGaHearingDetails(
                                GAHearingDetails.builder()
                                    .hearingPreferredLocation(
                                        getDynamicLocationsList("WXYZ - EW0 0HE",
                                                                getDynamicLocation("ABCD - RG0 0AL"),
                                                                getDynamicLocation("PQRS - GU0 0EE"),
                                                                getDynamicLocation("WXYZ - EW0 0HE"),
                                                                getDynamicLocation("LMNO - NE0 0BH"))).build())

                    )
                )
                .build();
            assertThat(helper.isApplicantAndRespondentLocationPrefSame(caseData)).isEqualTo(false);
        }

        @Test
        void shouldReturnTrue_whenLocationSelectedByApplicantAndBothRespondentIsSame() {
            GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
                .generalAppHearingDetails(
                    GAHearingDetails.builder()
                        .hearingPreferredLocation(
                            getDynamicLocationsList("ABCD - RG0 0AL",
                                                    getDynamicLocation("ABCD - RG0 0AL"),
                                                    getDynamicLocation("PQRS - GU0 0EE"),
                                                    getDynamicLocation("WXYZ - EW0 0HE"),
                                                    getDynamicLocation("LMNO - NE0 0BH"))).build())
                .respondentsResponses(
                    wrapElements(
                        new GARespondentResponse()
                            .setGaHearingDetails(
                                GAHearingDetails.builder()
                                    .hearingPreferredLocation(
                                        getDynamicLocationsList("ABCD - RG0 0AL",
                                                                getDynamicLocation("ABCD - RG0 0AL"),
                                                                getDynamicLocation("PQRS - GU0 0EE"),
                                                                getDynamicLocation("WXYZ - EW0 0HE"),
                                                                getDynamicLocation("LMNO - NE0 0BH"))).build()),
                        new GARespondentResponse()
                            .setGaHearingDetails(
                                GAHearingDetails.builder()
                                    .hearingPreferredLocation(
                                        getDynamicLocationsList("ABCD - RG0 0AL",
                                                                getDynamicLocation("ABCD - RG0 0AL"),
                                                                getDynamicLocation("PQRS - GU0 0EE"),
                                                                getDynamicLocation("WXYZ - EW0 0HE"),
                                                                getDynamicLocation("LMNO - NE0 0BH"))).build())

                        )
                )
                .build();
            assertThat(helper.isApplicantAndRespondentLocationPrefSame(caseData)).isEqualTo(true);
        }

        private DynamicListElement getDynamicLocation(String label) {
            return DynamicListElement.builder()
                .code(String.valueOf(UUID.randomUUID())).label(label).build();
        }

        private DynamicList getDynamicLocationsList(String chosenValue, DynamicListElement... elements) {
            Optional<DynamicListElement> first = Arrays.stream(elements)
                .filter(e -> e.getLabel().equals(chosenValue)).findFirst();
            DynamicList.DynamicListBuilder dynamicListBuilder = DynamicList.builder()
                .listItems(List.of(elements));
            first.ifPresent(dynamicListBuilder::value);
            return dynamicListBuilder.build();
        }
    }

    @Nested
    class IsOrderMakeDecisionMadeVisibleToDefendant {

        @Test
        void shouldReturnTrue_WhenJudgeDecideUncloaked_OrderMade() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(NO).build();
            assertThat(helper.isOrderMakeDecisionMadeVisibleToDefendant(caseData)).isEqualTo(true);

        }

        @Test
        void shouldReturnFalse_WhenApplicationIsWithNotice() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().requestForInformationApplication().build();
            assertThat(helper.isOrderMakeDecisionMadeVisibleToDefendant(caseData)).isEqualTo(false);

        }

        @Test
        void shouldReturnFalse_WhenJudgeDecide_WrittenRepresentationSequential() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().writtenRepresentationSequentialApplication().build();
            assertThat(helper.isOrderMakeDecisionMadeVisibleToDefendant(caseData)).isEqualTo(false);

        }

    }

    @Nested
    class IsApplicationUncloakedWithAdditionalFee {

        @Test
        void shouldReturnTrue_WhenApplicationIsUncloakedTypeRequestMoreInformation() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .judicialDecisionWithUncloakRequestForInformationApplication(SEND_APP_TO_OTHER_PARTY, NO, NO).build();
            assertThat(helper.isApplicationUncloakedWithAdditionalFee(caseData)).isTrue();

        }

        @Test
        void shouldReturnFalse_WhenApplicationIsCloakedTypeRequestMoreInformation() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .judicialDecisionWithUncloakRequestForInformationApplication(SEND_APP_TO_OTHER_PARTY, NO, YES).build();
            assertThat(helper.isApplicationUncloakedWithAdditionalFee(caseData)).isFalse();
        }

        @Test
        void shouldReturnFalse_WhenApplicationIsWithNoticeTypeRequestMoreInformation() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .judicialDecisionWithUncloakRequestForInformationApplication(REQUEST_MORE_INFORMATION, NO, NO).build();
            assertThat(helper.isApplicationUncloakedWithAdditionalFee(caseData)).isFalse();
        }

        @Test
        void shouldReturnFalse_WhenApplicationIsUncloakedTypeOrderMade() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .judicialOrderMadeWithUncloakApplication(NO).build();
            assertThat(helper.isApplicationUncloakedWithAdditionalFee(caseData)).isFalse();
        }
    }
}
