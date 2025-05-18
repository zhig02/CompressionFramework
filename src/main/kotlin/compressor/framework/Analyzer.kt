package compressor.framework
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
 * Интерфейс для анализа результатов сжатия.
 *
 * Предоставляет функциональность для обработки и анализа результатов
 * применения различных алгоритмов сжатия к тестовым данным.
 */
interface ResultAnalyzer {
    /**
     * Анализирует набор результатов сжатия.
     *
     * Обрабатывает коллекцию результатов сжатия для извлечения статистики,
     * построения графиков или других форм анализа эффективности алгоритмов.
     *
     * @param results Список результатов сжатия для анализа
     */
    fun analyze(results: List<CompressionResult>)
}

class CompressResultAnalyzer(): ResultAnalyzer{

    override fun analyze(results: List<CompressionResult>) {
        /**
         * Зависимость качества сжатия от степени энтропии
         * Зависимость скорости сжатия от объёма данных
         * Потребление памяти от алгоритма сжатия
         */
        results.forEach { compressionResult ->

        }
    }
}


fun main(){

    println("Result Analyzer")
}