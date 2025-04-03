package com.kanevsky.stats.dto;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

class StatsEntryDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidStatsEntry() {
        StatsEntryDto entry = createValidStatsEntry();

        Set<ConstraintViolation<StatsEntryDto>> violations = validator.validate(entry);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testEmptyPlayerName() {
        StatsEntryDto entry = createValidStatsEntry();
        entry.setPlayerName("");

        Set<ConstraintViolation<StatsEntryDto>> violations = validator.validate(entry);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());

        ConstraintViolation<StatsEntryDto> violation = violations.iterator().next();
        assertEquals("playerName", violation.getPropertyPath().toString());
        assertEquals("Player name cannot be empty", violation.getMessage());
    }

    @Test
    void testEmptyTeamName() {
        StatsEntryDto entry = createValidStatsEntry();
        entry.setTeamName("");

        Set<ConstraintViolation<StatsEntryDto>> violations = validator.validate(entry);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());

        ConstraintViolation<StatsEntryDto> violation = violations.iterator().next();
        assertEquals("teamName", violation.getPropertyPath().toString());
        assertEquals("Team name cannot be empty", violation.getMessage());
    }

    @Test
    void testNegativePoints() {
        StatsEntryDto entry = createValidStatsEntry();
        entry.getStats().setPoints(-1);

        Set<ConstraintViolation<StatsEntryDto>> violations = validator.validate(entry);

        assertFalse(violations.isEmpty());
        boolean foundPointsViolation = false;

        for (ConstraintViolation<StatsEntryDto> violation : violations) {
            if (violation.getPropertyPath().toString().equals("stats.points")) {
                foundPointsViolation = true;
                assertEquals("Points cannot be negative", violation.getMessage());
                break;
            }
        }

        assertTrue(foundPointsViolation, "Should have found a points validation violation");
    }

    @Test
    void testNegativeRebounds() {
        StatsEntryDto entry = createValidStatsEntry();
        entry.getStats().setRebounds(-1);

        Set<ConstraintViolation<StatsEntryDto>> violations = validator.validate(entry);

        assertFalse(violations.isEmpty());
        boolean foundViolation = false;

        for (ConstraintViolation<StatsEntryDto> violation : violations) {
            if (violation.getPropertyPath().toString().equals("stats.rebounds")) {
                foundViolation = true;
                assertEquals("Rebounds cannot be negative", violation.getMessage());
                break;
            }
        }

        assertTrue(foundViolation);
    }

    @Test
    void testFoulsExceedMax() {
        StatsEntryDto entry = createValidStatsEntry();
        entry.getStats().setFouls(7);

        Set<ConstraintViolation<StatsEntryDto>> violations = validator.validate(entry);

        assertFalse(violations.isEmpty());
        boolean foundFoulsViolation = false;

        for (ConstraintViolation<StatsEntryDto> violation : violations) {
            if (violation.getPropertyPath().toString().equals("stats.fouls")) {
                foundFoulsViolation = true;
                assertEquals("Fouls cannot exceed 6", violation.getMessage());
                break;
            }
        }

        assertTrue(foundFoulsViolation);
    }

    @Test
    void testMinutesPlayedExceedMax() {
        StatsEntryDto entry = createValidStatsEntry();
        entry.getStats().setMinutesPlayed(50.0);

        Set<ConstraintViolation<StatsEntryDto>> violations = validator.validate(entry);

        assertFalse(violations.isEmpty());
        boolean foundMinutesViolation = false;

        for (ConstraintViolation<StatsEntryDto> violation : violations) {
            if (violation.getPropertyPath().toString().equals("stats.minutesPlayed")) {
                foundMinutesViolation = true;
                assertEquals("Minutes played cannot exceed 48", violation.getMessage());
                break;
            }
        }

        assertTrue(foundMinutesViolation);
    }

    @Test
    void testMultipleViolations() {
        StatsEntryDto entry = createValidStatsEntry();
        entry.setPlayerName("");
        entry.getStats().setPoints(-10);
        entry.getStats().setFouls(7);

        Set<ConstraintViolation<StatsEntryDto>> violations = validator.validate(entry);

        assertEquals(3, violations.size());
    }

    @Test
    void testNullStats() {
        StatsEntryDto entry = createValidStatsEntry();
        entry.setStats(null);

        Set<ConstraintViolation<StatsEntryDto>> violations = validator.validate(entry);

        assertFalse(violations.isEmpty());
        boolean foundViolation = false;

        for (ConstraintViolation<StatsEntryDto> violation : violations) {
            if (violation.getPropertyPath().toString().equals("stats")) {
                foundViolation = true;
                assertEquals("Stats cannot be null", violation.getMessage());
                break;
            }
        }

        assertTrue(foundViolation);
    }

    private StatsEntryDto createValidStatsEntry() {
        StatsEntryDto entry = new StatsEntryDto();
        entry.setPlayerName("John Doe");
        entry.setTeamName("Lakers");

        StatsEntryDto.GameStatsDto stats = new StatsEntryDto.GameStatsDto();
        stats.setPoints(20);
        stats.setRebounds(5);
        stats.setAssists(3);
        stats.setSteals(1);
        stats.setBlocks(0);
        stats.setFouls(2);
        stats.setTurnovers(1);
        stats.setMinutesPlayed(25.5);

        entry.setStats(stats);

        return entry;
    }
}