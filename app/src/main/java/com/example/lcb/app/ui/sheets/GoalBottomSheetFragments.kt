package com.example.lcb.app.ui.sheets

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.lcb.app.LcbAppViewModel
import com.example.lcb.app.R
import com.example.lcb.app.data.StepDailyRecord
import com.example.lcb.app.ui.localeForLanguageCode
import com.example.lcb.app.ui.localizedContext
import com.example.lcb.app.ui.resolveAppLanguageCode
import com.example.lcb.app.ui.SystemLanguageCode
import com.example.lcb.app.ui.settings.LanguageOption
import com.example.lcb.app.ui.settings.languageOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

abstract class LocalizedBottomSheetDialogFragment : BottomSheetDialogFragment() {
    protected val viewModel: LcbAppViewModel by activityViewModels()

    protected val localizedContext: Context
        get() = requireContext().localizedContext(resolveCurrentLanguageCode())

    protected fun resolveCurrentLanguageCode(): String {
        return resolveAppLanguageCode(viewModel.language.value, requireContext().resources.configuration)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog ?: return
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.background = ColorDrawable(Color.TRANSPARENT)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
}

abstract class LocalizedDialogFragment : DialogFragment() {
    protected val viewModel: LcbAppViewModel by activityViewModels()

    protected val localizedContext: Context
        get() = requireContext().localizedContext(resolveCurrentLanguageCode())

    protected val appLocale: Locale
        get() = localeForLanguageCode(resolveCurrentLanguageCode()).takeIf { it != Locale.ROOT } ?: Locale.getDefault()

    protected fun resolveCurrentLanguageCode(): String {
        return resolveAppLanguageCode(viewModel.language.value, requireContext().resources.configuration)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setDimAmount(0.58f)
            setLayout(min(resources.displayMetrics.widthPixels - dp(60), dp(315)), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}

class StepCorrectionDialogFragment : LocalizedDialogFragment() {
    private lateinit var selectedDate: LocalDate
    private lateinit var today: LocalDate
    private lateinit var stepInput: EditText
    private lateinit var dateValue: TextView
    private var records: Map<String, StepDailyRecord> = emptyMap()

    override fun onCreateView(inflater: android.view.LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = localizedContext
        val data = viewModel.homeData.value
        today = runCatching { LocalDate.parse(data.today) }.getOrDefault(LocalDate.now())
        selectedDate = today
        records = (data.stepHistory + StepDailyRecord(data.today, data.todaySteps, data.stepGoal)).associateBy { it.date }

        stepInput = EditText(context).apply {
            setText(records[selectedDate.toString()]?.steps?.toString() ?: "0")
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(InputFilter.LengthFilter(7))
            background = null
            setPadding(0)
            selectAll()
        }
        dateValue = TextView(context).apply {
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            includeFontPadding = false
        }

        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.rgb(43, 42, 51), 16f, strokeColor = Color.rgb(99, 101, 117))
            setPadding(dp(16), dp(20), dp(16), dp(20))
            addView(title(context, context.getString(R.string.home_edit_steps_title), textSize = 18f, gravity = Gravity.CENTER))
            addView(spacer(context, 10))
            addView(label(context, context.getString(R.string.home_edit_steps_subtitle), gravity = Gravity.CENTER))
            addView(spacer(context, 20))
            addView(correctionRow(context, context.getString(R.string.home_edit_steps_date), dateValue).apply {
                setOnClickListener {
                    if (childFragmentManager.findFragmentByTag(StepDatePickerTag) == null) {
                        StepDatePickerDialogFragment
                            .newInstance(selectedDate, today)
                            .show(childFragmentManager, StepDatePickerTag)
                    }
                }
            })
            addView(spacer(context, 16))
            addView(correctionRow(context, context.getString(R.string.home_edit_steps_steps), stepInput))
            addView(spacer(context, 24))
            addView(actionRow(
                context = context,
                cancelText = context.getString(R.string.settings_cancel),
                confirmText = context.getString(R.string.home_edit_steps_save),
                confirmEnabled = { stepInput.text.toString().toIntOrNull()?.let { it >= 0 } == true },
                onCancel = { dismiss() },
                onConfirm = {
                    stepInput.text.toString().toIntOrNull()?.takeIf { it >= 0 }?.let {
                        viewModel.setStepsForDate(selectedDate.toString(), it)
                    }
                    dismiss()
                },
            ))
            refreshDate()
        }
    }

