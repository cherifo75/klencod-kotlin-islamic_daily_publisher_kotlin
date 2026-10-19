package com.example.islamicdaily

import android.graphics.*
import android.media.MediaCodec
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import java.io.File
import java.util.concurrent.Executors

class VideoComposer {
    private val executor = Executors.newSingleThreadExecutor()

    fun create(item: ContentItem, output: File, done: (Result<File>) -> Unit) {
        executor.execute {
            try {
                val recorder = MediaRecorder()
                recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                recorder.setVideoSize(720, 1280)
                recorder.setVideoFrameRate(30)
                recorder.setVideoEncodingBitRate(2_400_000)
                recorder.setOutputFile(output.absolutePath)
                recorder.prepare()
                val surface = recorder.surface
                recorder.start()
                val start = System.currentTimeMillis()
                var frame = 0
                while (System.currentTimeMillis() - start < 10_000) {
                    val canvas = surface.lockCanvas(null)
                    drawFrame(canvas, item, frame)
                    surface.unlockCanvasAndPost(canvas)
                    frame++
                    Thread.sleep(33)
                }
                recorder.stop(); recorder.reset(); recorder.release(); surface.release()
                done(Result.success(output))
            } catch (t: Throwable) { done(Result.failure(t)) }
        }
    }

    private fun drawFrame(canvas: Canvas, item: ContentItem, frame: Int) {
        val w = canvas.width.toFloat(); val h = canvas.height.toFloat()
        val gradient = LinearGradient(0f, 0f, w, h, Color.rgb(10, 54, 45), Color.rgb(21, 104, 78), Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = gradient })
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(30, 242, 200, 112) }
        canvas.drawCircle(w * .82f, h * .18f, 180f + (frame % 30), glow)
        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(242, 213, 145); textSize = 30f; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER }
        canvas.drawText(if (item.type == "آية") "آية اليوم" else "حديث اليوم", w / 2, h * .20f, title)
        val body = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textSize = 34f; textAlign = Paint.Align.CENTER; typeface = Typeface.create("sans", Typeface.NORMAL) }
        drawWrapped(canvas, item.text, body, w / 2, h * .40f, w * .82f, 54f)
        val ref = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(220, 235, 235, 235); textSize = 22f; textAlign = Paint.Align.CENTER }
        canvas.drawText(item.reference, w / 2, h * .78f, ref)
        val mark = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(180, 255, 255, 255); textSize = 18f; textAlign = Paint.Align.CENTER }
        canvas.drawText("ذكر يومي  •  مصدر موثق", w / 2, h * .91f, mark)
    }

    private fun drawWrapped(c: Canvas, text: String, p: Paint, x: Float, top: Float, max: Float, line: Float) {
        val words = text.split(" "); val lines = mutableListOf<String>(); var current = ""
        for (word in words) { val next = if (current.isEmpty()) word else "$current $word"; if (p.measureText(next) > max && current.isNotEmpty()) { lines.add(current); current = word } else current = next }
        if (current.isNotEmpty()) lines.add(current)
        val first = top - (lines.size - 1) * line / 2
        lines.forEachIndexed { i, s -> c.drawText(s, x, first + i * line, p) }
    }
}
