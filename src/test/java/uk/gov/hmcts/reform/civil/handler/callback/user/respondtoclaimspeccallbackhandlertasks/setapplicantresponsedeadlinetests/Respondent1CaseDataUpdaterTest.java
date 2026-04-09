package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinetests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks.setapplicantresponsedeadlinespec.Respondent1CaseDataUpdater;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
class Respondent1CaseDataUpdaterTest {

    @Mock
    private DeadlinesCalculator deadlinesCalculator;

    @InjectMocks
    private Respondent1CaseDataUpdater updater;

    private CaseData caseData;
    private Address correspondenceAddress;
    private Address respondent1CopyAddress;

    @BeforeEach
    void setUp() {
        correspondenceAddress = new Address();
        correspondenceAddress.setPostCode("SW1A 1AA");
        correspondenceAddress.setAddressLine1("Correspondence Street");

        respondent1CopyAddress = new Address();
        respondent1CopyAddress.setAddressLine1("Triple street");
        respondent1CopyAddress.setPostCode("AB1 2CD");

        caseData = CaseDataBuilder.builder().build();
        caseData.setSpecAoSApplicantCorrespondenceAddressRequired(NO);
        caseData.setSpecAoSApplicantCorrespondenceAddressdetails(correspondenceAddress);
        Party individual = new Party();
        individual.setType(Party.Type.INDIVIDUAL);
        individual.setPartyName("RESPONDENT_INDIVIDUAL");
        caseData.setRespondent1(individual);
        Party party = new Party();
        party.setPartyName("Party 2");
        party.setPrimaryAddress(respondent1CopyAddress);
        caseData.setRespondent1Copy(party);

        // Mock the deadline calculator
        when(deadlinesCalculator.addMonthsToDateToNextWorkingDayAtMidnight(anyInt(), any()))
                .thenReturn(LocalDateTime.now().plusMonths(36));
    }

    @Test
    void shouldUpdateCaseDataWhenCorrespondenceAddressIsNotRequired() {
        // When
        updater.update(caseData);

        // Then - Check the actual caseData object that was modified
        Party updatedRespondent1 = caseData.getRespondent1();
        assertThat(updatedRespondent1).isNotNull();
        assertThat(updatedRespondent1.getPrimaryAddress()).isNotNull();
        assertThat(updatedRespondent1.getPrimaryAddress()).isEqualTo(correspondenceAddress);
        assertThat(updatedRespondent1.getPrimaryAddress().getPostCode()).isEqualTo("SW1A 1AA");
        assertThat(updatedRespondent1.getPrimaryAddress().getAddressLine1()).isEqualTo("Correspondence Street");
        assertThat(caseData.getRespondent1Copy()).isNull();
        assertThat(caseData.getClaimDismissedDeadline()).isNotNull();
    }

    @Test
    void shouldUpdateCaseDataWhenCorrespondenceAddressIsRequired() {
        // Given - Set to null to trigger the else branch
        caseData.setSpecAoSApplicantCorrespondenceAddressRequired(null);

        // When
        updater.update(caseData);

        // Then - Check the actual caseData object that was modified
        Party updatedRespondent1 = caseData.getRespondent1();
        assertThat(updatedRespondent1).isNotNull();
        assertThat(updatedRespondent1.getPrimaryAddress()).isNotNull();
        assertThat(updatedRespondent1.getPrimaryAddress()).isEqualTo(respondent1CopyAddress);
        assertThat(updatedRespondent1.getPrimaryAddress().getAddressLine1()).isEqualTo("Triple street");
        assertThat(updatedRespondent1.getPrimaryAddress().getPostCode()).isEqualTo("AB1 2CD");
        assertThat(caseData.getRespondent1Copy()).isNull();
        assertThat(caseData.getClaimDismissedDeadline()).isNotNull();
    }

    @Test
    void shouldSetRespondent1CopyToNull() {
        // When
        updater.update(caseData);

        // Then
        assertThat(caseData.getRespondent1Copy()).isNull();
    }

    @Test
    void shouldSetClaimDismissedDeadline() {
        // When
        updater.update(caseData);

        // Then
        assertThat(caseData.getClaimDismissedDeadline()).isNotNull();
    }
}
