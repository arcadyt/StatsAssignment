package com.kanevsky.stats.grpc;

import com.kanevsky.stats.dto.StatsEntryDto;
import com.kanevsky.stats.mappers.IGrpcMapper;
import com.kanevsky.stats.service.IngestService;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class StatsGrpcService extends StatsServiceGrpc.StatsServiceImplBase {

    private IngestService ingestService;

    private IGrpcMapper grpcMapper;

    @Override
    public void ingestStats(StatsBatchRequest request, StreamObserver<IngestResponse> responseObserver) {
        try {
            List<StatsEntryDto> entries = grpcMapper.toStatsEntryDtoList(request.getEntriesList());
            int successCount = ingestService.processBatchEntries(entries);

            IngestResponse response = IngestResponse.newBuilder()
                    .setSuccessCount(successCount)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            ProcessingError error = ProcessingError.newBuilder()
                    .setFieldPath("")
                    .setMessage("Error processing stats: " + e.getMessage())
                    .build();

            IngestResponse response = IngestResponse.newBuilder()
                    .setSuccessCount(0)
                    .addErrors(error)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}