    fun onStepDateSelected(date: LocalDate) {
        selectedDate = date.coerceAtMost(today)
        refreshDate()
    }

    private fun refreshDate() {
        val context = localizedContext
        dateValue.text = formatCorrectionDate(context, selectedDate, today, appLocale)
        stepInput.setText(records[selectedDate.toString()]?.steps?.toString() ?: "0")
        stepInput.selectAll()
    }

    private companion object {
        const val StepDatePickerTag = "step_date_picker_dialog"
    }
}

class StepDatePickerDialogFragment : LocalizedDialogFragment() {
    private var selectedDate: LocalDate = LocalDate.now()
    private var maxDate: LocalDate = LocalDate.now()
    private var visibleMonth: YearMonth = YearMonth.now()
    private lateinit var monthTitle: TextView
    private lateinit var nextButton: TextView
    private lateinit var dayGrid: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedDate = arguments?.getString(ArgSelectedDate)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now()
        maxDate = arguments?.getString(ArgMaxDate)?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now()
        if (selectedDate > maxDate) selectedDate = maxDate
        visibleMonth = YearMonth.from(selectedDate)
    }

    override fun onCreateView(inflater: android.view.LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = localizedContext
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            background = rounded(Color.rgb(43, 42, 51), 16f, strokeColor = Color.rgb(99, 101, 117))
            setPadding(dp(16), dp(18), dp(16), dp(18))
            addView(title(context, context.getString(R.string.home_edit_steps_date), textSize = 18f, gravity = Gravity.CENTER))
            addView(spacer(context, 16))
            addView(monthHeader(context))
            addView(spacer(context, 10))
            addView(weekdayGrid(context))
            dayGrid = GridLayout(context).apply {
                columnCount = DaysPerWeek
                rowCount = CalendarRows
                alignmentMode = GridLayout.ALIGN_BOUNDS
                useDefaultMargins = false
                layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
            }
            addView(dayGrid)
            addView(spacer(context, 18))
            addView(actionRow(
                context = context,
                cancelText = context.getString(R.string.settings_cancel),
                confirmText = context.getString(R.string.settings_confirm),
                confirmEnabled = { true },
                onCancel = { dismiss() },
                onConfirm = {
                    (parentFragment as? StepCorrectionDialogFragment)?.onStepDateSelected(selectedDate)
                    dismiss()
                },
            ))
            renderCalendar()
        }
    }

    private fun monthHeader(context: Context): LinearLayout {
        val previousButton = navigationButton(context, "<") {
            visibleMonth = visibleMonth.minusMonths(1)
            renderCalendar()
        }
        monthTitle = TextView(context).apply {
            textSize = 15f
            setTextColor(Color.WHITE)
            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
            includeFontPadding = false
        }
        nextButton = navigationButton(context, ">") {
            val nextMonth = visibleMonth.plusMonths(1)
            if (!nextMonth.isAfter(YearMonth.from(maxDate))) {
                visibleMonth = nextMonth
                renderCalendar()
            }
        }
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(previousButton, LinearLayout.LayoutParams(dp(42), dp(36)))
            addView(monthTitle, LinearLayout.LayoutParams(0, dp(36), 1f))
            addView(nextButton, LinearLayout.LayoutParams(dp(42), dp(36)))
        }
    }

    private fun weekdayGrid(context: Context): GridLayout {
        return GridLayout(context).apply {
            columnCount = DaysPerWeek
            useDefaultMargins = false
            layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
            val firstMonday = LocalDate.of(2024, 1, 1)
            repeat(DaysPerWeek) { index ->
                val label = firstMonday.plusDays(index.toLong()).format(DateTimeFormatter.ofPattern("EEE", appLocale))
                addView(TextView(context).apply {
                    text = label.take(2)
                    textSize = 11f
                    setTextColor(MutedText)
                    gravity = Gravity.CENTER
                    includeFontPadding = false
                }, calendarCellParams(row = 0, column = index, height = 22))
            }
        }
    }

    private fun renderCalendar() {
        val context = localizedContext
        monthTitle.text = visibleMonth.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy", appLocale))
        val nextMonthDisabled = !visibleMonth.plusMonths(1).isBefore(YearMonth.from(maxDate).plusMonths(1))
        nextButton.alpha = if (nextMonthDisabled) 0.35f else 1f
        dayGrid.removeAllViews()

        val firstDay = visibleMonth.atDay(1)
        val leadingBlankCount = firstDay.dayOfWeek.value - 1
        val dayCount = visibleMonth.lengthOfMonth()
        repeat(CalendarRows * DaysPerWeek) { index ->
            val dayOfMonth = index - leadingBlankCount + 1
            if (dayOfMonth in 1..dayCount) {
                val date = visibleMonth.atDay(dayOfMonth)
                dayGrid.addView(dayCell(context, date), calendarCellParams(index / DaysPerWeek, index % DaysPerWeek))
            } else {
                dayGrid.addView(View(context), calendarCellParams(index / DaysPerWeek, index % DaysPerWeek))
            }
        }
    }

    private fun dayCell(context: Context, date: LocalDate): TextView {
        val disabled = date > maxDate
        val selected = date == selectedDate
        return TextView(context).apply {
            text = date.dayOfMonth.toString()
            textSize = if (selected) 15f else 13f
            setTextColor(
                when {
                    disabled -> Color.argb(70, 255, 255, 255)
                    selected -> Color.WHITE
                    else -> Color.rgb(222, 222, 226)
                },
            )
            setTypeface(Typeface.DEFAULT, if (selected) Typeface.BOLD else Typeface.NORMAL)
            gravity = Gravity.CENTER
            includeFontPadding = false
            background = if (selected) rounded(AccentBlue, 100f) else null
            isEnabled = !disabled
            // This calendar intentionally avoids platform DatePickerDialog, whose themed
            // resources are unstable on several OEM ROMs when opened from a localized dialog.
            setOnClickListener {
                selectedDate = date
                renderCalendar()
            }
        }
    }

    private fun navigationButton(context: Context, textValue: String, onClick: () -> Unit): TextView {
        return TextView(context).apply {
            text = textValue
            textSize = 18f
            setTextColor(Color.WHITE)
            setTypeface(Typeface.DEFAULT, Typeface.BOLD)
            gravity = Gravity.CENTER
            includeFontPadding = false
            background = rounded(Color.rgb(55, 54, 66), 100f, strokeColor = Color.rgb(87, 90, 105))
            setOnClickListener { onClick() }
        }
    }

    companion object {
        private const val ArgSelectedDate = "selected_date"
        private const val ArgMaxDate = "max_date"
        private const val DaysPerWeek = 7
        private const val CalendarRows = 6

        fun newInstance(selectedDate: LocalDate, maxDate: LocalDate): StepDatePickerDialogFragment {
            return StepDatePickerDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ArgSelectedDate, selectedDate.toString())
                    putString(ArgMaxDate, maxDate.toString())
                }
            }
        }
    }
}

