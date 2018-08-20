package com.slim.preference

import android.content.Context
import android.preference.DialogPreference
import android.preference.Preference
import android.util.AttributeSet
import android.view.View
import android.widget.NumberPicker

import com.slim.slimfilemanager.R

class NumberPickerPreference(context: Context, attrs: AttributeSet)
/**TypedArray prefType = context.obtainStyledAttributes(attrs,
 * R.styleable.Preference, 0, 0);
 * TypedArray numberPickerType = context.obtainStyledAttributes(attrs,
 * R.styleable.NumberPickerPreference, 0, 0);
 *
 * mMaxExternalKey = numberPickerType.getString(R.styleable.NumberPickerPreference_maxExternal);
 * mMinExternalKey = numberPickerType.getString(R.styleable.NumberPickerPreference_minExternal);
 *
 * mMax = prefType.getInt(R.styleable.Preference_max, 5);
 * mMin = prefType.getInt(R.styleable.Preference_min, 0);
 *
 * mDefault = prefType.getInt(R.styleable.Preference_defaultValue, mMin);
 *
 * prefType.recycle();
 * numberPickerType.recycle(); */
    : DialogPreference(context, attrs) {
    private val mMin: Int = 0
    private val mMax: Int = 0
    private val mDefault: Int = 0

    private val mMaxExternalKey: String? = null
    private val mMinExternalKey: String? = null
    private var mMaxExternalPreference: Preference? = null
    private var mMinExternalPreference: Preference? = null

    private var mNumberPicker: NumberPicker? = null

    private var mValueSummary = false

    override fun onAttachedToActivity() {
        // External values
        if (mMaxExternalKey != null) {
            val maxPreference = findPreferenceInHierarchy(mMaxExternalKey)
            if (maxPreference != null) {
                if (maxPreference is NumberPickerPreference) {
                    mMaxExternalPreference = maxPreference
                }
            }
        }
        if (mMinExternalKey != null) {
            val minPreference = findPreferenceInHierarchy(mMinExternalKey)
            if (minPreference != null) {
                if (minPreference is NumberPickerPreference) {
                    mMinExternalPreference = minPreference
                }
            }
        }

        if (summary == "%s") {
            mValueSummary = true
            summary = Integer.toString(getPersistedInt(mDefault))
        }
    }

    override fun onCreateDialogView(): View {
        var max = mMax
        var min = mMin

        // External values
        if (mMaxExternalKey != null && mMaxExternalPreference != null) {
            max = mMaxExternalPreference!!.sharedPreferences.getInt(mMaxExternalKey, mMax)
        }
        if (mMinExternalKey != null && mMinExternalPreference != null) {
            min = mMinExternalPreference!!.sharedPreferences.getInt(mMinExternalKey, mMin)
        }

        val view = View.inflate(context, R.layout.number_picker_dialog, null)

        mNumberPicker = view.findViewById<View>(R.id.number_picker) as NumberPicker

        // Initialize state
        mNumberPicker!!.maxValue = max
        mNumberPicker!!.minValue = min
        mNumberPicker!!.value = getPersistedInt(mDefault)
        mNumberPicker!!.wrapSelectorWheel = false
        mNumberPicker!!.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        return view
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            persistInt(mNumberPicker!!.value)
            if (mValueSummary) {
                summary = Integer.toString(mNumberPicker!!.value)
            }
        }
    }

}
