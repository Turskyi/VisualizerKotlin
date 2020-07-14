package io.github.turskyi.visualizer.audiovisuals

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.annotation.ColorInt
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Abstract class representing a shape and a trail for where it's been
 */
internal abstract class TrailedShape(// Variables for determining size
    private val mMultiplier: Float) {

    companion object {
        // Static variables for the center of the parent view and the minimum size of all of the shapes
        private var sViewCenterX = 0f
        private var sViewCenterY = 0f
        private var sMinSize = 0f

        // Static methods
        fun setMinSize(minSize: Float) {
            sMinSize = minSize
        }

        fun setViewCenterY(viewCenterY: Float) {
            sViewCenterY = viewCenterY
        }

        fun setViewCenterX(viewCenterX: Float) {
            sViewCenterX = viewCenterX
        }
    }

    // Variables for determining trail
    private val mTrailPath: Path = Path()
    private val mTrailList: LinkedList<TrailPoint> = LinkedList()

    // Paint for drawing
    private val mPaint: Paint = Paint()
    private val mTrailPaint: Paint = Paint()

    // Variable for determining position
    private var mShapeRadiusFromCenter = 0f


    init {

        // Setup trail variables

        // Setup paint and attributes
        mPaint.style = Paint.Style.FILL
        mTrailPaint.style = Paint.Style.STROKE
        mTrailPaint.strokeWidth = 5f
        mTrailPaint.strokeJoin = Paint.Join.ROUND
        mTrailPaint.strokeCap = Paint.Cap.ROUND
    }

    /**
     * This draw method abstracts out what is common between drawing all shapes
     *
     * @param canvas         The canvas to draw on
     * @param currentFreqAve The average frequency for the same, which determines the boost in size
     * @param currentAngle   The current angle around the center to draw the shape
     */
    fun draw(canvas: Canvas, currentFreqAve: Float, currentAngle: Double) {
        val currentSize = sMinSize + mMultiplier * currentFreqAve

        // Calculate where the shape is
        val shapeCenterX = calcLocationInAnimationX(mShapeRadiusFromCenter, currentAngle)
        val shapeCenterY = calcLocationInAnimationY(mShapeRadiusFromCenter, currentAngle)

        // Calculate where the next point in the trail is
        val trailX = calcLocationInAnimationX(mShapeRadiusFromCenter + currentSize - sMinSize, currentAngle)
        val trailY = calcLocationInAnimationY(mShapeRadiusFromCenter + currentSize - sMinSize, currentAngle)
        mTrailPath.rewind() // clear the trail
        mTrailList.add(TrailPoint(trailX, trailY, currentAngle)) // add the new line segment

        // Keep the trail size correct
        while (currentAngle - mTrailList.first.theta > 2 * Math.PI) {
            mTrailList.poll()
        }

        // Draw the trail
        mTrailPath.moveTo(mTrailList.first.x, mTrailList.first.y)
        for (trailPoint in mTrailList) {
            mTrailPath.lineTo(trailPoint.x, trailPoint.y)
        }
        canvas.drawPath(mTrailPath, mTrailPaint)

        // Call the abstract drawThisShape method, this must be defined for each shape.
        drawThisShape(shapeCenterX, shapeCenterY, currentSize, canvas, mPaint)
    }

    /**
     * Determines how to draw the particular shape
     *
     * @param shapeCenterX Center X position of the shape
     * @param shapeCenterY Center Y position of the shape
     * @param currentSize  Size of the shape
     * @param canvas       The canvas to draw on
     * @param paint        The paint to draw with
     */
    protected abstract fun drawThisShape(shapeCenterX: Float, shapeCenterY: Float, currentSize: Float, canvas: Canvas, paint: Paint)

    /**
     * Clears the trail
     */
    fun restartTrail() {
        mTrailList.clear()
    }

    /**
     * Calculates the center x location
     *
     * @param radiusFromCenter    The distance from the center of this shape
     * @param currentAngleRadians The current angle of the shape
     */
    private fun calcLocationInAnimationX(radiusFromCenter: Float, currentAngleRadians: Double): Float {
        return (sViewCenterX + cos(currentAngleRadians) * radiusFromCenter).toFloat()
    }

    /**
     * Calculates the center y location
     *
     * @param radiusFromCenter    The distance from the center of this shape
     * @param currentAngleRadians The current angle of the shape
     */
    private fun calcLocationInAnimationY(radiusFromCenter: Float, currentAngleRadians: Double): Float {
        return (sViewCenterY + sin(currentAngleRadians) * radiusFromCenter).toFloat()
    }

    /**
     * Sets the shape color
     *
     * @param color Color to set this shape to
     */
    fun setShapeColor(@ColorInt color: Int) {
        mPaint.color = color
    }

    /**
     * Sets the trail color
     *
     * @param color Color to set this trail to
     */
    fun setTrailColor(@ColorInt color: Int) {
        mTrailPaint.color = color
    }

    /**
     * Sets the shapes distance from the center of the view
     *
     * @param mShapeRadiusFromCenter Distance from center of the view
     */
    fun setShapeRadiusFromCenter(mShapeRadiusFromCenter: Float) {
        this.mShapeRadiusFromCenter = mShapeRadiusFromCenter
    }

    /**
     * Inner class representing points in the trail
     */
    private class TrailPoint internal constructor(val x: Float, val y: Float, val theta: Double)
}