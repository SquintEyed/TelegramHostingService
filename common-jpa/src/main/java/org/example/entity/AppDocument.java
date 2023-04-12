package org.example.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_document")
@EqualsAndHashCode(exclude = "id")
public class AppDocument {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_file_id")
    private String telegramFileId;

    @Column(name = "doc_name")
    private String docName;

    @OneToOne
    private BinaryContent binaryContent;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "file_size")
    private Long fileSize;
}
