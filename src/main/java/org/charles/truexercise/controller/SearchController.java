package org.charles.truexercise.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.charles.truexercise.dto.CompanyRequest;
import org.charles.truexercise.dto.CompanyResponse;
import org.charles.truexercise.service.RequestProcessingService;
import org.charles.truexercise.exception.InternalServerException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1")
@Slf4j
@AllArgsConstructor
public class SearchController {

    private final RequestProcessingService requestProcessingService;

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
