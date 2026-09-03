package uk.gov.hmcts.reform.civil.controllers.fees;

import feign.FeignException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.exceptions.InternalServerErrorException;
import uk.gov.hmcts.reform.civil.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.civil.exceptions.UpstreamUnavailableException;
import uk.gov.hmcts.reform.civil.model.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.civil.service.AuthorisationService;
import uk.gov.hmcts.reform.civil.service.PaymentRequestUpdateCallbackService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ServiceRequestUpdateCallbackController {

    private final PaymentRequestUpdateCallbackService requestUpdateCallbackService;

    private final AuthorisationService authorisationService;

    @PutMapping(path = "/service-request-update", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Ways to pay will call this API and send the status of payment with other details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed."),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "503", description = "Downstream service unavailable"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public void serviceRequestUpdate(
        @RequestHeader("ServiceAuthorization") String s2sToken,
        @RequestBody ServiceRequestUpdateDto serviceRequestUpdateDto) {
        if (!authorisationService.isServiceAuthorized(s2sToken)) {
            throw new InvalidTokenException("Invalid S2S token");
        }

        try {
            requestUpdateCallbackService.processCallback(serviceRequestUpdateDto, FeeType.HEARING.name());
        } catch (FeignException.GatewayTimeout | FeignException.BadGateway | FeignException.ServiceUnavailable ex) {
            log.warn(
                "Transient downstream failure for CaseID: {}",
                serviceRequestUpdateDto.getCcdCaseNumber(),
                ex
            );
            throw new UpstreamUnavailableException("CCD", serviceRequestUpdateDto.getCcdCaseNumber(), null, ex);
        } catch (Exception ex) {
            log.error(
                "Payment callback is unsuccessful for the CaseID: {}",
                serviceRequestUpdateDto.getCcdCaseNumber(),
                ex
            );
            throw new InternalServerErrorException(ex);
        }
    }
}
