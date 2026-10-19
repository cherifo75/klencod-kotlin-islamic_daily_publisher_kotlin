package com.example.islamicdaily

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : android.app.Activity() {
    private val composer = VideoComposer()
    private val items = mutableListOf(
        ContentItem("q1", "آية", "إِنَّ مَعَ الْعُسْرِ يُسْرًا", "الشرح 94:6", "https://tanzil.net/", approved = false),
        ContentItem("q2", "آية", "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ", "البقرة 2:152", "https://tanzil.net/", approved = false),
        ContentItem("h1", "حديث", "من لا يَرحم لا يُرحم", "صحيح البخاري 7376؛ صحيح مسلم 2318", "https://sunnah.com/", grade = "صحيح", approved = false)
    )
    private lateinit var list: LinearLayout
    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    override fun onCreate(state: Bundle?) { super.onCreate(state); buildUi() }

    private fun buildUi() {
        val scroll = ScrollView(this); list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(20), dp(16), dp(28)) }; scroll.addView(list); setContentView(scroll); render()
    }

    private fun render() {
        list.removeAllViews()
        list.addView(TextView(this).apply { text = "ذكر يومي"; textSize = 28f; setTextColor(0xff145844.toInt()); setPadding(0, 0, 0, dp(4)) })
        list.addView(TextView(this).apply { text = "راجع المحتوى أولًا، ثم أنشئ الفيديو وشاركه إلى TikTok."; textSize = 15f; setPadding(0, 0, 0, dp(16)) })
        items.forEach { item -> addCard(item) }
    }

    private fun addCard(item: ContentItem) {
        val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(14)); setBackgroundColor(0xfff0f6f3.toInt()) }
        val heading = TextView(this).apply { text = "${item.type}  •  ${item.reference}"; textSize = 16f; setTextColor(0xff145844.toInt()) }
        val body = TextView(this).apply { text = item.text; textSize = 20f; gravity = Gravity.RIGHT; setPadding(0, dp(10), 0, dp(8)) }
        val source = TextView(this).apply { text = "المصدر: ${item.sourceUrl}${item.grade?.let { "  •  الدرجة: $it" } ?: ""}"; textSize = 12f; setTextColor(0xff5a6b63.toInt()) }
        val review = CheckBox(this).apply { text = "راجعت النص والمصدر وأوافق على إنشاء الفيديو"; isChecked = item.approved; setOnCheckedChangeListener { _, checked -> item.approved = checked } }
        val make = Button(this).apply { text = "إنشاء فيديو"; setOnClickListener { createVideo(item) } }
        val share = Button(this).apply { text = "مشاركة إلى TikTok"; isEnabled = item.videoPath != null; setOnClickListener { shareVideo(item) } }
        card.addView(heading); card.addView(body); card.addView(source); card.addView(review); card.addView(make); card.addView(share)
        list.addView(card, LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 0, 0, dp(14)) })
    }

    private fun createVideo(item: ContentItem) {
        if (!item.approved) { Toast.makeText(this, "راجع المحتوى وفعّل الموافقة أولًا", Toast.LENGTH_LONG).show(); return }
        Toast.makeText(this, "جارٍ إنشاء الفيديو...", Toast.LENGTH_SHORT).show()
        val file = File(cacheDir, "${item.id}_${System.currentTimeMillis()}.mp4")
        composer.create(item, file) { result -> runOnUiThread { result.onSuccess { item.videoPath = it.absolutePath; Toast.makeText(this, "تم إنشاء الفيديو", Toast.LENGTH_LONG).show(); render() }.onFailure { Toast.makeText(this, "فشل إنشاء الفيديو: ${it.message}", Toast.LENGTH_LONG).show() } } }
    }

    private fun shareVideo(item: ContentItem) {
        val file = item.videoPath?.let(::File) ?: return
        val uri: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "video/mp4"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }, "مشاركة الفيديو"))
    }
}