package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.engine.ExpressionEvaluator

class CalculatorWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_CALC_KEY) {
            val appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            val key = intent.getStringExtra(EXTRA_KEY) ?: return

            val prefs = getPrefs(context)
            val expKey = "exp_$appWidgetId"
            val resKey = "res_$appWidgetId"

            var currentExp = prefs.getString(expKey, "") ?: ""
            var currentRes = prefs.getString(resKey, "0") ?: "0"

            when (key) {
                KEY_CLEAR -> {
                    currentExp = ""
                    currentRes = "0"
                }
                KEY_BACKSPACE -> {
                    if (currentExp.isNotEmpty()) {
                        currentExp = currentExp.dropLast(1)
                        if (currentExp.isEmpty()) {
                            currentRes = "0"
                        } else {
                            val eval = ExpressionEvaluator.evaluate(currentExp)
                            if (eval is ExpressionEvaluator.EvalResult.Success) {
                                currentRes = eval.formatted
                            }
                        }
                    }
                }
                KEY_EQUALS -> {
                    if (currentExp.isNotEmpty()) {
                        when (val eval = ExpressionEvaluator.evaluate(currentExp)) {
                            is ExpressionEvaluator.EvalResult.Success -> {
                                currentRes = eval.formatted
                                currentExp = eval.formatted
                            }
                            is ExpressionEvaluator.EvalResult.Error -> {
                                currentRes = "Error"
                            }
                        }
                    }
                }
                else -> {
                    // Operator or digit input
                    currentExp = handleKeyInput(currentExp, key)
                    val eval = ExpressionEvaluator.evaluate(currentExp)
                    if (eval is ExpressionEvaluator.EvalResult.Success) {
                        currentRes = eval.formatted
                    }
                }
            }

            prefs.edit()
                .putString(expKey, currentExp)
                .putString(resKey, currentRes)
                .apply()

            val appWidgetManager = AppWidgetManager.getInstance(context)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            } else {
                val ids = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, CalculatorWidgetProvider::class.java)
                )
                for (id in ids) {
                    updateAppWidget(context, appWidgetManager, id)
                }
            }
        }
    }

    private fun handleKeyInput(currentExp: String, key: String): String {
        val opChars = setOf('+', '-', '−', '×', '÷', '^', '.')
        if (key in setOf("+", "-", "−", "×", "÷")) {
            if (currentExp.isEmpty()) {
                return if (key == "-" || key == "−") key else ""
            }
            // Collapse trailing operators if stacked
            var stripped = currentExp
            while (stripped.isNotEmpty() && stripped.last() in opChars) {
                val cluster = stripped.takeLast(2)
                if ((key == "-" || key == "−") && (cluster == "×" || cluster == "÷")) {
                    break
                }
                stripped = stripped.dropLast(1)
            }
            return "$stripped$key"
        }
        if (key == ".") {
            if (currentExp.isEmpty() || currentExp.last() in opChars || currentExp.last() == '(') {
                return "${currentExp}0."
            }
            var numLen = 0
            while (numLen < currentExp.length && (currentExp[currentExp.length - 1 - numLen].isDigit() || currentExp[currentExp.length - 1 - numLen] == '.')) {
                numLen++
            }
            val literal = currentExp.takeLast(numLen)
            if (literal.contains('.')) return currentExp
            return "$currentExp."
        }
        return "$currentExp$key"
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = getPrefs(context)
        val editor = prefs.edit()
        for (id in appWidgetIds) {
            editor.remove("exp_$id")
            editor.remove("res_$id")
        }
        editor.apply()
    }

    companion object {
        const val ACTION_CALC_KEY = "com.example.widget.ACTION_CALC_KEY"
        const val EXTRA_KEY = "extra_calc_key"

        const val KEY_CLEAR = "C"
        const val KEY_BACKSPACE = "DEL"
        const val KEY_EQUALS = "="

        private fun getPrefs(context: Context): SharedPreferences {
            return context.getSharedPreferences("widget_calculator_prefs", Context.MODE_PRIVATE)
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = getPrefs(context)
            val exp = prefs.getString("exp_$appWidgetId", "") ?: ""
            val res = prefs.getString("res_$appWidgetId", "0") ?: "0"

            val views = RemoteViews(context.packageName, R.layout.widget_calculator)
            views.setTextViewText(R.id.widget_display_expression, exp)
            views.setTextViewText(R.id.widget_display_result, res)

            // Open main app when display area is tapped
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_display_container, openAppPendingIntent)

            // Bind button intents
            val buttonMap = mapOf(
                R.id.widget_btn_c to KEY_CLEAR,
                R.id.widget_btn_open_paren to "(",
                R.id.widget_btn_close_paren to ")",
                R.id.widget_btn_div to "÷",
                R.id.widget_btn_7 to "7",
                R.id.widget_btn_8 to "8",
                R.id.widget_btn_9 to "9",
                R.id.widget_btn_mul to "×",
                R.id.widget_btn_4 to "4",
                R.id.widget_btn_5 to "5",
                R.id.widget_btn_6 to "6",
                R.id.widget_btn_sub to "−",
                R.id.widget_btn_1 to "1",
                R.id.widget_btn_2 to "2",
                R.id.widget_btn_3 to "3",
                R.id.widget_btn_add to "+",
                R.id.widget_btn_0 to "0",
                R.id.widget_btn_dot to ".",
                R.id.widget_btn_backspace to KEY_BACKSPACE,
                R.id.widget_btn_eq to KEY_EQUALS
            )

            for ((viewId, keyVal) in buttonMap) {
                val clickIntent = Intent(context, CalculatorWidgetProvider::class.java).apply {
                    action = ACTION_CALC_KEY
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra(EXTRA_KEY, keyVal)
                }
                val reqCode = appWidgetId * 100 + viewId
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    reqCode,
                    clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(viewId, pendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
