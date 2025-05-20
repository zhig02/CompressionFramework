package compressor.framework

import compressor.libs.GzipUtils
import compressor.libs.Lz4Utils
import compressor.libs.ZlibUtils
import java.io.File
import kotlin.io.readBytes

/**
 * Интерфейс для алгоритмов сжатия и декомпрессии данных.
 *
 * Определяет базовый функционал для работы с различными алгоритмами сжатия.
 * Поддерживает операции как с байтовыми массивами, так и с файлами.
 *
 * @property name имя алгоритма сжатия
 */
interface Compressor {
    val name: String

    /**
     * Сжимает данные из байтового массива.
     *
     * @param data Исходные данные для сжатия
     * @return Результат операции сжатия
     */
    fun compress(data: ByteArray): CompressionResult

    /**
     * Сжимает данные из файла по указанному пути.
     *
     * @param path Путь к файлу, содержащему данные для сжатия
     * @return Результат операции сжатия
     */
    fun compress(path: String): CompressionResult

    /**
     * Распаковывает сжатые данные из байтового массива.
     *
     * @param data Сжатые данные для распаковки
     * @return Результат операции распаковки
     */
    fun decompress(data: ByteArray): CompressionResult

    /**
     * Распаковывает данные из файла по указанному пути.
     *
     * @param path Путь к файлу, содержащему сжатые данные
     * @return Результат операции распаковки
     */
    fun decompress(path: String): CompressionResult
}


/**
 * Реестр доступных алгоритмов сжатия.
 *
 * Предоставляет доступ к алгоритмам сжатия,
 * их регистрацию и получение по имени.
 */
object CompressorRegistry {
    private val compressors = mutableMapOf<String, Compressor>()


    /**
     * Регистрирует новый алгоритм сжатия в реестре.
     *
     * @param compressor Экземпляр алгоритма сжатия для регистрации
     */
    fun register(compressor: Compressor) {
        compressors[compressor.name] = compressor
    }

    /**
     * Получает алгоритм сжатия по его имени.
     *
     * @param name Имя алгоритма сжатия
     * @return Найденный алгоритм или null, если алгоритм не зарегистрирован
     */
    fun getCompressor(name: String): Compressor? = compressors[name]

    /**
     * Получает список всех зарегистрированных алгоритмов сжатия.
     *
     * @return Список всех зарегистрированных алгоритмов
     */
    fun getAllCompressors(): List<Compressor> = compressors.values.toList()
}

/**
 * Implementation of the Compressor interface using ZLib compression algorithm.
 */
class ZlibCompressor : Compressor {
    override val name: String = "zlib"

    override fun compress(data: ByteArray): CompressionResult {
        val startTime = System.currentTimeMillis()
        val compressedData = ZlibUtils.compress(data)
        val endTime = System.currentTimeMillis()

        return CompressionResult(
            originalSize = data.size,
            compressedSize = compressedData.size,
            compressionRatio = data.size.toDouble() / compressedData.size,
            compressionTimeMs = (endTime - startTime).toInt(),
            data = compressedData,
            dataFile = ""
        )
    }

    override fun decompress(data: ByteArray): CompressionResult {
        val startTime = System.currentTimeMillis()
        val decompressedData = ZlibUtils.decompress(data)
        val endTime = System.currentTimeMillis()

        return CompressionResult(
            originalSize = data.size,
            compressedSize = decompressedData.size,
            compressionRatio = data.size.toDouble() / decompressedData.size,
            compressionTimeMs = (endTime - startTime).toInt(),
            data = decompressedData,
            dataFile = ""
        )
    }

    override fun compress(path: String): CompressionResult {
        val startTime = System.currentTimeMillis()
        val file = File(path)
        val data = file.readBytes()
        val compressedData = ZlibUtils.compress(data)
        val endTime = System.currentTimeMillis()

        return CompressionResult(
            originalSize = data.size,
            compressedSize = compressedData.size,
            compressionRatio = data.size.toDouble() / compressedData.size,
            compressionTimeMs = (endTime - startTime).toInt(),
            data = compressedData,
            dataFile = path
        )
    }

    override fun decompress(path: String): CompressionResult {
        val startTime = System.currentTimeMillis()
        val file = File(path)
        val compressedData = file.readBytes()
        val decompressedData = ZlibUtils.decompress(compressedData)
        val endTime = System.currentTimeMillis()

        return CompressionResult(
            originalSize = compressedData.size,
            compressedSize = decompressedData.size,
            compressionRatio = compressedData.size.toDouble() / decompressedData.size,
            compressionTimeMs = (endTime - startTime).toInt(),
            data = decompressedData,
            dataFile = path
        )
    }
}

/**
 * Implementation of the Compressor interface using GZIP compression algorithm.
 */
class GzipCompressor : Compressor {
    override val name: String = "gzip"

    override fun compress(data: ByteArray): CompressionResult {
        val startTime = System.currentTimeMillis()
        val compressedData = GzipUtils.compress(data)
        val endTime = System.currentTimeMillis()

        return CompressionResult(
            originalSize = data.size,
            compressedSize = compressedData.size,
            compressionRatio = data.size.toDouble() / compressedData.size,
            compressionTimeMs = (endTime - startTime).toInt(),
            data = compressedData,
            dataFile = ""
        )
    }

