package com.back.file_upload.repositories;

import com.back.file_upload.models.FileDataDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FileDataRepository extends JpaRepository<FileDataDTO, String> {

    List<FileDataDTO> findAllByUsername(String username);

    @Query("SELECT file FROM FileDataDTO file WHERE file.username IN (:usernames)")
    List<FileDataDTO> getAllByUsernames(List<String> usernames);
}
