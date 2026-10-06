package com.wms.domain.base;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@MappedSuperclass
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name_creator", nullable = false)
    private String userNameCreator; 

    @Column(name = "user_name_last_change", nullable = false)
    private String userNameLastChange;

    @Column(name = "creation_date")
    private LocalDateTime creationDate; 

    @Column(name = "last_change_date")
    private LocalDateTime lastChangeDate; 
}