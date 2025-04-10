package com.tsl.service;

import com.tsl.config.SMTPConfig;
import com.tsl.response.EmailValidationResponse;
import org.springframework.beans.factory.annotation.Autowired;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Hashtable;
import java.util.regex.Pattern;

public class SingleEmailVerificationService {
    @Autowired
    private SMTPConfig smtpConfig;
    private static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static EmailValidationResponse validateEmail(String email) {
        EmailValidationResponse response = new EmailValidationResponse();
        response.setEmail(email);

        // Step 1: Syntax Validation
        if (!EMAIL_REGEX.matcher(email).matches()) {
            response.setMessage("Invalid email syntax.");
            return response;
        }
        // Step 2: Check for MX records (Domain check)
        if (hasMXRecord(email)) {
            response.setMessage("Email syntax is valid, and the domain has MX records. Checking SMTP...");
            // Step 3: SMTP check (Global email existence check)
            if (checkSMTP(email)) {
                response.setMessage("Email is valid and exists (SMTP check passed).");
            } else {
                response.setMessage("Email is valid, but SMTP check failed (email does not exist). Risky.");
            }
        } else {
            response.setMessage("Email syntax is valid, but the domain does not have MX records. Risky.");
        }
        return response;
    }
    // Smtp connection  with  -- Authentication credential
//private static boolean checkSMTP(String email) {
//    String domain = email.substring(email.indexOf("@") + 1);
//    String smtpServer = getMXServer(domain);
//    if (smtpServer == null) {
//        return false; // No MX server found
//    }
//
//   String username = "support@mail.propeltechsystems.com";
//   String password = "TLinuxlab87";
//   // String username = "crm@tslmarketing.in";
//   // String password = "TSL@Pune1";
//
////    try (Socket socket = new Socket(smtpServer, 587);
////         BufferedReader initialReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
////         BufferedWriter initialWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
////
////        // Read server greeting
////        if (!readResponse(initialReader).startsWith("220")) {
////            return false;
////        }
////
////        // Send EHLO
////        sendCommand(initialWriter, "EHLO localhost");
////        if (!readResponse(initialReader).contains("250")) {
////            return false;
////        }
////
////        // Initiate STARTTLS
////        sendCommand(initialWriter, "STARTTLS");
////        if (!readResponse(initialReader).startsWith("220")) {
////            return false;
////        }
////
////        // Upgrade to SSL
////        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
////        try (SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(socket, smtpServer, 587, true);
////             BufferedReader reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
////             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sslSocket.getOutputStream()))) {
////
////            // Re-send EHLO
////            sendCommand(writer, "EHLO localhost");
////            if (!readResponse(reader).contains("250")) {
////                return false;
////            }
////
////            // Authenticate
////            sendCommand(writer, "AUTH LOGIN");
////            if (!readResponse(reader).startsWith("334")) {
////                return false;
////            }
////            sendCommand(writer, Base64.getEncoder().encodeToString(username.getBytes()));
////            if (!readResponse(reader).startsWith("334")) {
////                return false;
////            }
////            sendCommand(writer, Base64.getEncoder().encodeToString(password.getBytes()));
////            if (!readResponse(reader).startsWith("235")) {
////                return false;
////            }
////
////            // Verify email
////            sendCommand(writer, "MAIL FROM:<" + username + ">");
////            if (!readResponse(reader).startsWith("250")) {
////                return false;
////            }
////            sendCommand(writer, "RCPT TO:<" + email + ">");
////            String response = readResponse(reader);
////            return response.startsWith("250");
////        }
////    } catch (IOException e) {
////        e.printStackTrace();
////        return false;
////    }
//
//    // checking with AUTH  PLAIN ()
//
//    try (Socket socket = new Socket(smtpServer, 587);
//         BufferedReader initialReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//         BufferedWriter initialWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
//
//        // Read server greeting
//        if (!readResponse(initialReader).startsWith("220")) {
//            return false;
//        }
//
//        // Send EHLO
//        sendCommand(initialWriter, "EHLO localhost");
//        String ehloResponse = readResponse(initialReader);
//        System.out.println("EHLO Response: " + ehloResponse);
//        if (!readResponse(initialReader).contains("250")) {
//            return false;
//        }
//
//        // Initiate STARTTLS
//        sendCommand(initialWriter, "STARTTLS");
//        if (!readResponse(initialReader).startsWith("220")) {
//            return false;
//        }
//
//        // Upgrade to SSL
//        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
//        try (SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(socket, smtpServer, 587, true);
//             BufferedReader reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
//             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sslSocket.getOutputStream()))) {
//
//            // Re-send EHLO
//            sendCommand(writer, "EHLO localhost");
//            String rehloResponse = readResponse(reader);
//            System.out.println("EHLO Response: " + rehloResponse);
//            if (!readResponse(reader).contains("250")) {
//                return false;
//            }
//
//            // Authenticate using AUTH PLAIN
//            String authPlain = "\0" + username + "\0" + password;
//            String encodedAuth = Base64.getEncoder().encodeToString(authPlain.getBytes(StandardCharsets.UTF_8));
//            sendCommand(writer, "AUTH PLAIN " + encodedAuth);
//            String authResponse = readResponse(reader);
//            System.out.println("AUTH Response: " + authResponse);
//            if (!readResponse(reader).startsWith("235")) {
//                return false;
//            }
//
//            // Verify email
//            sendCommand(writer, "MAIL FROM:<" + username + ">");
//            String mailFromResponse = readResponse(reader);
//            System.out.println("MAIL FROM Response: " + mailFromResponse);
//            if (!readResponse(reader).startsWith("250")) {
//                return false;
//            }
//            sendCommand(writer, "RCPT TO:<" + email + ">");
//            String rcptToResponse = readResponse(reader);
//            System.out.println("RCPT TO Response: " + rcptToResponse);
//            String response = readResponse(reader);
//            return response.startsWith("250");
//        }
//    } catch (IOException e) {
//        e.printStackTrace();
//        return false;
//    }
//}
    // SMTP CORRECTED CODE FOR AUTHENTICATION SOLVER

