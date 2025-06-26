package com.polyHub.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "face_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaceId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "vector_path", length = 255)
    private String vectorPath;
}
