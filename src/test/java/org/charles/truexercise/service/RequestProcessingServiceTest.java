package org.charles.truexercise.service;

import org.charles.truexercise.dto.*;
import org.charles.truexercise.dto.truProxyApi.*;
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
        autoCloseable = MockitoAnnotations.openMocks(this);
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
                ResponseEntity.status(HttpStatus.OK).body(TruProxyApiCompanySearchResponse.builder().page_number(1).total_results(0).build());

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
                ResponseEntity.status(HttpStatus.OK).body(TruProxyApiCompanySearchResponse.builder().page_number(1).total_results(0).build());

        doReturn(truApicompanySearchResponseEntity).when(truProxyApiService).getCompanies(companyRequest.getCompanyName(), companyRequest.getApiKey());

        //WHEN
        CompanyResponse companyResponse = requestProcessingService.getCompanies(companyRequest);

        //THEN
        verify(truProxyApiService, times(1)).getCompanies(companyRequest.getCompanyName(), companyRequest.getApiKey());
        assertEquals(0, companyResponse.getTotal_results());

    }

    @ParameterizedTest
    @MethodSource("getCompanies_parameters_provider")
    void verify_getCompanies_filterTruProxyResponseByNameAndActive(String queryParameter, String companyName, String companyNumber,
                                                                   boolean activeOnly, int expectedCount,
                                                                   String firstCompanyNumber, String lastCompanyNumber) {
        //GIVEN
        CompanyRequest companyRequest = CompanyRequest.builder()
                .companyName(companyName)
                .companyNumber(companyNumber)
                .apiKey("testKey")
                .activeOnly(activeOnly)
                .build();

        when(truProxyApiService.getCompanies(queryParameter, "testKey")).thenReturn(getCompanies_truProxyCall_dataProvider());

        doAnswer(input -> {
            TruProxyApiCompany truProxyApiCompany = input.getArgument(0);
            return Company.builder()
                    .title(truProxyApiCompany.getTitle())
                    .company_number(truProxyApiCompany.getCompany_number())
                    .company_status(truProxyApiCompany.getCompany_status())
                    .build();
        }).when(requestProcessingService).mapToCompany(any(TruProxyApiCompany.class));

        doNothing().when(requestProcessingService).getOfficers(any(Company.class), any(String.class));

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

    private static Stream<Object[]> getCompanies_parameters_provider() {
        return Stream.of(
                //queryParameter, companyName, companyNumber, activeOnly, expectedCount, firstCompanyNumber, lastCompanyNumber
                new Object[]{"BBC LIMITED", "BBC LIMITED", "", true, 1, "A12345", ""}, //Test 1 - filter by name and only show active
                new Object[]{"BBC LIMITED", "BBC LIMITED", "", false, 2, "A12345", "012345QQ"}, //Test 2 - filter by name and show all
                new Object[]{"345", "", "345", true, 0, "", ""}, //Test 3 - filter by number and only show active
                new Object[]{"345", "", "345", false, 1, "345", ""} //Test 4 - filter by number and show all
        );
    }

    private ResponseEntity<TruProxyApiCompanySearchResponse> getCompanies_truProxyCall_dataProvider() {
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
        response.setPage_number(1);
        response.setKind("search#companies");
        response.setTotal_results(9);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Test
    void verify_getOfficers_noOfficersFound() {
        //GIVEN - No officers exist
        Company company = Company.builder().title("BBC LIMITED").company_number("A12345").company_status("active").build();

        when(truProxyApiService.getOfficers(company.getCompany_number(), "testKey"))
                .thenReturn(new ResponseEntity<>(
                        TruProxyApiOfficerSearchResponse.builder().total_results(0).resigned_count(0).build(),
                        HttpStatus.OK));

        //WHEN - Officers are fetched
        requestProcessingService.getOfficers(company, "testKey");

        //THEN - No officers are returned
        assertNull(company.getOfficers());
    }

    @Test
    void verify_getOfficers_inactiveOfficersFound() {
        //GIVEN - 2 inactive officers returned
        Company company = Company.builder().title("BBC LIMITED").company_number("A12345").company_status("active").build();
        TruProxyApiOfficerSearchResponse truProxyApiOfficerSearchResponse =
                TruProxyApiOfficerSearchResponse.builder().total_results(2).resigned_count(2).build();
        ArrayList<TruProxyApiOfficer> officers = new ArrayList<>();
        officers.add(TruProxyApiOfficer.builder().name("Bob").resigned_on("2001-02-01").build());
        officers.add(TruProxyApiOfficer.builder().name("Bill").resigned_on("2002-02-01").build());
        truProxyApiOfficerSearchResponse.setItems(officers);

        when(truProxyApiService.getOfficers(company.getCompany_number(), "testKey"))
                .thenReturn(new ResponseEntity<>(truProxyApiOfficerSearchResponse, HttpStatus.OK));

        //WHEN - officers are fetched
        requestProcessingService.getOfficers(company, "testKey");

        //THEN - 0 officers are returned
        assertNull(company.getOfficers());
    }

    @Test
    void verify_getOfficers_inactiveAndActiveOfficersFound() {
        //GIVEN - 1 active and 1 inactive officer exist
        Company company = Company.builder().title("BBC LIMITED").company_number("A12345").company_status("active").build();
        TruProxyApiOfficerSearchResponse truProxyApiOfficerSearchResponse =
                TruProxyApiOfficerSearchResponse.builder().total_results(2).resigned_count(1).build();
        ArrayList<TruProxyApiOfficer> officers = new ArrayList<>();
        officers.add(TruProxyApiOfficer.builder().name("Bob").resigned_on("2001-02-01").build());
        officers.add(TruProxyApiOfficer.builder().name("Bill").build());
        truProxyApiOfficerSearchResponse.setItems(officers);

        when(truProxyApiService.getOfficers(company.getCompany_number(), "testKey"))
                .thenReturn(new ResponseEntity<>(truProxyApiOfficerSearchResponse, HttpStatus.OK));

        doAnswer(input -> {
            TruProxyApiOfficer truProxyApiOfficer = input.getArgument(0);
            return Officer.builder()
                    .name(truProxyApiOfficer.getName())
                    .build();
        }).when(requestProcessingService).mapToOfficer(any(TruProxyApiOfficer.class));

        //WHEN - officers are fetched
        requestProcessingService.getOfficers(company, "testKey");

        //THEN - Only the active officer is returned
        assertEquals(1,company.getOfficers().size() );
        assertEquals("Bill", company.getOfficers().getFirst().getName());
    }

    @Test
    void verify_getOfficers_onlyActiveOfficersFound() {
        //GIVEN - 1 active and 1 inactive officer exist
        Company company = Company.builder().title("BBC LIMITED").company_number("A12345").company_status("active").build();
        TruProxyApiOfficerSearchResponse truProxyApiOfficerSearchResponse =
                TruProxyApiOfficerSearchResponse.builder().total_results(2).resigned_count(1).build();
        ArrayList<TruProxyApiOfficer> officers = new ArrayList<>();
        officers.add(TruProxyApiOfficer.builder().name("Bob").build());
        officers.add(TruProxyApiOfficer.builder().name("Bill").build());
        truProxyApiOfficerSearchResponse.setItems(officers);

        when(truProxyApiService.getOfficers(company.getCompany_number(), "testKey"))
                .thenReturn(new ResponseEntity<>(truProxyApiOfficerSearchResponse, HttpStatus.OK));

        doAnswer(input -> {
            TruProxyApiOfficer truProxyApiOfficer = input.getArgument(0);
            return Officer.builder()
                    .name(truProxyApiOfficer.getName())
                    .build();
        }).when(requestProcessingService).mapToOfficer(any(TruProxyApiOfficer.class));

        //WHEN - officers are fetched
        requestProcessingService.getOfficers(company, "testKey");

        //THEN - Both the active officers are returned
        assertEquals(2,company.getOfficers().size() );
        assertEquals("Bob", company.getOfficers().getFirst().getName());
        assertEquals("Bill", company.getOfficers().getLast().getName());
    }

    @Test
    void verify_mapToCompany_null() {
        //GIVEN a null TruProxyApiCompany
        TruProxyApiCompany truProxyApiCompany = null;

        //WHEN mapToCompany is called
        Company company = requestProcessingService.mapToCompany(truProxyApiCompany);

        //THEN null is returned
        assertNull(company);
    }

    @Test
    void verify_mapToAddress_null() {
        //GIVEN a null TruProxyApiAddress
        TruProxyApiAddress truProxyApiAddress = null;

        //WHEN mapToCompany is called
        Address address = requestProcessingService.mapToAddress(truProxyApiAddress);

        //THEN null is returned
        assertNull(address);
    }

    @Test
    void verify_mapToOfficer_null() {
        //GIVEN a null TruProxyApiAddress
        TruProxyApiOfficer truProxyApiOfficer = null;

        //WHEN mapToCompany is called
        Officer officer = requestProcessingService.mapToOfficer(truProxyApiOfficer);

        //THEN null is returned
        assertNull(officer);
    }

    @Test
    void verifyMapToCompanyAndMapToAddressMapCorrectly() {
        //GIVEN a TruProxy company with address
        TruProxyApiAddress truProxyApiAddress = TruProxyApiAddress.builder()
                .premises("Premises 1")
                .address_line_1("Line 1")
                .locality("Locality 1")
                .postal_code("PO 1")
                .country("Country 1")
                .build();
        TruProxyApiCompany truProxyApiCompany = TruProxyApiCompany.builder()
                .title("Company 1")
                .company_number("1234")
                .company_type("Type 1")
                .company_status("Status 1")
                .date_of_creation("2001-01-01")
                .address(truProxyApiAddress)
                .build();

        //WHEN mapToCompany is called
        Company company = requestProcessingService.mapToCompany(truProxyApiCompany);

        //THEN the company and address data is mapped correctly
        assertEquals(truProxyApiCompany.getTitle(),company.getTitle());
        assertEquals(truProxyApiCompany.getCompany_number(),company.getCompany_number());
        assertEquals(truProxyApiCompany.getCompany_type(),company.getCompany_type());
        assertEquals(truProxyApiCompany.getCompany_status(),company.getCompany_status());
        assertEquals(truProxyApiCompany.getDate_of_creation(),company.getDate_of_creation());
        //and validate company address
        assertEquals(truProxyApiCompany.getAddress().getPremises(),company.getAddress().getPremises());
        assertEquals(truProxyApiCompany.getAddress().getAddress_line_1(),company.getAddress().getAddress_line_1());
        assertEquals(truProxyApiCompany.getAddress().getLocality(),company.getAddress().getLocality());
        assertEquals(truProxyApiCompany.getAddress().getPostal_code(),company.getAddress().getPostal_code());
        assertEquals(truProxyApiCompany.getAddress().getCountry(),company.getAddress().getCountry());
    }

    @Test
    void verifyMapToOfficerAndMapToAddressMapCorrectly() {
        //GIVEN a TruProxy Officer with address
        TruProxyApiAddress truProxyApiAddress = TruProxyApiAddress.builder()
                .premises("Premises 2")
                .address_line_1("Line 2")
                .locality("Locality 2")
                .postal_code("PO 2")
                .country("Country 2")
                .build();
        TruProxyApiOfficer truProxyApiOfficer = TruProxyApiOfficer.builder()
                .name("Name 1")
                .officer_role("Role 1")
                .appointed_on("2001-01-01")
                .address(truProxyApiAddress)
                .build();

        //WHEN mapToOfficer is called
        Officer officer = requestProcessingService.mapToOfficer(truProxyApiOfficer);

        //THEN the officer and address data is mapped correctly
        assertEquals(truProxyApiOfficer.getName(),officer.getName());
        assertEquals(truProxyApiOfficer.getOfficer_role(),officer.getOfficer_role());
        assertEquals(truProxyApiOfficer.getAppointed_on(),officer.getAppointed_on());
        //and validate company address
        assertEquals(truProxyApiOfficer.getAddress().getPremises(),officer.getAddress().getPremises());
        assertEquals(truProxyApiOfficer.getAddress().getAddress_line_1(),officer.getAddress().getAddress_line_1());
        assertEquals(truProxyApiOfficer.getAddress().getLocality(),officer.getAddress().getLocality());
        assertEquals(truProxyApiOfficer.getAddress().getPostal_code(),officer.getAddress().getPostal_code());
        assertEquals(truProxyApiOfficer.getAddress().getCountry(),officer.getAddress().getCountry());
    }

}

