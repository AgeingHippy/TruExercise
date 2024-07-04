package org.charles.truexercise.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import org.charles.truexercise.utility.Utilities;

import java.text.MessageFormat;

@Data
@Builder
public class CompanyRequest {
    private String companyName;
    private String companyNumber;
    @JsonIgnore
    private String apiKey;
    @JsonIgnore
    private boolean activeOnly;

    public void setActiveOnly(String activeOnly) {
        this.activeOnly = !activeOnly.isBlank() && activeOnly.equalsIgnoreCase("yes");
    }

    //override toString to mask apiKey when logging
    @Override
    public String toString() {
        return MessageFormat.format("CompanyRequest(companyName={0}, companyNumber={1}, apiKey={2}, activeOnly={3})"
                ,companyName, companyNumber, apiKey != null ? Utilities.maskString(apiKey) : apiKey, activeOnly);
    }
}
