package org.example.compressor.libs

import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.io.copyTo
import kotlin.io.inputStream
import kotlin.io.use

/**
 * Utility object for GZIP compression and decompression operations.
 */
object GzipUtils {
    private const val DEFAULT_BUFFER_SIZE = 8192

    /**
     * Compresses data using GZIP algorithm.
     *
     * @param input Byte array to compress
     * @return Compressed byte array
     */
    fun compress(input: ByteArray): ByteArray {
        val startTime = System.currentTimeMillis()
        val outputStream = ByteArrayOutputStream(input.size)

        GZIPOutputStream(outputStream).use { it.write(input) }
        val result = outputStream.toByteArray()

        val endTime = System.currentTimeMillis()
        println("Compression: ${input.size} bytes → ${result.size} bytes (${result.size * 100.0 / input.size}%) in ${endTime - startTime}ms")

        return result
    }

    /**
     * Decompresses GZIP compressed data.
     *
     * @param input Compressed byte array
     * @return Original decompressed data
     */
    fun decompress(input: ByteArray): ByteArray {
        val startTime = System.currentTimeMillis()
        val outputStream = ByteArrayOutputStream(input.size * 2) // Estimate output size

        GZIPInputStream(input.inputStream()).use { inStream ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var n: Int
            while (inStream.read(buffer).also { n = it } != -1) {
                outputStream.write(buffer, 0, n)
            }
        }

        val result = outputStream.toByteArray()
        val endTime = System.currentTimeMillis()
        println("Decompression: ${input.size} bytes → ${result.size} bytes in ${endTime - startTime}ms")

        return result
    }

    /**
     * Compresses a file using GZIP algorithm.
     *
     * @param inputPath Path to the file to compress
     * @param outputPath Path to save the compressed file
     * @return Size of the compressed file in bytes
     */
    fun compressFile(inputPath: String, outputPath: String): Long {
        val startTime = System.currentTimeMillis()
        val inputSize = File(inputPath).length()

        Files.newInputStream(Paths.get(inputPath)).use { inputStream ->
            Files.newOutputStream(Paths.get(outputPath)).use { outputStream ->
                GZIPOutputStream(outputStream).use { gzipOut ->
                    inputStream.copyTo(gzipOut, DEFAULT_BUFFER_SIZE)
                }
            }
        }

        val outputSize = File(outputPath).length()
        val endTime = System.currentTimeMillis()
        println(
            "File compression: $inputPath ($inputSize bytes) → $outputPath ($outputSize bytes) " +
                    "(${outputSize * 100.0 / inputSize}%) in ${endTime - startTime}ms"
        )

        return outputSize
    }

    /**
     * Decompresses a GZIP compressed file.
     *
     * @param inputPath Path to the compressed file
     * @param outputPath Path to save the decompressed file
     * @return Size of the decompressed file in bytes
     */
    fun decompressFile(inputPath: String, outputPath: String): Long {
        val startTime = System.currentTimeMillis()
        val inputSize = File(inputPath).length()

        Files.newInputStream(Paths.get(inputPath)).use { inputStream ->
            Files.newOutputStream(Paths.get(outputPath)).use { outputStream ->
                GZIPInputStream(inputStream).use { gzipIn ->
                    gzipIn.copyTo(outputStream, DEFAULT_BUFFER_SIZE)
                }
            }
        }

        val outputSize = File(outputPath).length()
        val endTime = System.currentTimeMillis()
        println(
            "File decompression: $inputPath ($inputSize bytes) → $outputPath ($outputSize bytes) " +
                    "in ${endTime - startTime}ms"
        )

        return outputSize
    }
}