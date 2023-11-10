package uk.gov.hmcts.reform.civil.controllers.fees;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.List;

@Tag(name = "Fees Controller")
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
    @Operation(summary = "Gets a group of claim amount ranges and associated fees for those ranges")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized")})
    public ResponseEntity<List<Fee2Dto>> getFeeRanges() {
        List<Fee2Dto> feeRanges = feesService.getFeeRange();
        return new ResponseEntity<>(feeRanges, HttpStatus.OK);
    }

    @GetMapping("/claim/{claimAmount}")
    @Operation(summary = "Gets the claim fee associated with an amount")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized")})
    public ResponseEntity<Fee> getClaimFee(@PathVariable("claimAmount") BigDecimal claimAmount) {
        Fee fee = feesService.getFeeDataByTotalClaimAmount(claimAmount);
        return new ResponseEntity<>(fee, HttpStatus.OK);
    }

    @GetMapping("/hearing/{claimAmount}")
    @Operation(summary = "Gets the hearing fee associated with an amount")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Not Authorized")})
    public ResponseEntity<Fee> getHearingFee(@PathVariable("claimAmount") BigDecimal claimAmount) {
        Fee fee = feesService.getHearingFeeDataByTotalClaimAmount(claimAmount);
        return new ResponseEntity<>(fee, HttpStatus.OK);
    }
}
