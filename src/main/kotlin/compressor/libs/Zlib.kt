package org.example.compressor.libs
import org.apache.commons.compress.compressors.deflate.DeflateCompressorInputStream
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream
import kotlin.also
import kotlin.collections.contentEquals
import kotlin.io.copyTo
import kotlin.io.inputStream
import kotlin.io.readBytes
import kotlin.io.use
import kotlin.io.writeBytes

/**
 * Utility object for zlib/DEFLATE compression and decompression operations.
 */
object ZlibUtils {
    private const val DEFAULT_BUFFER_SIZE = 8192

    /**
     * Compresses data using zlib algorithm with java.util.zip.
     *
     * @param input Byte array to compress
     * @param level Compression level (0-9, where 9 is maximum compression)
     * @return Compressed byte array
     */
    fun compress(input: ByteArray, level: Int = Deflater.DEFAULT_COMPRESSION): ByteArray {
        val startTime = System.currentTimeMillis()
        val deflater = Deflater(level)
        val outputStream = ByteArrayOutputStream(input.size)

        DeflaterOutputStream(outputStream, deflater).use { it.write(input) }
        val result = outputStream.toByteArray()

        val endTime = System.currentTimeMillis()
        println("Compression: ${input.size} bytes → ${result.size} bytes (${result.size * 100.0 / input.size}%) in ${endTime - startTime}ms")

        return result
    }

    /**
     * Decompresses zlib compressed data using java.util.zip.
     *
     * @param input Compressed byte array
     * @return Original decompressed data
     */
    fun decompress(input: ByteArray): ByteArray {
        val startTime = System.currentTimeMillis()
        val inflater = Inflater()
        val outputStream = ByteArrayOutputStream(input.size * 2) // Estimate output size

        InflaterInputStream(input.inputStream(), inflater).use { inStream ->
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
     * Compresses a file using DEFLATE algorithm.
     *
     * @param inputPath Path to the file to compress
     * @param outputPath Path to save the compressed file
     * @param useCommonsCompress Whether to use Apache Commons Compress (true) or java.util.zip (false)
     * @return Size of the compressed file in bytes
     */
    fun compressFile(inputPath: String, outputPath: String, useCommonsCompress: Boolean = false): Long {
        val startTime = System.currentTimeMillis()
        val inputSize = File(inputPath).length()

        Files.newInputStream(Paths.get(inputPath)).use { inputStream ->
            Files.newOutputStream(Paths.get(outputPath)).use { outputStream ->
                val bufferedOut = BufferedOutputStream(outputStream)

                if (useCommonsCompress) {
                    DeflateCompressorOutputStream(bufferedOut)
                        .use { deflateOut ->
                        inputStream.copyTo(deflateOut, DEFAULT_BUFFER_SIZE)
                    }
                } else {
                    DeflaterOutputStream(bufferedOut).use { deflateOut ->
                        inputStream.copyTo(deflateOut, DEFAULT_BUFFER_SIZE)
                    }
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
     * Decompresses a DEFLATE compressed file.
     *
     * @param inputPath Path to the compressed file
     * @param outputPath Path to save the decompressed file
     * @param useCommonsCompress Whether to use Apache Commons Compress (true) or java.util.zip (false)
     * @return Size of the decompressed file in bytes
     */
    fun decompressFile(inputPath: String, outputPath: String, useCommonsCompress: Boolean = false): Long {
        val startTime = System.currentTimeMillis()
        val inputSize = File(inputPath).length()

        Files.newInputStream(Paths.get(inputPath)).use { inputStream ->
            Files.newOutputStream(Paths.get(outputPath)).use { outputStream ->
                if (useCommonsCompress) {
                    DeflateCompressorInputStream(inputStream)
                        .use { deflateIn ->
                        deflateIn.copyTo(outputStream, DEFAULT_BUFFER_SIZE)
                    }
                } else {
                    InflaterInputStream(inputStream).use { inflateIn ->
                        inflateIn.copyTo(outputStream, DEFAULT_BUFFER_SIZE)
                    }
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


fun main() {

    val entropy = 0.9
    // Compress a file
    val file = File("data/kotlinInt_${entropy}")
    val originalData = file.readBytes()
    val compressedData = ZlibUtils.compress(originalData)

    // Save compressed data
    File("data/kotlinInt_${entropy}.zlib").writeBytes(compressedData)

    // Decompress to verify
    val decompressedData = ZlibUtils.decompress(compressedData)
    assert(decompressedData.contentEquals(originalData)) { "Decompression failed!" }
}