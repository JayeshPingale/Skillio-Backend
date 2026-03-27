package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConvertLeadAndEnrollResponse {

    private Long enrollmentId;
    private Long studentId;
}
