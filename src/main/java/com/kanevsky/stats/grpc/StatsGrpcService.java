package com.kanevsky.stats.grpc;

import com.kanevsky.stats.dto.StatsEntryDto;
import com.kanevsky.stats.service.IngestService;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatsGrpcService extends StatsServiceGrpc.StatsServiceImplBase {
    
    @Autowired
    private IngestService ingestService;
    
    @Override
    public void ingestStats(StatsBatchRequest request, StreamObserver<IngestResponse> responseObserver) {
        try {
            List<StatsEntryDto> entries = convertToEntryDtos(request);
            int successCount = ingestService.processBatchEntries(entries);
            
            IngestResponse response = IngestResponse.newBuilder()
                    .setSuccessCount(successCount)
                    .build();
                    
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
    
    private List<StatsEntryDto> convertToEntryDtos(StatsBatchRequest request) {
        List<StatsEntryDto> entries = new ArrayList<>();
        
        for (StatsEntry entry : request.getEntriesList()) {
            StatsEntryDto.GameStatsDto gameStatsDto = new StatsEntryDto.GameStatsDto();
            gameStatsDto.setPoints(entry.getStats().getPoints());
            gameStatsDto.setRebounds(entry.getStats().getRebounds());
            gameStatsDto.setAssists(entry.getStats().getAssists());
            gameStatsDto.setSteals(entry.getStats().getSteals());
            gameStatsDto.setBlocks(entry.getStats().getBlocks());
            gameStatsDto.setFouls(entry.getStats().getFouls());
            gameStatsDto.setTurnovers(entry.getStats().getTurnovers());
            gameStatsDto.setMinutesPlayed(entry.getStats().getMinutesPlayed());
            
            StatsEntryDto statsEntryDto = new StatsEntryDto();
            statsEntryDto.setPlayerName(entry.getPlayerName());
            statsEntryDto.setTeamName(entry.getTeamName());
            statsEntryDto.setStats(gameStatsDto);
            
            entries.add(statsEntryDto);
        }
        
        return entries;
    }
}