class LanguageBottomSheetFragment : LocalizedBottomSheetDialogFragment() {
    private var selectedLanguage: String = SystemLanguageCode

    override fun onCreateView(inflater: android.view.LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = localizedContext
        selectedLanguage = viewModel.language.value
        return sheetContainer(context, bottomPadding = 18).apply {
            addView(handle(context))
            addView(title(context, context.getString(R.string.settings_select_language), textSize = 17f, gravity = Gravity.CENTER))
            addView(spacer(context, 14))
            val optionContainer = vertical(context).apply {
                background = rounded(Color.rgb(36, 36, 43), 16f, strokeColor = Color.argb(15, 255, 255, 255))
                setPadding(dp(6))
            }
            val optionRows = mutableListOf<TextView>()
            val options = languageOptions(context)
            options.forEach { option ->
                val row = TextView(context).apply {
                    text = option.label
                    textSize = 14f
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(14), 0, dp(14), 0)
                    layoutParams = LinearLayout.LayoutParams(matchParent, dp(38))
                    setOnClickListener {
                        selectedLanguage = option.code
                        renderLanguageRows(optionRows, options)
                    }
                }
                optionRows += row
                optionContainer.addView(row)
            }
            addView(optionContainer)
            renderLanguageRows(optionRows, options)
            addView(spacer(context, 14))
            addView(actionRow(
                context = context,
                cancelText = context.getString(R.string.settings_cancel),
                confirmText = context.getString(R.string.settings_confirm),
                confirmEnabled = { true },
                onCancel = { dismiss() },
                onConfirm = {
                    viewModel.setLanguage(selectedLanguage)
                    dismiss()
                },
            ))
        }
    }

    private fun renderLanguageRows(rows: List<TextView>, options: List<LanguageOption>) {
        rows.forEachIndexed { index, row ->
            val option = options[index]
            val selected = option.code == selectedLanguage
            row.text = if (selected) "${option.label}   ✓" else option.label
            row.setTextColor(if (selected) Color.WHITE else MutedText)
            row.setTypeface(Typeface.DEFAULT, if (selected) Typeface.BOLD else Typeface.NORMAL)
        }
    }
}

