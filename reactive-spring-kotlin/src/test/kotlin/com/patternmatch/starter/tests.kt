package com.patternmatch.starter

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.LocalDateTime

@SpringBootTest
class StarterApplicationTest {
    @Test
    fun contextLoads() {
    }
}

@WebFluxTest
class ApiTest() {

    @MockBean
    lateinit var historicalEntryRepository: HistoricalEntryRepository

    @Autowired
    lateinit var webClient: WebTestClient


    @Test
    suspend fun testGetHistoricalEntries() {

        whenever(historicalEntryRepository.findAll(Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(flowOf(HistoricalEntry(1, LocalDateTime.now(), "test", "val")))

        webClient.get().uri("/api/v1/historical-data?limit=10&offset=0")
                .header(HttpHeaders.ACCEPT, "application/json")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].entryKey").isEqualTo("test")
                .jsonPath("$[0].entryValue").isEqualTo("val")
                .jsonPath("$[0].entryDate").exists()

        verify(historicalEntryRepository, times(1)).findAll(Mockito.anyInt(), Mockito.anyInt())
    }
}
