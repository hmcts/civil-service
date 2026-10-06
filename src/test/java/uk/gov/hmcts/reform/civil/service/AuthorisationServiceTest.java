package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorisationServiceTest {

    @InjectMocks
    AuthorisationService authorisationService;

    @Mock
    ServiceAuthorisationApi serviceAuthorisationApi;

    @Mock
    IdamClient idamClient;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(
            authorisationService,
            "s2sAuthorisedServices",
            List.of("civil_service", "civil-citizen-ui")
        );
        ReflectionTestUtils.setField(
            authorisationService,
            "paymentCallbackAuthorisedServices",
            List.of("payment_app")
        );
    }

    @Test
    void authoriseWhenTheServiceIsCalledFromCitizenUi() {

        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("civil-citizen-ui");
        assertTrue(authorisationService.authoriseService("Bearer abcasda"));

    }

    @Test
    void authoriseWhenTheServiceAuthHeaderIsNull() {
        assertFalse(authorisationService.authoriseService(null));
    }

    @Test
    void doNotAuthoriseWhenTheServiceIsCalledFromUnknownApi() {
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("unknown_api");
        assertFalse(authorisationService.authoriseService("Bearer abc"));

    }

    @Test
    void throwUnAuthorisedExceptionWhenS2sTokenIsMalformed() {
        assertFalse(authorisationService.authoriseService("Bearer malformed"));
    }

    @Test
    void authoriseUserTheServiceIsCalledWithValidToken() {
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid(UUID.randomUUID().toString()).build());
        assertTrue(authorisationService.authoriseUser("Bearer abcasda"));
    }

    @Test
    void authoriseUserTheServiceIsCalledWithNullToken() {
        assertFalse(authorisationService.authoriseUser(null));
    }

    @Test
    void doNotAuthoriseUserWhenCalledWithInvalidToken() {
        assertFalse(authorisationService.authoriseUser("Bearer malformed"));
    }

    @Test
    void checkIsAuthorizedForUserAndServiceReturnFalse() {
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid(UUID.randomUUID().toString()).build());
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("unknown_api");
        assertFalse(authorisationService.isServiceAndUserAuthorized("Bearer abcasda", "s2s token"));
    }

    @Test
    void checkIsAuthorizedForServiceReturnFalse() {
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("unknown_api");
        assertFalse(authorisationService.isServiceAuthorized("s2s token"));
    }

    @Test
    void authorisePaymentCallbackWhenServiceIsPaymentApp() {
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("payment_app");

        assertTrue(authorisationService.isPaymentCallbackServiceAuthorized("s2s token"));
    }

    @Test
    void doNotAuthorisePaymentCallbackForServiceOnGeneralAllowlist() {
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn("civil_service");

        assertTrue(authorisationService.isServiceAuthorized("s2s token"));
        assertFalse(authorisationService.isPaymentCallbackServiceAuthorized("s2s token"));
    }
}
