package uk.gov.hmcts.reform.civil.controllers.fees;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.service.FeesService;
import uk.gov.hmcts.reform.fees.client.model.Fee2Dto;

import java.math.BigDecimal;

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
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", value = "Authorization token",
            required = true, dataType = "string", paramType = "header") })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 401, message = "Not Authorized")})
    public ResponseEntity<Fee2Dto[]> getFeeRanges() {
        Fee2Dto[] feeRanges = feesService.getFeeRange();
        return new ResponseEntity<>(feeRanges, HttpStatus.OK);
    }

    @GetMapping("/claim/{claimAmount}")
    @ApiOperation("Gets the claim fee associated with an amount")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", value = "Authorization token",
            required = true, dataType = "string", paramType = "header") })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 401, message = "Not Authorized")})
    public ResponseEntity<Fee> getClaimFee(@PathVariable("claimAmount") BigDecimal claimAmount) {
        Fee fee = feesService.getFeeDataByTotalClaimAmount(claimAmount);
        return new ResponseEntity<>(fee, HttpStatus.OK);
    }

    @GetMapping("/hearing/{claimAmount}")
    @ApiOperation("Gets the hearing fee associated with an amount")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "Authorization", value = "Authorization token",
            required = true, dataType = "string", paramType = "header") })
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 401, message = "Not Authorized")})
    public ResponseEntity<Fee> getHearingFee(@PathVariable("claimAmount") BigDecimal claimAmount) {
        Fee fee = feesService.getHearingFeeDataByTotalClaimAmount(claimAmount);
        return new ResponseEntity<>(fee, HttpStatus.OK);
    }
}
