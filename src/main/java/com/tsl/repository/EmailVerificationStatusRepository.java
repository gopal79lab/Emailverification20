package com.tsl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tsl.model.EmailVerificationStatus;
import com.tsl.model.UploadedFile;

public interface EmailVerificationStatusRepository extends JpaRepository<EmailVerificationStatus,Long> {

	List<EmailVerificationStatus> findByUploadedFile(UploadedFile uploadedFile);

	@Modifying
    @Query("DELETE FROM EmailVerificationStatus e WHERE e.uploadedFile.id = :fileId")
    void deleteByUploadedFileId(@Param("fileId") Long fileId);
}
