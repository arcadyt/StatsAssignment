package com.kanevsky.stats.service;

import com.kanevsky.stats.IngestException;
import com.kanevsky.stats.model.Stats;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Service
public class ExtractorService /*implements IExtractorService*/ {
    // all stuff below are deprecated
//
//        @Autowired
//        private Set<ACsvStatsCollectionStrategy> collectors;
//
//        @Override
//        public long ingestInputStream(InputStream inputStream) throws IngestException {
////        var columnNames = List.of("playerID", "birthYear", "birthMonth", "birthDay", "birthCountry", "birthState", "birthCity", "deathYear", "deathMonth", "deathDay", "deathCountry", "deathState", "deathCity", "nameFirst", "nameLast", "nameGiven", "weight", "height", "bats", "throws", "debut", "finalGame", "retroID", "bbrefID");
//            Map<String, String> csvColumnNameToDbColumnName = PlayerColumnUtils.getCsvColumnNameToDbColumnName();
//
//            long count = 0;
//
//
//            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
//                 CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader())) {
//                Map<String, Integer> headerMap = parser.getHeaderMap();
//                for (CSVRecord stat : parser) {
//                    processRecord(stat, headerMap);
//                    ++count;
//                }
//            } catch (IOException e) {
//                log.error("Batch insert failed", e);
//                throw new IngestException("Batch insert failed", e);
//            }
//            return count;
//        }
//
//        private void processRecord(CSVRecord record) {
//            Stats stats = extractStats(record);
//            collectors.forEach(collector->{
//                String key = collector.extractKey(record);
//                collector.submit(key, stats);
//            });
//        }
//
//        private Stats extractStats(CSVRecord record) {
//            return null;
//        }
//
//    /*private void addLineToPreparedStatement(String[] lineData, Map<Integer, Integer> mapping, PreparedStatement preparedStatement, List<String> columnNames, Map<String, Integer> columnNameToSQLTypes) throws SQLException {
//        Map<Integer, Function> sqlTypeToMapper = Map.of(
//                Types.VARCHAR, Function.identity(),
//                Types.INTEGER, (s) -> Integer.parseInt(s.toString()),
//                Types.DATE, (s) -> LocalDate.parse(s.toString()),
//                Types.OTHER, Function.identity());
//
//        for (int i = 0; i < lineData.length; i++) {
//            String cellValue = lineData[mapping.get(i)];
//            String columnName = columnNames.get(i);
//            Integer sqlType = columnNameToSQLTypes.get(columnName);
//
//            if (cellValue == null || cellValue.isBlank()) {
//                preparedStatement.setNull(i + 1, sqlType);
//            } else {
//                preparedStatement.setObject(i + 1, sqlTypeToMapper.get(sqlType).apply(lineData[i]), sqlType);
//            }
//        }
//
//        preparedStatement.addBatch();
//    }*/
//
//        private Map<Integer, Integer> buildCsvColumnIndexMappingToQueryIndex(String headline, List<String> columnNames) {
//            var csvHeadlineColumns = Arrays.asList(headline.split(",")); // Assuming CSV fields are comma-separated
//            Map<Integer, Integer> result = new HashMap<>();
//            for (int queryColumnIndex = 0; queryColumnIndex < columnNames.size(); queryColumnIndex++) {
//                String queryColumnName = columnNames.get(queryColumnIndex);
//                int headlineColumnIndex = csvHeadlineColumns.indexOf(queryColumnName);
//                result.put(headlineColumnIndex, queryColumnIndex);
//            }
//            return result;
//        }
//
//        private void executeIntermediateBatchIfReachedCapacity(PreparedStatement preparedStatement, long count) throws SQLException {
//            if (count % batchSize == 0) {
//                preparedStatement.executeBatch();
////            jdbcTemplate.getDataSource().getConnection().commit();
//            }
//        }
//
//        private void executeRemainderBatch(PreparedStatement preparedStatement) throws SQLException {
//            preparedStatement.executeBatch();
////        jdbcTemplate.getDataSource().getConnection().commit();
//            jdbcTemplate.getDataSource().getConnection().setAutoCommit(true);
//    }
}

