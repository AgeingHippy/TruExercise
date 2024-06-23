package org.charles.controller;

import org.charles.TruExerciseApplication;
import org.charles.dto.Company;
import org.charles.dto.CompanyRequest;
import org.charles.dto.CompanyResponse;
import org.charles.dto.Officer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TruExerciseApplication.class)
public class SearchControllerIntegrationTest {

    @Autowired
    private SearchController searchController;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void ensureDataFetchedFromTruProxyAndIsPreservedAndResponseIsAsExpectedWithMultipleCompanies() throws IOException, URISyntaxException {
        final String companyTitle = "DISSOLVED LTD";
        //Setup mocked responses for TruProxyApiCalls
        String companySearchResponse;
        String officerSearch_10432398;
        String officerSearch_11686010;

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("TruProxyApiCompanyResponse_Dissolved_ltd.txt")) {
            companySearchResponse = new String(Objects.requireNonNull(inputStream).readAllBytes());
        }
        try (InputStream inputStream2 = getClass().getClassLoader().getResourceAsStream("TruProxyApiOfficers_10432398.txt")) {
            officerSearch_10432398 = new String(Objects.requireNonNull(inputStream2).readAllBytes());
        }

        try (InputStream inputStream3 = getClass().getClassLoader().getResourceAsStream("TruProxyApiOfficers_11686010.txt")) {
            officerSearch_11686010 = new String(Objects.requireNonNull(inputStream3).readAllBytes());
        }


        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("https://exercise.trunarrative.cloud/TruProxyAPI/rest/Companies/v1/Search?Query=DISSOLVED+LTD")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(companySearchResponse)
                );


        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("https://exercise.trunarrative.cloud/TruProxyAPI/rest/Companies/v1/Officers?CompanyNumber=11686010")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(officerSearch_11686010)
                );

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("https://exercise.trunarrative.cloud/TruProxyAPI/rest/Companies/v1/Officers?CompanyNumber=10432398")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(officerSearch_10432398)
                );

        //make the searchRequest
        CompanyResponse response =
                searchController.getCompany("TestApiKey", "no", CompanyRequest.builder().companyName(companyTitle).build());

        //verify response content
        assert (response.getTotal_results() == 2); //proves only companies with matching names are selected, and not limited to active status
        assert (response.getItems().size() == 2);

        //verify company 11686010
        Company company_11686010 = response.getItems().getFirst();
        assert (Objects.equals(company_11686010.getTitle(), companyTitle));
        assert (Objects.equals(company_11686010.getCompany_number(), "11686010"));
        assert (Objects.equals(company_11686010.getCompany_status(), "dissolved"));
        assert (Objects.equals(company_11686010.getCompany_type(), "ltd"));
        assert (Objects.equals(company_11686010.getDate_of_creation(), "2018-11-20"));
        //verify company 11686010 address
        assert (Objects.equals(company_11686010.getAddress().getPremises(), "Latif House"));
        assert (Objects.equals(company_11686010.getAddress().getAddress_line_1(), "First Way"));
        assert (Objects.equals(company_11686010.getAddress().getLocality(), "Wembley"));
        assert (Objects.equals(company_11686010.getAddress().getPostal_code(), "HA9 0JD"));
        assert (Objects.equals(company_11686010.getAddress().getCountry(), "United Kingdom"));
        //verify company 11686010 officers
        assert (company_11686010.getOfficers().size() == 1);
        Officer officer = company_11686010.getOfficers().getFirst();
        assert (Objects.equals(officer.getName(), "LATIF, Amir"));
        assert (Objects.equals(officer.getOfficer_role(), "director"));
        assert (Objects.equals(officer.getAppointed_on(), "2018-11-20"));
        //Verify Officer Address
        assert (Objects.equals(officer.getAddress().getPremises(), "Latif House"));
        assert (Objects.equals(officer.getAddress().getAddress_line_1(), "First Way"));
        assert (Objects.equals(officer.getAddress().getLocality(), "Wembley"));
        assert (Objects.equals(officer.getAddress().getPostal_code(), "HA9 0JD"));
        assert (Objects.equals(officer.getAddress().getCountry(), "United Kingdom"));

        //random checks on second company
        Company company_10432398 = response.getItems().getLast();
        assert (Objects.equals(company_10432398.getTitle(), companyTitle));
        assert (Objects.equals(company_10432398.getCompany_number(), "10432398"));
        assert (Objects.equals(company_10432398.getCompany_status(), "dissolved"));
        //verify only active officer returned, and is the correct one
        assert (company_10432398.getOfficers().size() == 1);
        assert (Objects.equals(company_10432398.getOfficers().getFirst().getName(), "STEPHENS, Graham Robertson"));


        //todo - verify data written to the database, and is written correctly
    }

    //todo -write integration test fetching data from local db

}
