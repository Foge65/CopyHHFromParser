package team.firestorm.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import team.firestorm.entity.FileEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends CrudRepository<FileEntity, UUID> {
    Optional<FileEntity> findByFilePath(String string);

    Optional<FileEntity> findFirstByUploadedFalse();

    @Modifying
    @Query("UPDATE FileEntity f SET f.uploaded = :status WHERE f.filePath = :filePath")
    void updateUploadedByFilePath(String filePath, boolean status);
}
