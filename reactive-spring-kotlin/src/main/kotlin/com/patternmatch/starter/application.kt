package com.patternmatch.starter

import io.r2dbc.h2.H2ConnectionConfiguration
import io.r2dbc.h2.H2ConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.flow.Flow
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.reactive.function.server.json
import java.time.LocalDateTime

data class HistoricalEntry(@Id val id: Long?, val entryDate: LocalDateTime, val entryKey: String, val entryValue: String)

interface HistoricalEntryRepository : ReactiveCrudRepository<HistoricalEntry, Long> {
    @Query("SELECT * FROM historical_entry LIMIT :limit OFFSET :offset ")
    suspend fun findAll(limit: Int, offset: Int): Flow<HistoricalEntry>
}

@RestController
class HistoricalDataController(val historicalEntryRepository: HistoricalEntryRepository) {
    @GetMapping("/api/v1/historical-data")
    suspend fun getHistoricalData(@RequestParam limit: Int, @RequestParam offset: Int) = historicalEntryRepository.findAll(limit, offset)
}

@Configuration
class RouterConfig() {
    @Bean
    fun historicalEntryRoutes(historicalEntryRepository: HistoricalEntryRepository) = coRouter {
        GET("/api/v2/historical-data") { ServerResponse.ok().json().bodyAndAwait(historicalEntryRepository.findAll(10, 0)) }
    }
}

@Configuration
class DatabaseConfiguration : AbstractR2dbcConfiguration() {
    @Bean
    override fun connectionFactory(): ConnectionFactory {
        return H2ConnectionFactory(H2ConnectionConfiguration.builder()
                .file("./historicaldata;AUTO_RECONNECT=TRUE")
                .username("sa")
                .password("")
                .build())
    }
}

@SpringBootApplication
@EnableR2dbcRepositories
class StarterApplication

fun main(args: Array<String>) {
    runApplication<StarterApplication>(*args)
}
