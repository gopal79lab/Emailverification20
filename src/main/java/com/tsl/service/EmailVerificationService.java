//package com.tsl.service;
//
//import com.tsl.model.EmailVerificationResponse;
//import com.tsl.model.UploadedFile;
//import com.tsl.repository.EmailRecordRepository;
//import com.tsl.repository.UploadedFileRepository;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.xbill.DNS.Lookup;
//import org.xbill.DNS.MXRecord;
//import org.xbill.DNS.Record;
//import org.xbill.DNS.Type;
//
//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.InetAddress;
//import java.net.Socket;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class EmailVerifierService {
//
//    @Autowired
//    private UploadedFileRepository uploadedFileRepository;
//
//    @Autowired
//    private EmailRecordRepository emailVerificationResponseRepository;
//
//    // Process and verify emails from the uploaded file
//    public byte[] processAndVerifyEmails(InputStream inputStream, String fileName) {
//        try (Workbook inputWorkbook = WorkbookFactory.create(inputStream);
//             Workbook outputWorkbook = new XSSFWorkbook()) {
//
//            Sheet inputSheet = inputWorkbook.getSheetAt(0);
//            Sheet outputSheet = outputWorkbook.createSheet("Verification Results");
//
//            // Save metadata for the uploaded file
//            UploadedFile uploadedFile = new UploadedFile();
//            uploadedFile.setFileName(fileName);
//            uploadedFile.setEmailVerified(false);
//            uploadedFile = uploadedFileRepository.save(uploadedFile);
//
//            List<EmailVerificationResponse> responses = new ArrayList<>();
//
//            // Create header row for the result Excel sheet
//            Row headerRow = outputSheet.createRow(0);
//            headerRow.createCell(0).setCellValue("Email");
//            headerRow.createCell(1).setCellValue("Status");
//
//            int rowIndex = 1; // Start populating results after the header
//            for (Row row : inputSheet) {
//                if (row.getRowNum() == 0) continue; // Skip the header of input file
//
//                Cell emailCell = row.getCell(0);
//                if (emailCell != null && emailCell.getCellType() == CellType.STRING) {
//                    String email = emailCell.getStringCellValue().trim();
//                    boolean isValid = verifyEmail(email);
//
//                    // Save the verification result
//                    EmailVerificationResponse response = new EmailVerificationResponse();
//                    response.setEmail(email);
//                    response.setStatus(isValid ? "Valid" : "Invalid");
//                    response.setUploadedFile(uploadedFile);
//                    responses.add(response);
//
//                    // Write the result to the output sheet
//                    Row outputRow = outputSheet.createRow(rowIndex++);
//                    outputRow.createCell(0).setCellValue(email);
//                    outputRow.createCell(1).setCellValue(isValid ? "Valid" : "Invalid");
//                }
//            }
//
//            // Save responses and mark the file as verified
//            emailVerificationResponseRepository.saveAll(responses);
//            uploadedFile.setEmailVerified(true);
//            uploadedFileRepository.save(uploadedFile);
//
//            // Write output workbook to byte array
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            outputWorkbook.write(outputStream);
//            return outputStream.toByteArray();
//
//        } catch (Exception e) {
//            throw new RuntimeException("Error processing and verifying emails", e);
//        }
//    }

