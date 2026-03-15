package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.model.compilation.Compilation;

import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @Override
    @EntityGraph(attributePaths = "events")
    Optional<Compilation> findById(Long id);
}
