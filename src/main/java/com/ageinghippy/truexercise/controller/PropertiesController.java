package com.ageinghippy.truexercise.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name="Properties Controller", description="TruExercise Information API to assist debugging")
@RestController
@Slf4j
@RequiredArgsConstructor
public class PropertiesController {

    @Value("${spring.profiles.active:}")
    private String springProfilesActive;

    @Value("${db.hostname:}")
    private String dbHostName;

    @GetMapping("/info")
    public Map<String, String> getInfo() {
        return Map.of("spring.profiles.active",springProfilesActive,
                "db.hostname",dbHostName);
    }
}
