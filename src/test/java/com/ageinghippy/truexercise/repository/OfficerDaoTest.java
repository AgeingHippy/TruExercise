package com.ageinghippy.truexercise.repository;

import com.ageinghippy.truexercise.dto.Officer;
import com.ageinghippy.truexercise.repository.OfficerDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@JdbcTest
@Sql(scripts = {"classpath:address_data.sql","classpath:officer_data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class OfficerDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private OfficerDao officerDao;

    @BeforeEach
    public void init() {
        officerDao = new OfficerDao(jdbcTemplate);
    }

    @Test
    void verifyOfficersPopulatedCorrectlyOnFindByCompanyNumber_oneOfficerFound() {

        ArrayList<Officer> officers = officerDao.findByCompanyNumber("1002");

        assertEquals(1, officers.size());
        assertEquals("1002",officers.getFirst().getCompany_number());
        assertEquals("officer 2001",officers.getFirst().getName());
        assertEquals("role 21",officers.getFirst().getOfficer_role());
        assertEquals("2001-02-11",officers.getFirst().getAppointed_on());
        assertEquals(BigInteger.valueOf(7),officers.getFirst().getAddress_id());
        assertNull(officers.getFirst().getAddress());
    }

    @Test
    void verifyOfficersPopulatedCorrectlyOnFindByCompanyNumber_multipleOfficerFound() {

        ArrayList<Officer> officers = officerDao.findByCompanyNumber("1001");

        assertEquals(2, officers.size());
        assertEquals("1001",officers.getFirst().getCompany_number());
        assertEquals("officer 1001",officers.getFirst().getName());
        assertEquals("role 11",officers.getFirst().getOfficer_role());
        assertEquals("2001-01-11",officers.getFirst().getAppointed_on());
        assertEquals(BigInteger.valueOf(5),officers.getFirst().getAddress_id());
        assertNull(officers.getFirst().getAddress());

        assertEquals("1001",officers.getLast().getCompany_number());
        assertEquals("officer 1002",officers.getLast().getName());
        assertEquals("role 12",officers.getLast().getOfficer_role());
        assertEquals("2001-01-12",officers.getLast().getAppointed_on());
        assertEquals(BigInteger.valueOf(6),officers.getLast().getAddress_id());
        assertNull(officers.getLast().getAddress());
    }

    @Test
    void verifyDataSavedCorrectlyOnSaveOfficer() {

        Officer officer = Officer.builder()
                .company_number("1005")
                .name("officer new")
                .officer_role("role new")
                .appointed_on("1001-01-01")
                .address_id(BigInteger.valueOf(1)).build();

        officerDao.saveOfficer(officer);

        List<Map<String, Object>> myList = jdbcTemplate.queryForList("SELECT * FROM officer WHERE company_number = '1005'");

        assertEquals(1, myList.size());
        assertEquals("1005", myList.getFirst().get("company_number"));
        assertEquals("officer new", myList.getFirst().get("name"));
        assertEquals("role new", myList.getFirst().get("officer_role"));
        assertEquals("1001-01-01", myList.getFirst().get("appointed_on"));
        assertEquals(BigInteger.valueOf(1), BigInteger.valueOf((Long) myList.getFirst().get("address_id")));
    }
}