class StepGoalBottomSheetFragment : LocalizedBottomSheetDialogFragment() {
    override fun onCreateView(inflater: android.view.LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = localizedContext
        val input = EditText(context).apply {
            setText(viewModel.homeData.value.stepGoal.toString())
            selectAll()
            textSize = 28f
            setTextColor(Color.WHITE)
            setHintTextColor(MutedText)
            typeface = Typeface.DEFAULT_BOLD
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(InputFilter.LengthFilter(7))
            background = rounded(Color.rgb(31, 31, 37), 16f, strokeColor = Color.argb(82, 29, 107, 242))
            setPadding(dp(14), 0, dp(14), 0)
            layoutParams = LinearLayout.LayoutParams(matchParent, dp(64))
        }
        return sheetContainer(context, topRadius = 28f).apply {
            addView(handle(context, top = 12, bottom = 10))
            addView(title(context, context.getString(R.string.home_set_step_goal), textSize = 20f))
            addView(label(context, context.getString(R.string.home_step_goal_label)))
            addView(spacer(context, 18))
            addView(input)
            addView(spacer(context, 20))
            addView(actionRow(
                context = context,
                cancelText = context.getString(R.string.settings_cancel),
                confirmText = context.getString(R.string.settings_confirm),
                confirmEnabled = { input.text.toString().toIntOrNull()?.let { it >= 1 } == true },
                onCancel = { dismiss() },
                onConfirm = {
                    input.text.toString().toIntOrNull()?.takeIf { it >= 1 }?.let(viewModel::setStepGoal)
                    dismiss()
                },
            ))
            input.post {
                input.requestFocus()
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                    ?.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }
}

class WaterGoalBottomSheetFragment : LocalizedBottomSheetDialogFragment() {
    private var selectedGoal = DefaultWaterGoal

    override fun onCreateView(inflater: android.view.LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = localizedContext
        selectedGoal = WaterGoalOptions.minByOrNull { abs(it - viewModel.hydrateData.value.waterGoalMl) } ?: DefaultWaterGoal
        return sheetContainer(context, topRadius = 22f, bottomPadding = 20).apply {
            val skip = TextView(context).apply {
                text = context.getString(R.string.hydrate_goal_skip)
                textSize = 12f
                setTextColor(MutedText)
                gravity = Gravity.END
                layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
                setOnClickListener { dismiss() }
            }
            addView(skip)
            addView(spacer(context, 16))
            addView(title(context, context.getString(R.string.hydrate_goal_title), textSize = 18f, gravity = Gravity.CENTER))
            addView(spacer(context, 10))
            addView(label(context, context.getString(R.string.hydrate_goal_recommendation, RecommendedWaterRange), gravity = Gravity.CENTER))
            addView(spacer(context, 20))
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
            }
            val buttons = mutableListOf<TextView>()
            WaterGoalOptions.forEach { goal ->
                val button = TextView(context).apply {
                    text = String.format(Locale.US, "%,d", goal)
                    textSize = 14f
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply {
                        if (goal != WaterGoalOptions.first()) leftMargin = dp(8)
                    }
                    setOnClickListener {
                        selectedGoal = goal
                        renderGoalButtons(buttons)
                    }
                }
                buttons += button
                row.addView(button)
            }
            addView(row)
            renderGoalButtons(buttons)
            addView(spacer(context, 24))
            addView(primaryButton(context, context.getString(R.string.hydrate_goal_next)) {
                viewModel.setWaterGoalMl(selectedGoal)
                dismiss()
            })
        }
    }

    private fun renderGoalButtons(buttons: List<TextView>) {
        buttons.forEachIndexed { index, button ->
            val selected = WaterGoalOptions[index] == selectedGoal
            button.background = rounded(if (selected) Color.rgb(21, 60, 100) else Color.rgb(44, 44, 50), 100f)
            button.setTextColor(if (selected) AccentBlue else Color.rgb(161, 161, 161))
            button.setTypeface(Typeface.DEFAULT, if (selected) Typeface.BOLD else Typeface.NORMAL)
        }
    }
}

class WeightEntryBottomSheetFragment : LocalizedBottomSheetDialogFragment() {
    private var unit = WeightUnit.Kg
    private var pickerWeightTenths = DefaultWeightTenthsKg