//    // Verify an email
//    private boolean verifyEmail(String email) {
//        if (email == null || !email.contains("@")) return false;
//
//        String domain = email.substring(email.indexOf("@") + 1);
//        try {
//            List<String> mxRecords = getMXRecords(domain);
//            if (mxRecords == null || mxRecords.isEmpty()) return false;
//
//            for (String mxRecord : mxRecords) {
//                if (checkSMTP(mxRecord, email)) {
//                    return true;
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Error verifying email: " + e.getMessage());
//        }
//        return false;
//    }
//
//    // Get MX records for the domain
//    private List<String> getMXRecords(String domain) {
//        try {
//            Record[] records = new Lookup(domain, Type.MX).run();
//            if (records == null) return null;
//
//            List<String> mxRecords = new ArrayList<>();
//            for (Record record : records) {
//                mxRecords.add(((MXRecord) record).getTarget().toString());
//            }
//            return mxRecords;
//        } catch (Exception e) {
//            System.err.println("Error retrieving MX records: " + e.getMessage());
//            return null;
//        }
//    }
//
//    // Check email using SMTP
//    private boolean checkSMTP(String smtpHost, String email) {
//        try (Socket socket = new Socket(smtpHost, 25);
//             InputStream reader = socket.getInputStream();
//             OutputStream writer = socket.getOutputStream()) {
//
//            sendCommand(writer, "HELO " + InetAddress.getLocalHost().getHostName());
//            readResponse(reader);
//
//            sendCommand(writer, "MAIL FROM:<test@example.com>");
//            readResponse(reader);
//
//            sendCommand(writer, "RCPT TO:<" + email + ">");
//            String response = readResponse(reader);
//
//            return response.startsWith("250");
//
//        } catch (Exception e) {
//            System.err.println("SMTP connection failed: " + e.getMessage());
//            return false;
//        }
//    }
//
//    private void sendCommand(OutputStream writer, String command) throws Exception {
//        writer.write((command + "\r\n").getBytes());
//        writer.flush();
//    }
//
//    private String readResponse(InputStream reader) throws Exception {
//        StringBuilder response = new StringBuilder();
//        int ch;
//        while ((ch = reader.read()) != -1) {
//            response.append((char) ch);
//            if (response.toString().endsWith("\r\n")) break;
//        }
//        return response.toString().trim();
//    }
//}

package com.tsl.service;
import com.tsl.model.EmailVerificationResponse;
import com.tsl.model.EmailVerificationStatus;
import com.tsl.model.UploadedFile;
import com.tsl.repository.EmailVerificationResponseRepository;
import com.tsl.repository.EmailVerificationStatusRepository;
import com.tsl.repository.UploadedFileRepository;

import jakarta.transaction.Transactional;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.tsl.service.SingleEmailVerificationService.getMXServer;

