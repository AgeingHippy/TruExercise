package com.ageinghippy.truexercise.controller;

import com.ageinghippy.truexercise.controller.SearchController;
import com.ageinghippy.truexercise.TruExerciseApplication;
import com.ageinghippy.truexercise.dto.Company;
import com.ageinghippy.truexercise.dto.CompanyRequest;
import com.ageinghippy.truexercise.dto.CompanyResponse;
import com.ageinghippy.truexercise.dto.Officer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("searchControllerIntegrationTest_data/TruProxyApiCompanyResponse_Dissolved_ltd.txt")) {
            companySearchResponse = new String(Objects.requireNonNull(inputStream).readAllBytes());
        }
        try (InputStream inputStream2 = getClass().getClassLoader().getResourceAsStream("searchControllerIntegrationTest_data/TruProxyApiOfficers_10432398.txt")) {
            officerSearch_10432398 = new String(Objects.requireNonNull(inputStream2).readAllBytes());
        }

        try (InputStream inputStream3 = getClass().getClassLoader().getResourceAsStream("searchControllerIntegrationTest_data/TruProxyApiOfficers_11686010.txt")) {
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
        Officer officer_10432398 = company_11686010.getOfficers().getFirst();
        assert (Objects.equals(officer_10432398.getName(), "LATIF, Amir"));
        assert (Objects.equals(officer_10432398.getOfficer_role(), "director"));
        assert (Objects.equals(officer_10432398.getAppointed_on(), "2018-11-20"));
        //Verify Officer Address
        assert (Objects.equals(officer_10432398.getAddress().getPremises(), "Latif House"));
        assert (Objects.equals(officer_10432398.getAddress().getAddress_line_1(), "First Way"));
        assert (Objects.equals(officer_10432398.getAddress().getLocality(), "Wembley"));
        assert (Objects.equals(officer_10432398.getAddress().getPostal_code(), "HA9 0JD"));
        assert (Objects.equals(officer_10432398.getAddress().getCountry(), "United Kingdom"));

        //random checks on second company
        Company company_10432398 = response.getItems().getLast();
        assert (Objects.equals(company_10432398.getTitle(), companyTitle));
        assert (Objects.equals(company_10432398.getCompany_number(), "10432398"));
        assert (Objects.equals(company_10432398.getCompany_status(), "dissolved"));
        //verify only active officer returned, and is the correct one
        assert (company_10432398.getOfficers().size() == 1);
        assert (Objects.equals(company_10432398.getOfficers().getFirst().getName(), "STEPHENS, Graham Robertson"));


        //Verify data written to the database, and is written correctly
        //verify company data written correctly
        List<Map<String, Object>> companiesList = jdbcTemplate.queryForList("SELECT * FROM company");
        assertEquals(2, companiesList.size());
        //verify first company data
        assertEquals(company_11686010.getCompany_number(), companiesList.getFirst().get("company_number"));
        assertEquals(company_11686010.getTitle(), companiesList.getFirst().get("title"));
        assertEquals(company_11686010.getCompany_status(), companiesList.getFirst().get("company_status"));
        assertEquals(company_11686010.getCompany_type(), companiesList.getFirst().get("company_type"));
        assertEquals(company_11686010.getDate_of_creation(), companiesList.getFirst().get("date_of_creation"));
        //and loosely verify second company
        assertEquals(company_10432398.getCompany_number(), companiesList.getLast().get("company_number"));

        //verify addresses written correctly
        List<Map<String, Object>> addressList = jdbcTemplate.queryForList("SELECT * FROM address");
        assertEquals(4,addressList.size());

        //verify company # 10432398 address written correctly
        Map<String,Object> address_10432398 = addressList.stream()
                .filter(address -> companiesList.getFirst().get("address_id").equals(address.get("address_id")))
                .findAny()
                .orElse(null);
        assertNotNull(address_10432398);
        assert (Objects.equals(company_11686010.getAddress().getPremises(), address_10432398.get("premises")));
        assert (Objects.equals(company_11686010.getAddress().getAddress_line_1(), address_10432398.get("address_line_1")));
        assert (Objects.equals(company_11686010.getAddress().getLocality(), address_10432398.get("locality")));
        assert (Objects.equals(company_11686010.getAddress().getPostal_code(), address_10432398.get("postal_code")));
        assert (Objects.equals(company_11686010.getAddress().getCountry(), address_10432398.get("country")));

        //Verify officers
        List<Map<String, Object>> officerList = jdbcTemplate.queryForList("SELECT * FROM officer");
        assertEquals(2,officerList.size());
        //verify company # 10432398 Officer written correctly
        Map<String,Object> officer_10432398_db = officerList.stream()
                .filter(officer -> company_11686010.getCompany_number().equals(officer.get("company_number")))
                .findAny()
                .orElse(null);
        assertNotNull(officer_10432398_db);
        assertEquals(officer_10432398.getName(),officer_10432398_db.get("name"));
        assertEquals(officer_10432398.getOfficer_role(),officer_10432398_db.get("officer_role"));
        assertEquals(officer_10432398.getCompany_number(),officer_10432398_db.get("company_number"));
        assertEquals(officer_10432398.getAppointed_on(),officer_10432398_db.get("appointed_on"));

        //verify officer_10432398 address saved correctly
        Map<String,Object> address_officer = addressList.stream()
                .filter(address -> officer_10432398_db.get("address_id").equals(address.get("address_id")))
                .findAny()
                .orElse(null);
        assertNotNull(address_officer);
        assert (Objects.equals(officer_10432398.getAddress().getPremises(), address_officer.get("premises")));
        assert (Objects.equals(officer_10432398.getAddress().getAddress_line_1(), address_officer.get("address_line_1")));
        assert (Objects.equals(officer_10432398.getAddress().getLocality(), address_officer.get("locality")));
        assert (Objects.equals(officer_10432398.getAddress().getPostal_code(), address_officer.get("postal_code")));
        assert (Objects.equals(officer_10432398.getAddress().getCountry(), address_officer.get("country")));
    }

    //todo -write integration test fetching data from local db

}
