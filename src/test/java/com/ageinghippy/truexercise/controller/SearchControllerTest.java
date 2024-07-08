package com.ageinghippy.truexercise.controller;

import com.ageinghippy.truexercise.dto.*;
import com.ageinghippy.truexercise.service.RequestProcessingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.stream.Stream;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(SearchController.class)
public class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    RequestProcessingService requestProcessingService;

    @ParameterizedTest
    @MethodSource("validateInputParametersDataProvider")
        //Validate input parameters handled correctly
    void validateInputParameters(String apiKey, String activeOnly, String companyName, String companyNumber, ResultMatcher expectedResult, int serviceInvocations) throws Exception {
        CompanyResponse companyResponse = createCompanyResponse(companyName, companyNumber, activeOnly);

        when(this.requestProcessingService.processCompanyRequest(any(CompanyRequest.class))).thenReturn(companyResponse);

        ArgumentCaptor<CompanyRequest> capturedCompanyRequest = ArgumentCaptor.forClass(CompanyRequest.class);

        if (apiKey == null) {
            //call without x-api-key header
            mockMvc.perform(
                            MockMvcRequestBuilders
                                    .post("/v1/company")
                                    .queryParam("activeOnly", activeOnly)
                                    .content(String.format("{\"companyName\":\"%s\",\"companyNumber\":\"%s\"}", companyName, companyNumber))
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(expectedResult);
        } else if (companyName == null && companyNumber == null) {
            //call without body
            mockMvc.perform(
                            MockMvcRequestBuilders
                                    .post("/v1/company")
                                    .header("x-api-key", apiKey)
                                    .queryParam("activeOnly", activeOnly)
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(expectedResult);
        } else if (activeOnly == null) {
            //call without query parameter
            mockMvc.perform(
                            MockMvcRequestBuilders
                                    .post("/v1/company")
                                    .header("x-api-key", apiKey)
                                    .content(String.format("{\"companyName\":\"%s\",\"companyNumber\":\"%s\"}", companyName, companyNumber))
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(expectedResult);
        } else {
            //call with all specified
            mockMvc.perform(
                            MockMvcRequestBuilders
                                    .post("/v1/company")
                                    .header("x-api-key", apiKey)
                                    .queryParam("activeOnly", activeOnly)
                                    .content(String.format("{\"companyName\":\"%s\",\"companyNumber\":\"%s\"}", companyName, companyNumber))
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(expectedResult);
        }

        verify(requestProcessingService, times(serviceInvocations)).processCompanyRequest(any(CompanyRequest.class));

        if (serviceInvocations > 0) {
            verify(requestProcessingService).processCompanyRequest(capturedCompanyRequest.capture());

            assertEquals("Name matches", capturedCompanyRequest.getValue().getCompanyName(), companyName);
            assertEquals("Number matches", capturedCompanyRequest.getValue().getCompanyNumber(), companyNumber);
            assertEquals("API Key matches", capturedCompanyRequest.getValue().getApiKey(), apiKey);
            assertEquals("ActiveOnly matches", capturedCompanyRequest.getValue().isActiveOnly(), companyResponse.getItems().getFirst().getCompany_status().equalsIgnoreCase("active"));
        }

    }

    // Static method providing the parameters for validateActiveSetCorrectly
    private static Stream<Object[]> validateInputParametersDataProvider() {
        return Stream.of(
                new Object[]{"TestKey", "", "Test Company Name", "12345", status().isOk(), 1},    // Test case 1 - empty activeOnly param
                new Object[]{"TestKey", "", "", "12345", status().isOk(), 1},    // Test case 2 - empty company name
                new Object[]{"TestKey", "yes", "Test Company Name", "", status().isOk(), 1},   // Test case 3 - Empty company Number
                new Object[]{"TestKey", "yes", "", "", status().isExpectationFailed(), 0},   // Test case 4 - Empty company Name and Number
                new Object[]{"TestKey", "NO", "Test Company Name", "", status().isOk(), 1},   // Test case 5 - uppercase inactiveOnly value
                new Object[]{"", "no", "Test Company Name", "", status().isExpectationFailed(), 0},    // Test case 6 - Empty x-api-key header
                new Object[]{"TestKey", "biscuit", "Test Company Name", "12345", status().isExpectationFailed(), 0},// Test case 7 - invalid activeOnly value
                new Object[]{null, "yes", "Test Company Name", "12345", status().isForbidden(), 0},    // Test case 8 - no x-api-key header provided
                new Object[]{"TestKey", null, "Test Company Name", "12345", status().isOk(), 1},    // Test case 9 - no query parameter provided
                new Object[]{"TestKey", "", null, null, status().isBadRequest(), 0}// Test case 10 - no body provided
        );
    }

    private CompanyResponse createCompanyResponse(String companyName, String companyNumber, String activeOnly) {
        CompanyResponse companyResponse = CompanyResponse.builder().total_results(1).build();
        companyResponse.addCompany(createCompany(companyName, companyNumber, activeOnly));
        return companyResponse;
    }

    private Company createCompany(String companyName, String companyNumber, String activeOnly) {
        Company company = Company.builder().title(companyName).company_number(companyNumber).address(Address.builder().address_line_1("Line 1").build()).build();
        company.addOfficer(Officer.builder().name("Bob").address(Address.builder().address_line_1("Line 2").build()).build());
        company.setCompany_status((activeOnly == null || activeOnly.isBlank() || activeOnly.equalsIgnoreCase("no")) ? "dissolved" : "active");
        return company;
    }

    @Test
        //validate exceptions result in 500 internal server error
    void validateExceptionHandling() throws Exception {
        when(this.requestProcessingService.processCompanyRequest(any(CompanyRequest.class))).thenThrow(new RuntimeException("Mock exception"));

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post("/v1/company")
                                .header("x-api-key", "TestApiKey")
                                .queryParam("activeOnly", "yes")
                                .content(String.format("{\"companyName\":\"%s\",\"companyNumber\":\"%s\"}", "BBC", "123456"))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isInternalServerError());

    }

}
