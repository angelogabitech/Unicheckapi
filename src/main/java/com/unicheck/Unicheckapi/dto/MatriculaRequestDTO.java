
package com.unicheck.Unicheckapi.dto;

import lombok.Data;
import java.util.UUID;
//Requisição Matricula
@Data
public class MatriculaRequestDTO {

    private UUID alunoId;
    private UUID disciplinaId;

}