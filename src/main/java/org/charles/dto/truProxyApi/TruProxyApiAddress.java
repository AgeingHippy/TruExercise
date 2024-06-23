package org.charles.dto.truProxyApi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TruProxyApiAddress {
    private String premises;
    private String address_line_1;
    private String locality;
    private String postal_code;
    private String country;
}