@Service
    public class EmailVerificationService {

        @Autowired
        private UploadedFileRepository uploadedFileRepository;

        @Autowired
        private EmailVerificationStatusRepository emailVerificationStatusRepository;

        @Autowired
        private EmailVerificationResponseRepository emailVerificationResponseRepository;
        
        public byte[] getVerificationResults1(Long fileId) throws Exception {
            UploadedFile uploadedFile = uploadedFileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("Uploaded file not found"));

            List<EmailVerificationStatus> statuses = emailVerificationStatusRepository.findByUploadedFile(uploadedFile);

            try (Workbook outputWorkbook = new XSSFWorkbook()) {
                Sheet outputSheet = outputWorkbook.createSheet("Verification Results");

                Row headerRow = outputSheet.createRow(0);
                headerRow.createCell(0).setCellValue("Email");
                headerRow.createCell(1).setCellValue("Status");
                headerRow.createCell(2).setCellValue("SMTP Response");

                int rowIndex = 1;
                for (EmailVerificationStatus status : statuses) {
                    Row outputRow = outputSheet.createRow(rowIndex++);
                    outputRow.createCell(0).setCellValue(status.getEmail());
                    outputRow.createCell(1).setCellValue(status.getStatus());
                    outputRow.createCell(2).setCellValue("Valid".equals(status.getStatus()) ? "250 OK" : "SMTP Verification Failed");
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputWorkbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
             public UploadedFile saveUploadedFile1(InputStream inputStream, String fileName) throws Exception {
                 //store exel file to Core_File-db
            try (Workbook inputWorkbook = WorkbookFactory.create(inputStream)) {
                UploadedFile uploadedFile = new UploadedFile();
                uploadedFile.setFileName(fileName);
                uploadedFile.setUploadTime(LocalDateTime.now());
                uploadedFile.setVerified(false);  
                uploadedFile = uploadedFileRepository.save(uploadedFile);


                //Read each email and store in "Detail_email_core-db"
                List<EmailVerificationStatus> statusList = new ArrayList<>();
                Sheet inputSheet = inputWorkbook.getSheetAt(0);

                for (Row row : inputSheet) {
                    if (row.getRowNum() == 0) continue;
                    String email = row.getCell(0).getStringCellValue().trim();
                    EmailVerificationStatus status = new EmailVerificationStatus();
                    status.setEmail(email);
                    status.setStatus("Pending");
                    status.setUploadedFile(uploadedFile);
                    statusList.add(status);
                }
                emailVerificationStatusRepository.saveAll(statusList);
                 return uploadedFile;
            }
         }

             public byte[] verifyEmails(Long fileId) throws Exception {
            // get exel file from File Repository
             UploadedFile uploadedFile = uploadedFileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("Uploaded file not found"));
           // get all email from that file and store in List format as statuses
            List<EmailVerificationStatus> statuses = emailVerificationStatusRepository.findByUploadedFile(uploadedFile);
            try (Workbook outputWorkbook = new XSSFWorkbook()) {
                Sheet outputSheet = outputWorkbook.createSheet("Verification Results");

                Row headerRow = outputSheet.createRow(0);
                headerRow.createCell(0).setCellValue("Email");
                headerRow.createCell(1).setCellValue("Status");
                headerRow.createCell(2).setCellValue("SMTP Response");

                int rowIndex = 1, validCount = 0, invalidCount = 0;
                for (EmailVerificationStatus status : statuses) {
                    String email = status.getEmail();
                    String verificationStatus;
                    String smtpResponse;

                    if (isValidFormat(email)) {
                        boolean isVerified = verifyEmail(email);
                        verificationStatus = isVerified ? "Valid" : "Invalid";
                        smtpResponse = isVerified ? "250 OK" : "SMTP Verification Failed";
                    } else {
                        verificationStatus = "Invalid";
                        smtpResponse = "Invalid Format";
                    }

                    status.setStatus(verificationStatus);
                    emailVerificationStatusRepository.save(status);

                    Row outputRow = outputSheet.createRow(rowIndex++);
                    outputRow.createCell(0).setCellValue(email);
                    outputRow.createCell(1).setCellValue(verificationStatus);
                    outputRow.createCell(2).setCellValue(smtpResponse);

                    if ("Valid".equals(verificationStatus)) validCount++;
                    else invalidCount++;
                }

                uploadedFile.setVerified(true);
                uploadedFileRepository.save(uploadedFile);
                int TotalEmail=validCount+invalidCount;
                EmailVerificationResponse response = new EmailVerificationResponse();
                response.setTotalEmails(TotalEmail);
                response.setNoOfValidEmails(validCount);
                response.setNoOfInvalidEmails(invalidCount);
                response.setFileName(uploadedFile.getFileName());
                response.setUploadedFile(uploadedFile);
                emailVerificationResponseRepository.save(response);

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputWorkbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }

        private boolean isValidFormat(String email) {
            String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
            boolean valid = email.matches(regex);
            System.out.println("Email: " + email + ", Valid Format: " + valid);
            return valid;
        }

        public boolean verifyEmail(String email) {
            if (email == null || !email.contains("@")) {
                System.err.println("Invalid email format: " + email);
                return false;
            }

            String domain = email.substring(email.indexOf("@") + 1);
            try {
                List<String> mxRecords = getMXRecords(domain);
                if (mxRecords == null || mxRecords.isEmpty()) {
                    System.err.println("No MX records found for domain: " + domain);
                    return false;
                }

                for (String mxRecord : mxRecords) {
                    System.out.println("Trying MX record: " + mxRecord);
                   // if (checkSMTP(mxRecord, email)) {
                    if (checkSMTP(email)) {

                        System.out.println("SMTP validation succeeded for email: " + email);
                        return true;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error during email verification: " + e.getMessage());
            }

            System.err.println("SMTP validation failed for email: " + email);
            return false;
        }
//        private boolean checkSMTP(String smtpHost, String email) {
//            try (Socket socket = new Socket()) {
//                socket.connect(new InetSocketAddress(smtpHost, 25), 10000); // Set 10-second timeout
//
//                try (InputStream reader = socket.getInputStream();
//                     OutputStream writer = socket.getOutputStream()) {
//
//                    sendCommand(writer, "HELO example.com");
//                    if (!readResponse(reader).startsWith("250")) return false;
//
//                    sendCommand(writer, "MAIL FROM:<test@example.com>");
//                    if (!readResponse(reader).startsWith("250")) return false;
//
//                    // Send RCPT TO command
//                    sendCommand(writer, "RCPT TO:<" + email + ">");
//                    String response = readResponse(reader);
//
//                    // Validate RCPT response
//                    return response.startsWith("250");
//                }
//                } catch (Exception e) {
//                System.err.println("SMTP validation error for email: " + email + " - " + e.getMessage());
//                return false;
//            }
//        }


        //Green Code, Running Code, port 25 - check SMTP
        private boolean checkSMTP(String email) {
            String domain = email.substring(email.indexOf("@") + 1);
            String smtpServer = getMXServer(domain);
            if (smtpServer == null) {
                return false; // No MX server found
            }
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(smtpServer, 25), 50000); // Timeout of 5 seconds
                socket.setSoTimeout(5000); // Set timeout for reading data from the socket
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                // Read the server's greeting
                String response = reader.readLine();
                if (!response.startsWith("220")) {
                    return false; // SMTP Server not available
                }
                // Send HELO command
                writer.write("HELO " + domain + "\r\n");
                writer.flush();
                response = reader.readLine();
                if (!response.startsWith("250")) {
                    return false; // HELO command failed
                }

                // Send MAIL FROM command (this is just for identification, not an actual email)
                writer.write("MAIL FROM:<test@" + domain + ">\r\n");
                writer.flush();
                response = reader.readLine();
                if (!response.startsWith("250")) {
                    return false; // MAIL FROM command failed
                }

                // Send RCPT TO command to check the recipient email address
                writer.write("RCPT TO:<" + email + ">\r\n");
                writer.flush();
                response = reader.readLine();
                return response.startsWith("250"); // If server responds with 250, the email address exists

            } catch (IOException e) {
                e.printStackTrace();
                return false; // SMTP check failed
            }
        }

        private List<String> getMXRecords(String domain) {
            try {
                Record[] records = new Lookup(domain, Type.MX).run();
                if (records == null) {
                    System.err.println("No MX records found for domain: " + domain);
                    return Collections.emptyList();
                }

                List<String> mxRecords = new ArrayList<>();
                for (Record record : records) {
                    mxRecords.add(((MXRecord) record).getTarget().toString());
                }
                System.out.println("Retrieved MX Records: " + mxRecords);
                return mxRecords;
            } catch (Exception e) {
                System.err.println("Failed to retrieve MX records for domain: " + domain + " - " + e.getMessage());
                return Collections.emptyList();
            }
        }

        private void sendCommand(OutputStream writer, String command) throws Exception {
            writer.write((command + "\r\n").getBytes());
            writer.flush();
            System.out.println("Sent command: " + command);
        }

        private String readResponse(InputStream reader) throws Exception {
            StringBuilder response = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                response.append((char) ch);
                if (response.toString().endsWith("\r\n")) break;
            }
            String finalResponse = response.toString().trim();
            System.out.println("SMTP Response: " + finalResponse);
            return finalResponse;
        }






        public List<UploadedFile> getAllUploadedFiles() {
            return uploadedFileRepository.findAll();
        }
        @Transactional
        public void deleteFile(Long fileId) {
            emailVerificationStatusRepository.deleteByUploadedFileId(fileId);
            uploadedFileRepository.deleteById(fileId);
        }
	
	
    }

