package compressor.framework

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.log2
import kotlin.random.Random

/**
 * Конфигурация генератора данных для тестирования алгоритмов сжатия.
 *
 * @property originalSize Размер генерируемых данных в байтах
 * @property dataType Тип генерируемых данных
 * @property entropy Степень энтропии данных (от 0.0 до 1.0), где:
 *   - 0.0 означает минимальную энтропию (максимальную предсказуемость)
 *   - 1.0 означает максимальную энтропию (полную случайность)
 */
class GeneratorConfig(
    val originalSize: Int,         // размер генерируемых данных
    val dataType: DataType, // тип данных
    val entropy: Double,    //значение энтропии
)




/**
 * Типы данных, поддерживаемые генератором.
 *
 * @property bytesPerElement Количество байт, занимаемое одним элементом данного типа
 */
enum class DataType(val bytesPerElement: Int) {
    BYTE(1),         // 1 байт
    INT(4),          // 4 байта
    FLOAT(4),        // 4 байта
    DOUBLE(8),       // 8 байтов
}

/**
 * Интерфейс для генерации тестовых данных с заданными параметрами.
 */
interface DataGenerator {
    /**
     * Генерирует данные в соответствии с конфигурацией и возвращает их как массив байт.
     *
     * @return Сгенерированные данные в виде байтового массива
     */
    fun generate(): ByteArray

    /**
     * Генерирует данные и сохраняет их в файл по указанному пути.
     *
     * @param path Путь к файлу для сохранения сгенерированных данных
     */
    fun generate(path: String)

    /**
     * Возвращает метаданные о генерации данных.
     *
     * @return Конфигурация, использованная при генерации
     */
    //fun getMetadata(): DataConfig
}

/**
 * Общий генератор данных.
 *
 *
 * @param minValue - Минимальное число в сгенерированных данных
 * @param maxValue - Максимальное число в сгенерированных данных
 * @param config Конфигурация генератора данны
 */
