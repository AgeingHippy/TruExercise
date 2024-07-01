package org.charles.truexercise.service;

import org.charles.truexercise.TruExerciseApplication;
import org.charles.truexercise.dto.CompanyRequest;
import org.charles.truexercise.dto.truProxyApi.TruProxyApiCompanySearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class TruProxyApiServiceTest {

    @Autowired
    private TruProxyApiService truProxyApiService;

    @Autowired
    private RestTemplate restTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
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
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(companySearchResponse)
                );


        ResponseEntity<TruProxyApiCompanySearchResponse> response = truProxyApiService.getCompanies(companyRequest);

        assert(Objects.requireNonNull(response.getBody()).getTotal_results() == 20);
        assert(response.getBody().getItems().size() == 20);

        //validate first element
        assert(Objects.equals(response.getBody().getItems().getFirst().getCompany_number(), "06500244"));
        assert(Objects.equals(response.getBody().getItems().getFirst().getTitle(), "BBC LIMITED"));
        assert(Objects.equals(response.getBody().getItems().getFirst().getCompany_type(), "ltd"));
        assert(Objects.equals(response.getBody().getItems().getFirst().getCompany_status(), "active"));
        assert(Objects.equals(response.getBody().getItems().getFirst().getDate_of_creation(), "2008-02-11"));
        //and related address
        assert(Objects.equals(response.getBody().getItems().getFirst().getAddress().getPremises(), "Boswell Cottage Main Street"));
        assert(Objects.equals(response.getBody().getItems().getFirst().getAddress().getAddress_line_1(), "North Leverton"));
        assert(Objects.equals(response.getBody().getItems().getFirst().getAddress().getLocality(), "Retford"));
        assert(Objects.equals(response.getBody().getItems().getFirst().getAddress().getPostal_code(), "DN22 0AD"));
        assert(Objects.equals(response.getBody().getItems().getFirst().getAddress().getCountry(), "England"));

        //and finally just check the last item to bracket the collection
        assert(Objects.equals(response.getBody().getItems().getLast().getCompany_number(), "13637184"));
    }

    @Test
    void noCompaniesTest() throws URISyntaxException {

        CompanyRequest companyRequest = CompanyRequest.builder().companyName("BBC LIMITED").apiKey("TestApiKey").activeOnly(false).build();

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("https://exercise.trunarrative.cloud/TruProxyAPI/rest/Companies/v1/Search?Query=BBC+LIMITED")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withStatus(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("""
                                        {
                                            "page_number": 1,
                                            "kind": "search#companies"
                                        }""")
                );


        ResponseEntity<TruProxyApiCompanySearchResponse> response = truProxyApiService.getCompanies(companyRequest);

        assert(Objects.requireNonNull(response.getBody()).getTotal_results() == 0);
        assert(response.getBody().getItems() == null);

    }

    //toDo - Add similar tests for Officers. Principle is the same.
}
