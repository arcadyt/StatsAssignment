# NBA Statistics Tracking System

A memory-efficient backend system for tracking NBA player statistics and calculating aggregate team and player stats in real-time with relatively high throughput.

## Architecture Overview

The system is built using a modular, layered architecture with:

- **Core Domain Layer**: Contains the statistics model (`Stats`) and calculation logic
- **Repository Layer**: Thread-safe repositories for storing and managing statistics
- **Service Layer**: Business logic for processing statistics
- **API Layer**: Both REST and gRPC endpoints for data ingestion and retrieval

### Key Components

- **Stats**: Core domain model for basketball statistics (points, rebounds, assists, etc.)
- **AverageCalculator**: Thread-safe component for calculating running averages of statistics
- **StatsRepo**: Repository using `ConcurrentHashMap` for thread-safe storage
- **IngestService**: Processes incoming statistics from both REST and gRPC interfaces
- **StatsService**: Retrieves aggregated player and team statistics
- **Validation**: Input validation for both REST (using Jakarta validation) and gRPC (using protovalidate)

## API Documentation

### REST API

#### Ingest Statistics
```
POST /api/ingest/
Content-Type: application/json

{
  "entries": [
    {
      "playerName": "LeBron James",
      "teamName": "Lakers",
      "points": 28,
      "rebounds": 7,
      "assists": 8,
      "steals": 1,
      "blocks": 1,
      "fouls": 2,
      "turnovers": 3,
      "minutesPlayed": 36.5
    }
  ]
}
```

Response: 200 OK (No content returned on success)

Error Response (400 Bad Request):
```json
{
  "status": 400,
  "message": "Failed to ingest stats: [error details]"
}
```

#### Get Player Statistics
```
GET /api/stats/player/{playerName}
```

Response:
```json
{
  "playerName": "LeBron James",
  "points": 28.0,
  "rebounds": 7.0,
  "assists": 8.0,
  "steals": 1.0,
  "blocks": 1.0,
  "fouls": 2.0,
  "turnovers": 3.0,
  "minutesPlayed": 36.5
}
```

Error Response (404 Not Found):
```json
{
  "status": 404,
  "message": "Player not found: LeBron James"
}
```

#### Get Team Statistics
```
GET /api/stats/team/{teamName}
```

Response:
```json
{
  "teamName": "Lakers",
  "points": 22.5,
  "rebounds": 6.2,
  "assists": 5.1,
  "steals": 0.8,
  "blocks": 0.6,
  "fouls": 2.4,
  "turnovers": 2.1,
  "minutesPlayed": 32.7
}
```

Error Response (404 Not Found):
```json
{
  "status": 404,
  "message": "Team not found: Lakers"
}
```

### gRPC Service

The gRPC service is defined in the `stats.proto` file:

```protobuf
service StatsService {
  rpc IngestStats (StatsBatchRequest) returns (IngestResponse) {}
}

message StatsBatchRequest {
  repeated StatsEntry entries = 1 [(buf.validate.field).repeated.min_items = 1];
}

message StatsEntry {
  string player_name = 1 [(buf.validate.field).string.min_len = 1];
  string team_name = 2 [(buf.validate.field).string.min_len = 1];
  GameStats stats = 3 [(buf.validate.field).required = true];
}

message GameStats {
  int32 points = 1 [(buf.validate.field).int32.gte = 0];
  int32 rebounds = 2 [(buf.validate.field).int32.gte = 0];
  int32 assists = 3 [(buf.validate.field).int32.gte = 0];
  int32 steals = 4 [(buf.validate.field).int32.gte = 0];
  int32 blocks = 5 [(buf.validate.field).int32.gte = 0];
  int32 fouls = 6 [(buf.validate.field).int32 = {gte: 0, lte: 6}];
  int32 turnovers = 7 [(buf.validate.field).int32.gte = 0];
  double minutes_played = 8 [(buf.validate.field).double = {gte: 0, lte: 48}];
}

message IngestResponse {
  int32 success_count = 1;
  repeated ProcessingError errors = 2;
}
```

