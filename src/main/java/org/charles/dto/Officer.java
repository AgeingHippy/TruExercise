package org.charles.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Officer {
    @JsonIgnore
    private String company_number;
    private String name;
    private String officer_role;
    private String appointed_on;
    private Address address;
    @JsonIgnore
    BigInteger address_id;

}
