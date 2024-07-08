package com.ageinghippy.truexercise.dto.truProxyApi;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruProxyApiOfficer {

    private String name;
    private String officer_role;
    private String appointed_on;
    private String resigned_on;
    private TruProxyApiAddress address;

}
