package org.charles.truexercise.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.charles.truexercise.dto.Company;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;

@Repository
@Slf4j
@AllArgsConstructor
public class CompanyDao {

    private final JdbcTemplate jdbcTemplate;

    private static final String findCompanyByCompanyNumber_SQL = "SELECT * FROM company WHERE company_number = ?";

    private static final String saveCompany_SQL = """
            INSERT INTO Company
            ( title, company_number, company_type, company_status, date_of_creation, address_id)
            values ( ?, ?, ?, ?, ?, ?)""";

    public boolean companyExists(String companyNumber) {
        log.trace("CompanyDao.companyExists {}", companyNumber);
        return (findByCompanyNumber(companyNumber) != null);
    }

    public Company findByCompanyNumber(String companyNumber) {
        log.trace("CompanyDao.findByCompanyNumber {}", companyNumber);
        Company company;

        try {
            company = jdbcTemplate.queryForObject(findCompanyByCompanyNumber_SQL, new BeanPropertyRowMapper<>(Company.class), companyNumber);
        } catch (EmptyResultDataAccessException ex) {
            company = null;
        }
        return company;
    }

    public void saveCompany(Company company) {
        log.trace("CompanyDao.saveCompany {}", company);

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(saveCompany_SQL);
            ps.setString(1, company.getTitle());
            ps.setString(2, company.getCompany_number());
            ps.setString(3, company.getCompany_type());
            ps.setString(4, company.getCompany_status());
            ps.setString(5, company.getDate_of_creation());
            ps.setLong(6, company.getAddress_id().longValue());
            return ps;
        });

    }

}
