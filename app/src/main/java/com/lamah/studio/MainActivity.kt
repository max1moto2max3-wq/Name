package com.lamah.studio

import android.content.ContentValues
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val bg = Color.rgb(12, 14, 19)
    private val panelColor = Color.rgb(23, 26, 34)
    private val raised = Color.rgb(34, 37, 48)
    private val accent = Color.rgb(176, 145, 255)
    private val primary = Color.rgb(246, 244, 251)
    private val muted = Color.rgb(157, 162, 177)
    private lateinit var preview: ImageView
    private lateinit var video: VideoView
    private lateinit var empty: LinearLayout
    private lateinit var name: TextView
    private lateinit var status: TextView
    private var bitmap: Bitmap? = null
    private var videoSelected = false
    private var brightness = 0f
    private var contrast = 1f
    private var saturation = 1f
    private var rotation = 0f
    private var flipH = false
    private var flipV = false

    private val picker = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(20)
    ) { items ->
        if (items.isNotEmpty()) loadMedia(items.first())
        if (items.size > 1) toast("تم استيراد أول ملف من المجموعة؛ دعم إدارة المجموعة قيد التطوير")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        createUi()
    }

    private fun createUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
        }
        val scroll = ScrollView(this).apply { isFillViewport = true; overScrollMode = View.OVER_SCROLL_NEVER }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(28))
        }

        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        brand.addView(txt("لَمْعَة", 27, primary, true))
        brand.addView(txt("استوديو إبداعي للصور والفيديو", 12, muted))
        header.addView(brand)
        header.addView(txt("مشروع محلي  •  01", 11, accent, true).apply {
            setPadding(dp(12), dp(9), dp(12), dp(9)); background = shape(raised, 18)
        })
        body.addView(header)
        gap(body, 24)
        body.addView(txt("حوّل لحظاتك إلى قصة", 23, primary, true))
        body.addView(txt("كل أدواتك الإبداعية في مساحة واحدة", 13, muted))
        gap(body, 18)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(12))
            background = shape(panelColor, 22)
        }
        val frame = FrameLayout(this).apply { background = shape(Color.rgb(8, 9, 13), 16) }
        preview = ImageView(this).apply { scaleType = ImageView.ScaleType.FIT_CENTER; visibility = View.GONE }
        video = VideoView(this).apply { visibility = View.GONE }
        empty = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(25), dp(20), dp(25))
        }
        empty.addView(txt("✦", 38, accent, true).apply { gravity = Gravity.CENTER })
        empty.addView(txt("مساحة المعاينة", 18, primary, true).apply { gravity = Gravity.CENTER })
        empty.addView(txt("ابدأ باستيراد صورة أو فيديو", 12, muted).apply { gravity = Gravity.CENTER })
        frame.addView(preview, FrameLayout.LayoutParams(-1, -1))
        frame.addView(video, FrameLayout.LayoutParams(-1, -1))
        frame.addView(empty, FrameLayout.LayoutParams(-1, -1))
        card.addView(frame, LinearLayout.LayoutParams(-1, dp(270)))
        gap(card, 12)
        val meta = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        name = txt("لم يتم اختيار ملف", 12, muted)
        name.layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        status = txt("جاهز للتحرير", 11, accent)
        meta.addView(name); meta.addView(status); card.addView(meta)
        body.addView(card)
        gap(body, 14)
        body.addView(button("＋  استيراد الصور والفيديو", accent, bg) {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }, LinearLayout.LayoutParams(-1, dp(52)))
        gap(body, 24)

        body.addView(section("أدوات الضبط", "تحكم دقيق بمظهر الصورة"))
        gap(body, 12)
        val adjustments = panel()
        slider(adjustments, "الإضاءة", -100, 100, 0) { brightness = it / 100f; refresh() }
        slider(adjustments, "التباين", 0, 200, 100) { contrast = it / 100f; refresh() }
        slider(adjustments, "التشبّع", 0, 200, 100) { saturation = it / 100f; refresh() }
        body.addView(adjustments)
        gap(body, 20)

        body.addView(section("التحويل والتكوين", "رتّب الكادر بالطريقة التي تريدها"))
        gap(body, 12)
        val transforms = panel()
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        row.addView(small("تدوير 90°") { rotation = (rotation + 90f) % 360f; refresh() })
        row.addView(small("قلب أفقي") { flipH = !flipH; refresh() })
        row.addView(small("قلب رأسي") { flipV = !flipV; refresh() })
        transforms.addView(row); body.addView(transforms)
        gap(body, 20)

        body.addView(section("مساحة العمل", "أدوات التحرير المتاحة في هذه النسخة"))
        gap(body, 12)
        val features = panel()
        listOf("استيراد الصور والفيديو من الجهاز", "معاينة وتشغيل الفيديو", "ضبط الإضاءة والتباين والتشبّع", "تدوير الصورة وقلبها", "حفظ الصورة المعدّلة في المعرض").forEach {
            features.addView(txt("✦   " + it, 13, primary).apply { setPadding(0, dp(9), 0, dp(9)) })
        }
        body.addView(features)
        gap(body, 20)
        body.addView(button("حفظ الصورة المعدّلة", accent, bg) { exportImage() }, LinearLayout.LayoutParams(-1, dp(52)))
        gap(body, 10)
        body.addView(txt("تتم معالجة الصور على جهازك دون رفعها إلى خادم.", 11, muted).apply { gravity = Gravity.CENTER })
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun loadMedia(uri: Uri) {
        val mime = contentResolver.getType(uri).orEmpty()
        videoSelected = mime.startsWith("video/")
        brightness = 0f; contrast = 1f; saturation = 1f; rotation = 0f; flipH = false; flipV = false
        name.text = uri.lastPathSegment?.substringAfterLast('/') ?: "ملف مستورد"
        status.text = if (videoSelected) "فيديو" else "صورة"
        if (videoSelected) {
            bitmap = null
            preview.visibility = View.GONE
            empty.visibility = View.GONE
            video.visibility = View.VISIBLE
            video.setVideoURI(uri)
            video.setMediaController(MediaController(this).apply { setAnchorView(video) })
            video.setOnPreparedListener { player -> player.isLooping = true; player.start() }
        } else {
            try {
                val loaded = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                if (loaded == null) { toast("تعذّر فتح هذه الصورة"); return }
                bitmap = loaded
                video.stopPlayback()
                video.visibility = View.GONE
                preview.visibility = View.VISIBLE
                empty.visibility = View.GONE
                refresh()
            } catch (_: Exception) { toast("تعذّر تحميل الصورة. جرّب ملفاً بصيغة مدعومة.") }
        }
    }

    private fun colorMatrix(): ColorMatrix {
        val offset = (1f - contrast) * 128f + brightness * 255f
        val matrix = ColorMatrix(floatArrayOf(
            contrast, 0f, 0f, 0f, offset,
            0f, contrast, 0f, 0f, offset,
            0f, 0f, contrast, 0f, offset,
            0f, 0f, 0f, 1f, 0f
        ))
        matrix.postConcat(ColorMatrix().apply { setSaturation(saturation) })
        return matrix
    }

    private fun transformMatrix(): Matrix = Matrix().apply {
        postRotate(rotation)
        postScale(if (flipH) -1f else 1f, if (flipV) -1f else 1f)
    }

    private fun refresh() {
        val source = bitmap ?: return
        preview.colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix())
        preview.imageMatrix = transformMatrix()
        preview.scaleType = ImageView.ScaleType.MATRIX
        preview.setImageBitmap(source)
    }

    private fun exportImage() {
        val source = bitmap
        if (source == null || videoSelected) { toast("استورد صورة أولاً لحفظ النسخة المعدّلة"); return }
        var transformed: Bitmap? = null
        var output: Bitmap? = null
        try {
            transformed = Bitmap.createBitmap(source, 0, 0, source.width, source.height, transformMatrix(), true)
            output = Bitmap.createBitmap(transformed.width, transformed.height, Bitmap.Config.ARGB_8888)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix()) }
            Canvas(output).drawBitmap(transformed, 0f, 0f, paint)
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "Lamah_" + stamp + ".jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= 29) put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Lamah")
            }
            val target = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("MediaStore insert failed")
            val saved = contentResolver.openOutputStream(target)?.use {
                output.compress(Bitmap.CompressFormat.JPEG, 95, it)
            } ?: false
            if (!saved) throw IllegalStateException("Image encoding failed")
            toast("تم حفظ الصورة في Pictures/Lamah")
        } catch (_: Exception) {
            toast("تعذّر حفظ الصورة. حاول مجدداً.")
        } finally {
            output?.recycle()
            if (transformed != null && transformed !== source) transformed.recycle()
        }
    }

    private fun slider(parent: LinearLayout, title: String, min: Int, max: Int, initial: Int, changed: (Int) -> Unit) {
        val block = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val line = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        line.addView(txt(title, 13, primary).apply { layoutParams = LinearLayout.LayoutParams(0, -2, 1f) })
        val value = txt(initial.toString(), 11, accent, true)
        line.addView(value); block.addView(line)
        val seek = SeekBar(this).apply {
            this.max = max - min
            progress = initial - min
            progressTintList = android.content.res.ColorStateList.valueOf(accent)
            thumbTintList = android.content.res.ColorStateList.valueOf(accent)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val actual = progress + min
                    value.text = actual.toString()
                    changed(actual)
                }
                override fun onStartTrackingTouch(bar: SeekBar?) = Unit
                override fun onStopTrackingTouch(bar: SeekBar?) = Unit
            })
        }
        block.addView(seek); parent.addView(block); gap(parent, 8)
    }

    private fun section(title: String, sub: String) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        addView(txt(title, 17, primary, true))
        addView(txt(sub, 11, muted))
    }

    private fun panel() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14), dp(14), dp(14), dp(14))
        background = shape(panelColor, 18)
    }

    private fun small(caption: String, action: () -> Unit) =
        button(caption, raised, primary, action).apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(44), 1f).apply {
                marginStart = dp(4); marginEnd = dp(4)
            }
            textSize = 11f
        }

    private fun button(caption: String, color: Int, foreground: Int, action: () -> Unit) =
        Button(this).apply {
            text = caption
            setTextColor(foreground)
            textSize = 14f
            isAllCaps = false
            background = shape(color, 14)
            minHeight = dp(48)
            setPadding(dp(10), dp(8), dp(10), dp(8))
            setOnClickListener { action() }
        }

    private fun txt(value: String, size: Int, color: Int, bold: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
        }

    private fun gap(parent: LinearLayout, height: Int) {
        parent.addView(View(this), LinearLayout.LayoutParams(1, dp(height)))
    }

    private fun shape(color: Int, radius: Int) = android.graphics.drawable.GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).roundToInt()
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
