package org.example.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_photo")
@EqualsAndHashCode(exclude = "id")
public class AppPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_file_id")
    private String telegramFileId;

    @Column(name = "file_size")
    private Integer fileSize;

    @OneToOne
    private BinaryContent binaryContent;
}
