package com.ageinghippy.truexercise.service;

import com.ageinghippy.truexercise.dto.*;
import com.ageinghippy.truexercise.dto.truProxyApi.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.ageinghippy.truexercise.utility.Utilities;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@AllArgsConstructor
public class RequestProcessingService {

    private final TruProxyApiService truProxyApiService;
    private final PersistenceService persistenceService;

    public CompanyResponse processCompanyRequest(CompanyRequest companyRequest) {
        log.trace("RequestProcessingService.processCompanyRequest {}", companyRequest);

        CompanyResponse companyResponse = new CompanyResponse();

        //if company number provided search local repository and return
        if (companyRequest.getCompanyNumber() != null && !companyRequest.getCompanyNumber().isBlank()) {
            log.info("RequestProcessingService.processCompanyRequest - trying to fetch from cache");
            Company company = persistenceService.fetchCompany(companyRequest.getCompanyNumber());
            if (company != null && (company.getCompany_status().equals("active") || !companyRequest.isActiveOnly())) {
                companyResponse.setTotal_results(1);
                companyResponse.addCompany(company);
            }
        }

        //if not found locally then make TruProxyAPI call
        if (companyResponse.getTotal_results() == 0) {
            log.info("RequestProcessingService.processCompanyRequest - trying to fetch from TruProxyApi");
            companyResponse = getCompanies(companyRequest);
            if (companyResponse.getTotal_results() > 0) {
                log.info("RequestProcessingService.processCompanyRequest - trying to write to cache");
                //if found in TruProxyApi, store in cache
                persistenceService.saveCompanies(companyResponse.getItems());
            }
        }

        return companyResponse;
    }

    protected CompanyResponse getCompanies(CompanyRequest companyRequest) {
        log.trace("RequestProcessingService.getCompanies {}", companyRequest);

        boolean searchByName;
        String queryParameter;

        if ((companyRequest.getCompanyName() != null && !companyRequest.getCompanyName().isBlank()) &&
                (companyRequest.getCompanyNumber() == null || companyRequest.getCompanyNumber().isBlank())) {
            searchByName = true;
            queryParameter = companyRequest.getCompanyName();
        } else {
            searchByName = false;
            queryParameter = companyRequest.getCompanyNumber();
        }

        ResponseEntity<TruProxyApiCompanySearchResponse> truApicompanySearchResponseEntity = truProxyApiService.getCompanies(queryParameter, companyRequest.getApiKey());

        CompanyResponse companyResponse = new CompanyResponse();

        if (Objects.requireNonNull(truApicompanySearchResponseEntity.getBody()).getTotal_results() != 0) {
            (searchByName //if true we need to filter by companyName, else we filter by company number
                    ? (truApicompanySearchResponseEntity.getBody().getItems().stream().filter(truProxyCompany -> truProxyCompany.getTitle().equals(companyRequest.getCompanyName())).toList())
                    : (truApicompanySearchResponseEntity.getBody().getItems().stream().filter(truProxyCompany -> truProxyCompany.getCompany_number().equals(companyRequest.getCompanyNumber())).toList()))
                    .forEach(truProxyApiCompany -> {
                        if (truProxyApiCompany.getCompany_status().equals("active") || !companyRequest.isActiveOnly()) {
                            Company company = mapToCompany(truProxyApiCompany);
                            getOfficers(company, companyRequest.getApiKey());
                            companyResponse.addCompany(company);
                        }
                    });
        }

        companyResponse.setTotal_results(companyResponse.getItems().size());

        return companyResponse;
    }

    protected void getOfficers(Company company, String apiKey) {
        log.trace("RequestProcessingService.getOfficers {} {}", company, Utilities.maskString(apiKey));
        ResponseEntity<TruProxyApiOfficerSearchResponse> truProxyApiOfficerSearchResponse = truProxyApiService.getOfficers(company.getCompany_number(), apiKey);

        if (Objects.requireNonNull(truProxyApiOfficerSearchResponse.getBody()).getTotal_results()
                - truProxyApiOfficerSearchResponse.getBody().getResigned_count()
                > 0) {
            //Extract and add only active officers
            truProxyApiOfficerSearchResponse.getBody().getItems().forEach(truProxyApiOfficer -> {
                if (truProxyApiOfficer.getResigned_on() == null || truProxyApiOfficer.getResigned_on().isBlank()) {
                    Officer officer = mapToOfficer(truProxyApiOfficer);
                    officer.setCompany_number(company.getCompany_number()); //FK reference for persistence in cache
                    company.addOfficer(officer);
                }
            });
        }

    }

    protected Company mapToCompany(TruProxyApiCompany truProxyApiCompany) {
        Company company = null;

        if (truProxyApiCompany != null) {
            company = new Company();
            company.setTitle(truProxyApiCompany.getTitle());
            company.setCompany_number(truProxyApiCompany.getCompany_number());
            company.setCompany_type(truProxyApiCompany.getCompany_type());
            company.setCompany_status(truProxyApiCompany.getCompany_status());
            company.setDate_of_creation(truProxyApiCompany.getDate_of_creation());
            company.setAddress(mapToAddress(truProxyApiCompany.getAddress()));
        }

        return company;
    }

    protected Address mapToAddress(TruProxyApiAddress truProxyApiAddress) {
        Address address = null;

        if (truProxyApiAddress != null) {
            address = new Address();
            address.setPremises(truProxyApiAddress.getPremises());
            address.setAddress_line_1(truProxyApiAddress.getAddress_line_1());
            address.setLocality(truProxyApiAddress.getLocality());
            address.setPostal_code(truProxyApiAddress.getPostal_code());
            address.setCountry(truProxyApiAddress.getCountry());
        }

        return address;
    }

    protected Officer mapToOfficer(TruProxyApiOfficer truProxyApiOfficer) {
        Officer officer = null;
        if (truProxyApiOfficer != null) {
            officer = new Officer();
            officer.setName(truProxyApiOfficer.getName());
            officer.setOfficer_role(truProxyApiOfficer.getOfficer_role());
            officer.setAppointed_on(truProxyApiOfficer.getAppointed_on());
            officer.setAddress(mapToAddress(truProxyApiOfficer.getAddress()));
        }

        return officer;
    }

}
