package io.github.turskyi.visualizer.audiovisuals

import android.content.Context
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.media.audiofx.Visualizer.OnDataCaptureListener
import io.github.turskyi.visualizer.R

class AudioInputReader(visualizerView: VisualizerView, context: Context) {
    private val mVisualizerView: VisualizerView = visualizerView
    private val mContext: Context = context
    private var mPlayer: MediaPlayer? = null
    private var mVisualizer: Visualizer? = null

    init {
        mPlayer = MediaPlayer.create(mContext, R.raw.royals)
        initVisualizer(mPlayer)
    }

    private fun initVisualizer(mPlayer: MediaPlayer?) {
        // Setup media player
        mPlayer?.isLooping = true

        // Setup the Visualizer
        // Connect it to the media player
        mVisualizer = mPlayer?.audioSessionId?.let { Visualizer(it) }
        mVisualizer?.measurementMode = Visualizer.MEASUREMENT_MODE_PEAK_RMS
        mVisualizer?.scalingMode = Visualizer.SCALING_MODE_NORMALIZED

        // Set the size of the byte array returned for visualization
        mVisualizer?.captureSize = Visualizer.getCaptureSizeRange()[0]
        // Whenever audio data is available, update the visualizer view
        mVisualizer?.setDataCaptureListener(
            object : OnDataCaptureListener {
                override fun onWaveFormDataCapture(
                    visualizer: Visualizer,
                    bytes: ByteArray, samplingRate: Int
                ) {
                    // Do nothing, we are only interested in the FFT (aka fast Fourier transform)
                }

                override fun onFftDataCapture(
                    visualizer: Visualizer,
                    bytes: ByteArray, samplingRate: Int
                ) {
                    // If the Visualizer is ready and has data, send that data to the VisualizerView
                    mVisualizer?.let {
                        if (mVisualizer != null && it.enabled) {
                            mVisualizerView.updateFFT(bytes)
                        }
                    }
                }
            },
            Visualizer.getMaxCaptureRate(), false, true)
        // Start everything
        mVisualizer?.enabled = true
        mPlayer?.start()
    }

    fun shutdown(isFinishing: Boolean) {
        if (mPlayer != null) {
            mPlayer?.pause()
            if (isFinishing) {
                mVisualizer?.release()
                mPlayer?.release()
                mPlayer = null
                mVisualizer = null
            }
        }

        mVisualizer?.enabled = false
    }

    fun restart() {
        if (mPlayer != null) {
            mPlayer?.start()
        }
        mVisualizer?.enabled = true
        mVisualizerView.restart()
    }
}