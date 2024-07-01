package org.charles.truexercise.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.charles.truexercise.dto.Company;
import org.charles.truexercise.repository.AddressDao;
import org.charles.truexercise.repository.CompanyDao;
import org.charles.truexercise.repository.OfficerDao;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
@AllArgsConstructor
public class PersistenceService {

    private final CompanyDao companyDao;
    private final AddressDao addressDao;
    private final OfficerDao officerDao;

    public Company fetchCompany(String companyNumber) {
        log.trace("PersistenceService.fetchCompany {}}", companyNumber);
        Company company = companyDao.findByCompanyNumber(companyNumber);

        if (company != null) {
            if (company.getAddress_id() != null) {
                //fetch address
                company.setAddress(addressDao.findById(company.getAddress_id()));
            }
            //populate officers
            company.setOfficers(officerDao.findByCompanyNumber(companyNumber));
            //populate officer addresses
            company.getOfficers().forEach(officer -> officer.setAddress(addressDao.findById(officer.getAddress_id())));
        }

        return company;
    }

    public void saveCompanies(ArrayList<Company> companies) {
        log.trace("PersistenceService.saveCompanies {}", companies);

        companies.forEach(company -> {
            //only save company if it doesn't exist
            //Note, we assume the application is periodically refreshed so any changes in officers will only be out of date for a short time
            if (!companyDao.companyExists(company.getCompany_number())) {
                company.setAddress_id(addressDao.saveAddress(company.getAddress()));
                companyDao.saveCompany(company);
                company.getOfficers().forEach(officer -> {
                    officer.setAddress_id(addressDao.saveAddress(officer.getAddress()));
                    officerDao.saveOfficer(officer);
                });
            }
        });
    }

}
