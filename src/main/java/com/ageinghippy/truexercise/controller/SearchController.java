package com.ageinghippy.truexercise.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.ageinghippy.truexercise.dto.CompanyRequest;
import com.ageinghippy.truexercise.dto.CompanyResponse;
import com.ageinghippy.truexercise.service.RequestProcessingService;
import com.ageinghippy.truexercise.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name="Company Search", description="TruExercise Company Search API")
@RestController
@RequestMapping("v1")
@Slf4j
@RequiredArgsConstructor //todo - @AllArgsConstructor temporarily removed due to injected property value
public class SearchController {

    private final RequestProcessingService requestProcessingService;

    @Value("${truexercise.env.property}")
    private String truexerciseEnvProperty;

    @Value("${truexercise.env.internal.property}")
    private String internalProperty;

    @Value("${truexercise.env.external.property}")
    private String externalProperty;

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

    @GetMapping(value="/hello")
    public Map<String,String> printProperties() {
        return Map.of(
                "truexercise.env.property", truexerciseEnvProperty
                ,"truexercise.env.internal.property", internalProperty
                , "truexercise.env.external.property", externalProperty
        );
    }

}
