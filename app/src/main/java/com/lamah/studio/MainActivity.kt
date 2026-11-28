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
import androidx.exifinterface.media.ExifInterface
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    private val bg = Color.rgb(8, 10, 19)
    private val panelColor = Color.rgb(19, 23, 38)
    private val raised = Color.rgb(31, 38, 59)
    private val accent = Color.rgb(116, 224, 255)
    private val primary = Color.rgb(244, 248, 255)
    private val muted = Color.rgb(151, 166, 190)
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
    private var cropRatio: Float? = null
    private var overlayText: String = ""
    private var filterPreset: String = "original"
    private var filterView: View? = null
    private var adjustmentsView: View? = null
    private var cropView: View? = null
    private var textView: View? = null

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
        intent?.data?.let { loadMedia(it) }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { loadMedia(it) }
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
        brand.addView(txt("VYNTRO", 27, primary, true))
        brand.addView(txt("استوديو بصري يصنع الفرق", 12, muted))
        header.addView(brand)
        header.addView(txt("CREATIVE STUDIO  •  01", 10, accent, true).apply {
            setPadding(dp(12), dp(9), dp(12), dp(9)); background = shape(raised, 18)
        })
        body.addView(header)
        gap(body, 14)
        val quickNav = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        quickNav.addView(small("الضبط") { jumpTo(adjustmentsView) })
        quickNav.addView(small("الفلاتر") { jumpTo(filterView) })
        quickNav.addView(small("القص") { jumpTo(cropView) })
        quickNav.addView(small("النص") { jumpTo(textView) })
        body.addView(quickNav)
        gap(body, 14)
        gap(body, 24)
        body.addView(txt("رؤيتك. بأسلوبك.", 25, primary, true))
        body.addView(txt("اصنع صوراً تستحق أن تُروى", 13, muted))
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
        body.addView(button("＋  أضف صورة أو فيديو", accent, bg) {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }, LinearLayout.LayoutParams(-1, dp(52)))
        gap(body, 24)

        body.addView(section("ضبط الصورة", "تحكم في الضوء والعمق والألوان"))
        gap(body, 12)
        val adjustments = panel()
        slider(adjustments, "الإضاءة", -100, 100, 0) { brightness = it / 100f; refresh() }
        slider(adjustments, "التباين", 0, 200, 100) { contrast = it / 100f; refresh() }
        slider(adjustments, "التشبّع", 0, 200, 100) { saturation = it / 100f; refresh() }
        body.addView(adjustments)
        adjustmentsView = adjustments
        gap(body, 20)

        body.addView(section("الفلاتر الاحترافية", "معاينة فورية وتُطبّق نفسها عند التصدير"))
        gap(body, 12)
        val filterPanel = panel()
        val filterRow = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        filterRow.addView(small("أصلي") { applyFilter("original") })
        filterRow.addView(small("سينمائي") { applyFilter("cinema") })
        filterRow.addView(small("حيوي") { applyFilter("vivid") })
        filterRow.addView(small("أبيض وأسود") { applyFilter("mono") })
        filterPanel.addView(filterRow)
        body.addView(filterPanel)
        filterView = filterPanel
        gap(body, 20)

        body.addView(section("قص الصورة", "اختر أبعاداً شائعة أو ارجع إلى الإطار الأصلي"))
        gap(body, 12)
        val cropPanel = panel()
        val cropRow = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        cropRow.addView(small("1:1") { setCrop(1f) })
        cropRow.addView(small("4:5") { setCrop(4f / 5f) })
        cropRow.addView(small("16:9") { setCrop(16f / 9f) })
        cropPanel.addView(cropRow)
        gap(cropPanel, 8)
        cropPanel.addView(button("إلغاء القص", raised, primary) { cropRatio = null; refresh(); toast("تمت استعادة الإطار الأصلي") })
        body.addView(cropPanel)
        cropView = cropPanel
        gap(body, 20)

        body.addView(section("النص على الصورة", "أضف عنواناً يظهر في المعاينة والملف المحفوظ"))
        gap(body, 12)
        val textPanel = panel()
        textPanel.addView(button("＋  إضافة أو تعديل النص", raised, primary) { editOverlayText() })
        body.addView(textPanel)
        textView = textPanel
        gap(body, 20)

        body.addView(section("التكوين", "أعد تشكيل الصورة بالطريقة التي تريدها"))
        gap(body, 12)
        val transforms = panel()
        val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        row.addView(small("تدوير 90°") { rotation = (rotation + 90f) % 360f; refresh() })
        row.addView(small("قلب أفقي") { flipH = !flipH; refresh() })
        row.addView(small("قلب رأسي") { flipV = !flipV; refresh() })
        transforms.addView(row); body.addView(transforms)
        gap(body, 20)

        body.addView(section("أدوات VYNTRO", "كل ما هو متاح فعلياً في مساحة التحرير"))
        gap(body, 12)
        val features = panel()
        listOf("استيراد الصور والفيديو من الجهاز", "معاينة وتشغيل الفيديو", "ضبط الإضاءة والتباين والتشبّع", "فلاتر أصلية وسينمائية وحيوية وأبيض وأسود", "قص الصورة بنسب متعددة", "إضافة نص وتضمينه في الحفظ", "تدوير الصورة وقلبها", "حفظ الصورة المعدّلة في المعرض").forEach {
            features.addView(txt("✦   " + it, 13, primary).apply { setPadding(0, dp(9), 0, dp(9)) })
        }
        body.addView(features)
        gap(body, 20)
        body.addView(button("حفظ الصورة المعدّلة", accent, bg) { exportImage() }, LinearLayout.LayoutParams(-1, dp(52)))
        gap(body, 10)
        body.addView(txt("تتم معالجة الصور على جهازك دون رفعها إلى خادم.", 11, muted).apply { gravity = Gravity.CENTER })
        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        // A real in-app launch screen: branded, animated, and removed before editing.
        val stage = FrameLayout(this)
        stage.addView(root, FrameLayout.LayoutParams(-1, -1))
        val launch = FrameLayout(this).apply {
            background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(8, 13, 31), Color.rgb(25, 28, 62), Color.rgb(8, 36, 54))
            )
        }
        val center = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        val mark = TextView(this).apply {
            text = "V"
            textSize = 48f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
            background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(88, 115, 255), Color.rgb(65, 218, 232))
            ).apply { cornerRadius = dp(27).toFloat() }
            elevation = dp(18).toFloat()
        }
        center.addView(mark, LinearLayout.LayoutParams(dp(92), dp(92)))
        val brandTitle = txt("VYNTRO", 34, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.16f
        }
        center.addView(brandTitle)
        center.addView(txt("CREATE WITHOUT LIMITS", 10, Color.rgb(151, 222, 245), true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.24f
        })
        val launchParams = FrameLayout.LayoutParams(-2, -2, Gravity.CENTER)
        launchParams.bottomMargin = dp(12)
        launch.addView(center, launchParams)
        launch.addView(txt("YOUR VISUAL SPACE", 10, Color.rgb(133, 151, 184), true).apply {
            gravity = Gravity.CENTER
            letterSpacing = 0.18f
        }, FrameLayout.LayoutParams(-1, dp(48), Gravity.BOTTOM))
        stage.addView(launch, FrameLayout.LayoutParams(-1, -1))
        setContentView(stage)
        launch.alpha = 1f
        launch.postDelayed({
            launch.animate().alpha(0f).setDuration(420).withEndAction {
                stage.removeView(launch)
            }.start()
        }, 1050)
    }

    private fun loadMedia(uri: Uri) {
        val mime = contentResolver.getType(uri).orEmpty()
        videoSelected = mime.startsWith("video/")
        brightness = 0f; contrast = 1f; saturation = 1f; rotation = 0f; flipH = false; flipV = false
        cropRatio = null; overlayText = ""; filterPreset = "original"
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
                val orientation = contentResolver.openInputStream(uri)?.use { input ->
                    ExifInterface(input).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                } ?: ExifInterface.ORIENTATION_NORMAL
                val decoded = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                if (decoded == null) { toast("تعذّر فتح هذه الصورة"); return }
                val loaded = applyExifOrientation(decoded, orientation)
                if (loaded !== decoded && !decoded.isRecycled) decoded.recycle()
                bitmap = loaded
                video.stopPlayback()
                video.visibility = View.GONE
                preview.visibility = View.VISIBLE
                empty.visibility = View.GONE
                refresh()
            } catch (_: Exception) { toast("تعذّر تحميل الصورة. جرّب ملفاً بصيغة مدعومة.") }
        }
    }

    private fun applyExifOrientation(source: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.setScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.setRotate(90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.setRotate(-90f); matrix.postScale(-1f, 1f) }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
            else -> return source
        }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
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
        val preset = when (filterPreset) {
            "cinema" -> ColorMatrix(floatArrayOf(
                1.10f, 0f, 0f, 0f, -5f,
                0f, 1.06f, 0f, 0f, -3f,
                0f, 0f, 0.94f, 0f, 2f,
                0f, 0f, 0f, 1f, 0f
            ))
            "vivid" -> ColorMatrix(floatArrayOf(
                1.12f, 0f, 0f, 0f, 0f,
                0f, 1.10f, 0f, 0f, 0f,
                0f, 0f, 1.12f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))
            "mono" -> ColorMatrix().apply { setSaturation(0f) }
            else -> null
        }
        if (preset != null) matrix.postConcat(preset)
        return matrix
    }

    private fun transformMatrix(width: Int, height: Int): Matrix {
        val matrix = Matrix()
        matrix.postRotate(rotation)
        matrix.postScale(if (flipH) -1f else 1f, if (flipV) -1f else 1f)
        val bounds = RectF(0f, 0f, width.toFloat(), height.toFloat())
        matrix.mapRect(bounds)
        matrix.postTranslate(-bounds.left, -bounds.top)
        return matrix
    }

    /**
     * Creates the exact bitmap used by both preview and export. The transformed
     * bounds are normalized so rotations never crop the corners.
     */
    private fun cropSource(source: Bitmap): Bitmap {
        val ratio = cropRatio ?: return source
        val sourceRatio = source.width.toFloat() / source.height.toFloat()
        val width: Int
        val height: Int
        if (sourceRatio > ratio) {
            height = source.height
            width = (height * ratio).roundToInt().coerceIn(1, source.width)
        } else {
            width = source.width
            height = (width / ratio).roundToInt().coerceIn(1, source.height)
        }
        val left = (source.width - width) / 2
        val top = (source.height - height) / 2
        return Bitmap.createBitmap(source, left, top, width, height)
    }

    private fun renderEditedBitmap(source: Bitmap): Bitmap {
        val cropped = cropSource(source)
        try {
            val matrix = transformMatrix(cropped.width, cropped.height)
            val bounds = RectF(0f, 0f, cropped.width.toFloat(), cropped.height.toFloat())
            matrix.mapRect(bounds)
            val outWidth = bounds.width().roundToInt().coerceAtLeast(1)
            val outHeight = bounds.height().roundToInt().coerceAtLeast(1)
            val output = Bitmap.createBitmap(outWidth, outHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                colorFilter = android.graphics.ColorMatrixColorFilter(colorMatrix())
            }
            canvas.drawBitmap(cropped, matrix, paint)
            if (overlayText.isNotBlank()) {
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    textSize = (outWidth * 0.065f).coerceIn(22f, 96f)
                    typeface = Typeface.create("sans-serif-bold", Typeface.BOLD)
                    textAlign = Paint.Align.RIGHT
                    setShadowLayer(5f, 0f, 2f, Color.BLACK)
                }
                val x = outWidth - outWidth * 0.06f
                val y = outHeight - outHeight * 0.08f
                val textWidth = textPaint.measureText(overlayText)
                val box = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(145, 5, 10, 22) }
                canvas.drawRoundRect(
                    x - textWidth - outWidth * 0.035f, y - textPaint.textSize * 1.25f,
                    x + outWidth * 0.025f, y + textPaint.textSize * 0.35f,
                    16f, 16f, box
                )
                canvas.drawText(overlayText, x, y, textPaint)
            }
            return output
        } finally {
            if (cropped !== source && !cropped.isRecycled) cropped.recycle()
        }
    }

    private fun applyFilter(name: String) {
        if (bitmap == null || videoSelected) {
            toast("استورد صورة أولاً لتجربة الفلاتر")
            return
        }
        filterPreset = name
        refresh()
    }

    private fun setCrop(ratio: Float) {
        if (bitmap == null || videoSelected) {
            toast("استورد صورة أولاً لاستخدام القص")
            return
        }
        cropRatio = ratio
        refresh()
    }

    private fun editOverlayText() {
        if (bitmap == null || videoSelected) {
            toast("استورد صورة أولاً لإضافة النص")
            return
        }
        val field = EditText(this).apply {
            setSingleLine(true)
            hint = "اكتب النص هنا"
            setText(overlayText)
            setSelection(text?.length ?: 0)
        }
        android.app.AlertDialog.Builder(this)
            .setTitle("النص على الصورة")
            .setView(field)
            .setPositiveButton("تطبيق") { _, _ ->
                overlayText = field.text.toString().trim().take(80)
                refresh()
            }
            .setNeutralButton("إزالة النص") { _, _ -> overlayText = ""; refresh() }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun jumpTo(target: View?) {
        if (target == null) return
        val scroll = target.parent
        var top = target.top
        var parent = target.parent
        while (parent is View && parent !is ScrollView) {
            top += parent.top
            parent = parent.parent
        }
        (parent as? ScrollView)?.smoothScrollTo(0, top)
    }

    private fun refresh() {
        val source = bitmap ?: return
        try {
            val rendered = renderEditedBitmap(source)
            val previous = preview.drawable
            preview.setImageBitmap(rendered)
            if (previous is android.graphics.drawable.BitmapDrawable) {
                val oldBitmap = previous.bitmap
                if (oldBitmap !== source && oldBitmap !== rendered && !oldBitmap.isRecycled) oldBitmap.recycle()
            }
            preview.scaleType = ImageView.ScaleType.FIT_CENTER
        } catch (_: OutOfMemoryError) {
            toast("الصورة كبيرة جداً للمعاينة على ذاكرة الجهاز")
        } catch (_: Exception) {
            toast("تعذّرت معاينة التعديلات")
        }
    }

    private fun exportImage() {
        val source = bitmap
        if (source == null || videoSelected) {
            toast("استورد صورة أولاً لحفظ النسخة المعدّلة")
            return
        }
        var output: Bitmap? = null
        var target: Uri? = null
        try {
            output = renderEditedBitmap(source)
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "VYNTRO_" + stamp + ".jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= 29) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/VYNTRO")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            target = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("تعذّر إنشاء ملف الصورة")
            val saved = contentResolver.openOutputStream(target!!, "w")?.use { stream ->
                output.compress(Bitmap.CompressFormat.JPEG, 95, stream)
            } ?: false
            if (!saved) throw IllegalStateException("تعذّر ترميز الصورة")
            if (Build.VERSION.SDK_INT >= 29) {
                val publish = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
                contentResolver.update(target!!, publish, null, null)
            }
            toast("تم حفظ الصورة في Pictures/VYNTRO")
        } catch (_: OutOfMemoryError) {
            target?.let { contentResolver.delete(it, null, null) }
            toast("الصورة أكبر من الذاكرة المتاحة. جرّب صورة أصغر.")
        } catch (_: Exception) {
            target?.let { contentResolver.delete(it, null, null) }
            toast("تعذّر حفظ الصورة. حاول مجدداً.")
        } finally {
            output?.recycle()
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
