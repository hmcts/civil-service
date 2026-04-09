package uk.gov.hmcts.reform.civil.service.mediation.helpers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.Party.Type;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.citizenui.MediationLiPCarm;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.mediation.MediationLitigant;
import uk.gov.hmcts.reform.civil.utils.ElementUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.SINGLE_DATE;

@ExtendWith(MockitoExtension.class)
class UnrepresentedLitigantPopulatorTest {

    @InjectMocks
    private UnrepresentedLitigantPopulator unrepresentedLitigantPopulator;
    @Mock
    private MediationLiPCarm mediationLiPCarm;

    @Test
    void shouldPopulateLitigantWhereMediationLipCarmIsNull() {
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        party.setPartyName("John Doe");
        party.setPartyEmail("party@email.com");
        party.setPartyPhone("123456789");

        MediationLitigant litigant = unrepresentedLitigantPopulator.populator(
            new MediationLitigant(),
            party,
            "Contact Person",
            null
        );

        assertThat(litigant.getMediationContactEmail()).isEqualTo("party@email.com");
        assertThat(litigant.getMediationContactNumber()).isEqualTo("123456789");
        assertThat(litigant.getDateRangeToAvoid()).hasSize(1);
        Assertions.assertNull(litigant.getDateRangeToAvoid().get(0).getDateFrom());
        Assertions.assertNull(litigant.getDateRangeToAvoid().get(0).getDateTo());
    }

    @Test
    void shouldPopulateLitigantWithCorrectMediationInfo_whenAllFieldsCorrect() {

        when(mediationLiPCarm.getIsMediationEmailCorrect()).thenReturn(YES);
        when(mediationLiPCarm.getIsMediationPhoneCorrect()).thenReturn(YES);
        when(mediationLiPCarm.getHasUnavailabilityNextThreeMonths()).thenReturn(YES);

        LocalDate fixedDate = LocalDate.of(2024, 6, 10);
        UnavailableDate unavailableDate = new UnavailableDate()
            .setDate(fixedDate)
            .setUnavailableDateType(SINGLE_DATE);
        Element<UnavailableDate> elementUnavailableDate = ElementUtils.element(unavailableDate);
        when(mediationLiPCarm.getUnavailableDatesForMediation()).thenReturn(List.of(elementUnavailableDate));

        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        party.setPartyName("John Doe");
        party.setPartyEmail("party@email.com");
        party.setPartyPhone("123456789");

        MediationLitigant litigant = unrepresentedLitigantPopulator.populator(
            new MediationLitigant(),
            party,
            "Contact Person",
            mediationLiPCarm
        );

        assertThat(litigant.getMediationContactEmail()).isEqualTo("party@email.com");
        assertThat(litigant.getMediationContactNumber()).isEqualTo("123456789");
        assertThat(litigant.getDateRangeToAvoid()).hasSize(1);
        assertThat(litigant.getDateRangeToAvoid().get(0).getDateFrom()).isEqualTo("2024-06-10");
        assertThat(litigant.getDateRangeToAvoid().get(0).getDateTo()).isEqualTo("2024-06-10");
    }

    @Test
    void shouldPopulateLitigantWithAlternativeMediationInfo_whenEmailAndPhoneIncorrect() {
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        party.setPartyName("John Doe");
        party.setPartyEmail("party@email.com");
        party.setPartyPhone("123456789");

        when(mediationLiPCarm.getIsMediationEmailCorrect()).thenReturn(NO);
        when(mediationLiPCarm.getIsMediationPhoneCorrect()).thenReturn(NO);
        when(mediationLiPCarm.getAlternativeMediationEmail()).thenReturn("alt@email.com");
        when(mediationLiPCarm.getAlternativeMediationTelephone()).thenReturn("987654321");
        when(mediationLiPCarm.getHasUnavailabilityNextThreeMonths()).thenReturn(NO);

        MediationLitigant litigant = unrepresentedLitigantPopulator.populator(
            new MediationLitigant(),
            party,
            "Contact Person",
            mediationLiPCarm
        );

        assertThat(litigant.getMediationContactEmail()).isEqualTo("alt@email.com");
        assertThat(litigant.getMediationContactNumber()).isEqualTo("987654321");

        assertThat(litigant.getDateRangeToAvoid()).hasSize(1);
        assertThat(litigant.getDateRangeToAvoid().get(0).getDateFrom()).isNull();
        assertThat(litigant.getDateRangeToAvoid().get(0).getDateTo()).isNull();
    }

    @Test
    void shouldHandleNoUnavailabilityForMediation() {
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        party.setPartyName("John Doe");
        party.setPartyEmail("party@email.com");
        party.setPartyPhone("123456789");

        when(mediationLiPCarm.getIsMediationEmailCorrect()).thenReturn(YES);
        when(mediationLiPCarm.getIsMediationPhoneCorrect()).thenReturn(YES);
        when(mediationLiPCarm.getHasUnavailabilityNextThreeMonths()).thenReturn(NO);

        MediationLitigant litigant = unrepresentedLitigantPopulator.populator(
            new MediationLitigant(),
            party,
            "Contact Person",
            mediationLiPCarm
        );

        assertThat(litigant.getMediationContactEmail()).isEqualTo("party@email.com");
        assertThat(litigant.getMediationContactNumber()).isEqualTo("123456789");
        assertThat(litigant.getDateRangeToAvoid().get(0).getDateFrom()).isNull();
        assertThat(litigant.getDateRangeToAvoid().get(0).getDateTo()).isNull();
    }

    @Test
    void shouldUseOriginalMediationContactForNonIndividual() {
        Party party = new Party();
        party.setType(Party.Type.ORGANISATION);
        party.setPartyName("Doe Inc.");
        party.setPartyEmail("corp@email.com");
        party.setPartyPhone("123456789");

        when(mediationLiPCarm.getIsMediationContactNameCorrect()).thenReturn(YES);
        when(mediationLiPCarm.getIsMediationEmailCorrect()).thenReturn(YES);
        when(mediationLiPCarm.getIsMediationPhoneCorrect()).thenReturn(YES);
        when(mediationLiPCarm.getHasUnavailabilityNextThreeMonths()).thenReturn(NO);

        MediationLitigant litigant = unrepresentedLitigantPopulator.populator(
            new MediationLitigant(),
            party,
            "Jane Doe",
            mediationLiPCarm
        );

        assertThat(litigant.getMediationContactName()).isEqualTo("Jane Doe");
        assertThat(litigant.getMediationContactEmail()).isEqualTo("corp@email.com");
        assertThat(litigant.getMediationContactNumber()).isEqualTo("123456789");
    }

    @Test
    void shouldUseAlternativeMediationContactName_whenIncorrect() {
        Party party = new Party();
        party.setType(Type.ORGANISATION);
        party.setIndividualFirstName("John");
        party.setIndividualLastName("Doe");
        party.setPartyEmail("party@email.com");
        party.setPartyPhone("123456789");

        when(mediationLiPCarm.getIsMediationContactNameCorrect()).thenReturn(NO);
        when(mediationLiPCarm.getAlternativeMediationContactPerson()).thenReturn("Alternative Contact");

        MediationLitigant litigant = unrepresentedLitigantPopulator.populator(
            new MediationLitigant(),
            party,
            "Original Contact",
            mediationLiPCarm
        );

        assertThat(litigant.getMediationContactName()).isEqualTo("Alternative Contact");
    }
}
