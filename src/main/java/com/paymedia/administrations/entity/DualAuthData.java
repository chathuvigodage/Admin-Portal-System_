package com.paymedia.administrations.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DualAuthData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "entity")
    private String entity;

    @Column(name = "old_data", columnDefinition = "TEXT")
    private String oldData;

    @Lob
    @Column(name = "new_data", columnDefinition = "LONGTEXT")
    private String newData;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "reviewed_by")
    private Integer reviewedBy;

    @Column(name = "action")
    private String action;

    @Column(name = "status")
    private String status;

}

