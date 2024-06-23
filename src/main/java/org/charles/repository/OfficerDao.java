package org.charles.repository;

import lombok.extern.slf4j.Slf4j;
import org.charles.dto.Officer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

@Repository
@Slf4j
public class OfficerDao {

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final String findOfficersByCompanyNumber_SQL = "SELECT * FROM officer WHERE company_number = ?";
    private static final String saveOfficer_SQL = """
            INSERT INTO officer
            (company_number, name, officer_role, appointed_on, address_id)
            VALUES (?,?,?,?,?)""";

    public ArrayList<Officer> findByCompanyNumber(String companyNumber) {
        log.trace("OfficerDao.findByCompanyNumber {}", companyNumber);
        ArrayList<Officer> officers = new ArrayList<>();

        try {
            officers = (ArrayList<Officer>) jdbcTemplate.query(findOfficersByCompanyNumber_SQL, getMapper(), companyNumber);
        } catch (EmptyResultDataAccessException ex) {
            //ignore
        }

        return officers;
    }

    public void saveOfficer(Officer officer) {
        log.trace("OfficerDao.saveOfficer {}", officer);

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(saveOfficer_SQL);
            ps.setString(1, officer.getCompany_number());
            ps.setString(2, officer.getName());
            ps.setString(3, officer.getOfficer_role());
            ps.setString(4, officer.getAppointed_on());
            ps.setLong(5, officer.getAddress_id().longValue());
            return ps;
        });
    }

    //todo - make private??
    public static RowMapper<Officer> getMapper() {
        return new RowMapper<Officer>() {
            @Override
            public Officer mapRow(ResultSet rs, int rowNum) throws SQLException {
                Officer officer = new Officer();

                officer.setCompany_number(rs.getString("company_number"));
                officer.setName(rs.getString("name"));
                officer.setOfficer_role(rs.getString("officer_role"));
                officer.setAppointed_on(rs.getString("appointed_on"));
                officer.setAddress_id(BigInteger.valueOf(rs.getLong("address_id")));

                return officer;
            }
        };
    }

}
