package org.charles.truexercise.service;

import org.charles.truexercise.dto.CompanyRequest;
import org.charles.truexercise.dto.truProxyApi.TruProxyApiCompanySearchResponse;
import org.charles.truexercise.dto.truProxyApi.TruProxyApiOfficerSearchResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

public class TruProxyApiServiceTest {

    private static TruProxyApiService truProxyApiService;

    private static RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeAll
    public static void init() {
        restTemplate = new RestTemplate();
        truProxyApiService = new TruProxyApiService(restTemplate);
    }

    @BeforeEach
    public void init2() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void verifySearchByCompanyNumberIfProvided() throws IOException, URISyntaxException {
        String companySearchResponse;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("TruProxyApiCompanyNumberResponse.txt")) {
            companySearchResponse = new String(Objects.requireNonNull(inputStream).readAllBytes());
        }

        CompanyRequest companyRequest = CompanyRequest.builder().companyName("BBC LIMITED").companyNumber("06500244").apiKey("TestApiKey").activeOnly(false).build();

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("https://exercise.trunarrative.cloud/TruProxyAPI/rest/Companies/v1/Search?Query=06500244")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("x-api-key", companyRequest.getApiKey()))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(companySearchResponse)
                );

        ResponseEntity<TruProxyApiCompanySearchResponse> response = truProxyApiService.getCompanies(companyRequest.getCompanyNumber(), companyRequest.getApiKey());

        assertEquals(1, Objects.requireNonNull(response.getBody()).getTotal_results());
        assertEquals(1, response.getBody().getItems().size());
        assertEquals("BBC LIMITED", response.getBody().getItems().getFirst().getTitle());
    }

    @Test
    void companiesAndAddressesAreMappedCorrectly() throws IOException, URISyntaxException {
        String companySearchResponse;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("TruProxyApiCompanyResponse.txt")) {
            companySearchResponse = new String(Objects.requireNonNull(inputStream).readAllBytes());
        }

        CompanyRequest companyRequest = CompanyRequest.builder().companyName("BBC LIMITED").apiKey("TestApiKey").activeOnly(false).build();

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("https://exercise.trunarrative.cloud/TruProxyAPI/rest/Companies/v1/Search?Query=BBC+LIMITED")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("x-api-key", companyRequest.getApiKey()))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(companySearchResponse)
                );


        ResponseEntity<TruProxyApiCompanySearchResponse> response = truProxyApiService.getCompanies(companyRequest.getCompanyName(), companyRequest.getApiKey());

        assertEquals(20, Objects.requireNonNull(response.getBody()).getTotal_results());
        assertEquals(20, response.getBody().getItems().size());

        //validate first element
        assertEquals("06500244", response.getBody().getItems().getFirst().getCompany_number());
        assertEquals("BBC LIMITED", response.getBody().getItems().getFirst().getTitle());
        assertEquals("ltd", response.getBody().getItems().getFirst().getCompany_type());
        assertEquals("active", response.getBody().getItems().getFirst().getCompany_status());
        assertEquals("2008-02-11", response.getBody().getItems().getFirst().getDate_of_creation());
        //and related address
        assertEquals("Boswell Cottage Main Street", response.getBody().getItems().getFirst().getAddress().getPremises());
        assertEquals("North Leverton", response.getBody().getItems().getFirst().getAddress().getAddress_line_1());
        assertEquals("Retford", response.getBody().getItems().getFirst().getAddress().getLocality());
        assertEquals("DN22 0AD", response.getBody().getItems().getFirst().getAddress().getPostal_code());
        assertEquals("England", response.getBody().getItems().getFirst().getAddress().getCountry());

        //and finally just check the last item to bracket the collection
        assertEquals("13637184", response.getBody().getItems().getLast().getCompany_number());
    }

    @Test
    void noCompaniesTest() throws URISyntaxException {

        CompanyRequest companyRequest = CompanyRequest.builder().companyName("BBC LIMITED").apiKey("TestApiKey").activeOnly(false).build();

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("https://exercise.trunarrative.cloud/TruProxyAPI/rest/Companies/v1/Search?Query=BBC+LIMITED")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("x-api-key", companyRequest.getApiKey()))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("""
                                        {
                                            "page_number": 1,
                                            "kind": "search#companies"
                                        }""")
                );


        ResponseEntity<TruProxyApiCompanySearchResponse> response = truProxyApiService.getCompanies(companyRequest.getCompanyName(), companyRequest.getApiKey());

        assertEquals(0, Objects.requireNonNull(response.getBody()).getTotal_results());
        assertNull(response.getBody().getItems());

    }

    @Test
    void verifyOfficersAndAddressesAreMappedCorrectly() throws IOException, URISyntaxException {
        String officerSearchResponse;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("TruProxyApiOfficerResponse.txt")) {
            officerSearchResponse = new String(Objects.requireNonNull(inputStream).readAllBytes());
        }

        String companyNumber = "06500244";
        String apiKey = "testApiKey";

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("https://exercise.trunarrative.cloud/TruProxyAPI/rest/Companies/v1/Officers?CompanyNumber=06500244")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("x-api-key", apiKey))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(officerSearchResponse)
                );


        ResponseEntity<TruProxyApiOfficerSearchResponse> response = truProxyApiService.getOfficers(companyNumber, apiKey);

        assertEquals(8, Objects.requireNonNull(response.getBody()).getTotal_results());
        assertEquals(8, response.getBody().getItems().size());

        //validate first element
        assertEquals("RE SECRETARIES LIMITED", response.getBody().getItems().getFirst().getName());
        assertEquals("corporate-secretary", response.getBody().getItems().getFirst().getOfficer_role());
        assertEquals("2021-08-25", response.getBody().getItems().getFirst().getAppointed_on());
        assertNull(response.getBody().getItems().getFirst().getResigned_on());
        //and related address
        assertEquals("1-3", response.getBody().getItems().getFirst().getAddress().getPremises());
        assertEquals("Strand", response.getBody().getItems().getFirst().getAddress().getAddress_line_1());
        assertEquals("London", response.getBody().getItems().getFirst().getAddress().getLocality());
        assertEquals("WC2N 5JR", response.getBody().getItems().getFirst().getAddress().getPostal_code());
        assertEquals("England", response.getBody().getItems().getFirst().getAddress().getCountry());

        //and finally just check the last item to bracket the collection and the resigned_on property
        assertEquals("2017-04-04", response.getBody().getItems().getLast().getResigned_on());
    }

}