class DataGeneratorClass<T : Comparable<T>>(
    private val minValue: T,
    private val maxValue: T,
    private val config: GeneratorConfig,
) : DataGenerator {

    init {
        require(minValue <= maxValue) { "Min value must be less than or equal to max value" }
        require(config.entropy in 0.0..1.0) { "Entropy must be between 0.0 and 1.0" }
        // Validate that T matches the datatype in config
        validateType()
    }

    private fun validateType() {
        when (minValue) {
            is Int -> require(config.dataType == DataType.INT) { "DataType mismatch: expected INT for Int values" }
            is Float -> require(config.dataType == DataType.FLOAT) { "DataType mismatch: expected FLOAT for Float values" }
            is Double -> require(config.dataType == DataType.DOUBLE) { "DataType mismatch: expected DOUBLE for Double values" }
            is Byte -> require(config.dataType == DataType.BYTE) { "DataType mismatch: expected CHAR for Char values" }
            else -> throw IllegalArgumentException("Unsupported type: ${minValue::class.simpleName}")
        }
    }

    private val random = Random.Default

//    override fun getMetadata(): DataConfig {
//        return config
//    }

    override fun generate(path: String) {
        val data = generate()
        File(path).writeBytes(data)
        //config.dataFile = path
    }

    override fun generate(): ByteArray {
        val probabilities = calculateProbabilityFromEntropy()
        val elementCount = (config.originalSize / config.dataType.bytesPerElement)
        val buffer = ByteBuffer.allocate(config.originalSize)
        buffer.order(ByteOrder.nativeOrder())

        when (minValue) {
            is Int -> {
                val array = IntArray(elementCount)
                var currentIndex = 0
                val min = minValue as Int
                val max = maxValue as Int

                for (i in probabilities.indices) {
                    val count = (probabilities[i] * elementCount).toInt()
                    array.fill(
                        random.nextInt(from = min, until = max),
                        currentIndex,
                        (currentIndex + count).coerceAtMost(elementCount)
                    )
                    currentIndex += count
                }

                while (currentIndex < elementCount) {
                    array[currentIndex++] = random.nextInt(from = min, until = max)
                }

                array.shuffle()
                //println("generateInt:: array: ${array.contentToString()}")
                array.forEach { buffer.putInt(it) }
            }

            is Double -> {
                val array = DoubleArray(elementCount)
                var currentIndex = 0
                val min = minValue as Double
                val max = maxValue as Double

                for (i in probabilities.indices) {
                    val count = (probabilities[i] * elementCount).toInt()
                    array.fill(
                        random.nextDouble(from = min, until = max),
                        currentIndex,
                        (currentIndex + count).coerceAtMost(elementCount)
                    )
                    currentIndex += count
                }

                while (currentIndex < elementCount) {
                    array[currentIndex++] = random.nextDouble(from = min, until = max)
                }

                array.shuffle()
                //println("generateDouble:: array: ${array.contentToString()}")
                array.forEach { buffer.putDouble(it) }
            }

            is Float -> {
                val array = FloatArray(elementCount)
                var currentIndex = 0
                val min = minValue as Float
                val max = maxValue as Float

                for (i in probabilities.indices) {
                    val count = (probabilities[i] * elementCount).toInt()
                    array.fill(
                        min + random.nextFloat() * (max - min),
                        currentIndex,
                        (currentIndex + count).coerceAtMost(elementCount)
                    )
                    currentIndex += count
                }

                while (currentIndex < elementCount) {
                    array[currentIndex++] = min + random.nextFloat() * (max - min)
                }

                array.shuffle()
                //println("generateFloat:: array: ${array.contentToString()}")
                array.forEach { buffer.putFloat(it) }
            }

            is Byte -> {
                val array = ByteArray(elementCount)
                var currentIndex = 0
                val min = (minValue as Byte).toInt()
                val max = (maxValue as Byte).toInt()

                for (i in probabilities.indices) {
                    val count = (probabilities[i] * elementCount).toInt()
                    array.fill(
                        random.nextInt(min, max).toByte(),
                        currentIndex,
                        (currentIndex + count).coerceAtMost(elementCount)
                    )
                    currentIndex += count
                }

                while (currentIndex < elementCount) {
                    array[currentIndex++] = random.nextInt(min, max).toByte()
                }

                array.shuffle()
//                println(
//                    "generateByte:: array: [${
//                        array.joinToString(", ") { byte ->
//                            "0x%02X".format(byte.toInt() and 0xFF)
//                        }
//                    }]"
//                )
                return array // For Byte arrays, we can return directly
            }

            else -> throw IllegalArgumentException("Unsupported type: ${minValue::class.simpleName}")
        }

        return buffer.array()
    }

    private fun calculateProbabilityFromEntropy(): List<Double> {
        val numberOfElements: Int = config.originalSize / config.dataType.bytesPerElement

        // Start with uniform distribution
        val probabilities = MutableList(numberOfElements) { 1.0 / numberOfElements }
        var currentEntropy = 1.0

        // Iteratively adjust probabilities to reach target entropy
        val stepSize = 0.01
        var iterations = 0

        while (abs(currentEntropy - config.entropy) > 0.001 && iterations < 2000) {
            if (currentEntropy > config.entropy) {
                // Increase first probability, decrease others
                var delta = stepSize
                if (probabilities[0] + delta > 0.99) {
                    delta = 0.99 - probabilities[0]
                }

                probabilities[0] += delta
                val remaining = 1.0 - probabilities[0]
                for (i in 1 until numberOfElements) {
                    probabilities[i] = remaining / (numberOfElements - 1)
                }
            } else {
                // Decrease first probability, increase others
                var delta = stepSize
                if (probabilities[0] - delta < 1.0 / numberOfElements) {
                    delta = probabilities[0] - 1.0 / numberOfElements
                }

                probabilities[0] -= delta
                val remaining = 1.0 - probabilities[0]
                for (i in 1 until numberOfElements) {
                    probabilities[i] = remaining / (numberOfElements - 1)
                }
            }

            // Recalculate entropy
            currentEntropy = calculateEntropy(probabilities)
            //config.entropy = currentEntropy
            iterations++

        }
        return probabilities
    }

    fun calculateEntropy(probabilities: List<Double>, base: Double = Math.E): Double {
        // Validate input
        require(probabilities.all { it >= 0 }) { "All probabilities must be non-negative" }

        // Calculate entropy: -sum(p_i * log(p_i))
        var result = 0.0
        for (p in probabilities) {
            if (p > 0) { // Avoid log(0)
                result -= p * (ln(p) / ln(base))
            }
        }
        return result / ln(probabilities.size.toDouble())
    }
}

