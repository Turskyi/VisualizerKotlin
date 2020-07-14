package io.github.turskyi.visualizer

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import io.github.turskyi.visualizer.audiovisuals.AudioInputReader
import io.github.turskyi.visualizer.audiovisuals.VisualizerView

class MainActivity : AppCompatActivity(R.layout.activity_main), OnSharedPreferenceChangeListener {
    companion object {
        private const val MY_PERMISSION_RECORD_AUDIO_REQUEST_CODE = 88
    }

    private var mVisualizerView: VisualizerView? = null
    private var mAudioInputReader: AudioInputReader? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mVisualizerView = findViewById(R.id.activity_visualizer)
        setupSharedPreferences()
        setupPermissions()
    }

    private fun setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        mVisualizerView?.setShowBass(sharedPreferences.getBoolean(getString(R.string.pref_show_bass_key),
            resources.getBoolean(R.bool.pref_show_bass_default)))
        mVisualizerView?.setShowMid(sharedPreferences.getBoolean(getString(R.string.pref_show_mid_range_key),
            resources.getBoolean(R.bool.pref_show_mid_range_default)))
        mVisualizerView?.setShowTreble(sharedPreferences.getBoolean(getString(R.string.pref_show_treble_key),
            resources.getBoolean(R.bool.pref_show_treble_default)))
        loadColorFromPreferences(sharedPreferences)
        loadSizeFromSharedPreferences(sharedPreferences)

        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun loadColorFromPreferences(sharedPreferences: SharedPreferences) {
        val color = sharedPreferences.getString(getString(R.string.pref_color_key),
            getString(R.string.pref_color_red_value))
        mVisualizerView?.setColor(color)
    }

    private fun loadSizeFromSharedPreferences(sharedPreferences: SharedPreferences) {
        val minSize = sharedPreferences.getString(getString(R.string.pref_size_key),
            getString(R.string.pref_size_default))?.toFloat() ?: 1F
        mVisualizerView?.setMinSizeScale(minSize)
    }

    // Updates the screen if the shared preferences change. This method is required when you make a
    // class implement OnSharedPreferenceChangedListener
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            getString(R.string.pref_show_bass_key) -> {
                mVisualizerView?.setShowBass(sharedPreferences.getBoolean(key,
                    resources.getBoolean(R.bool.pref_show_bass_default)))
            }
            getString(R.string.pref_show_mid_range_key) -> {
                mVisualizerView?.setShowMid(sharedPreferences.getBoolean(key,
                    resources.getBoolean(R.bool.pref_show_mid_range_default)))
            }
            getString(R.string.pref_show_treble_key) -> {
                mVisualizerView?.setShowTreble(sharedPreferences.getBoolean(key,
                    resources.getBoolean(R.bool.pref_show_treble_default)))
            }
            getString(R.string.pref_color_key) -> {
                loadColorFromPreferences(sharedPreferences)
            }
            getString(R.string.pref_size_key) -> {
                loadSizeFromSharedPreferences(sharedPreferences)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * Methods for setting up the menu
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        val inflater = menuInflater
        /* Use the inflater's inflate method to inflate our visualizer_menu layout to this menu */
        inflater.inflate(R.menu.menu_visualizer, menu)
        /* Return true so that the visualizer_menu is displayed in the Toolbar */return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            val startSettingsActivity = Intent(this, SettingsActivity::class.java)
            startActivity(startSettingsActivity)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    /*
      Below this point is code you do not need to modify; it deals with permissions
      and starting/cleaning up the AudioInputReader
     */
    /**
     * onPause Cleanup audio stream
     */
    override fun onPause() {
        super.onPause()
        mAudioInputReader?.shutdown(isFinishing)
    }

    override fun onResume() {
        super.onResume()
        mAudioInputReader?.restart()
    }

    /**
     * App Permissions for Audio
     */
    private fun setupPermissions() {
        // If we don't have the record audio permission...
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // And if we're on SDK M or later...
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Ask again, nicely, for the permissions.
                val permissionsWeNeed = arrayOf(Manifest.permission.RECORD_AUDIO)
                requestPermissions(permissionsWeNeed, MY_PERMISSION_RECORD_AUDIO_REQUEST_CODE)
            }
        } else {
            // Otherwise, permissions were granted and we are ready to go!
            mAudioInputReader = mVisualizerView?.let { AudioInputReader(it, this) }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == MY_PERMISSION_RECORD_AUDIO_REQUEST_CODE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // The permission was granted! Start up the visualizer!
                mAudioInputReader = mVisualizerView?.let { AudioInputReader(it, this) }
            } else {
                Toast.makeText(this, "Permission for audio not granted. Visualizer can't run.", Toast.LENGTH_LONG).show()
                finish()
                // The permission was denied, so we can show a message why we can't run the app
                // and then close the app.
            }
            // Other permissions could go down here
        }
    }
}