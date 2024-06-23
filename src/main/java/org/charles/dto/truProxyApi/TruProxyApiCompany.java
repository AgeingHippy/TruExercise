package org.charles.dto.truProxyApi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TruProxyApiCompany {
    private String title;
    private String company_number;
    private String company_type;
    private String company_status;
    private String date_of_creation;
    private TruProxyApiAddress address;
}