    override fun onCreateView(inflater: android.view.LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = localizedContext
        pickerWeightTenths = viewModel.weightData.value.weightRecords.maxByOrNull { it.date }?.weightTenthsKg ?: DefaultWeightTenthsKg
        val wholePicker = WeightWheelPickerView(context)
        val decimalPicker = WeightWheelPickerView(context)
        val lbsTab = TextView(context)
        val kgTab = TextView(context)

        fun currentUnitTenths(): Int = pickerWeightTenths.coerceIn(unit.minTenths, unit.maxTenths)
        fun syncPickers() {
            val current = currentUnitTenths()
            wholePicker.setRange(unit.wholeRange.first, unit.wholeRange.last)
            wholePicker.setValue((current / 10).coerceIn(unit.wholeRange), notify = false)
            decimalPicker.setRange(0, 9)
            decimalPicker.setValue(current % 10, notify = false)
        }
        fun renderTabs() {
            listOf(WeightUnit.Lbs to lbsTab, WeightUnit.Kg to kgTab).forEach { (tabUnit, tab) ->
                val selected = tabUnit == unit
                tab.background = rounded(if (selected) AccentBlue else Color.TRANSPARENT, 100f)
                tab.setTextColor(if (selected) Color.WHITE else Color.rgb(217, 217, 217))
                tab.setTypeface(Typeface.DEFAULT, if (selected) Typeface.BOLD else Typeface.NORMAL)
            }
        }

        wholePicker.onValueChanged = { newValue ->
            pickerWeightTenths = (newValue * 10 + decimalPicker.value).coerceIn(unit.minTenths, unit.maxTenths)
        }
        decimalPicker.onValueChanged = { newValue ->
            pickerWeightTenths = (wholePicker.value * 10 + newValue).coerceIn(unit.minTenths, unit.maxTenths)
        }

        return sheetContainer(context, topRadius = 18f, bottomPadding = 42).apply {
            addView(handle(context, width = 34, height = 4, top = 9, bottom = 14))
            addView(title(context, context.getString(R.string.home_weight_current), textSize = 18f, gravity = Gravity.CENTER))
            addView(spacer(context, 18))
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                background = rounded(Color.rgb(66, 66, 68), 40f)
                layoutParams = LinearLayout.LayoutParams(dp(116), dp(28)).apply { gravity = Gravity.CENTER_HORIZONTAL }
                lbsTab.configureTab(context.getString(R.string.unit_lbs)) {
                    if (unit != WeightUnit.Lbs) {
                        pickerWeightTenths = WeightUnit.Lbs.fromKgTenths(unit.toKgTenths(pickerWeightTenths))
                        unit = WeightUnit.Lbs
                        renderTabs()
                        syncPickers()
                    }
                }
                kgTab.configureTab(context.getString(R.string.unit_kg)) {
                    if (unit != WeightUnit.Kg) {
                        pickerWeightTenths = WeightUnit.Kg.fromKgTenths(unit.toKgTenths(pickerWeightTenths))
                        unit = WeightUnit.Kg
                        renderTabs()
                        syncPickers()
                    }
                }
                addView(lbsTab)
                addView(kgTab)
            })
            addView(spacer(context, 20))
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(matchParent, dp(140))
                addView(wholePicker, LinearLayout.LayoutParams(dp(92), matchParent))
                addView(TextView(context).apply {
                    text = "·"
                    textSize = 32f
                    setTextColor(Color.WHITE)
                    gravity = Gravity.CENTER
                }, LinearLayout.LayoutParams(dp(44), matchParent))
                addView(decimalPicker, LinearLayout.LayoutParams(dp(92), matchParent))
            })
            addView(spacer(context, 20))
            addView(label(context, context.getString(R.string.home_weight_privacy), gravity = Gravity.CENTER))
            addView(spacer(context, 28))
            addView(actionRow(
                context = context,
                cancelText = context.getString(R.string.settings_cancel),
                confirmText = context.getString(R.string.home_edit_steps_save),
                confirmEnabled = { true },
                onCancel = { dismiss() },
                onConfirm = {
                    val kgTenths = unit.toKgTenths(pickerWeightTenths).coerceIn(300, 2500)
                    viewModel.setWeightForDate(LocalDate.now().toString(), kgTenths)
                    dismiss()
                },
            ))
            renderTabs()
            syncPickers()
        }
    }
}

