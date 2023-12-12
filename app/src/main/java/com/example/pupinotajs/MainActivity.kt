package com.example.pupinotajs

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.pupinotajs.databinding.ActivityMainBinding


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
            R.id.action_settings -> true
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
        }
        this.isOpen = !this.isOpen
    }
}
