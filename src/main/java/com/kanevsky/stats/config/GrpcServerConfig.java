package com.kanevsky.stats.config;

import com.kanevsky.stats.grpc.StatsGrpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class GrpcServerConfig {

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    @Autowired
    private StatsGrpcService statsGrpcService;

    private Server server;

    @Bean
    public Server grpcServer() throws IOException {
        server = ServerBuilder.forPort(grpcPort)
                .addService(statsGrpcService)
                .build();

        try {
            server.start();
            log.info("gRPC Server started on port {}", grpcPort);

            // Start server in a separate thread
            Thread awaitThread = new Thread(() -> {
                try {
                    server.awaitTermination();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("gRPC server interrupted: {}", e.getMessage());
                }
            });
            awaitThread.setDaemon(false);
            awaitThread.start();

            return server;
        } catch (IOException e) {
            log.error("Failed to start gRPC server: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PreDestroy
    public void stopServer() {
        if (server != null) {
            try {
                server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
                log.info("gRPC server shut down successfully");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Error during gRPC server shutdown: {}", e.getMessage());
            }
        }
    }
}