The gRPC service runs on port 9090 by default.

## Design Considerations

### Performance Characteristics

- **Memory-efficient**: Memory consumption depends almost exclusively on the quantity of players/teams, not the number of game records processed. New game stats are incorporated into running averages, and original game data is garbage-collected.

- **Fine-grained concurrency**: Read-write locks are applied per player/team and per bucket upon insertion, not for the entire underlying map, allowing for good concurrent performance.

- **Relatively high-throughput**: The system provides good throughput because:
    - Calculations are performed on write, minimizing read overhead
    - Thread-safety is implemented with granular locking
    - Default Tomcat thread pool (200 max threads, 10 min spare threads, 8192 max connections) provides solid concurrency

  To further increase throughput, configure `server.tomcat.max-threads` and related properties in `application.properties`.

### Dual Ingestion Methods (REST vs gRPC)

The system supports two methods for data ingestion:

1. **REST API**: Traditional HTTP-based approach
2. **gRPC**: Binary protocol approach

For a production environment with high data volumes, gRPC would be the preferred choice due to:
- **Performance**: Significantly lower latency and higher throughput
- **Efficiency**: Protocol buffers provide more compact data representation
- **Type Safety**: Strongly-typed protocol definitions
- **Validation**: Schema validation built into the protocol via protovalidate

The validation in gRPC is handled through interceptors, keeping the service logic clean and focused on the business domain.

## Running the Project

### Local Deployment with Docker Compose

```bash
docker build -t nba-stats-service .
docker-compose up -d
```

This starts the service on:
- REST API: http://localhost:8080/
- gRPC API: localhost:9090

To test the API:

```bash
# Ingest stats using curl
curl -X POST http://localhost:8080/api/ingest/ \
  -H "Content-Type: application/json" \
  -d '{"entries":[{"playerName":"LeBron James","teamName":"Lakers","stats":{"points":28,"rebounds":7,"assists":8,"steals":1,"blocks":1,"fouls":2,"turnovers":3,"minutesPlayed":36.5}}]}'

# Get player stats
curl http://localhost:8080/api/stats/player/LeBron%20James

# Get team stats
curl http://localhost:8080/api/stats/team/Lakers
```

For gRPC testing, you can use tools like grpcurl or BloomRPC.

## AWS Deployment Architecture

For a production deployment on AWS, I recommend:

1. **Container Orchestration**: Amazon ECS or EKS
    - Handles scaling and container management
    - Enables service discovery and load balancing

2. **API Gateway/Load Balancing**:
    - For REST endpoints: Application Load Balancer (ALB)
    - For gRPC: Network Load Balancer (NLB) with TLS passthrough

3. **Configuration Management**:
    - AWS Systems Manager Parameter Store for application configuration
    - Environment variables for container-specific settings

4. **Observability**:
    - CloudWatch for metrics and logs
    - X-Ray for distributed tracing

5. **CI/CD Pipeline**:
    - CodeBuild/CodePipeline for automated testing and deployment
    - ECR for container registry

For optimal performance, consider:
- Instance sizing based on expected number of players/teams
- Configuration of appropriate connection pools and timeout settings
- CloudWatch metrics for thread pool utilization and response times

## Trade-offs and Limitations

1. **In-memory Storage**:
    - Pro: Extremely fast access and low latency
    - Con: No persistence between restarts and limited by available memory
    - Possible enhancement: Add a persistence layer or distributed cache

2. **Dual API Approach**:
    - Pro: Flexibility for different client types and use cases
    - Con: Increased maintenance overhead for two parallel APIs
    - Recommendation: For production with high throughput, standardize on gRPC

3. **Calculation Strategy**:
    - Current approach: Calculate on write, store results
    - Pro: Fast reads, good for read-heavy workloads
    - Con: Slightly slower writes, more complex synchronization
    - Alternative: Calculate on read for write-heavy workloads

4. **Horizontal Scaling**:
    - Current limitation: In-memory state is not shared between instances
    - Possible enhancement: Implement distributed state management or sticky sessions
