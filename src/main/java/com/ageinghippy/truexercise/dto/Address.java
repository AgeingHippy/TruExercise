package com.ageinghippy.truexercise.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @JsonIgnore
    private BigInteger address_id;
    private String premises;
    private String address_line_1;
    private String locality;
    private String postal_code;
    private String country;

}
