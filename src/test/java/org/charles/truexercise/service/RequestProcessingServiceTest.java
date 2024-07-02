package org.charles.truexercise.service;

import org.charles.truexercise.dto.Company;
import org.charles.truexercise.dto.CompanyRequest;
import org.charles.truexercise.dto.CompanyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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


    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void verifyLocalRepositorySearchedIfCompanyNumberProvided_foundLocally() {
        CompanyRequest companyRequest = CompanyRequest.builder().companyNumber("1234").apiKey("TestApiKey").build();
        Company company = Company.builder().company_status("active").build();
        requestProcessingService = spy(new RequestProcessingService(truProxyApiService, persistenceService));

        when(this.persistenceService.fetchCompany(any(String.class))).thenReturn(company);

        CompanyResponse companyResponse = requestProcessingService.processCompanyRequest(companyRequest);

        verify(persistenceService, times(1)).fetchCompany(any(String.class));
        verify(requestProcessingService, never()).getCompanies(any());

        assertEquals(1,companyResponse.getTotal_results());
        assertEquals(company, companyResponse.getItems().getFirst());
    }

    @Test
    void verifyLocalRepositorySearchedIfCompanyNumberProvided_notFoundLocally() {
        CompanyRequest companyRequest = CompanyRequest.builder().companyNumber("1234").apiKey("TestApiKey").build();
        Company company = Company.builder().company_status("active").build();
        CompanyResponse companyResponse = CompanyResponse.builder().total_results(1).build();
        companyResponse.addCompany(company);

        requestProcessingService = spy(new RequestProcessingService(truProxyApiService, persistenceService));

        when(this.persistenceService.fetchCompany(any(String.class))).thenReturn(null);

        doReturn(companyResponse).when(requestProcessingService).getCompanies(companyRequest);

        doNothing().when(persistenceService).saveCompanies(any());


        CompanyResponse returnedCompanyResponse = requestProcessingService.processCompanyRequest(companyRequest);

        verify(persistenceService, times(1)).fetchCompany(any(String.class));
        verify(requestProcessingService, times(1)).getCompanies(any());
        verify(persistenceService,times(1)).saveCompanies(any());

        assertEquals(1,returnedCompanyResponse.getTotal_results());
        assertEquals(company, returnedCompanyResponse.getItems().getFirst());
    }

    @Test
    void verifyLocalRepositoryNotSearchedIfCompanyNumberNotProvided_foundRemotely() {
        CompanyRequest companyRequest = CompanyRequest.builder().companyName("Test Company").apiKey("TestApiKey").build();
        Company company = Company.builder().company_status("active").build();
        CompanyResponse companyResponse = CompanyResponse.builder().total_results(1).build();
        companyResponse.addCompany(company);

        requestProcessingService = spy(new RequestProcessingService(truProxyApiService, persistenceService));

        doReturn(companyResponse).when(requestProcessingService).getCompanies(companyRequest);

        doNothing().when(persistenceService).saveCompanies(any());

        CompanyResponse returnedCompanyResponse = requestProcessingService.processCompanyRequest(companyRequest);

        verify(persistenceService, times(0)).fetchCompany(any(String.class));
        verify(requestProcessingService, times(1)).getCompanies(any());
        verify(persistenceService,times(1)).saveCompanies(any());

        assertEquals(1,returnedCompanyResponse.getTotal_results());
        assertEquals(company, returnedCompanyResponse.getItems().getFirst());
    }

    @Test
    void verifyLocalRepositoryNotSearchedIfCompanyNumberNotProvided_notFoundRemotely() {
        CompanyRequest companyRequest = CompanyRequest.builder().companyName("Test Company").apiKey("TestApiKey").build();
        CompanyResponse internalCompanyResponse = CompanyResponse.builder().total_results(0).build();

        requestProcessingService = spy(new RequestProcessingService(truProxyApiService, persistenceService));

        doReturn(internalCompanyResponse).when(requestProcessingService).getCompanies(companyRequest);

        CompanyResponse companyResponse = requestProcessingService.processCompanyRequest(companyRequest);

        verify(persistenceService, times(0)).fetchCompany(any(String.class));
        verify(requestProcessingService, times(1)).getCompanies(any());
        verify(persistenceService,times(0)).saveCompanies(any());

        assertEquals(0,companyResponse.getTotal_results());
        assertNull(companyResponse.getItems());
    }

//    @Test
//    void verify_getCompanies_searchByCompanyNumberIfProvided() {
//        CompanyRequest companyRequest = CompanyRequest.builder()
//                .companyName("Test Company").companyNumber("1234").apiKey("TestApiKey").build();
//
//    }
}

