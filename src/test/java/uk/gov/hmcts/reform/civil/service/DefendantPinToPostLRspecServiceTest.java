package uk.gov.hmcts.reform.civil.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import feign.Request;
import feign.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.retry.annotation.EnableRetry;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.config.CMCPinVerifyConfiguration;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.claimstore.ClaimStoreService;
import uk.gov.hmcts.reform.civil.service.pininpost.CUIIdamClientService;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.service.pininpost.exception.PinNotMatchException;
import uk.gov.hmcts.reform.cmc.model.DefendantLinkStatus;
import uk.gov.hmcts.reform.idam.client.IdamApi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
    DefendantPinToPostLRspecService.class,
    JacksonAutoConfiguration.class,
    CUIIdamClientService.class
})
@EnableRetry
class DefendantPinToPostLRspecServiceTest {

    @Autowired
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;

    @Autowired
    private CUIIdamClientService cuiIdamClientService;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private ClaimStoreService claimStoreService;

    @MockBean
    private IdamApi idamApi;

    @MockBean
    private CMCPinVerifyConfiguration cmcPinVerifyConfiguration;

    @Mock
    Request request;

    @Nested
    class BuildDefendantPinToPost {

        @Test
        void shouldBuildDefendantPinToPost_whenInvoked() {
            DefendantPinToPostLRspec defendantPinToPostLRspec = defendantPinToPostLRspecService
                .buildDefendantPinToPost();
            assertThat(defendantPinToPostLRspec.getExpiryDate())
                .isEqualTo(getDate180days());
            assertThat(defendantPinToPostLRspec.getRespondentCaseRole())
                .isEqualTo(CaseRole.DEFENDANT.getFormattedName());
            assertThat(defendantPinToPostLRspec.getAccessCode())
                .isNotEmpty();
        }

        @Test
        void shouldRunRemovePinInPostData_whenInvoked() {
            CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
                .addRespondent1PinToPostLRspec(DefendantPinToPostLRspec.builder()
                                                   .accessCode("TEST1234")
                                                   .expiryDate(LocalDate.now().plusDays(180))
                                                   .build())
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .build();
            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
            DefendantPinToPostLRspec pinInPostData = DefendantPinToPostLRspec.builder()
                .expiryDate(LocalDate.now().plusDays(180))
                .build();

            Map<String, Object> data = new HashMap<>();
            data.put("respondent1PinToPostLRspec", pinInPostData);
            var updatedData = defendantPinToPostLRspecService.removePinInPostData(caseDetails);
            assertThat(updatedData).isEqualTo(data);
        }

        @Test
        void shouldCheckPinNotValid_whenInvoked() {
            CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .addRespondent1PinToPostLRspec(DefendantPinToPostLRspec.builder()
                                                   .accessCode("TEST12341")
                                                   .expiryDate(LocalDate.now().plusDays(180))
                                                   .build())
                .build();

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

            assertThrows(
                PinNotMatchException.class,
                () -> defendantPinToPostLRspecService.validatePin(caseDetails, "TEST00000")
            );
        }

        @Test
        void shouldCheckPinNotValidNoAccessCode_whenInvoked() {
            CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .addRespondent1PinToPostLRspec(DefendantPinToPostLRspec.builder()
                                                   .expiryDate(LocalDate.now().plusDays(180))
                                                   .build())
                .build();

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

            assertThrows(
                PinNotMatchException.class,
                () -> defendantPinToPostLRspecService.validatePin(caseDetails, "TEST00000")
            );
        }

        @Test
        void shouldCheckPinNotValidNoPinToPostObject_whenInvoked() {
            CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .build();

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

            assertThrows(
                PinNotMatchException.class,
                () -> defendantPinToPostLRspecService.validatePin(caseDetails, "TEST12342")
            );
        }

