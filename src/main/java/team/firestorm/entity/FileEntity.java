package team.firestorm.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "file")
@Data
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "is_uploaded")
    private boolean uploaded;
}
