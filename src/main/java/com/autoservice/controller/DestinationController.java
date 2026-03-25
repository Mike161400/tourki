package com.autoservice.controller;

import com.autoservice.domain.Destination;
import com.autoservice.repository.DestinationRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    private final DestinationRepository destinationRepository;

    public DestinationController(DestinationRepository destinationRepository) {
        this.destinationRepository = destinationRepository;
    }

    @GetMapping
    public List<Destination> getAll() {
        return destinationRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Destination> getById(@PathVariable Long id) {
        return destinationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody Destination destination) {
        if (destinationRepository.existsByNameAndCityAndCountry(
                destination.getName(),
                destination.getCity(),
                destination.getCountry())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Destination with same name/city/country already exists");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(destinationRepository.save(destination));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody Destination updated) {
        return destinationRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setCountry(updated.getCountry());
            existing.setCity(updated.getCity());
            existing.setDescription(updated.getDescription());
            return ResponseEntity.ok(destinationRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!destinationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        destinationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