private fun sheetContainer(context: Context, topRadius: Float = 22f, bottomPadding: Int = 24): LinearLayout {
    return LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        background = rounded(Color.rgb(30, 30, 36), topRadius, topOnly = true)
        setPadding(dp(20), 0, dp(20), dp(bottomPadding))
        layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
    }
}

private fun handle(
    context: Context,
    top: Int = 10,
    bottom: Int = 12,
    width: Int = 44,
    height: Int = 5,
): View {
    return View(context).apply {
        background = rounded(Color.argb(46, 255, 255, 255), 100f)
        layoutParams = LinearLayout.LayoutParams(dp(width), dp(height)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            topMargin = dp(top)
            bottomMargin = dp(bottom)
        }
    }
}

private fun title(context: Context, text: String, textSize: Float, gravity: Int = Gravity.START): TextView {
    return TextView(context).apply {
        this.text = text
        this.textSize = textSize
        setTextColor(Color.WHITE)
        setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        this.gravity = gravity
        includeFontPadding = false
        layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
    }
}

private fun label(context: Context, text: String, gravity: Int = Gravity.START): TextView {
    return TextView(context).apply {
        this.text = text
        textSize = 13f
        setTextColor(MutedText)
        this.gravity = gravity
        includeFontPadding = false
        layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
    }
}

private fun correctionRow(context: Context, labelText: String, valueView: TextView): LinearLayout {
    return LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        background = rounded(Color.rgb(55, 54, 66), 12f, strokeColor = Color.rgb(87, 90, 105))
        setPadding(dp(16), 0, dp(16), 0)
        layoutParams = LinearLayout.LayoutParams(matchParent, dp(43))
        addView(TextView(context).apply {
            text = labelText
            textSize = 14f
            setTextColor(Color.rgb(161, 161, 161))
            includeFontPadding = false
            gravity = Gravity.CENTER_VERTICAL
        }, LinearLayout.LayoutParams(wrapContent, matchParent))
        addView(valueView.apply {
            includeFontPadding = false
        }, LinearLayout.LayoutParams(0, matchParent, 1f))
    }
}

