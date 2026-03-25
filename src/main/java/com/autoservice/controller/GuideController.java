package com.autoservice.controller;

import com.autoservice.domain.Guide;
import com.autoservice.repository.GuideRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guides")
public class GuideController {

    private final GuideRepository guideRepository;

    public GuideController(GuideRepository guideRepository) {
        this.guideRepository = guideRepository;
    }

    @GetMapping
    public List<Guide> getAll() {
        return guideRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Guide> getById(@PathVariable Long id) {
        return guideRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public List<Guide> getActive() {
        return guideRepository.findByActiveTrue();
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Guide guide) {
        if (guide.getEmail() != null && guideRepository.existsByEmail(guide.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Guide with this email already exists");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(guideRepository.save(guide));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Guide updated) {
        return guideRepository.findById(id).map(existing -> {
            if (updated.getEmail() != null
                    && !updated.getEmail().equals(existing.getEmail())
                    && guideRepository.existsByEmail(updated.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Guide with this email already exists");
            }

            existing.setFullName(updated.getFullName());
            existing.setEmail(updated.getEmail());
            existing.setLanguages(updated.getLanguages());
            existing.setExperienceYears(updated.getExperienceYears());
            existing.setActive(updated.isActive());
            return ResponseEntity.ok(guideRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!guideRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        guideRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
