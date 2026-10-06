package com.example.engine

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.Stack
import kotlin.math.*

object ExpressionEvaluator {

    enum class AngleMode {
        DEG, RAD
    }

    sealed class EvalResult {
        data class Success(val value: Double, val formatted: String) : EvalResult()
        data class Error(val message: String) : EvalResult()
    }

    private val FUNCTIONS = setOf(
        "sin", "cos", "tan", "asin", "acos", "atan",
        "log", "ln", "sqrt", "cbrt", "abs", "fact", "NOT"
    )

    fun evaluate(expression: String, angleMode: AngleMode = AngleMode.DEG): EvalResult {
        if (expression.isBlank()) return EvalResult.Success(0.0, "0")

        try {
            val normalized = normalizeExpression(expression)
            val rawTokens = tokenize(normalized)
            if (rawTokens.isEmpty()) return EvalResult.Success(0.0, "0")

            val tokens = preprocessPercent(rawTokens)
            val rpn = shuntingYard(tokens)
            val result = evaluateRpn(rpn, angleMode)

            if (result.isNaN()) return EvalResult.Error("Undefined result")
            if (result.isInfinite()) return EvalResult.Error("Division by zero or overflow")

            return EvalResult.Success(result, formatResult(result))
        } catch (e: ArithmeticException) {
            return EvalResult.Error(e.message ?: "Math error")
        } catch (e: IllegalArgumentException) {
            return EvalResult.Error(e.message ?: "Invalid syntax")
        } catch (e: Exception) {
            return EvalResult.Error("Invalid expression")
        }
    }

    private fun normalizeExpression(raw: String): String {
        return raw
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace("π", "PI")
            .replace("e", "E")
            .replace(" ", "")
    }

    private fun tokenize(input: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val n = input.length

        var prevToken: String? = null

        while (i < n) {
            val c = input[i]

            // Digit or decimal point
            if (c.isDigit() || c == '.') {
                val sb = StringBuilder()
                while (i < n && (input[i].isDigit() || input[i] == '.')) {
                    sb.append(input[i])
                    i++
                }
                // Check for scientific notation exponent: e.g. 1E13, 1.5e-4, 2E+8
                if (i < n && (input[i] == 'E' || input[i] == 'e')) {
                    val hasDigitAfterE = i + 1 < n && input[i + 1].isDigit()
                    val hasSignedDigitAfterE = (i + 1 < n && (input[i + 1] == '+' || input[i + 1] == '-')) &&
                            (i + 2 < n && input[i + 2].isDigit())
                    if (hasDigitAfterE || hasSignedDigitAfterE) {
                        sb.append(input[i]) // E or e
                        i++
                        if (i < n && (input[i] == '+' || input[i] == '-')) {
                            sb.append(input[i])
                            i++
                        }
                        while (i < n && input[i].isDigit()) {
                            sb.append(input[i])
                            i++
                        }
                    }
                }
                // Handle implicit multiplication before number (e.g., )2 or PI 2
                if (prevToken == ")" || prevToken == "PI" || prevToken == "E" || prevToken == "!") {
                    tokens.add("*")
                }
                val num = sb.toString()
                tokens.add(num)
                prevToken = num
                continue
            }

            // Check shift operators << and >>
            if (c == '<' && i + 1 < n && input[i + 1] == '<') {
                tokens.add("<<")
                prevToken = "<<"
                i += 2
                continue
            }
            if (c == '>' && i + 1 < n && input[i + 1] == '>') {
                tokens.add(">>")
                prevToken = ">>"
                i += 2
                continue
            }

            // Word or Constant or Function (sin, cos, tan, log, ln, sqrt, cbrt, abs, PI, E, AND, OR, XOR, NOT)
            if (c.isLetter()) {
                val sb = StringBuilder()
                while (i < n && input[i].isLetter()) {
                    sb.append(input[i])
                    i++
                }
                val word = sb.toString()

                if (word == "PI" || word == "E") {
                    if (prevToken != null && (prevToken.isNumber() || prevToken == ")" || prevToken == "PI" || prevToken == "E" || prevToken == "!")) {
                        tokens.add("*")
                    }
                    tokens.add(word)
                    prevToken = word
                } else if (word == "AND" || word == "OR" || word == "XOR") {
                    tokens.add(word)
                    prevToken = word
                } else if (FUNCTIONS.contains(word)) {
                    if (prevToken != null && (prevToken.isNumber() || prevToken == ")" || prevToken == "PI" || prevToken == "E" || prevToken == "!")) {
                        tokens.add("*")
                    }
                    tokens.add(word)
                    prevToken = word
                } else {
                    throw IllegalArgumentException("Unknown symbol: $word")
                }
                continue
            }

            // Unary minus/plus handling
            if (c == '+' || c == '-') {
                val isUnary = prevToken == null || prevToken == "(" || isOperator(prevToken) || FUNCTIONS.contains(prevToken)
                if (isUnary) {
                    if (c == '-') {
                        tokens.add("u-")
                        prevToken = "u-"
                    }
                    // ignore unary plus
                    i++
                    continue
                } else {
                    tokens.add(c.toString())
                    prevToken = c.toString()
                    i++
                    continue
                }
            }

            // Operators & Parentheses
            if (c == '*' || c == '/' || c == '%' || c == '^' || c == '(' || c == ')' || c == '!') {
                if (c == '(' && prevToken != null && (prevToken.isNumber() || prevToken == ")" || prevToken == "PI" || prevToken == "E" || prevToken == "!")) {
                    tokens.add("*")
                }
                tokens.add(c.toString())
                prevToken = c.toString()
                i++
                continue
            }

            i++
        }
        return tokens
    }

