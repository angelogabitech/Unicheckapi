package com.unicheck.Unicheckapi.model;



import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "professores")
@EqualsAndHashCode(callSuper = true)
public class Professor extends Usuario {
    private String departamento;
}