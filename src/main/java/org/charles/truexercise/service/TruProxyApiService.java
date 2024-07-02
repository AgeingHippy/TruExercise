package org.charles.truexercise.service;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.charles.truexercise.dto.truProxyApi.TruProxyApiCompanySearchResponse;
import org.charles.truexercise.dto.truProxyApi.TruProxyApiOfficerSearchResponse;
import org.charles.truexercise.utility.Utilities;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@AllArgsConstructor
public class TruProxyApiService {
    //todo - implement WebClient
    static final String companySearchUrl = "https://exercise.trunarrative.cloud/TruProxyAPI/rest/Companies/v1/Search?Query=%s";
    static final String officerSearchUrl = "https://exercise.trunarrative.cloud/TruProxyAPI/rest/Companies/v1/Officers?CompanyNumber=%s";

    private final RestTemplate restTemplate;

    public ResponseEntity<TruProxyApiCompanySearchResponse> getCompanies(String queryParameter, String apiKey) {
        log.trace("TruProxyApiService.getCompanies {} {}", queryParameter, Utilities.maskString(apiKey));

        String searchBy = URLEncoder.encode(queryParameter, StandardCharsets.UTF_8);

        String url = String.format(companySearchUrl, searchBy);

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-api-key", URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
        HttpEntity<Object> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, TruProxyApiCompanySearchResponse.class);
    }

    public ResponseEntity<TruProxyApiOfficerSearchResponse> getOfficers(String companyNumber, String apiKey) {
        log.trace("TruProxyApiService.getOfficers");
        String url = String.format(officerSearchUrl, companyNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-api-key", URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
        HttpEntity<Object> entity = new HttpEntity<>(headers);


        return restTemplate.exchange(url, HttpMethod.GET, entity, TruProxyApiOfficerSearchResponse.class);
    }

}

