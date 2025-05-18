//package compressor.framework

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.collections.iterator
import kotlin.math.ln
import kotlin.math.log2
import kotlin.random.Random

fun <T> normalizedShannonEntropyExtra(x: List<T>): Double {
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

fun <T> normalizedShannonEntropyK1(text: List<T>): Double {
    if (text.size <= 1) {
        return 0.0
    }

    // Define alphabet (all unique elements)
    val alphabet = text.toSet()
    if (alphabet.size <= 1) {
        return 0.0
    }

    // For each element, find its contexts and next elements
    val contexts = mutableMapOf<T, MutableList<T>>()
    for (i in 0 until text.size - 1) {
        val context = text[i]
        val nextChar = text[i + 1]

        if (context !in contexts) {
            contexts[context] = mutableListOf()
        }
        contexts[context]?.add(nextChar)
    }

    // Calculate entropy for each context
    val totalLength = contexts.values.sumOf { it.size }.toDouble()
    var entropyK1 = 0.0

    for ((context, nextChars) in contexts) {
        val contextProb = nextChars.size / totalLength

        // Zero-order entropy for this context
        val counts = mutableMapOf<T, Int>()
        for (char in nextChars) {
            counts[char] = counts.getOrDefault(char, 0) + 1
        }

        var entropyContext = 0.0
        for (count in counts.values) {
            val p = count.toDouble() / nextChars.size
            entropyContext -= p * log2(p)
        }

        entropyK1 += contextProb * entropyContext
    }

    // Maximum entropy is log(|alphabet|)
    val maxEntropy = log2(alphabet.size.toDouble())

    return entropyK1 / maxEntropy
}

/**
 * Calculates Shannon entropy from a list of probabilities.
 * Similar to scipy.entropy function.
 *
 * @param probabilities List of probability values (should sum to 1.0)
 * @param base Base of the logarithm (default is e, giving result in nats)
 * @return Entropy value
 */
fun calculateEntropyOnData(probabilities: List<Double>, base: Double = Math.E): Double {
    // Validate input
    require(probabilities.all { it >= 0 }) { "All probabilities must be non-negative" }

    // Calculate entropy: -sum(p_i * log(p_i))
    var result = 0.0
    for (p in probabilities) {
        if (p > 0) { // Avoid log(0)
            result -= p * (ln(p) / ln(base))
        }
    }
    return result/ ln(probabilities.size.toDouble())
}

// With base 2 (bits) as most commonly used in information theory
fun entropyBits(probabilities: List<Double>): Double {
    return calculateEntropyOnData(probabilities, 2.0)
}

fun main() {
    // Example data
    val zero = List(10) { 1 }
    val x = listOf(1, 2, 2, 3, 3, 3, 4, 4, 4, 4)
    val y = (1..10).toList()
    val z = List(10) { Random.nextDouble(-1000.0, 1000.0) }
    val mississippi = "mississippimississippi"
    val string0101 = "0101010101010101"

    // Read binary data file
    val file = File("data/kotlinDouble_0.1")
    val data = file.readBytes()

    // Define the size of one int (4 bytes) and calculate the number of integers
    val intSize = 4
    val numIntegers = data.size / intSize

    // Parse integers from the byte array
    val buffer = ByteBuffer.wrap(data)
    buffer.order(ByteOrder.nativeOrder()) // Use native byte order

    val numbers = List(numIntegers) { buffer.getInt() }

    println("Found numbers: ${numbers.take(10)}")

    val text = numbers

    val result = normalizedShannonEntropyExtra(text)
    println("Normalized Shannon Entropy: $result")

    val resultK1 = normalizedShannonEntropyK1(text)
    println("First order Entropy: $resultK1")
}
