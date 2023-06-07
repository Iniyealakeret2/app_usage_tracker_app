package com.hour.hour.helper

import com.csvreader.CsvReader
import com.csvreader.CsvWriter
import com.hour.hour.model.UsageRecord
import java.io.File
import java.io.FileWriter

@Suppress("MemberVisibilityCanBePrivate")
object CsvHelper {
    val usageHeader = arrayOf("packageName", "startTime", "duration")

    fun write(file: File, records: List<UsageRecord>): Boolean {
        val alreadyExist = File(file.path).exists()
        return try {
            val writer = CsvWriter(FileWriter(file, true), ',')
            if (!alreadyExist) {
                writer.writeRecord(usageHeader)
            }
            for (record in records) {
                writer.writeRecord(record.toArray())
            }
            writer.close()
            true
        } catch (e: Exception) {
            Logger.e("CsvHelper", e.message + ": " + e.localizedMessage)
            false
        }
    }

    fun read(file: File): List<UsageRecord> {
        try {
            if (!file.canRead()) {
                Logger.d("CsvHelper", "can't read file ${file.path}")
                return listOf()
            }
            val records = arrayListOf<UsageRecord>()
            val reader = CsvReader(file.path)
            reader.readHeaders()
            while (reader.readRecord()) {
                val record = UsageRecord(reader.get(0), reader.get(1).toLong(), reader.get(2).toLong())
                records.add(record)
            }
            reader.close()
            return records
        } catch (e: Exception) {
            Logger.e("CsvHelper", e.message + ": " + e.localizedMessage)
            return listOf()
        }
    }
}
