package org.charles.service;

import lombok.extern.slf4j.Slf4j;
import org.charles.dto.Company;
import org.charles.repository.AddressDao;
import org.charles.repository.CompanyDao;
import org.charles.repository.OfficerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service
@Slf4j
public class PersistenceService {

    @Autowired
    CompanyDao companyDao;
    @Autowired
    AddressDao addressDao;
    @Autowired
    OfficerDao officerDao;

    public Company fetchCompany(String companyNumber) {
        log.trace("PersistenceService.fetchCompany {}}",companyNumber);
        Company company = companyDao.findByCompanyNumber(companyNumber);

        if (company != null) {
            if (company.getAddress_id() != null) {
                //fetch address
                company.setAddress(addressDao.findById(company.getAddress_id()));
            }
            //populate officers
            company.setOfficers(officerDao.findByCompanyNumber(companyNumber));
            //populate officer addresses
            company.getOfficers().forEach(officer -> {
                officer.setAddress(addressDao.findById(officer.getAddress_id()));
            });
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
