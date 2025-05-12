package team.firestorm.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import team.firestorm.entity.FileEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends CrudRepository<FileEntity, UUID> {
    Optional<FileEntity> findFirstByUploadedFalse();

    @Modifying
    @Query("UPDATE FileEntity f SET f.uploaded = :status WHERE f.filePath = :filePath")
    void updateUploadedByFilePath(String filePath, boolean status);

    @Query("SELECT f.filePath FROM FileEntity f WHERE f.filePath ILIKE :path%")
    List<String> findAllByFilePathStartsWith(String path);

    @Query("SELECT f.filePath FROM FileEntity f WHERE LOWER(f.filePath) LIKE LOWER(CONCAT('%', :date, '%'))")
    List<String> findFilePathByDateStartWith(String date);
}
