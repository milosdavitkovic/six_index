package com.six.indexreview.infrastructure.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "security")
public class SecurityEntity {
    @Id
    private Integer id;

    protected SecurityEntity() {
    }

    public SecurityEntity(Integer id) {
        this.id = id;
    }
}