    private static boolean checkSMTP(String email) {
        String domain = email.substring(email.indexOf("@") + 1);
        String smtpServer = getMXServer(domain);
        if (smtpServer == null) {
            return false; // No MX server found
        }

        String username = System.getenv("SMTP_USERNAME"); // Use environment variables
        String password = System.getenv("SMTP_PASSWORD");

        if (username == null || password == null) {
            throw new IllegalStateException("SMTP credentials are not set.");
        }

        try (Socket socket = new Socket(smtpServer, 587);
             BufferedReader initialReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter initialWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            if (!readAndLogResponse(initialReader, "Greeting").startsWith("220")) return false;

            // Send EHLO
            sendCommand(initialWriter, "EHLO localhost");
            if (!readAndLogResponse(initialReader, "EHLO").contains("250")) return false;

            // Initiate STARTTLS
            sendCommand(initialWriter, "STARTTLS");
            if (!readAndLogResponse(initialReader, "STARTTLS").startsWith("220")) return false;

            // Upgrade to SSL
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            try (SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(socket, smtpServer, 587, true);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sslSocket.getOutputStream()))) {

                // Re-send EHLO
                sendCommand(writer, "EHLO localhost");
                if (!readAndLogResponse(reader, "Re-EHLO").contains("250")) return false;

                // Authenticate using AUTH PLAIN
                String authPlain = "\0" + username + "\0" + password;
                String encodedAuth = Base64.getEncoder().encodeToString(authPlain.getBytes(StandardCharsets.UTF_8));
                sendCommand(writer, "AUTH PLAIN " + encodedAuth);
                if (!readAndLogResponse(reader, "AUTH PLAIN").startsWith("235")) return false;

                // Verify email
                sendCommand(writer, "MAIL FROM:<" + username + ">");
                if (!readAndLogResponse(reader, "MAIL FROM").startsWith("250")) return false;

                sendCommand(writer, "RCPT TO:<" + email + ">");
                String rcptResponse = readAndLogResponse(reader, "RCPT TO");
                return rcptResponse.startsWith("250");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

//    private static void sendCommand(BufferedWriter writer, String command) throws IOException {
//        writer.write(command + "\r\n");
//        writer.flush();
//    }
//
    private static String readAndLogResponse(BufferedReader reader, String step) throws IOException {
        String response = reader.readLine();
        System.out.println(step + " Response: " + response);
        return response;
    }
//
//    private static String getMXServer(String domain) {
//        // Implement MX lookup logic (or use a library)
//        return "mail." + domain;
//    }



    // SMTp connection without credential  port 25 - Green Code
//private static boolean checkSMTP(String email) {
//    String domain = email.substring(email.indexOf("@") + 1);
//    String smtpServer = getMXServer(domain);
//    if (smtpServer == null) {
//        return false; // No MX server found
//    }
//    try (Socket socket = new Socket()) {
//        socket.connect(new InetSocketAddress(smtpServer, 25), 50000); // Timeout of 5 seconds
//        socket.setSoTimeout(5000); // Set timeout for reading data from the socket
//        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//
//        // Read the server's greeting
//        String response = reader.readLine();
//        if (!response.startsWith("220")) {
//            return false; // SMTP Server not available
//        }
//        // Send HELO command
//        writer.write("HELO " + domain + "\r\n");
//        writer.flush();
//        response = reader.readLine();
//        if (!response.startsWith("250")) {
//            return false; // HELO command failed
//        }
//
//        // Send MAIL FROM command (this is just for identification, not an actual email)
//        writer.write("MAIL FROM:<test@" + domain + ">\r\n");
//        writer.flush();
//        response = reader.readLine();
//        if (!response.startsWith("250")) {
//            return false; // MAIL FROM command failed
//        }
//
//        // Send RCPT TO command to check the recipient email address
//        writer.write("RCPT TO:<" + email + ">\r\n");
//        writer.flush();
//        response = reader.readLine();
//        return response.startsWith("250"); // If server responds with 250, the email address exists
//
//    } catch (IOException e) {
//        e.printStackTrace();
//        return false; // SMTP check failed
//    }
//}


    static String getMXServer(String domain) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            DirContext ictx = new InitialDirContext(env);
            javax.naming.directory.Attributes attributes = ictx.getAttributes(domain, new String[]{"MX"});
            javax.naming.directory.Attribute mxRecords = attributes.get("MX");

            if (mxRecords != null) {
                String mxServer = mxRecords.get(0).toString();
                return mxServer.split(" ")[1]; // Extract the server hostname
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean hasMXRecord(String email) {
        String domain = email.substring(email.indexOf("@") + 1);
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            DirContext ictx = new InitialDirContext(env);
            javax.naming.directory.Attributes attributes = ictx.getAttributes(domain, new String[]{"MX"});
            return attributes.get("MX") != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void sendCommand(BufferedWriter writer, String command) throws IOException {
        writer.write(command + "\r\n");
        writer.flush();
    }

    private static String readResponse(BufferedReader reader) throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line).append("\n");
            if (line.startsWith("250 ") || line.startsWith("220 ") || line.startsWith("334 ") || line.startsWith("235 ")) {
                break;
            }
        }
        return response.toString().trim();
    }


