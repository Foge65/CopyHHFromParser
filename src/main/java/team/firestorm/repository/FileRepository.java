package team.firestorm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.firestorm.entity.FileEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {
    Optional<FileEntity> findByFilePath(String string);
}