    private fun preprocessPercent(tokens: List<String>): List<String> {
        val currentTokens = tokens.toMutableList()
        var percentIdx = currentTokens.indexOf("%")

        while (percentIdx != -1) {
            if (percentIdx == 0) {
                throw IllegalArgumentException("Invalid syntax")
            }

            // Find the start of operand B preceding %
            val bEnd = percentIdx
            val bStart: Int
            if (currentTokens[percentIdx - 1] == ")") {
                var depth = 1
                var j = percentIdx - 2
                while (j >= 0 && depth > 0) {
                    if (currentTokens[j] == ")") depth++
                    else if (currentTokens[j] == "(") depth--
                    j--
                }
                bStart = j + 1
            } else {
                bStart = percentIdx - 1
            }

            val bTokens = currentTokens.subList(bStart, bEnd).toList()

            // Check if there is an operator preceding B
            val prevOpIdx = bStart - 1
            if (prevOpIdx >= 0 && (currentTokens[prevOpIdx] == "+" || currentTokens[prevOpIdx] == "-")) {
                // Preceded by + or - : percentage of the preceding expression A
                // Find start of A (preceding expression at the same parenthesis level)
                var depth = 0
                var j = prevOpIdx - 1
                while (j >= 0) {
                    if (currentTokens[j] == ")") depth++
                    else if (currentTokens[j] == "(") {
                        if (depth == 0) break
                        depth--
                    }
                    j--
                }
                val aStart = j + 1
                val aTokens = currentTokens.subList(aStart, prevOpIdx).toList()

                // Replace B % with ((A) * (B) / 100)
                val replacement = mutableListOf<String>()
                replacement.add("(")
                replacement.add("(")
                replacement.addAll(aTokens)
                replacement.add(")")
                replacement.add("*")
                replacement.add("(")
                replacement.addAll(bTokens)
                replacement.add(")")
                replacement.add("/")
                replacement.add("100")
                replacement.add(")")

                for (k in bEnd downTo bStart) {
                    currentTokens.removeAt(k)
                }
                currentTokens.addAll(bStart, replacement)
            } else {
                // Standalone percentage or after * / : B % -> ((B) / 100)
                val replacement = mutableListOf<String>()
                replacement.add("(")
                replacement.addAll(bTokens)
                replacement.add("/")
                replacement.add("100")
                replacement.add(")")

                for (k in bEnd downTo bStart) {
                    currentTokens.removeAt(k)
                }
                currentTokens.addAll(bStart, replacement)
            }

            percentIdx = currentTokens.indexOf("%")
        }

        return currentTokens
    }

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val opStack = Stack<String>()

