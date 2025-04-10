package com.tsl.controller;

import com.tsl.response.EmailValidationResponse;
import com.tsl.service.SingleEmailVerificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/verifyemailaddress")
public class SingleEmailVerificationController {

    @PostMapping("/verify-email")
    public String verifySingleEmail(@RequestParam("email") String email, Model model) {
        EmailValidationResponse response = SingleEmailVerificationService.validateEmail(email);
        model.addAttribute("responses", List.of(response)); // Wrap response in a list for consistency
        return "result"; // Refers to result.html
    }
}
//single Email api
//http://localhost:8080/verifyemailaddress/verify-email



