package uk.gov.hmcts.reform.civil.service;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.claimstore.ClaimStoreService;
import uk.gov.hmcts.reform.civil.service.pininpost.CUIIdamClientService;
import uk.gov.hmcts.reform.civil.service.pininpost.DefendantPinToPostLRspecService;
import uk.gov.hmcts.reform.civil.service.pininpost.exception.PinNotMatchException;
import uk.gov.hmcts.reform.cmc.model.DefendantLinkStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantPinToPostLRspecServiceTest {

    @InjectMocks
    private DefendantPinToPostLRspecService defendantPinToPostLRspecService;

    @Mock
    private CUIIdamClientService cuiIdamClientService;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private ClaimStoreService claimStoreService;

    @Nested
    class BuildDefendantPinToPost {

        @Test
        void shouldBuildDefendantPinToPost_whenInvoked() {
            DefendantPinToPostLRspec defendantPinToPostLRspec = defendantPinToPostLRspecService.buildDefendantPinToPost();
            assertThat(defendantPinToPostLRspec.getExpiryDate()).isEqualTo(LocalDate.now().plusDays(180));
            assertThat(defendantPinToPostLRspec.getRespondentCaseRole()).isEqualTo(CaseRole.DEFENDANT.getFormattedName());
            assertThat(defendantPinToPostLRspec.getAccessCode()).isNotEmpty();
        }

        @Test
        void shouldRunRemovePinInPostData_whenInvoked() {
            CaseData caseData = createCaseDataWithPin("TEST1234", 180);
            CaseDetails caseDetails = createCaseDetails(caseData);
            DefendantPinToPostLRspec pinInPostData = createPinInPostData(180);

            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

            Map<String, Object> expectedData = new HashMap<>();
            expectedData.put("respondent1PinToPostLRspec", pinInPostData);

            Map<String, Object> updatedData = defendantPinToPostLRspecService.removePinInPostData(caseDetails);

            assertThat(updatedData).isEqualTo(expectedData);
        }

        @Test
        void shouldCheckPinNotValid_whenInvoked() {
            CaseData caseData = createCaseDataWithPin("TEST12341", 180);
            CaseDetails caseDetails = createCaseDetails(caseData);

            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

            assertThrows(PinNotMatchException.class, () -> defendantPinToPostLRspecService.validatePin(caseDetails, "TEST00000"));
        }

        @Test
        void shouldCheckPinNotValidNoAccessCode_whenInvoked() {
            CaseData caseData = createCaseDataWithExpiry(180);
            CaseDetails caseDetails = createCaseDetails(caseData);

            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

            assertThrows(PinNotMatchException.class, () -> defendantPinToPostLRspecService.validatePin(caseDetails, "TEST00000"));
        }

        @Test
        void shouldCheckPinNotValidNoPinToPostObject_whenInvoked() {
            CaseData caseData = createCaseData();
            CaseDetails caseDetails = createCaseDetails(caseData);

            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

            assertThrows(PinNotMatchException.class, () -> defendantPinToPostLRspecService.validatePin(caseDetails, "TEST12342"));
        }

        @Test
        void shouldCheckPinNotValidPinExpired_whenInvoked() {
            CaseData caseData = createCaseDataWithPin("TEST12341", -1);
            CaseDetails caseDetails = createCaseDetails(caseData);

            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);

            assertThrows(PinNotMatchException.class, () -> defendantPinToPostLRspecService.validatePin(caseDetails, "TEST12341"));
        }

        @Test
        void shouldCheckPinIsValidForCMC_whenInvoked() {
            Response response = createResponseWithHeader(HttpStatus.SC_MOVED_TEMPORARILY, "Location");

            when(cuiIdamClientService.authenticatePinUser("TEST1234", "620MC123")).thenReturn(response);

            Assertions.assertDoesNotThrow(() -> defendantPinToPostLRspecService.validateOcmcPin("TEST1234", "620MC123"));
        }

        @Test
        void shouldCheckPinIsNotValidForCMC_whenInvoked() {
            Response response = createResponse(HttpStatus.SC_BAD_REQUEST);

            when(cuiIdamClientService.authenticatePinUser("TEST1234", "620MC123")).thenReturn(response);

            assertThrows(PinNotMatchException.class, () -> defendantPinToPostLRspecService.validateOcmcPin("TEST1234", "620MC123"));
        }

        @Test
        void shouldResetPinExpiryDateSuccessfully() {
            LocalDate expiryDate = LocalDate.of(2022, 1, 1);
            DefendantPinToPostLRspec initialPin = createPinToPost("TEST1234", expiryDate);

            DefendantPinToPostLRspec resetPin = defendantPinToPostLRspecService.resetPinExpiryDate(initialPin);

            assertThat(resetPin.getExpiryDate()).isEqualTo(LocalDate.now().plusDays(180));
            assertThat(resetPin.getAccessCode()).isEqualTo(initialPin.getAccessCode());
        }

        @Test
        void shouldReturnTrueIfOcmcDefendantIsLinked() {
            when(claimStoreService.isOcmcDefendantLinked("620MC123")).thenReturn(createDefendantLinkStatus(true));

            boolean status = defendantPinToPostLRspecService.isOcmcDefendantLinked("620MC123");

            assertTrue(status);
        }

        @Test
        void shouldReturnFalseIfOcmcDefendantIsNotLinked() {
            when(claimStoreService.isOcmcDefendantLinked("620MC123")).thenReturn(createDefendantLinkStatus(false));

            boolean status = defendantPinToPostLRspecService.isOcmcDefendantLinked("620MC123");

            assertFalse(status);
        }

        @Test
        void shouldReturnTrueIfDefendantIsLinked() {
            IdamUserDetails idamUserDetails = new IdamUserDetails();
            idamUserDetails.setId("1234");
            idamUserDetails.setEmail("partyemail@gmail.com");
            CaseData caseData = new CaseDataBuilder()
                .defendantUserDetails(idamUserDetails)
                .build();
            CaseDetails caseDetails = createCaseDetails(caseData);
            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
            boolean status = defendantPinToPostLRspecService.isDefendantLinked(caseDetails);

            assertTrue(status);
        }

        @Test
        void shouldReturnFalseIfDefendantIsNotLinked() {
            CaseData caseData = new CaseDataBuilder().defendantUserDetails(null).build();
            CaseDetails caseDetails = createCaseDetails(caseData);
            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
            boolean status = defendantPinToPostLRspecService.isDefendantLinked(caseDetails);

            assertFalse(status);
        }

        @Test
        void shouldReturnFalseIfUserDetailsEmailIsEmpty() {
            IdamUserDetails idamUserDetails = new IdamUserDetails();
            idamUserDetails.setId("1234");
            CaseData caseData = new CaseDataBuilder().defendantUserDetails(idamUserDetails).build();
            CaseDetails caseDetails = createCaseDetails(caseData);
            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
            boolean status = defendantPinToPostLRspecService.isDefendantLinked(caseDetails);

            assertFalse(status);
        }

        @Test
        void shouldReturnFalseIfUserDetailsIdIsEmpty() {
            IdamUserDetails idamUserDetails = new IdamUserDetails();
            idamUserDetails.setEmail("test@test.com");
            CaseData caseData = new CaseDataBuilder().defendantUserDetails(idamUserDetails).build();
            CaseDetails caseDetails = createCaseDetails(caseData);
            when(caseDetailsConverter.toCaseData(caseDetails)).thenReturn(caseData);
            boolean status = defendantPinToPostLRspecService.isDefendantLinked(caseDetails);

            assertFalse(status);
        }

        private CaseData createCaseDataWithPin(String accessCode, int daysToExpiry) {
            return new CaseDataBuilder().atStateClaimSubmitted()
                .addRespondent1PinToPostLRspec(createPinToPost(accessCode, LocalDate.now().plusDays(daysToExpiry)))
                .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
                .build();
        }

        private CaseData createCaseDataWithExpiry(int daysToExpiry) {
            return new CaseDataBuilder().atStateClaimSubmitted()
                .addRespondent1PinToPostLRspec(createPinWithExpiry(LocalDate.now().plusDays(daysToExpiry)))
                .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
                .build();
        }

        private CaseData createCaseData() {
            return new CaseDataBuilder().atStateClaimSubmitted()
                .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
                .build();
        }

        private CaseDetails createCaseDetails(CaseData caseData) {
            return CaseDetailsBuilder.builder().data(caseData).build();
        }

        private DefendantPinToPostLRspec createPinInPostData(int daysToExpiry) {
            return createPinWithExpiry(LocalDate.now().plusDays(daysToExpiry));
        }

        private DefendantPinToPostLRspec createPinToPost(String accessCode, LocalDate expiryDate) {
            DefendantPinToPostLRspec pin = new DefendantPinToPostLRspec();
            pin.setAccessCode(accessCode);
            pin.setExpiryDate(expiryDate);
            return pin;
        }

        private DefendantPinToPostLRspec createPinWithExpiry(LocalDate expiryDate) {
            DefendantPinToPostLRspec pin = new DefendantPinToPostLRspec();
            pin.setExpiryDate(expiryDate);
            return pin;
        }

        private Response createResponse(int status) {
            return Response.builder().request(Request.create(Request.HttpMethod.GET, "url", Map.of(), null, null, null)).status(status).headers(new HashMap<>()).build();
        }

        private Response createResponseWithHeader(int status, String headerName) {
            Map<String, Collection<String>> headers = new HashMap<>();
            headers.put(headerName, List.of("Location"));
            return Response.builder().request(Request.create(Request.HttpMethod.GET, "url", Map.of(), null, null, null)).status(status).headers(headers).build();
        }

        private DefendantLinkStatus createDefendantLinkStatus(boolean linked) {
            return new DefendantLinkStatus().setLinked(linked);
        }

        @Test
        void shouldHandleFeignExceptionWhenRemovePinInPostDataFails() {
            CaseData caseData = createCaseDataWithPin("TEST1234", 180);
            CaseDetails caseDetails = createCaseDetails(caseData);
            FeignException feignException = new FeignException.BadRequest("Bad request", Request.create(Request.HttpMethod.GET, "url", Map.of(), null, null, null), null, null);

            when(caseDetailsConverter.toCaseData(caseDetails)).thenThrow(feignException);

            assertThrows(FeignException.class, () -> defendantPinToPostLRspecService.removePinInPostData(caseDetails));
        }

        @Test
        void shouldHandleMissingLocationHeaderWhenValidateOcmcPin() {
            Map<String, Collection<String>> headers = new HashMap<>();
            headers.put("Location", Collections.emptyList());
            Response response = Response.builder()
                .request(Request.create(Request.HttpMethod.GET, "url", Map.of(), null, null, null))
                .status(HttpStatus.SC_MOVED_TEMPORARILY)
                .headers(headers)
                .build();

            when(cuiIdamClientService.authenticatePinUser("TEST1234", "620MC123")).thenReturn(response);

            assertThrows(IllegalArgumentException.class, () -> defendantPinToPostLRspecService.validateOcmcPin("TEST1234", "620MC123"));
        }
    }
}
