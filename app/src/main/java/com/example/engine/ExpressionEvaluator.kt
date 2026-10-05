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
        "log", "ln", "sqrt", "cbrt", "abs", "fact"
    )

    fun evaluate(expression: String, angleMode: AngleMode = AngleMode.DEG): EvalResult {
        if (expression.isBlank()) return EvalResult.Success(0.0, "0")

        try {
            val normalized = normalizeExpression(expression)
            val tokens = tokenize(normalized)
            if (tokens.isEmpty()) return EvalResult.Success(0.0, "0")

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
                // Handle implicit multiplication before number (e.g., )2 or PI 2
                if (prevToken == ")" || prevToken == "PI" || prevToken == "E" || prevToken == "!") {
                    tokens.add("*")
                }
                val num = sb.toString()
                tokens.add(num)
                prevToken = num
                continue
            }

            // Word or Constant or Function (sin, cos, tan, log, ln, sqrt, cbrt, abs, PI, E)
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
            "%" -> {
                if (abs(b) < 1e-15) throw ArithmeticException("Modulo by zero")
                a % b
            }
            "^" -> a.pow(b)
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
        return token == "+" || token == "-" || token == "*" || token == "/" || token == "%" || token == "^"
    }

    private fun precedence(op: String): Int {
        return when (op) {
            "u-" -> 5
            "^" -> 4
            "*", "/", "%" -> 3
            "+", "-" -> 2
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
