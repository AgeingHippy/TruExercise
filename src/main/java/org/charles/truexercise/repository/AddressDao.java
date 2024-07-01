package org.charles.truexercise.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.charles.truexercise.dto.Address;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;

@Repository
@Slf4j
@AllArgsConstructor
public class AddressDao {

    private final JdbcTemplate jdbcTemplate;

    private static final String findById_SQL = "SELECT * from address WHERE address_id = ?";
    private static final String saveAddress_SQL = """
            INSERT INTO address
            ( premises, address_line_1, locality, postal_code, country)
            values ( ?, ?, ?, ?, ?)""";

    public Address findById(BigInteger addressId) {
        log.trace("AddressDao.findById {}", addressId);
        Address address;

        try {
            address = jdbcTemplate.queryForObject(findById_SQL, new BeanPropertyRowMapper<>(Address.class), addressId);
        } catch (EmptyResultDataAccessException ex) {
            address = null;
        }

        return address;
    }

    public BigInteger saveAddress(Address address) {
        log.trace("AddressDao.saveAddress {}", address);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(saveAddress_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, address.getPremises());
            ps.setString(2, address.getAddress_line_1());
            ps.setString(3, address.getLocality());
            ps.setString(4, address.getPostal_code());
            ps.setString(5, address.getCountry());
            return ps;
        }, keyHolder);

        return BigInteger.valueOf(Objects.requireNonNull(keyHolder.getKey()).longValue());
    }
}
