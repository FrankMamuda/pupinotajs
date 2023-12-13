package org.factory12.pupinotajs


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.util.SparseArray
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.factory12.pupinotajs.databinding.ActivityMainBinding
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var animClockwise: Animation? = null
    private var animCounterClockwise: Animation? = null
    private var isOpen = false
    private var transitionDrawable: TransitionDrawable? = null
    private var editText: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // populate content
        this.binding = ActivityMainBinding.inflate(this.layoutInflater)
        this.setContentView(this.binding.root)

        // populate vowels
        this.vowels.put(257, 'a')
        this.vowels.put(275, 'e')
        this.vowels.put(299, 'i')
        this.vowels.put(363, 'u')
        this.vowels.put(256, 'A')
        this.vowels.put(274, 'E')
        this.vowels.put(298, 'I')
        this.vowels.put(362, 'U')

        // set up status bar
        this.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        this.window.statusBarColor = ContextCompat.getColor(this, R.color.bean)
        this.setSupportActionBar(this.binding.toolbar)

        // setup animations
        this.animClockwise = AnimationUtils.loadAnimation(this, R.anim.rotate_cw)
        this.animCounterClockwise = AnimationUtils.loadAnimation(this, R.anim.rotate_ccw)

       // binding.
        this.editText = this.binding.editText
        /*this.editText!!.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (this.isOpen) {
                this.binding.fab.startAnimation(this.animClockwise)
                this.transitionDrawable?.reverseTransition(1000)
            }
        }*/

        // UGLY but unfortunately TransitionDrawable does not work with vector drawables
        val makeBitmapDrawable =  fun (drawableId: Int): Drawable {
            val drawable = ContextCompat.getDrawable(this, drawableId)
            val bitmap = Bitmap.createBitmap(
                drawable!!.intrinsicWidth,
                drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return BitmapDrawable(this.resources, bitmap)
        }
        this.transitionDrawable = TransitionDrawable(arrayOf(
            makeBitmapDrawable(R.drawable.bean_plain),
            makeBitmapDrawable(R.drawable.bean_lv)))
        this.transitionDrawable!!.isCrossFadeEnabled = true

        // set this animated image for the action button
        this.binding.fab.setImageDrawable(transitionDrawable)

        // binding.fab.setImageDrawable(TransitionDrawable(backgrounds))
        this.binding.fab.setOnClickListener {
            this.animateFab()
            val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_copy -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("Pupiņotājs", this.editText?.text)
                clipboard.setPrimaryClip(clip)
                true
            }
            R.id.action_clear -> {
                this.editText?.text?.clear()
                true
            }
            R.id.action_settings -> {
                val i = Intent(this@MainActivity, SettingsActivity::class.java)
                startActivity(i)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun animateFab() {
        if (this.isOpen) {
            this.binding.fab.startAnimation(this.animClockwise)
            this.transitionDrawable?.reverseTransition(1000)
        } else {
            this.binding.fab.startAnimation(this.animCounterClockwise)
            this.transitionDrawable?.startTransition(1000)
            val curr = this.editText?.text.toString()
            val conv = this.translate(curr, "p", false)
            this.editText?.setText(conv)
        }
        this.isOpen = !this.isOpen
    }

    private val vowels = SparseArray<Char>()
    private fun translate(inputBuffer: String, symbol: String, keepMacrons: Boolean): String {
        var msg: String

        val isVowel = fun (ch: Char): Boolean {
            return ch == 'o' || ch == 'O' || this.vowels.indexOfKey(ch.code) >= 0 || this.vowels.indexOfValue(ch) >= 0
        }

        val stripMacron = fun (ch: Char, keepMacrons: Boolean): Char {
            // determine appropriate char for conversion
            if (this.vowels.indexOfKey(ch.code) >= 0 && !keepMacrons )
                return this.vowels.get(ch.code)

            return ch
        }

        // retrieve input text and store it in both a local and class buffer
        msg = inputBuffer

        // make sure it is not empty
        if (msg.isEmpty()) return ""

        // iterate through the whole message char by char
        var y = 0
        while (y < msg.length) {

            // get the current char
            val ch: Char = msg[y]

            // test if the char is a vowel
            if (isVowel(ch)) {
                var vowelArray = ""

                // strip macrons
                vowelArray += stripMacron(ch, keepMacrons)

                // read ahead
                while (true) {
                    // failsafe
                    if (y == msg.length - 1) break

                    // another vowel?
                    if (isVowel(msg[y + 1])) {
                        vowelArray += stripMacron(msg[y + 1], keepMacrons)
                        y++
                    } else break
                }

                // append the newly generated syllables to the local buffer
                msg = StringBuffer(msg).insert(
                    y + 1,
                    symbol + vowelArray.lowercase(Locale.getDefault())
                ).toString()

                // advance
                y += vowelArray.length + symbol.length
            }
            y++
        }

        // all done, return the converted string
        return msg
    }
}
