package com.kanevsky.stats.grpc;

import com.kanevsky.stats.dto.StatsEntryDto;
import com.kanevsky.stats.mappers.IGrpcMapper;
import com.kanevsky.stats.service.IngestService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class StatsGrpcServiceIntegrationTest {

    @MockBean
    private IngestService ingestService;

    @SpyBean
    private IGrpcMapper grpcMapper;

    @Mock
    private StreamObserver<IngestResponse> responseObserver;

    @Captor
    private ArgumentCaptor<IngestResponse> responseCaptor;

    private StatsGrpcService statsGrpcService;

    @BeforeEach
    public void setup() {
        statsGrpcService = new StatsGrpcService(ingestService, grpcMapper);
    }

    @Test
    public void testIngestStats_Success() {
        // Arrange
        StatsBatchRequest request = createStatsBatchRequest();
        List<StatsEntryDto> mappedEntries = new ArrayList<>();

        when(grpcMapper.toStatsEntryDtoList(request.getEntriesList())).thenReturn(mappedEntries);
        when(ingestService.processBatchEntries(mappedEntries)).thenReturn(3);

        // Act
        statsGrpcService.ingestStats(request, responseObserver);

        // Assert
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any());

        IngestResponse response = responseCaptor.getValue();
        assertEquals(3, response.getSuccessCount());
        assertEquals(0, response.getErrorsCount());

        verify(ingestService).processBatchEntries(any());
    }

    @Test
    public void testIngestStats_ServiceError() {
        // Arrange
        StatsBatchRequest request = createStatsBatchRequest();
        List<StatsEntryDto> mappedEntries = new ArrayList<>();

        when(grpcMapper.toStatsEntryDtoList(request.getEntriesList())).thenReturn(mappedEntries);
        when(ingestService.processBatchEntries(any())).thenThrow(new RuntimeException("Test exception"));

        // Act
        statsGrpcService.ingestStats(request, responseObserver);

        // Assert
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();
        verify(responseObserver, never()).onError(any()); // The service handles exceptions itself

        IngestResponse response = responseCaptor.getValue();
        assertEquals(0, response.getSuccessCount());
        assertEquals(1, response.getErrorsCount());

        ProcessingError error = response.getErrors(0);
        assertEquals("", error.getFieldPath());
        assertTrue(error.getMessage().contains("Test exception"));

        verify(ingestService).processBatchEntries(any());
    }

    @Test
    public void testIngestStats_PartialSuccess() {
        // Arrange
        StatsBatchRequest request = createStatsBatchRequest();
        List<StatsEntryDto> mappedEntries = new ArrayList<>();

        when(grpcMapper.toStatsEntryDtoList(request.getEntriesList())).thenReturn(mappedEntries);
        when(ingestService.processBatchEntries(mappedEntries)).thenReturn(1);  // Only 1 successful out of 3

        // Act
        statsGrpcService.ingestStats(request, responseObserver);

        // Assert
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        IngestResponse response = responseCaptor.getValue();
        assertEquals(1, response.getSuccessCount());
        assertEquals(0, response.getErrorsCount());

        verify(ingestService).processBatchEntries(any());
    }

    @Test
    public void testIngestStats_MappingError() {
        // Arrange
        StatsBatchRequest request = createStatsBatchRequest();

        when(grpcMapper.toStatsEntryDtoList(request.getEntriesList())).thenThrow(new RuntimeException("Mapping error"));

        // Act
        statsGrpcService.ingestStats(request, responseObserver);

        // Assert
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        IngestResponse response = responseCaptor.getValue();
        assertEquals(0, response.getSuccessCount());
        assertEquals(1, response.getErrorsCount());

        ProcessingError error = response.getErrors(0);
        assertTrue(error.getMessage().contains("Mapping error"));

        verify(ingestService, never()).processBatchEntries(any());
    }

    private StatsBatchRequest createStatsBatchRequest() {
        GameStats gameStats1 = GameStats.newBuilder()
                .setPoints(25)
                .setRebounds(10)
                .setAssists(5)
                .setSteals(2)
                .setBlocks(1)
                .setFouls(3)
                .setTurnovers(2)
                .setMinutesPlayed(35.5)
                .build();

        GameStats gameStats2 = GameStats.newBuilder()
                .setPoints(18)
                .setRebounds(12)
                .setAssists(3)
                .setSteals(1)
                .setBlocks(2)
                .setFouls(2)
                .setTurnovers(1)
                .setMinutesPlayed(32.0)
                .build();

        GameStats gameStats3 = GameStats.newBuilder()
                .setPoints(30)
                .setRebounds(8)
                .setAssists(7)
                .setSteals(3)
                .setBlocks(0)
                .setFouls(4)
                .setTurnovers(3)
                .setMinutesPlayed(38.5)
                .build();

        StatsEntry entry1 = StatsEntry.newBuilder()
                .setPlayerName("Player1")
                .setTeamName("Team1")
                .setStats(gameStats1)
                .build();

        StatsEntry entry2 = StatsEntry.newBuilder()
                .setPlayerName("Player2")
                .setTeamName("Team1")
                .setStats(gameStats2)
                .build();

        StatsEntry entry3 = StatsEntry.newBuilder()
                .setPlayerName("Player3")
                .setTeamName("Team2")
                .setStats(gameStats3)
                .build();

        return StatsBatchRequest.newBuilder()
                .addEntries(entry1)
                .addEntries(entry2)
                .addEntries(entry3)
                .build();
    }
}