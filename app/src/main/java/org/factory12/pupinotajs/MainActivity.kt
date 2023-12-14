package org.factory12.pupinotajs


import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.text.method.KeyListener
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
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import org.factory12.pupinotajs.databinding.ActivityMainBinding
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var animClockwise: Animation? = null
    private var animCounterClockwise: Animation? = null
    private var isOpen = false
    private var transitionDrawable: TransitionDrawable? = null
    private var editText: EditText? = null

    @SuppressLint("ClickableViewAccessibility")
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

        // set dark system icons
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false

        // setup animations
        this.animClockwise = AnimationUtils.loadAnimation(this, R.anim.rotate_cw)
        this.animCounterClockwise = AnimationUtils.loadAnimation(this, R.anim.rotate_ccw)

        // setup copy
        this.editText = this.binding.editText
        this.editText!!.setOnClickListener { view ->
            if (this.isOpen && this.editText!!.text.isNotEmpty()) {
                Snackbar.make(view, "Pupiņteksts nokopēts!", Snackbar.LENGTH_LONG).show()
                this.copyToClipboard()
            }
        }

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
        this.binding.fab.setOnClickListener {view ->
            if (this.editText?.text.toString().isEmpty() && !this.isOpen) {
                Snackbar.make(view, "Ievadi taču tekstu!", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
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
                this.copyToClipboard()
                true
            }
            R.id.action_clear -> {
                if (this.isOpen) this.animateFab(true)
                else this.editText?.text?.clear()
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

    private var prev: String? = null

    private fun copyToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("Pupiņotājs", this.editText?.text)
        clipboard.setPrimaryClip(clip)
    }

    private fun animateFab(clear : Boolean = false) {
        if (this.isOpen) {
            this.binding.fab.startAnimation(this.animCounterClockwise)
            this.transitionDrawable?.reverseTransition(400)
            this.editText?.keyListener = this.editText?.tag as KeyListener

            if (clear) {
                this.editText?.text?.clear()
            } else {
                this.editText?.setText(prev)
                this.editText?.setSelection(this.editText!!.length())
            }

            this.animateColor(R.color.bean, R.color.red)
        } else {
            this.prev = this.editText?.text.toString()
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val mac =  prefs.getBoolean("macron",false)
            val cheese =  prefs.getString("char","p") as String

            this.binding.fab.startAnimation(this.animClockwise)
            this.transitionDrawable?.startTransition(400)
            val curr = this.editText?.text.toString()
            val conv = this.translate(curr, cheese, mac)
            this.editText?.setText(conv)

            this.editText?.tag = this.editText?.keyListener
            this.editText?.keyListener = null

            this.animateColor(R.color.red, R.color.bean)
        }
        this.isOpen = !this.isOpen
    }

    private fun animateColor(from: Int, to: Int) {
        val colorFrom = ContextCompat.getColor(this, from)
        val colorTo = ContextCompat.getColor(this, to)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.setDuration(250) // milliseconds
        colorAnimation.addUpdateListener { animator -> this.binding.fab.supportBackgroundTintList = (
                (ColorStateList.valueOf(animator.animatedValue as Int)))
        }

        colorAnimation.start()
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