private fun calendarCellParams(row: Int, column: Int, height: Int = 38): GridLayout.LayoutParams {
    return GridLayout.LayoutParams(GridLayout.spec(row), GridLayout.spec(column, 1f)).apply {
        width = 0
        this.height = dp(height)
        setMargins(dp(2), dp(2), dp(2), dp(2))
    }
}

private fun actionRow(
    context: Context,
    cancelText: String,
    confirmText: String,
    confirmEnabled: () -> Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
): LinearLayout {
    return LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
        addView(actionButton(context, cancelText, Color.rgb(42, 42, 48), Color.WHITE, onCancel), LinearLayout.LayoutParams(0, dp(48), 1f))
        addView(actionButton(context, confirmText, AccentBlue, Color.WHITE) {
            if (confirmEnabled()) onConfirm()
        }, LinearLayout.LayoutParams(0, dp(48), 1f).apply { leftMargin = dp(12) })
    }
}

private fun formatCorrectionDate(context: Context, date: LocalDate, today: LocalDate, locale: Locale): String {
    val dateText = date.format(DateTimeFormatter.ofPattern("MMM d", locale))
    return if (date == today) {
        context.getString(R.string.home_edit_steps_today_date, dateText)
    } else {
        date.format(DateTimeFormatter.ofPattern("EEE, MMM d", locale))
    }
}

private fun actionButton(context: Context, text: String, bg: Int, fg: Int, onClick: () -> Unit): TextView {
    return TextView(context).apply {
        this.text = text
        textSize = 16f
        setTextColor(fg)
        setTypeface(Typeface.DEFAULT, Typeface.BOLD)
        gravity = Gravity.CENTER
        background = rounded(bg, 100f)
        setOnClickListener { onClick() }
    }
}

private fun primaryButton(context: Context, text: String, onClick: () -> Unit): TextView {
    return actionButton(context, text, AccentBlue, Color.WHITE, onClick).apply {
        layoutParams = LinearLayout.LayoutParams(matchParent, dp(42))
    }
}

private fun TextView.configureTab(textValue: String, onClick: () -> Unit) {
    text = textValue
    textSize = 13f
    gravity = Gravity.CENTER
    includeFontPadding = false
    layoutParams = LinearLayout.LayoutParams(0, matchParent, 1f)
    setOnClickListener { onClick() }
}

private fun vertical(context: Context): LinearLayout = LinearLayout(context).apply {
    orientation = LinearLayout.VERTICAL
    layoutParams = LinearLayout.LayoutParams(matchParent, wrapContent)
}

private fun spacer(context: Context, height: Int): View = View(context).apply {
    layoutParams = LinearLayout.LayoutParams(matchParent, dp(height))
}

private fun rounded(
    color: Int,
    radius: Float,
    strokeColor: Int? = null,
    topOnly: Boolean = false,
): GradientDrawable {
    return GradientDrawable().apply {
        setColor(color)
        if (topOnly) {
            cornerRadii = floatArrayOf(radius.dpFloat, radius.dpFloat, radius.dpFloat, radius.dpFloat, 0f, 0f, 0f, 0f)
        } else {
            cornerRadius = radius.dpFloat
        }
        strokeColor?.let { setStroke(1, it) }
    }
}

private const val RecommendedWaterRange = "2000 ~ 2600"
private const val DefaultWaterGoal = 2000
private const val DefaultWeightTenthsKg = 750
private val WaterGoalOptions = listOf(1500, 2000, 2500)
private const val AccentBlue = 0xFF1D6BF2.toInt()
private const val MutedText = 0xFF999999.toInt()
private val matchParent = ViewGroup.LayoutParams.MATCH_PARENT
private val wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT
private val Float.dpFloat: Float get() = this * android.content.res.Resources.getSystem().displayMetrics.density
private fun dp(value: Int): Int = (value * android.content.res.Resources.getSystem().displayMetrics.density).roundToInt()
