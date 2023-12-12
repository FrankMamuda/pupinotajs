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
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.pupinotajs.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var rotCW: Animation? = null
    private var rotCCW: Animation? = null
    private var isOpen = false
    private var transitionDrawable: TransitionDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        rotCW = AnimationUtils.loadAnimation(this, R.anim.rotate_cw)
        rotCCW = AnimationUtils.loadAnimation(this, R.anim.rotate_ccw)

        transitionDrawable = TransitionDrawable(arrayOf<Drawable>(
            getDrawableFromVectorDrawable(this, R.drawable.bean_plain),
            getDrawableFromVectorDrawable(this, R.drawable.bean_lv)))
        transitionDrawable!!.isCrossFadeEnabled = true

        binding.fab.setImageDrawable(transitionDrawable)

        // binding.fab.setImageDrawable(TransitionDrawable(backgrounds))
        binding.fab.setOnClickListener {
            animateFab()
        }
    }

    private fun getDrawableFromVectorDrawable(context: Context, drawableId: Int): Drawable {
        return BitmapDrawable(context.resources, getBitmapFromVectorDrawable(context, drawableId))
    }

    private fun getBitmapFromVectorDrawable(context: Context?, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context!!, drawableId)
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
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

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun animateFab() {
        if (isOpen) {
            binding.fab.startAnimation(rotCW)
            transitionDrawable?.reverseTransition(1000)
        } else {
            binding.fab.startAnimation(rotCCW)
            transitionDrawable?.startTransition(1000)
        }
        isOpen = !isOpen
    }
}