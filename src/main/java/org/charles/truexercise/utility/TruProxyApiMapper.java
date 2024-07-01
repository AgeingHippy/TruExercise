package org.charles.truexercise.utility;

import org.charles.truexercise.dto.Address;
import org.charles.truexercise.dto.Company;
import org.charles.truexercise.dto.Officer;
import org.charles.truexercise.dto.truProxyApi.TruProxyApiAddress;
import org.charles.truexercise.dto.truProxyApi.TruProxyApiCompany;
import org.charles.truexercise.dto.truProxyApi.TruProxyApiOfficer;
import org.springframework.stereotype.Component;

@Component
public class TruProxyApiMapper {

    public static Company mapToCompany(TruProxyApiCompany truProxyApiCompany) {
        Company company = new Company();

        company.setTitle(truProxyApiCompany.getTitle());
        company.setCompany_number(truProxyApiCompany.getCompany_number());
        company.setCompany_type(truProxyApiCompany.getCompany_type());
        company.setCompany_status(truProxyApiCompany.getCompany_status());
        company.setDate_of_creation(truProxyApiCompany.getDate_of_creation());
        company.setAddress(mapToAddress(truProxyApiCompany.getAddress()));

        return company;
    }

    public static Address mapToAddress(TruProxyApiAddress truProxyApiAddress) {
        Address address = new Address();

        address.setPremises(truProxyApiAddress.getPremises());
        address.setAddress_line_1(truProxyApiAddress.getAddress_line_1());
        address.setLocality(truProxyApiAddress.getLocality());
        address.setPostal_code(truProxyApiAddress.getPostal_code());
        address.setCountry(truProxyApiAddress.getCountry());

        return address;
    }

    public static Officer mapToOfficer(TruProxyApiOfficer truProxyApiOfficer) {
        Officer officer = new Officer();

        officer.setName(truProxyApiOfficer.getName());
        officer.setOfficer_role(truProxyApiOfficer.getOfficer_role());
        officer.setAppointed_on(truProxyApiOfficer.getAppointed_on());
        officer.setAddress(mapToAddress(truProxyApiOfficer.getAddress()));

        return officer;
    }

}
