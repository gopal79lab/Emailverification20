package com.tsl.response;

import lombok.Data;

@Data
public class EmailValidationResponse {
    private String email;
    private String message;
    private String mxRecordStatus;
    private String smtpStatus;

}
