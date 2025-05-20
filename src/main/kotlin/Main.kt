import compressor.framework.*


fun main() {
    println("HellOS")

    val analyzer = CompressAnalyzer(0.0, 1.0, 0.1,
        128, 1024*5, 256,
        listOf(DataType.INT, DataType.DOUBLE, DataType.FLOAT, DataType.BYTE)
    )

    CompressorRegistry.register(ZlibCompressor())
    CompressorRegistry.register(GzipCompressor())
    CompressorRegistry.register(Lz4Compressor())

    analyzer.analyze(CompressorRegistry)
    analyzer.plot()
}