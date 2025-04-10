//package com.tsl.controller;
package com.tsl.controller;

import com.tsl.model.UploadedFile;
import com.tsl.repository.UploadedFileRepository;
import com.tsl.service.EmailVerificationService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
//Initial Commit to Verify email validation tool with Credential.
@RequestMapping("/email-verification")
public class EmailVerificationController {

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Autowired
    private UploadedFileRepository uploadedFileRepository;

    @GetMapping
    public String showUploadForm(Model model) {
        model.addAttribute("uploadedFiles", emailVerificationService.getAllUploadedFiles());
        return "upload-form";
    }

    @PostMapping("/upload")
    // upload exel file
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            UploadedFile uploadedFile = emailVerificationService.saveUploadedFile1(file.getInputStream(), file.getOriginalFilename());
            redirectAttributes.addFlashAttribute("message", "File uploaded successfully! File ID: " + uploadedFile.getId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error uploading file: " + e.getMessage());
        }
        return "redirect:/email-verification";
    }

    @GetMapping("/verify/{fileId}")
    // Verify entire Exel File
    public String verifyFile(@PathVariable Long fileId, RedirectAttributes redirectAttributes) {
        try {
            emailVerificationService.verifyEmails(fileId);
            redirectAttributes.addFlashAttribute("message", "Verification completed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error during verification: " + e.getMessage());
        }
        return "redirect:/email-verification";
    }

    @GetMapping("/download/{fileId}")
    public void downloadFile(@PathVariable Long fileId, HttpServletResponse response) {
        try {
            byte[] fileContent = emailVerificationService.getVerificationResults1(fileId);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=verification-results.xlsx");
            response.getOutputStream().write(fileContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/search")
    public String searchUploadedFiles(@RequestParam("query") String query, Model model) {
        try {
            List<UploadedFile> files = uploadedFileRepository.findByFileNameContainingIgnoreCase(query);
            model.addAttribute("uploadedFiles", files);
        } catch (Exception e) {
            model.addAttribute("message", "Error during search: " + e.getMessage());
        }
        return "upload-form";
    }
    @GetMapping("/delete/{fileId}")
    public String deleteFile(@PathVariable Long fileId, RedirectAttributes redirectAttributes) {
        try {
            emailVerificationService.deleteFile(fileId); // Ensure this service deletes the file
            redirectAttributes.addFlashAttribute("message", "File removed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Failed to remove file: " + e.getMessage());
        }
        return "redirect:/email-verification";
    }}

//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import com.tsl.service.EmailVerifierService;
//
//	@Controller
//	@RequestMapping("/email-verification")
//	public class EmailVerifierController {
//
//	    @Autowired
//	    private EmailVerifierService emailVerifierService;
//
//	    private byte[] resultFile; 
//	    private String resultFileName; 
//
//	   
//	    @GetMapping
//	    public String showUploadPage() {
//	        return "upload-email-file";
//	    }
//
//	    @PostMapping("/upload")
//	    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
//	        try {
//	          
//	            resultFile = emailVerifierService.processAndVerifyEmails(file.getInputStream(), file.getOriginalFilename());
//	            resultFileName = "verification_results.xlsx"; 
//
//	            model.addAttribute("success", "File processed successfully!");
//	            return "download-email-results"; 
//	        } catch (Exception e) {
//	            model.addAttribute("error", "Error processing the file: " + e.getMessage());
//	            return "upload-email-file";
//	        }
//	    }
//	    @GetMapping("/download")
//	    public ResponseEntity<ByteArrayResource> downloadResultFile() {
//	        if (resultFile == null || resultFile.length == 0) {
//	            throw new RuntimeException("No file available for download!");
//	        }
//
//	        ByteArrayResource resource = new ByteArrayResource(resultFile);
//
//	        return ResponseEntity.ok()
//	                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + resultFileName)
//	                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//	                .contentLength(resultFile.length)
//	                .body(resource);
//	    }
//	}
