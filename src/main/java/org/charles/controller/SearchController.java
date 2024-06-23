package org.charles.controller;

import lombok.extern.slf4j.Slf4j;
import org.charles.dto.CompanyRequest;
import org.charles.dto.CompanyResponse;
import org.charles.exception.InternalServerException;
import org.charles.service.RequestProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1")
@Slf4j
public class SearchController {

    @Autowired
    RequestProcessingService requestProcessingService;

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
