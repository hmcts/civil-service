package uk.gov.hmcts.reform.civil.controllers.fees;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.fees.client.model.Fee2Dto;

@Api
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping(
    path = "/fees",
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class FeesController {

    private final FeesService feesService;

    @GetMapping("/ranges")
    @ApiOperation("Gets a group of claim amount ranges and associated fees for those ranges")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 401, message = "Not Authorized")})
    public ResponseEntity<Fee2Dto[]> getFeeRanges() {
        Fee2Dto[] feeRanges = feesService.getFeeRange();
        return new ResponseEntity<>(feeRanges, HttpStatus.OK);
    }
}