        for (token in tokens) {
            when {
                token.isNumber() || token == "PI" || token == "E" -> {
                    output.add(token)
                }
                FUNCTIONS.contains(token) -> {
                    opStack.push(token)
                }
                token == "!" -> {
                    // Postfix factorial
                    output.add("!")
                }
                token == "u-" -> {
                    opStack.push("u-")
                }
                token == "(" -> {
                    opStack.push(token)
                }
                token == ")" -> {
                    while (opStack.isNotEmpty() && opStack.peek() != "(") {
                        output.add(opStack.pop())
                    }
                    if (opStack.isNotEmpty() && opStack.peek() == "(") {
                        opStack.pop()
                    }
                    if (opStack.isNotEmpty() && FUNCTIONS.contains(opStack.peek())) {
                        output.add(opStack.pop())
                    }
                }
                isOperator(token) -> {
                    while (opStack.isNotEmpty() && opStack.peek() != "(" &&
                        (precedence(opStack.peek()) > precedence(token) ||
                                (precedence(opStack.peek()) == precedence(token) && !isRightAssociative(token)))
                    ) {
                        output.add(opStack.pop())
                    }
                    opStack.push(token)
                }
            }
        }

        while (opStack.isNotEmpty()) {
            val top = opStack.pop()
            if (top != "(" && top != ")") {
                output.add(top)
            }
        }

