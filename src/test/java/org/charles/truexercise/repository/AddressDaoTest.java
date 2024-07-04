package org.charles.truexercise.repository;


import org.charles.truexercise.dto.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@JdbcTest
@Sql(scripts = {"classpath:address_data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class AddressDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private AddressDao addressDao;

    @BeforeEach
    public void init() {
        addressDao = new AddressDao(jdbcTemplate);
    }

    @Test
    void verifyAddressPopulatedCorrectlyOnFindById() {

        Address address = addressDao.findById(BigInteger.ONE);

        assertEquals(BigInteger.valueOf(1), address.getAddress_id());
        assertEquals("premises 1", address.getPremises());
        assertEquals("address one", address.getAddress_line_1());
        assertEquals("locality #1", address.getLocality());
        assertEquals("PC01", address.getPostal_code());
        assertEquals("UK1", address.getCountry());

    }


    @Test
    void verifyNullReturnedWhenNotFound() {

        Address address = addressDao.findById(BigInteger.ZERO);

        assertNull(address);

    }

    @Test
    void verifyDataInsertedAndIndexReturnedCorrectlyBySaveAddress() {
        Address address = Address.builder()
                .premises("premises new")
                .address_line_1("address new")
                .locality("locality #new")
                .postal_code("PCnew")
                .country("UKnew").build();

        BigInteger addressId = addressDao.saveAddress(address);

        List<Map<String, Object>> myList = jdbcTemplate.queryForList("SELECT * FROM address WHERE address_id = ?", addressId);

        assertEquals(addressId, BigInteger.valueOf((Long) myList.getFirst().get("address_id")));
        assertEquals("premises new", myList.getFirst().get("premises"));
        assertEquals("address new", myList.getFirst().get("address_line_1"));
        assertEquals("locality #new", myList.getFirst().get("locality"));
        assertEquals("PCnew", myList.getFirst().get("postal_code"));
        assertEquals("UKnew", myList.getFirst().get("country"));
    }

}
