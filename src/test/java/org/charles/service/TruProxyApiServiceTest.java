package org.charles.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.charles.TruExerciseApplication;
import org.charles.dto.CompanyRequest;
import org.charles.dto.truProxyApi.TruProxyApiCompanySearchResponse;
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
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void getCompaniesTest() throws IOException, URISyntaxException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("TruProxyApiCompanyResponse.txt");

        String companySearchResponse = new String(inputStream.readAllBytes());

        CompanyRequest companyRequest = CompanyRequest.builder().companyName("BBC LIMITED").apiKey("TestApiKey").activeOnly(false).build();

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI("https://exercise.trunarrative.cloud/TruProxyAPI/rest/Companies/v1/Search?Query=BBC+LIMITED")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(
                        withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(companySearchResponse))
                );


        ResponseEntity<TruProxyApiCompanySearchResponse> response = truProxyApiService.getCompanies(companyRequest);

        assert (1 == 1);

    }
}
