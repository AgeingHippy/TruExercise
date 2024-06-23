package org.charles.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@Builder
@Slf4j
public class CompanyResponse {
    private int total_results;
    private ArrayList<Company> items;

    public CompanyResponse() {
        total_results = 0;
        items = new ArrayList<>();
    }

    public void addCompany(Company company) {
        if (items == null) {
            log.debug("*********** INSTANTIATING LIST ***********");
            items = new ArrayList<>();
        }
        items.add(company);
    }
}
