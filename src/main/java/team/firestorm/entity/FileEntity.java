package team.firestorm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "file")
@Data
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "is_uploaded")
    private boolean uploaded;
}
