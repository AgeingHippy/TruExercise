package com.ageinghippy.truexercise.dto.truProxyApi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TruProxyApiOfficerSearchResponse {

    private int total_results;
    private int resigned_count;
    private String kind;
    private ArrayList<TruProxyApiOfficer> items;

}
