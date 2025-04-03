package com.kanevsky.stats.config;

import com.kanevsky.stats.grpc.StatsGrpcService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import java.io.IOException;

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
                .build()
                .start();
        
        // Start server in a separate thread
        Thread awaitThread = new Thread(() -> {
            try {
                server.awaitTermination();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        awaitThread.setDaemon(false);
        awaitThread.start();
        
        return server;
    }

    @PreDestroy
    public void stopServer() {
        if (server != null) {
            server.shutdown();
        }
    }
}