    override fun decompress(data: ByteArray): CompressionResult {
        val startTime = System.currentTimeMillis()
        val decompressedData = GzipUtils.decompress(data)
        val endTime = System.currentTimeMillis()

        return CompressionResult(
            originalSize = data.size,
            compressedSize = decompressedData.size,
            compressionRatio = data.size.toDouble() / decompressedData.size,
            compressionTimeMs = (endTime - startTime).toInt(),
            data = decompressedData,
            dataFile = ""
        )
    }

    override fun compress(path: String): CompressionResult {
        val startTime = System.currentTimeMillis()
        val file = File(path)
        val data = file.readBytes()
        val compressedData = GzipUtils.compress(data)
        val endTime = System.currentTimeMillis()

        return CompressionResult(
            originalSize = data.size,
            compressedSize = compressedData.size,
            compressionRatio = data.size.toDouble() / compressedData.size,
            compressionTimeMs = (endTime - startTime).toInt(),
            data = compressedData,
            dataFile = path
        )
    }

    override fun decompress(path: String): CompressionResult {
        val startTime = System.currentTimeMillis()
        val file = File(path)
        val compressedData = file.readBytes()
        val decompressedData = GzipUtils.decompress(compressedData)
        val endTime = System.currentTimeMillis()

        return CompressionResult(
            originalSize = compressedData.size,
            compressedSize = decompressedData.size,
            compressionRatio = compressedData.size.toDouble() / decompressedData.size,
            compressionTimeMs = (endTime - startTime).toInt(),
            data = decompressedData,
            dataFile = path
        )
    }
}


/**
 * Implementation of the Compressor interface using LZ4 compression algorithm.
 */
class Lz4Compressor : Compressor {
    override val name: String = "lz4"

    override fun compress(data: ByteArray): CompressionResult {
        val startTime = System.currentTimeMillis()
        val compressedData = Lz4Utils.compress(data)
        val endTime = System.currentTimeMillis()

        return CompressionResult(
            originalSize = data.size,
            compressedSize = compressedData.size,
            compressionRatio = data.size.toDouble() / compressedData.size,
            compressionTimeMs = (endTime - startTime).toInt(),
            data = compressedData,
            dataFile = ""
        )
    }

    override fun decompress(data: ByteArray): CompressionResult {
        val startTime = System.currentTimeMillis()
        val decompressedData = Lz4Utils.decompress(data)
        val endTime = System.currentTimeMillis()

        return CompressionResult(
            originalSize = data.size,
            compressedSize = decompressedData.size,
            compressionRatio = data.size.toDouble() / decompressedData.size,
            compressionTimeMs = (endTime - startTime).toInt(),
            data = decompressedData,
            dataFile = ""
        )
    }

    override fun compress(path: String): CompressionResult {
        val startTime = System.currentTimeMillis()
        val file = File(path)
        val data = file.readBytes()
        val compressedData = Lz4Utils.compress(data)
        val endTime = System.currentTimeMillis()

        return CompressionResult(
            originalSize = data.size,
            compressedSize = compressedData.size,
            compressionRatio = data.size.toDouble() / compressedData.size,
            compressionTimeMs = (endTime - startTime).toInt(),
            data = compressedData,
            dataFile = path
        )
    }

    override fun decompress(path: String): CompressionResult {
        val startTime = System.currentTimeMillis()
        val file = File(path)
        val compressedData = file.readBytes()
        val decompressedData = Lz4Utils.decompress(compressedData)
        val endTime = System.currentTimeMillis()

        return CompressionResult(
            originalSize = compressedData.size,
            compressedSize = decompressedData.size,
            compressionRatio = compressedData.size.toDouble() / decompressedData.size,
            compressionTimeMs = (endTime - startTime).toInt(),
            data = decompressedData,
            dataFile = path
        )
    }
}


fun main() {

    val entropy = 0.99
    println("Entropy: $entropy")

    CompressorRegistry.register(ZlibCompressor())
    CompressorRegistry.register(GzipCompressor())
    CompressorRegistry.register(Lz4Compressor())
    println("All compressors: ${CompressorRegistry.getAllCompressors().joinToString { compressor -> compressor.name }}")

    CompressorRegistry.getAllCompressors().forEach { compressor ->
        println("\n===${compressor.name}===")
        //Compress a file
        val inputPath = "./data/DOUBLE_1024_${entropy}"
        val originalData = File(inputPath).readBytes()
        val compressionResult = compressor.compress(inputPath)

        // Save compressed data
        val outputPath = "${inputPath}.${compressor.name}"
        File(outputPath).writeBytes(compressionResult.data)

        val decompressionResult = compressor.decompress(outputPath)
        val correct = decompressionResult.data.contentEquals(originalData)
        println("Data integrity check: ${if (correct) "PASSED" else "FAILED"}")
        println("Compression statistics:")
        println("Original size: ${compressionResult.originalSize} bytes")
        println("Compressed size: ${compressionResult.compressedSize} bytes")
        println("Compression ratio: ${compressionResult.compressionRatio}")
        println("Duration: ${compressionResult.compressionTimeMs}ms")
        println("File: ${compressionResult.dataFile}")
    }
}