        @Test
        void shouldCheckPinNotValidPinExpired_whenInvoked() {
            CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .addRespondent1PinToPostLRspec(DefendantPinToPostLRspec.builder()
                                                   .accessCode("TEST12341")
                                                   .expiryDate(LocalDate.now().minusDays(1))
                                                   .build())
                .build();

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

            assertThrows(
                PinNotMatchException.class,
                () -> defendantPinToPostLRspecService.validatePin(caseDetails, "TEST12341")
            );
        }

        @Test
        void shouldCheckPinIsValidForCMC_whenInvoked() {
            CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .build();

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            Map<String, Collection<String>> headers = new HashMap<>();
            List<String> header = Arrays.asList("Location");
            headers.put("Location", header);

            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
            when(cmcPinVerifyConfiguration.getClientId()).thenReturn("cmc_citizen");
            when(idamApi.authenticatePinUser(eq("TEST1234"), eq("cmc_citizen"), anyString(), eq("620MC123")))
                .thenReturn(Response.builder().request(request).status(HttpStatus.SC_MOVED_TEMPORARILY).headers(headers)
                                .build());

            Assertions.assertDoesNotThrow(() -> defendantPinToPostLRspecService.validateOcmcPin(
                "TEST1234",
                "620MC123"
            ));
        }

        @Test
        void shouldCheckPinIsNotValidForCMC_whenInvoked() {
            CaseData caseData = new CaseDataBuilder().atStateClaimSubmitted()
                .businessProcess(BusinessProcess.builder().status(BusinessProcessStatus.READY).build())
                .build();

            CaseDetails caseDetails = CaseDetailsBuilder.builder().data(caseData).build();

            Map<String, Collection<String>> headers = new HashMap<>();
            List<String> header = Arrays.asList("Location");
            headers.put("Location", header);

            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
            when(cmcPinVerifyConfiguration.getClientId()).thenReturn("cmc_citizen");
            when(idamApi.authenticatePinUser(eq("DummyPin"), eq("cmc_citizen"), anyString(), eq("620MC123")))
                .thenReturn(Response.builder().request(request).status(HttpStatus.SC_UNAUTHORIZED).headers(headers)
                                .build());

            Assertions.assertThrows(
                PinNotMatchException.class,
                () -> defendantPinToPostLRspecService.validateOcmcPin("DummyPin", "620MC123")
            );
            verify(idamApi).authenticatePinUser(
                eq("DummyPin"),
                eq("cmc_citizen"),
                anyString(),
                eq("620MC123")
            );
        }
    }

    @Test
    void shouldResetPinExpiryDateSuccessfully() {
        LocalDate expiryDate = LocalDate.of(
            2022,
            1,
            1
        );
        DefendantPinToPostLRspec initialPin = DefendantPinToPostLRspec.builder()
            .accessCode("TEST1234")
            .expiryDate(expiryDate)
            .build();

        DefendantPinToPostLRspec resetPin = defendantPinToPostLRspecService.resetPinExpiryDate(initialPin);

        assertThat(resetPin.getExpiryDate()).isEqualTo(LocalDate.now().plusDays(180));
        assertThat(resetPin.getAccessCode()).isEqualTo(initialPin.getAccessCode());
    }

    @Test
    void shouldReturnTrueIfDefenentIsLinked() {
        when(claimStoreService.isOcmcDefendantLinked("620MC123")).thenReturn(DefendantLinkStatus.builder().linked(true).build());

        boolean status = defendantPinToPostLRspecService.isOcmcDefendantLinked("620MC123");

        assertTrue(status);
    }

    @Test
    void shouldReturnFalseIfDefenentIsNotLinked() {
        when(claimStoreService.isOcmcDefendantLinked("620MC123")).thenReturn(DefendantLinkStatus.builder().linked(false).build());

        boolean status = defendantPinToPostLRspecService.isOcmcDefendantLinked("620MC123");

        assertFalse(status);
    }

    private LocalDate getDate180days() {
        return LocalDate.now()
            .plusDays(180);
    }

}
