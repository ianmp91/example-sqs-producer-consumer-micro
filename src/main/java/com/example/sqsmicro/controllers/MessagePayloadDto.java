package com.example.sqsmicro.controllers;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author ian.paris
 * @since 2025-12-17
 */
@Data
@Getter
@Setter
public class MessagePayloadDto {
    private String payload;
    private Map<String, String> metadata;
}
