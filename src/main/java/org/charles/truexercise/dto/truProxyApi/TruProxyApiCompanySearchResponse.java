package org.charles.truexercise.dto.truProxyApi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruProxyApiCompanySearchResponse {
    private String page_number;
    private String kind;
    private int total_results;
    private ArrayList<TruProxyApiCompany> items;

}
