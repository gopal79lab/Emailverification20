package com.tsl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EmailVerificationProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailVerificationProjectApplication.class, args);
	}

}

// http://localhost:8080/email-verification
// single email verification - http://localhost:8080/verifyemailaddress/email-verification


