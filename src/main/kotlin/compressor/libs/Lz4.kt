package compressor.libs

import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Utility object for LZ4 compression and decompression operations using Apache Commons Compress.
 */
object Lz4Utils {
    private const val DEFAULT_BUFFER_SIZE = 8192

    /**
     * Compresses data using LZ4 algorithm.
     *
     * @param input Byte array to compress
     * @return Compressed byte array
     */
    fun compress(input: ByteArray): ByteArray {
        val startTime = System.currentTimeMillis()
        val outputStream = ByteArrayOutputStream(input.size)

        BlockLZ4CompressorOutputStream(outputStream).use { lz4Out ->
            lz4Out.write(input)
        }

        val result = outputStream.toByteArray()
        val endTime = System.currentTimeMillis()
        println("Compression: ${input.size} bytes → ${result.size} bytes (${result.size * 100.0 / input.size}%) in ${endTime - startTime}ms")

        return result
    }

    /**
     * Decompresses LZ4 compressed data.
     *
     * @param input Compressed byte array
     * @return Original decompressed data
     */
    fun decompress(input: ByteArray): ByteArray {
        val startTime = System.currentTimeMillis()
        val outputStream = ByteArrayOutputStream(input.size * 4) // Estimate output size

        BlockLZ4CompressorInputStream(input.inputStream()).use { lz4In ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var n: Int
            while (lz4In.read(buffer).also { n = it } != -1) {
                outputStream.write(buffer, 0, n)
            }
        }

        val result = outputStream.toByteArray()
        val endTime = System.currentTimeMillis()
        println("Decompression: ${input.size} bytes → ${result.size} bytes in ${endTime - startTime}ms")

        return result
    }

    /**
     * Compresses a file using LZ4 algorithm.
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
                BlockLZ4CompressorOutputStream(outputStream).use { lz4Out ->
                    inputStream.copyTo(lz4Out, DEFAULT_BUFFER_SIZE)
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
     * Decompresses an LZ4 compressed file.
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
                BlockLZ4CompressorInputStream(inputStream).use { lz4In ->
                    lz4In.copyTo(outputStream, DEFAULT_BUFFER_SIZE)
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