//package com.tsl.model;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import jakarta.persistence.CascadeType;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.OneToMany;
//import jakarta.persistence.Table;
//import lombok.Data;
//
//
//@Entity
//@Data
//@Table(name="FileAbout")
//public class UploadedFile {
//
//	@Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String fileName;
//
//    private String rowData; // Stores raw data from each row as a string
//
//    private boolean emailVerified; // Tracks if emails have been verified
//
//}
package com.tsl.model;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "core_file_db")
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="Id")
    private Long id;
    @Column(name="FileName")
    private String fileName;  

    @Column(name="Verified")
    private boolean verified;
    @Column(name="UploadTime")
    private LocalDateTime uploadTime;
}

	
	


	

	
	


	

