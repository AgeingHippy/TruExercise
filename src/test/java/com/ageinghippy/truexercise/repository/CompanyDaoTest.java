package com.ageinghippy.truexercise.repository;

import com.ageinghippy.truexercise.dto.Company;
import com.ageinghippy.truexercise.repository.CompanyDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Sql(scripts = {"classpath:address_data.sql","classpath:company_data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class CompanyDaoTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private CompanyDao companyDao;

    @BeforeEach
    public void init() {
        companyDao = new CompanyDao(jdbcTemplate);
    }

    @Test
    void verifyCompanyPopulatedCorrectlyOnFindByCompanyNumber() {

        Company company = companyDao.findByCompanyNumber("1003");

        assertEquals("company three", company.getTitle());
        assertEquals("1003", company.getCompany_number());
        assertEquals("type 3", company.getCompany_type());
        assertEquals("suspended", company.getCompany_status());
        assertEquals("2001-03-13", company.getDate_of_creation());
        assertEquals(BigInteger.valueOf(3), company.getAddress_id());
        assertNull(company.getAddress());
        assertNull(company.getOfficers());
    }

    @Test
    void verifyNullReturnedWHenNotFound() {

        Company company = companyDao.findByCompanyNumber("9999");

        assertNull(company);
    }

    @Test
    void verifyDataSavedCorrectlyOnSaveCompany() {

        Company company = Company.builder()
                .company_number("8888")
                .title("Straight Eight")
                .company_status("pending")
                .company_type("ltd")
                .address_id(BigInteger.valueOf(1)).build();

        companyDao.saveCompany(company);

        List<Map<String, Object>> myList = jdbcTemplate.queryForList("SELECT * FROM company WHERE company_number = '8888'");

        assertEquals("8888", myList.getFirst().get("company_number"));
        assertEquals("Straight Eight", myList.getFirst().get("title"));
        assertEquals("pending", myList.getFirst().get("company_status"));
        assertEquals("ltd", myList.getFirst().get("company_type"));
        assertEquals(BigInteger.valueOf(1), BigInteger.valueOf((Long) myList.getFirst().get("address_id")));
    }

    @Test
    void verifyCompanyExistsReturnsFalseWhenCompanyNotFound() {
        assertFalse(companyDao.companyExists("9999"));
    }

    @Test
    void verifyCompanyExistsReturnsTrueCompanyFound() {
        assertTrue(companyDao.companyExists("1003"));
    }
}