fun <T> normalizedShannonEntropy(x: List<T>): Double {
    // Count frequencies of elements
    val counts = mutableMapOf<T, Int>()
    for (element in x) {
        counts[element] = counts.getOrDefault(element, 0) + 1
    }

    val total = counts.values.sum()

    // Calculate entropy
    var entropy = 0.0
    for (count in counts.values) {
        val p = count.toDouble() / total
        if (p > 0) {
            entropy -= p * log2(p)
        }
    }

    // Maximum entropy for n categories: log(n)
    val n = counts.size
    if (n <= 1) {
        return 0.0  // If only one unique element or empty list, entropy is zero
    }

    val normalizedEntropy = entropy / log2(n.toDouble())
    return normalizedEntropy
}

fun calculateEntropyFromBytes(data: ByteArray, dataType: DataType): Double {
    val buffer = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder())
    val count = data.size / dataType.bytesPerElement
    val values = when (dataType) {
        DataType.INT    -> List(count)    { buffer.getInt()    }
        DataType.DOUBLE -> List(count)    { buffer.getDouble() }
        DataType.FLOAT  -> List(count)    { buffer.getFloat()  }
        DataType.BYTE   -> data.toList()
    }
    return normalizedShannonEntropy(values)
}

fun reCalculationEntropy(filename: String, config: GeneratorConfig): Double {
    val file = File(filename)
    val data = file.readBytes()
    val buffer = ByteBuffer.wrap(data)
    buffer.order(ByteOrder.nativeOrder())

    val elementCount = data.size / config.dataType.bytesPerElement

    val values = when (config.dataType) {
        DataType.INT -> List(elementCount) { buffer.getInt() }
        DataType.DOUBLE -> List(elementCount) { buffer.getDouble() }
        DataType.FLOAT -> List(elementCount) { buffer.getFloat() }
        DataType.BYTE -> data.toList()
    }

    val entropy = normalizedShannonEntropy(values)
    //println("${config.dataType} data entropy: $entropy")
    return entropy
}


fun main() {

    val entropy = 0.99

    val configList = listOf(
        GeneratorConfig(1024, DataType.INT, entropy),
        GeneratorConfig(1024, DataType.DOUBLE, entropy),
        GeneratorConfig(1024, DataType.FLOAT, entropy),
        GeneratorConfig(1024, DataType.BYTE, entropy),
    )

    configList.forEach { config ->
        val generator = when (config.dataType) {
            DataType.INT -> DataGeneratorClass(Int.MIN_VALUE, Int.MAX_VALUE, config)
            DataType.DOUBLE -> DataGeneratorClass(Double.MIN_VALUE, Double.MAX_VALUE, config)
            DataType.FLOAT -> DataGeneratorClass(Float.MIN_VALUE, Float.MAX_VALUE, config)
            DataType.BYTE -> DataGeneratorClass(Byte.MIN_VALUE, Byte.MAX_VALUE, config)
        }
        val filename = "data/${config.dataType}_${config.originalSize}_${entropy}"
        generator.generate(filename)
        //println(generator.getMetadata())
        reCalculationEntropy(filename, config)
    }
}