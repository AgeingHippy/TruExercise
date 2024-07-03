package org.charles.truexercise.service;

import org.charles.truexercise.dto.Company;
import org.charles.truexercise.dto.CompanyRequest;
import org.charles.truexercise.dto.CompanyResponse;
import org.charles.truexercise.dto.truProxyApi.TruProxyApiCompany;
import org.charles.truexercise.dto.truProxyApi.TruProxyApiCompanySearchResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RequestProcessingServiceTest {

    private RequestProcessingService requestProcessingService;

    @Mock
    private TruProxyApiService truProxyApiService;

    @Mock
    private PersistenceService persistenceService;

    private AutoCloseable autoCloseable;

    @BeforeEach
    public void init() {
        autoCloseable =  MockitoAnnotations.openMocks(this);
        requestProcessingService = spy(new RequestProcessingService(truProxyApiService, persistenceService));
    }

    @AfterEach
    public void cleanUp() throws Exception {
        autoCloseable.close();
    }

    @Test
    void verifyLocalRepositorySearchedIfCompanyNumberProvided_foundLocally() {
        //GIVEN
        CompanyRequest companyRequest = CompanyRequest.builder().companyNumber("1234").apiKey("TestApiKey").build();
        Company company = Company.builder().company_status("active").build();

        when(this.persistenceService.fetchCompany(any(String.class))).thenReturn(company);

        //WHEN
        CompanyResponse companyResponse = requestProcessingService.processCompanyRequest(companyRequest);

        //THEN
        verify(persistenceService, times(1)).fetchCompany(any(String.class));
        verify(requestProcessingService, never()).getCompanies(any());

        assertEquals(1, companyResponse.getTotal_results());
        assertEquals(company, companyResponse.getItems().getFirst());
    }

    @Test
    void verifyLocalRepositorySearchedIfCompanyNumberProvided_notFoundLocally() {
        //GIVEN
        CompanyRequest companyRequest = CompanyRequest.builder().companyNumber("1234").apiKey("TestApiKey").build();
        Company company = Company.builder().company_status("active").build();
        CompanyResponse companyResponse = CompanyResponse.builder().total_results(1).build();
        companyResponse.addCompany(company);

        when(this.persistenceService.fetchCompany(any(String.class))).thenReturn(null);

        doReturn(companyResponse).when(requestProcessingService).getCompanies(companyRequest);

        doNothing().when(persistenceService).saveCompanies(any());

        //WHEN
        CompanyResponse returnedCompanyResponse = requestProcessingService.processCompanyRequest(companyRequest);

        //THEN
        verify(persistenceService, times(1)).fetchCompany(any(String.class));
        verify(requestProcessingService, times(1)).getCompanies(any());
        verify(persistenceService, times(1)).saveCompanies(any());

        assertEquals(1, returnedCompanyResponse.getTotal_results());
        assertEquals(company, returnedCompanyResponse.getItems().getFirst());
    }

    @Test
    void verifyLocalRepositoryNotSearchedIfCompanyNumberNotProvided_foundRemotely() {
        //GIVEN
        CompanyRequest companyRequest = CompanyRequest.builder().companyName("Test Company").apiKey("TestApiKey").build();
        Company company = Company.builder().company_status("active").build();
        CompanyResponse companyResponse = CompanyResponse.builder().total_results(1).build();
        companyResponse.addCompany(company);

        doReturn(companyResponse).when(requestProcessingService).getCompanies(companyRequest);

        doNothing().when(persistenceService).saveCompanies(any());

        //WHEN
        CompanyResponse returnedCompanyResponse = requestProcessingService.processCompanyRequest(companyRequest);

        //THEN
        verify(persistenceService, times(0)).fetchCompany(any(String.class));
        verify(requestProcessingService, times(1)).getCompanies(any());
        verify(persistenceService, times(1)).saveCompanies(any());

        assertEquals(1, returnedCompanyResponse.getTotal_results());
        assertEquals(company, returnedCompanyResponse.getItems().getFirst());
    }

    @Test
    void verifyLocalRepositoryNotSearchedIfCompanyNumberNotProvided_notFoundRemotely() {
        //GIVEN
        CompanyRequest companyRequest = CompanyRequest.builder().companyName("Test Company").apiKey("TestApiKey").build();
        CompanyResponse internalCompanyResponse = CompanyResponse.builder().total_results(0).build();

        doReturn(internalCompanyResponse).when(requestProcessingService).getCompanies(companyRequest);

        //WHEN
        CompanyResponse companyResponse = requestProcessingService.processCompanyRequest(companyRequest);

        //THEN
        verify(persistenceService, times(0)).fetchCompany(any(String.class));
        verify(requestProcessingService, times(1)).getCompanies(any());
        verify(persistenceService, times(0)).saveCompanies(any());

        assertEquals(0, companyResponse.getTotal_results());
        assertNull(companyResponse.getItems());
    }

    @Test
    void verify_getCompanies_searchByCompanyNumberIfProvided() {
        //GIVEN
        CompanyRequest companyRequest = CompanyRequest.builder()
                .companyName("Test Company").companyNumber("1234").apiKey("TestApiKey").build();

        ResponseEntity<TruProxyApiCompanySearchResponse> truApicompanySearchResponseEntity =
                ResponseEntity.status(HttpStatus.OK).body(TruProxyApiCompanySearchResponse.builder().page_number("1").total_results(0).build());

        doReturn(truApicompanySearchResponseEntity).when(truProxyApiService).getCompanies(companyRequest.getCompanyNumber(), companyRequest.getApiKey());

        //WHEN
        CompanyResponse companyResponse = requestProcessingService.getCompanies(companyRequest);

        //THEN
        verify(truProxyApiService, times(1)).getCompanies(companyRequest.getCompanyNumber(), companyRequest.getApiKey());
        assertEquals(0, companyResponse.getTotal_results());

    }

    @Test
    void verify_getCompanies_searchByCompanyNameIfNumberNotProvided() {
        //GIVEN
        CompanyRequest companyRequest = CompanyRequest.builder()
                .companyName("Test Company").apiKey("TestApiKey").build();

        ResponseEntity<TruProxyApiCompanySearchResponse> truApicompanySearchResponseEntity =
                ResponseEntity.status(HttpStatus.OK).body(TruProxyApiCompanySearchResponse.builder().page_number("1").total_results(0).build());

        doReturn(truApicompanySearchResponseEntity).when(truProxyApiService).getCompanies(companyRequest.getCompanyName(), companyRequest.getApiKey());

        //WHEN
        CompanyResponse companyResponse = requestProcessingService.getCompanies(companyRequest);

        //THEN
        verify(truProxyApiService, times(1)).getCompanies(companyRequest.getCompanyName(), companyRequest.getApiKey());
        assertEquals(0, companyResponse.getTotal_results());

    }

    @ParameterizedTest
    @MethodSource("verify_getCompanies_filterTruProxyResponseByName_dataProvider")
    void verify_getCompanies_filterTruProxyResponseByName(String queryParameter, String companyName, String companyNumber,
                                                          boolean activeOnly, int expectedCount,
                                                          String firstCompanyNumber, String lastCompanyNumber) {
        //GIVEN
        CompanyRequest companyRequest = CompanyRequest.builder()
                .companyName(companyName)
                .companyNumber(companyNumber)
                .apiKey("testKey")
                .activeOnly(activeOnly)
                .build();

        when(truProxyApiService.getCompanies(queryParameter, "testKey")).thenReturn(getCompaniesFilterByResponseDataSetup());

        doAnswer(input -> {
            TruProxyApiCompany truProxyApiCompany = input.getArgument(0);
            return Company.builder()
                    .title(truProxyApiCompany.getTitle())
                    .company_number(truProxyApiCompany.getCompany_number())
                    .company_status(truProxyApiCompany.getCompany_status())
                    .build();
        }).when(requestProcessingService).mapToCompany(any(TruProxyApiCompany.class));

        doNothing().when(requestProcessingService).getOfficers(any(Company.class),any(String.class));

        //WHEN
        CompanyResponse companyResponse = requestProcessingService.getCompanies(companyRequest);

        //THEN
        verify(truProxyApiService, times(1)).getCompanies(queryParameter, "testKey");
        assertEquals(expectedCount, companyResponse.getItems().size());
        if (!companyResponse.getItems().isEmpty()) {
            assertEquals(firstCompanyNumber, companyResponse.getItems().getFirst().getCompany_number());
        }
        if (companyResponse.getItems().size() > 1) {
            assertEquals(lastCompanyNumber, companyResponse.getItems().getLast().getCompany_number());
        }
    }

    private static Stream<Object[]> verify_getCompanies_filterTruProxyResponseByName_dataProvider() {
        return Stream.of(
                //queryParameter, companyName, companyNumber, activeOnly, expectedCount, firstCompanyNumber, lastCompanyNumber
                new Object[]{"BBC LIMITED", "BBC LIMITED", "", true, 1, "A12345", ""},
                new Object[]{"BBC LIMITED", "BBC LIMITED", "", false, 2, "A12345", "012345QQ"},
                new Object[]{"345", "", "345", true, 0, "", ""},
                new Object[]{"345", "", "345", false, 1, "345", ""}
        );
    }

    private ResponseEntity<TruProxyApiCompanySearchResponse> getCompaniesFilterByResponseDataSetup() {
        TruProxyApiCompanySearchResponse response = new TruProxyApiCompanySearchResponse();

        ArrayList<TruProxyApiCompany> truProxyApiCompanies = new ArrayList<>();

        truProxyApiCompanies.add(TruProxyApiCompany.builder().title("BBC 1").company_number("012345").company_status("active").build());
        truProxyApiCompanies.add(TruProxyApiCompany.builder().title("BBC 2").company_number("12345").company_status("active").build());
        truProxyApiCompanies.add(TruProxyApiCompany.builder().title("MY BBC BOOK").company_number("01234567").company_status("active").build());
        truProxyApiCompanies.add(TruProxyApiCompany.builder().title("BBC LTD").company_number("345").company_status("suspended").build());
        truProxyApiCompanies.add(TruProxyApiCompany.builder().title("BBC LIMITED").company_number("A12345").company_status("active").build());
        truProxyApiCompanies.add(TruProxyApiCompany.builder().title("BBC LLP").company_number("B12345").company_status("pending").build());
        truProxyApiCompanies.add(TruProxyApiCompany.builder().title("BBC LIMITED").company_number("012345QQ").company_status("pending").build());
        truProxyApiCompanies.add(TruProxyApiCompany.builder().title("B.B.C.").company_number("345A").company_status("active").build());
        truProxyApiCompanies.add(TruProxyApiCompany.builder().title("345 BBC").company_number("QA123").company_status("suspended").build());

        response.setItems(truProxyApiCompanies);
//        response.setPage_number(1);
        response.setKind("search#companies");
        response.setTotal_results(9);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

