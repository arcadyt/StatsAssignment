package com.kanevsky.stats.grpc.interceptors;

import build.buf.protovalidate.ValidationResult;
import com.kanevsky.stats.dto.StatsBatchRequestDto;
import com.kanevsky.stats.dto.StatsEntryDto;
import com.kanevsky.stats.grpc.GameStats;
import com.kanevsky.stats.grpc.StatsBatchRequest;
import com.kanevsky.stats.grpc.StatsEntry;
import com.kanevsky.stats.grpc.validations.ProtoValidationService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ValidationIntegrationTest {

    @Autowired
    private Validator validator;

    @Autowired
    private ProtoValidationService protoValidationService;

    @Test
    public void testValidStatsEntryDto() {
        // Arrange
        StatsEntryDto entry = createValidStatsEntry();

        // Act
        Set<ConstraintViolation<StatsEntryDto>> violations = validator.validate(entry);

        // Assert
        assertTrue(violations.isEmpty(), "Expected no validation violations");
    }

    @Test
    public void testStatsEntryDtoWithInvalidFouls() {
        // Arrange
        StatsEntryDto entry = createValidStatsEntry();
        entry.getStats().setFouls(7); // Invalid: max is 6

        // Act
        Set<ConstraintViolation<StatsEntryDto>> violations = validator.validate(entry);

        // Assert
        assertFalse(violations.isEmpty(), "Expected validation violations");
        assertEquals(1, violations.size(), "Expected exactly one validation violation");

        ConstraintViolation<StatsEntryDto> violation = violations.iterator().next();
        assertEquals("stats.fouls", violation.getPropertyPath().toString());
        assertEquals("Fouls cannot exceed 6", violation.getMessage());
    }

    @Test
    public void testStatsEntryDtoWithInvalidMinutesPlayed() {
        // Arrange
        StatsEntryDto entry = createValidStatsEntry();
        entry.getStats().setMinutesPlayed(50.0); // Invalid: max is 48

        // Act
        Set<ConstraintViolation<StatsEntryDto>> violations = validator.validate(entry);

        // Assert
        assertFalse(violations.isEmpty(), "Expected validation violations");
        assertEquals(1, violations.size(), "Expected exactly one validation violation");

        ConstraintViolation<StatsEntryDto> violation = violations.iterator().next();
        assertEquals("stats.minutesPlayed", violation.getPropertyPath().toString());
        assertEquals("Minutes played cannot exceed 48", violation.getMessage());
    }

    @Test
    public void testStatsEntryDtoWithEmptyPlayerName() {
        // Arrange
        StatsEntryDto entry = createValidStatsEntry();
        entry.setPlayerName(""); // Invalid: cannot be empty

        // Act
        Set<ConstraintViolation<StatsEntryDto>> violations = validator.validate(entry);

        // Assert
        assertFalse(violations.isEmpty(), "Expected validation violations");
        assertEquals(1, violations.size(), "Expected exactly one validation violation");

        ConstraintViolation<StatsEntryDto> violation = violations.iterator().next();
        assertEquals("playerName", violation.getPropertyPath().toString());
        assertEquals("Player name cannot be empty", violation.getMessage());
    }

    @Test
    public void testStatsEntryDtoWithMultipleViolations() {
        // Arrange
        StatsEntryDto entry = createValidStatsEntry();
        entry.setTeamName(""); // Invalid: cannot be empty
        entry.getStats().setPoints(-5); // Invalid: cannot be negative
        entry.getStats().setFouls(7); // Invalid: max is 6

        // Act
        Set<ConstraintViolation<StatsEntryDto>> violations = validator.validate(entry);

        // Assert
        assertFalse(violations.isEmpty(), "Expected validation violations");
        assertEquals(3, violations.size(), "Expected exactly three validation violations");

        // Create a list of violation messages for easier assertion
        List<String> violationMessages = new ArrayList<>();
        for (ConstraintViolation<StatsEntryDto> violation : violations) {
            violationMessages.add(violation.getMessage());
        }

        assertTrue(violationMessages.contains("Team name cannot be empty"));
        assertTrue(violationMessages.contains("Points cannot be negative"));
        assertTrue(violationMessages.contains("Fouls cannot exceed 6"));
    }

    @Test
    public void testStatsBatchRequestValidation() {
        // Arrange
        StatsBatchRequestDto batchRequest = new StatsBatchRequestDto();
        batchRequest.setEntries(new ArrayList<>()); // Invalid: must have at least one entry

        // Act
        Set<ConstraintViolation<StatsBatchRequestDto>> violations = validator.validate(batchRequest);

        // Assert
        assertFalse(violations.isEmpty(), "Expected validation violations");
        assertEquals(1, violations.size(), "Expected exactly one validation violation");

        ConstraintViolation<StatsBatchRequestDto> violation = violations.iterator().next();
        assertEquals("entries", violation.getPropertyPath().toString());
        assertEquals("At least one stats entry is required", violation.getMessage());
    }

    @Test
    public void testProtoValidation_ValidRequest() throws Exception {
        // Arrange
        StatsBatchRequest request = createValidStatsBatchRequest();

        // Act
        ValidationResult result = protoValidationService.validate(request);

        // Assert
        assertTrue(result.isSuccess(), "Expected valid proto message");
        assertEquals(0, result.getViolations().size());
    }

    @Test
    public void testProtoValidation_EmptyEntries() throws Exception {
        // Arrange
        StatsBatchRequest request = StatsBatchRequest.newBuilder().build();

        // Act
        ValidationResult result = protoValidationService.validate(request);

        // Assert
        assertFalse(result.isSuccess(), "Expected invalid proto message");
        assertEquals(1, result.getViolations().size());
        assertTrue(result.getViolations().get(0).toProto().getMessage().contains("at least 1 item"));
    }

    @Test
    public void testProtoValidation_InvalidStats() throws Exception {
        // Arrange
        GameStats invalidStats = GameStats.newBuilder()
                .setPoints(-10) // Negative points (invalid)
                .setRebounds(10)
                .setAssists(5)
                .setSteals(2)
                .setBlocks(1)
                .setFouls(3)
                .setTurnovers(2)
                .setMinutesPlayed(30.0)
                .build();

        StatsEntry entry = StatsEntry.newBuilder()
                .setPlayerName("Player1")
                .setTeamName("Team1")
                .setStats(invalidStats)
                .build();

        StatsBatchRequest request = StatsBatchRequest.newBuilder()
                .addEntries(entry)
                .build();

        // Act
        ValidationResult result = protoValidationService.validate(request);

        // Assert
        assertFalse(result.isSuccess(), "Expected invalid proto message");
        assertEquals(1, result.getViolations().size());
        assertTrue(result.getViolations().get(0).toProto().getMessage().contains("greater than or equal to"));
    }

    @Test
    public void testProtoValidation_MultipleViolations() throws Exception {
        // Arrange
        GameStats invalidStats = GameStats.newBuilder()
                .setPoints(-10) // Negative points (invalid)
                .setRebounds(10)
                .setAssists(5)
                .setSteals(2)
                .setBlocks(1)
                .setFouls(7) // Exceeds max fouls (invalid)
                .setTurnovers(2)
                .setMinutesPlayed(50.0) // Exceeds max minutes (invalid)
                .build();

        StatsEntry entry = StatsEntry.newBuilder()
                .setPlayerName("") // Empty name (invalid)
                .setTeamName("Team1")
                .setStats(invalidStats)
                .build();

        StatsBatchRequest request = StatsBatchRequest.newBuilder()
                .addEntries(entry)
                .build();

        // Act
        ValidationResult result = protoValidationService.validate(request);

        // Assert
        assertFalse(result.isSuccess(), "Expected invalid proto message");
        assertTrue(result.getViolations().size() > 1, "Expected multiple violations");
    }

    // Helper methods

    private StatsEntryDto createValidStatsEntry() {
        StatsEntryDto entry = new StatsEntryDto();
        entry.setPlayerName("John Doe");
        entry.setTeamName("Lakers");

        StatsEntryDto.GameStatsDto stats = new StatsEntryDto.GameStatsDto();
        stats.setPoints(25);
        stats.setRebounds(10);
        stats.setAssists(8);
        stats.setSteals(2);
        stats.setBlocks(1);
        stats.setFouls(3);
        stats.setTurnovers(2);
        stats.setMinutesPlayed(35.5);

        entry.setStats(stats);
        return entry;
    }

    private StatsBatchRequest createValidStatsBatchRequest() {
        GameStats gameStats = GameStats.newBuilder()
                .setPoints(25)
                .setRebounds(10)
                .setAssists(8)
                .setSteals(2)
                .setBlocks(1)
                .setFouls(3)
                .setTurnovers(2)
                .setMinutesPlayed(35.5)
                .build();

        StatsEntry entry = StatsEntry.newBuilder()
                .setPlayerName("John Doe")
                .setTeamName("Lakers")
                .setStats(gameStats)
                .build();

        return StatsBatchRequest.newBuilder()
                .addEntries(entry)
                .build();
    }
}