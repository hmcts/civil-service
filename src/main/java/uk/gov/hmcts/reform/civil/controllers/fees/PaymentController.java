package uk.gov.hmcts.reform.civil.controllers.fees;

import feign.FeignException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.service.PaymentsService;

import java.util.Map;

@Api
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(
    path = "/fees",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class PaymentController {

    private final PaymentsService paymentService;

    @PutMapping(value = "/service-request-update", consumes = "application/json")
    @ApiOperation("Gets updates from Ways to Pay on payment status")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", value = "Authorization token",
            required = true, dataType = "string", paramType = "header") })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 401, message = "Not Authorized")})
    public void updateServiceRequest(@RequestBody Map<String, Object> paymentUpdate) {
        try {
            paymentService.updateStatus(paymentUpdate);
        } catch (FeignException e) {
            log.error(String.format("Updating payment status failed: %s", e.contentUTF8()));
            throw e;
        }
    }

}
