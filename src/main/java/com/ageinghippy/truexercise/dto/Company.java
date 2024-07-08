package com.ageinghippy.truexercise.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigInteger;
import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Company {
    private String title;
    private String company_number;
    private String company_type;
    private String company_status;
    private String date_of_creation;
    @JsonIgnore
    private BigInteger address_id;
    private Address address;
    private ArrayList<Officer> officers;

    public void addOfficer(Officer officer) {
        if (officers == null) {
            officers = new ArrayList<>();
        }
        officers.add(officer);
    }

}
