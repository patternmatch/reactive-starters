package com.patternmatch.starter

import io.smallrye.mutiny.Multi
import io.vertx.mutiny.mysqlclient.MySQLPool
import io.vertx.mutiny.sqlclient.Row
import java.time.LocalDateTime
import javax.enterprise.inject.Default
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

data class HistoricalEntry(val id: Long?, val entryDate: LocalDateTime, val entryKey: String, val entryValue: String) {
    companion object {
        private fun from(row: Row) = HistoricalEntry(
                row.getLong("id"),
                row.getLocalDateTime("entry_date"),
                row.getString("entry_key"),
                row.getString("entry_value"))

        fun findAll(client: MySQLPool): Multi<HistoricalEntry> {
            return client.query("SELECT * FROM historical_entry ORDER BY entry_date ASC").execute()
                    .onItem().transformToMulti { set -> Multi.createFrom().iterable(set) }
                    .onItem().transform { from(it) }
        }
    }
}

@Path("/api/v1/historical-entry")
class HistoricalEntryResource {

    @Inject
    @field: Default
    lateinit var client: MySQLPool

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun get(): Multi<HistoricalEntry> = HistoricalEntry.findAll(client!!)
}