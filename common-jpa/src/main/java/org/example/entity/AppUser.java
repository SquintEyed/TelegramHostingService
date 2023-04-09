package org.example.entity;

import lombok.*;
import org.example.entity.enums.UserState;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_user")
@EqualsAndHashCode(exclude = "id")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_user_id")
    private Long telegramUserId;

    @CreationTimestamp
    @Column(name = "first_login_date")
    private LocalDateTime firstLoginDate;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "email")
    private String email;

    @Column(name = "is_active")
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_state")
    private UserState state;

}
