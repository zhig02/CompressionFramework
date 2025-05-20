package compressor.framework

import kotlin.math.abs
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.layers.line
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.points

/**
 * Результат операции сжатия или распаковки данных.
 *
 * Содержит метрики производительности и эффективности сжатия,
 * а также сами обработанные данные и метаданные.
 *
 * @property originalSize Размер исходных данных в байтах
 * @property compressedSize Размер сжатых данных в байтах
 * @property compressionRatio Коэффициент сжатия (отношение исходного размера к сжатому)
 * @property compressionTimeMs Время выполнения операции в миллисекундах
 * @property data Результирующие данные операции (сжатые или распакованные)
 * @property dataFile Путь к файлу с результатом операции (если применимо)
 */
class CompressionResult(
    val originalSize: Int,
    val compressedSize: Int,
    val compressionRatio: Double,
    val compressionTimeMs: Int,
    val data: ByteArray,
    val dataFile: String,
)

/**
 * Общий конфиг данных.
 * @property dataType Тип данных
 * @property originalSize Размер генерируемых данных в байтах
 * @property compressedSize Размер сжатых данных
 * @property compressionRatio Степень сжатия
 * @property compressionTimeMs Время сжатия
 * @property dataFile Файл с данными
 * @property entropy Степень энтропии данных (от 0.0 до 1.0), где:
 *   - 0.0 означает минимальную энтропию (максимальную предсказуемость)
 *   - 1.0 означает максимальную энтропию (полную случайность)
 * @property compressor Имя алгоритма сжатия
 */
data class DataConfig(
    val dataType: DataType,
    val originalSize: Int,
    val compressedSize: Int? = null,
    val compressionRatio: Double? = null,
    val compressionTimeMs: Int? = null,
    val dataFile: String? = null,
    val compressedFile: String? = null,
    val entropy: Double,
    val compressor: String? = null
)

/**
 * Интерфейс для анализа результатов сжатия.
 *
 * Предоставляет функциональность для обработки и анализа результатов
 * применения различных алгоритмов сжатия к тестовым данным.
 */
interface Analyzer {

    fun analyze(registry: CompressorRegistry): List<DataConfig>
    fun plot()
}

class CompressAnalyzer(
    eMin: Double, eMax: Double, eStep: Double,
    sMin: Int, sMax: Int, sStep: Int,
    types: List<DataType>
) : Analyzer {

    private val entropyRange: List<Double> = generateSequence(eMin) { it + eStep }
        .takeWhile { it <= eMax }
        .toList()

    private val sizeRange: List<Int> = generateSequence(sMin) { it + sStep }
        .takeWhile { it <= sMax }
        .toList()

    private val typeRange = types

    private var dataDescriptorList: List<DataConfig> = emptyList()

    override fun analyze(registry: CompressorRegistry): List<DataConfig> {
        /**
         * Зависимость качества сжатия от степени энтропии
         * Зависимость качества сжатия от объёма данных
         * Зависимость качества от типа
         */

        //HYPERLOOP
        dataDescriptorList = sizeRange.flatMap { size ->
            entropyRange.flatMap { entropy ->
                typeRange.flatMap { type ->
                    val config = GeneratorConfig(size, type, entropy)
                    val generator = when (type) {
                        DataType.INT -> DataGeneratorClass(Int.MIN_VALUE, Int.MAX_VALUE, config)
                        DataType.DOUBLE -> DataGeneratorClass(Double.MIN_VALUE, Double.MAX_VALUE, config)
                        DataType.FLOAT -> DataGeneratorClass(Float.MIN_VALUE, Float.MAX_VALUE, config)
                        DataType.BYTE -> DataGeneratorClass(Byte.MIN_VALUE, Byte.MAX_VALUE, config)
                    }
                    val data = generator.generate()
                    registry.getAllCompressors().map { compressor ->
                        val compressResult = compressor.compress(data)
                        val decompressResult = compressor.decompress(compressResult.data)
                        val correct = decompressResult.data.contentEquals(data)
                        if (!correct) {
                            throw IllegalStateException("Decompressed data does not match the original")
                        }
                        DataConfig(
                            dataType = type,
                            originalSize = size,
                            compressedSize = compressResult.compressedSize,
                            compressionRatio = size / compressResult.compressedSize.toDouble(),
                            compressionTimeMs = compressResult.compressionTimeMs,
                            entropy = calculateEntropyFromBytes(data, config.dataType),
                            compressor = compressor.name
                        )
                    }
                }
            }
        }
        return dataDescriptorList
    }

    override fun plot() {

        //Зависимость качества сжатия от степени энтропии

        val middleIndex = sizeRange.size / 2
        val middleSize = sizeRange[middleIndex]

        val groupTyped = dataDescriptorList
            .filter { it.originalSize == middleSize }
            .groupBy { it.dataType }

        groupTyped.forEach { (key, value) ->
            val dataset = mapOf(
                "entropy" to value.map { it.entropy },
                "ratio" to value.map { it.compressionRatio ?: 0.0 },
                "compressor" to value.map { it.compressor ?: "unknown" }
            )

            dataset.plot {
                groupBy("compressor") {
                    line {
                        x("entropy")
                        y("ratio")
                        color("compressor")
                    }
                }
                layout {
                    title = "$key type, size = $middleSize"
                }
            }.save("ratio_vs_entropy_$key.png")
        }

        //Зависимость качества сжатия от размера
        val middleEntropy = entropyRange[entropyRange.size/2]
        val groupEntropy = dataDescriptorList
            .filter { abs(it.entropy - middleEntropy) < 7e-2 }
            .groupBy { it.dataType }

        groupEntropy.forEach { (key, value) ->
            val dataSet = mapOf(
                "size" to value.map {it.originalSize},
                "ratio" to value.map {it.compressionRatio},
                "compressor" to value.map {it.compressor ?: "unknown"}
            )

            dataSet.plot {
                groupBy("compressor") {
                    line {
                        x("size")
                        y("ratio")
                        color("compressor")
                    }
                }
                layout {
                    title = "$key type, entropy = ${value[0].entropy}"
                }
            }.save("ratio_vs_size_$key.png")
        }

        //Зависимость качества сжатия от типа
        val filtered = dataDescriptorList
            .filter { it.originalSize == middleSize && abs(it.entropy - middleEntropy) < 7e-2 }


        val datasetByType = mapOf(
            "type" to filtered.map { it.dataType.toString() },
            "ratio" to filtered.map { it.compressionRatio ?: 0.0 },
            "compressor" to filtered.map { it.compressor ?: "unknown" }
        )

        datasetByType.plot {
            groupBy("compressor") {

                points {
                    x("type")
                    y("ratio")
                    color("compressor")
                }
            }
            layout {
                title = "Size = $middleSize, Entropy = ${filtered[0].entropy}"
            }
        }.save("ratio_vs_type.png")

    }
}


fun main() {

    println("Result Analyzer")
}