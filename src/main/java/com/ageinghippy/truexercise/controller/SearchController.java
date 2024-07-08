package com.ageinghippy.truexercise.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.ageinghippy.truexercise.dto.CompanyRequest;
import com.ageinghippy.truexercise.dto.CompanyResponse;
import com.ageinghippy.truexercise.service.RequestProcessingService;
import com.ageinghippy.truexercise.exception.InternalServerException;
import org.springframework.web.bind.annotation.*;

@Tag(name="Company Search", description="TruExercise Company Search API")
@RestController
@RequestMapping("v1")
@Slf4j
@AllArgsConstructor
public class SearchController {

    private final RequestProcessingService requestProcessingService;

    @Operation(
            summary = "Fetch companies",
            description = "Fetches companies and their associated *active* officers only."+
                    "Search is by by company number if provided." +
                    "Match is by exact name or company number. " +
                    "The activeOnly request parameter is optional and can be yes or no, defaulting to no if not provided."
    )
    @PostMapping(value = "/company", produces = "application/json")
    public CompanyResponse getCompany(@RequestHeader(name = "x-api-key") String apiKey,
                                      @RequestParam(defaultValue = "no") String activeOnly,
                                      @RequestBody CompanyRequest companyRequest) {

        log.trace("SearchController.getCompany {}, {}", companyRequest, activeOnly);
        if (apiKey.isBlank()) {
            throw new IllegalArgumentException("x-api-key header value is empty");
        }
        if (!activeOnly.equalsIgnoreCase("yes") && !activeOnly.equalsIgnoreCase("no")) {
            throw new IllegalArgumentException("Invalid active flag supplied. Requires 'yes' or 'no' or not specified");
        } else {
            companyRequest.setActiveOnly(activeOnly);
        }
        if ((companyRequest.getCompanyName() == null || companyRequest.getCompanyName().isBlank())
                && (companyRequest.getCompanyNumber() == null || companyRequest.getCompanyNumber().isBlank())) {
            throw new IllegalArgumentException("At least one of companyName or companyNumber must be supplied");
        }

        companyRequest.setApiKey(apiKey);

        CompanyResponse companyResponse;
        try {
            companyResponse = requestProcessingService.processCompanyRequest(companyRequest);
        } catch (Exception ex) {
            throw new InternalServerException("Internal Server Error", ex);
        }

        return companyResponse;
    }

}
