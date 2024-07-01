package org.charles.truexercise.dto;

import lombok.Builder;
import lombok.Data;
import org.charles.truexercise.utility.Utilities;

import java.text.MessageFormat;

@Data
@Builder
public class CompanyRequest {
    private String companyName;
    private String companyNumber;
    private String apiKey;
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
