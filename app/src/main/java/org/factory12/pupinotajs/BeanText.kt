package org.factory12.pupinotajs

import android.content.Context
import android.graphics.Color
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet

class BeanText : androidx.appcompat.widget.AppCompatEditText {
    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    private var textColorSpan: Any? = null
    override fun onSelectionChanged(selectionStart: Int, selectionEnd: Int) {
        var s = selectionStart
        var e = selectionEnd
        super.onSelectionChanged(s, e)
        if (this.textColorSpan == null) this.textColorSpan = ForegroundColorSpan(Color.WHITE)
        else this.text!!.removeSpan(this.textColorSpan)

        if (s > e) {
            val swap = s
            s = e
            e = swap
        }
        this.text!!.setSpan(this.textColorSpan, s, e, Spanned.SPAN_INTERMEDIATE)
    }
}