        return output
    }

    private fun evaluateRpn(rpn: List<String>, angleMode: AngleMode): Double {
        val stack = Stack<Double>()

        for (token in rpn) {
            when {
                token == "PI" -> stack.push(Math.PI)
                token == "E" -> stack.push(Math.E)
                token.toDoubleOrNull() != null -> stack.push(token.toDouble())
                token == "u-" -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Invalid syntax")
                    stack.push(-stack.pop())
                }
                token == "!" -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Invalid syntax")
                    val v = stack.pop()
                    stack.push(factorial(v))
                }
                token == "%" -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Invalid syntax")
                    stack.push(stack.pop() / 100.0)
                }
                FUNCTIONS.contains(token) -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Invalid syntax")
                    val arg = stack.pop()
                    stack.push(evalFunction(token, arg, angleMode))
                }
                isOperator(token) -> {
                    if (stack.size < 2) throw IllegalArgumentException("Invalid syntax")
                    val b = stack.pop()
                    val a = stack.pop()
                    stack.push(evalBinaryOp(token, a, b))
                }
                else -> throw IllegalArgumentException("Unknown token $token")
            }
        }

        if (stack.size != 1) throw IllegalArgumentException("Invalid syntax")
        return stack.pop()
    }

    private fun evalFunction(func: String, arg: Double, angleMode: AngleMode): Double {
        val radArg = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg

        return when (func) {
            "sin" -> {
                val s = sin(radArg)
                if (abs(s) < 1e-15) 0.0 else s
            }
            "cos" -> {
                val c = cos(radArg)
                if (abs(c) < 1e-15) 0.0 else c
            }
            "tan" -> {
                val t = tan(radArg)
                if (abs(t) > 1e14) throw ArithmeticException("Tangent undefined")
                if (abs(t) < 1e-15) 0.0 else t
            }
            "asin" -> {
                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("asin out of domain [-1, 1]")
                val res = asin(arg)
                if (angleMode == AngleMode.DEG) Math.toDegrees(res) else res
            }
            "acos" -> {
                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("acos out of domain [-1, 1]")
                val res = acos(arg)
                if (angleMode == AngleMode.DEG) Math.toDegrees(res) else res
            }
            "atan" -> {
                val res = atan(arg)
                if (angleMode == AngleMode.DEG) Math.toDegrees(res) else res
            }
            "log" -> {
                if (arg <= 0) throw ArithmeticException("log argument must be > 0")
                log10(arg)
            }
            "ln" -> {
                if (arg <= 0) throw ArithmeticException("ln argument must be > 0")
                ln(arg)
            }
            "sqrt" -> {
                if (arg < 0) throw ArithmeticException("Cannot take root of negative number")
                sqrt(arg)
            }
            "cbrt" -> cbrt(arg)
            "abs" -> abs(arg)
            "fact" -> factorial(arg)
            "NOT" -> (arg.toLong().inv()).toDouble()
            else -> throw IllegalArgumentException("Unknown function $func")
        }
    }

    private fun evalBinaryOp(op: String, a: Double, b: Double): Double {
        return when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> {
                if (abs(b) < 1e-15) throw ArithmeticException("Division by zero")
                a / b
            }
            "^" -> a.pow(b)
            "AND" -> (a.toLong() and b.toLong()).toDouble()
            "OR" -> (a.toLong() or b.toLong()).toDouble()
            "XOR" -> (a.toLong() xor b.toLong()).toDouble()
            "<<" -> (a.toLong() shl b.toInt()).toDouble()
            ">>" -> (a.toLong() shr b.toInt()).toDouble()
            else -> throw IllegalArgumentException("Unknown operator $op")
        }
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n > 170) throw ArithmeticException("Factorial out of supported range [0..170]")
        if (n == floor(n)) {
            var result = 1.0
            val intN = n.toLong()
            for (i in 2..intN) {
                result *= i
            }
            return result
        }
        // Lanczos approximation for gamma(n + 1)
        return gamma(n + 1)
    }

    private fun gamma(x: Double): Double {
        val g = 7
        val c = doubleArrayOf(
            0.99999999999980993,
            676.5203681218851,
            -1259.1392167224028,
            771.32342877765313,
            -176.61502916214059,
            12.507343278686905,
            -0.138571095831171,
            9.9843695780195716e-6,
            1.5056327351493116e-7
        )
        var z = x - 1
        var acc = c[0]
        for (i in 1 until g + 2) {
            acc += c[i] / (z + i)
        }
        val t = z + g + 0.5
        return sqrt(2 * Math.PI) * t.pow(z + 0.5) * exp(-t) * acc
    }

    private fun isOperator(token: String): Boolean {
        return token == "+" || token == "-" || token == "*" || token == "/" || token == "^" ||
                token == "AND" || token == "OR" || token == "XOR" || token == "<<" || token == ">>"
    }

    private fun precedence(op: String): Int {
        return when (op) {
            "^" -> 6
            "u-" -> 5
            "*", "/" -> 4
            "+", "-" -> 3
            "<<", ">>" -> 2
            "AND", "OR", "XOR" -> 1
            else -> 0
        }
    }

    private fun isRightAssociative(op: String): Boolean {
        return op == "^" || op == "u-"
    }

    private fun String.isNumber(): Boolean {
        return this.toDoubleOrNull() != null
    }

    fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return value.toString()

        // Handle integers
        if (value == floor(value) && abs(value) < 1e12) {
            return value.toLong().toString()
        }

        // Clean small inaccuracies
        val bd = BigDecimal(value).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros()
        val plain = bd.toPlainString()

        if (plain.length > 15 || abs(value) >= 1e12 || (abs(value) < 1e-6 && value != 0.0)) {
            val symbols = DecimalFormatSymbols(Locale.US)
            val formatter = DecimalFormat("0.######E0", symbols)
            return formatter.format(value)
        }

        return plain
    }
}
