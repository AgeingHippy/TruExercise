package org.charles.truexercise.service;

import org.charles.truexercise.dto.Address;
import org.charles.truexercise.dto.Company;
import org.charles.truexercise.dto.Officer;
import org.charles.truexercise.repository.AddressDao;
import org.charles.truexercise.repository.CompanyDao;
import org.charles.truexercise.repository.OfficerDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigInteger;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class PersistenceServiceTest {

    @Mock
    private CompanyDao companyDao;
    @Mock
    private AddressDao addressDao;
    @Mock
    private OfficerDao officerDao;

    private PersistenceService persistenceService;

    private AutoCloseable autoCloseable;

    @BeforeEach
    public void init() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        persistenceService = spy(new PersistenceService(companyDao, addressDao, officerDao));
    }

    @AfterEach
    public void cleanUp() throws Exception {
        autoCloseable.close();
    }

    @Test
    void verifyFindByCompanyReturnsNullWhenCompanyNotFound() {
        //GIVEN the company is not in the database
        String companyNumber = "1234";
        doReturn(null).when(persistenceService).fetchCompany(companyNumber);

        //WHEN we fetch the company
        Company company = companyDao.findByCompanyNumber(companyNumber);

        //THEN null is returned
        assertNull(company);
    }

    @Test
    void verifyFindByCompanyReturnsCompanyWhenFound() {
        //GIVEN the company is in the database
        String companyNumber = "1234";

        //following set to final as we want to ensure
        final Company testCompany = testCompanyObject(BigInteger.ONE);
        final Address testCompanyAddress = testCompanyAddressObject(BigInteger.ONE);
        final ArrayList<Officer> testOfficers = testCompanyOfficersObject(BigInteger.TWO, BigInteger.valueOf(3));
        final Address testOfficer1Address = testCompanyAddressObject(BigInteger.TWO);
        final Address testOfficer2Address = testCompanyAddressObject(BigInteger.valueOf(3));

        doReturn(testCompany).when(companyDao).findByCompanyNumber(companyNumber);
        doReturn(testCompanyAddress).when(addressDao).findById(testCompany.getAddress_id());

        doReturn(testOfficers).when(officerDao).findByCompanyNumber(companyNumber);
        doReturn(testOfficer1Address).when(addressDao).findById(testOfficers.getFirst().getAddress_id());
        doReturn(testOfficer2Address).when(addressDao).findById(testOfficers.getLast().getAddress_id());

        //WHEN we fetch the company
        Company company = persistenceService.fetchCompany(companyNumber);

        //THEN the company is returned with all components populated (company + 2 officers and all addresses)
        assertEquals(testCompany, company);
        assertEquals(testCompanyAddress,testCompany.getAddress());
        assertEquals(testOfficers.getFirst(),testCompany.getOfficers().getFirst() );
        assertEquals(testOfficer1Address,testCompany.getOfficers().getFirst().getAddress() );
        assertEquals(testOfficers.getLast(),testCompany.getOfficers().getLast() );
        assertEquals(testOfficer2Address,testCompany.getOfficers().getLast().getAddress() );
    }

    //todo - add saveCompanies unit tests


    private Company testCompanyObject(BigInteger addressId) {
        return Company.builder()
                .title("New Company")
                .company_number("1234")
                .company_status("active")
                .company_type("ltd")
                .date_of_creation("2001-01-01")
                .address_id(addressId)
                .build();
    }

    private Address testCompanyAddressObject(BigInteger addressId) {
        return Address.builder().address_id(addressId).build();
    }

    private ArrayList<Officer> testCompanyOfficersObject(BigInteger... addressIds) {
        ArrayList<Officer> officers = new ArrayList<>();
        for (BigInteger addressId:addressIds) {
            officers.add(Officer.builder().name("Name " + addressId).address_id(addressId).build());
        }

        return officers;
    }
}