    //  private static boolean checkSMTP(String email) {
//        String domain = email.substring(email.indexOf("@") + 1);
//        String smtpServer = getMXServer(domain);
//        if (smtpServer == null) {
//            return false; // No MX server found
//        }
//
//        String username = "support@mail.propeltechsystems.com";
//        String password = "Linuxlab87";
//
//        try (Socket socket = new Socket(smtpServer, 587);
//             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
//
//            // Read server greeting
//            if (!readResponse(reader).startsWith("220")) {
//                return false;
//            }
//
//            // Send EHLO
//            sendCommand(writer, "EHLO localhost");
//            if (!readResponse(reader).contains("250")) {
//                return false;
//            }
//
//            // Initiate STARTTLS
//            sendCommand(writer, "STARTTLS");
//            if (!readResponse(reader).startsWith("220")) {
//                return false;
//            }
//
//            // Upgrade to SSL
//            SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(socket, smtpServer, 587, true);
//            try (BufferedReader bufferedReader = reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()))) {
//            }
//            writer = new BufferedWriter(new OutputStreamWriter(sslSocket.getOutputStream()));
//
//            // Re-send EHLO
//            sendCommand(writer, "EHLO localhost");
//            if (!readResponse(reader).contains("250")) {
//                return false;
//            }
    /*
    //upgrade to SSL
    SSLSocket sslSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(socket,smtpServer, 587,true) ;
    try(BufferedReader bufferedReader

     */
//
//            // Authenticate
//            sendCommand(writer, "AUTH LOGIN");
//            if (!readResponse(reader).startsWith("334")) {
//                return false;
//            }
//            sendCommand(writer, Base64.getEncoder().encodeToString(username.getBytes()));
//            if (!readResponse(reader).startsWith("334")) {
//                return false;
//            }
//            sendCommand(writer, Base64.getEncoder().encodeToString(password.getBytes()));
//            if (!readResponse(reader).startsWith("235")) {
//                return false;
//            }
//
//            // Verify email
//            sendCommand(writer, "MAIL FROM:<" + username + ">");
//            if (!readResponse(reader).startsWith("250")) {
//                return false;
//            }
//            sendCommand(writer, "RCPT TO:<" + email + ">");
//            String response = readResponse(reader);
//            return response.startsWith("250");
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }


}

