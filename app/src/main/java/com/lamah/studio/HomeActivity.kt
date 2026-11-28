package com.lamah.studio

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

class HomeActivity : ComponentActivity() {
    private val bg = Color.rgb(8,10,19)
    private val panel = Color.rgb(19,23,38)
    private val raised = Color.rgb(31,38,59)
    private val accent = Color.rgb(116,224,255)
    private val white = Color.rgb(244,248,255)
    private val muted = Color.rgb(151,166,190)
    private lateinit var content: LinearLayout
    private var page = "home"

    private val picker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) openEditor(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        render("home")
    }

    private fun render(target: String) {
        page = target
        val root = col().apply { setBackgroundColor(bg); setPadding(dp(20),dp(18),dp(20),dp(12)) }
        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val brand = col()
        brand.addView(label("VYNTRO",26,white,true))
        brand.addView(label("مساحة إبداعك",12,muted))
        header.addView(brand,LinearLayout.LayoutParams(0,-2,1f))
        header.addView(label("V",24,accent,true).apply { gravity=Gravity.CENTER; background=shape(raised,16); setPadding(dp(15),dp(8),dp(15),dp(8)) })
        root.addView(header); gap(root,22)
        root.addView(label(when(target){"projects"->"مشاريعك";"settings"->"الإعدادات";else->"استوديو VYNTRO"},24,white,true))
        gap(root,5)
        root.addView(label(when(target){"projects"->"الملفات التي فتحتها مؤخراً";"settings"->"تحكم في تجربة التطبيق";else->"حرّر الصور والفيديو من مكان واحد"},12,muted))
        gap(root,18)
        val scroll=ScrollView(this).apply{isFillViewport=true}
        content=col()
        when(target){"projects"->projectsPage();"settings"->settingsPage();else->homePage()}
        scroll.addView(content)
        root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        val nav=LinearLayout(this).apply{gravity=Gravity.CENTER}
        nav.addView(navButton("الرئيسية","home"))
        nav.addView(navButton("المشاريع","projects"))
        nav.addView(navButton("الإعدادات","settings"))
        root.addView(nav)
        setContentView(root)
    }

    private fun homePage() {
        val hero=col().apply{setPadding(dp(18),dp(22),dp(18),dp(22));background=shape(panel,22)}
        hero.addView(label("اصنع شيئاً مختلفاً",22,white,true));gap(hero,6)
        hero.addView(label("أدوات تحرير مرنة لصورك ومقاطعك.",13,muted));gap(hero,18)
        hero.addView(action("＋  ابدأ مشروعاً جديداً",accent,bg){pickAny()})
        content.addView(hero);gap(content,16)
        content.addView(label("إنشاء جديد",16,white,true));gap(content,10)
        content.addView(action("تحرير صورة",raised,white){picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))})
        gap(content,8)
        content.addView(action("تحرير فيديو",raised,white){picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))})
        gap(content,18)
        content.addView(label("أدوات التحرير",16,white,true));gap(content,10)
        val tools=col().apply{setPadding(dp(14),dp(12),dp(14),dp(12));background=shape(panel,18)}
        listOf("الفلاتر وتصحيح الألوان","القص وتغيير أبعاد الصورة","النصوص والعناوين","التدوير والتحويل").forEach {
            tools.addView(label("✦   $it",13,white).apply{setPadding(0,dp(8),0,dp(8))})
        }
        content.addView(tools);gap(content,16)
        content.addView(label("تحرير الصور متاح حالياً. أدوات قص الفيديو وتحرير المسارات وتصدير الفيديو ما زالت قيد التطوير.",12,muted))
    }

    private fun projectsPage() {
        val entries=getSharedPreferences("vyntro_projects",MODE_PRIVATE).getStringSet("recent",emptySet()).orEmpty().toList().sortedDescending()
        if(entries.isEmpty()){
            val empty=col().apply{gravity=Gravity.CENTER;setPadding(dp(18),dp(34),dp(18),dp(34));background=shape(panel,20)}
            empty.addView(label("لا توجد ملفات حديثة",17,white,true).apply{gravity=Gravity.CENTER})
            gap(empty,8);empty.addView(label("استورد صورة أو فيديو لبدء العمل.",12,muted).apply{gravity=Gravity.CENTER})
            gap(empty,16);empty.addView(action("استيراد وسائط",accent,bg){pickAny()});content.addView(empty);return
        }
        entries.forEach { entry ->
            val parts=entry.split("|",limit=2);val uri=parts[0];val display=parts.getOrNull(1)?: "وسائط VYNTRO"
            val item=col().apply{setPadding(dp(14),dp(12),dp(14),dp(12));background=shape(panel,16)}
            item.addView(label(display,14,white,true));gap(item,8)
            item.addView(action("فتح في المحرر",raised,accent){openEditor(Uri.parse(uri))})
            content.addView(item);gap(content,9)
        }
        content.addView(label("هذه قائمة بالملفات المفتوحة مؤخراً، وليست مشاريع متعددة الطبقات قابلة للاستعادة.",11,muted))
    }

    private fun settingsPage() {
        val box=col().apply{setPadding(dp(16),dp(14),dp(16),dp(14));background=shape(panel,18)}
        listOf("اسم التطبيق" to "VYNTRO","مكان حفظ الصور" to "Pictures/VYNTRO","معالجة الصور" to "محلياً على الجهاز","إصدار الواجهة" to "2.0").forEach{(k,v)->
            box.addView(label(k,12,muted));box.addView(label(v,15,white,true));gap(box,14)
        }
        content.addView(box);gap(content,14)
        content.addView(action("مسح قائمة الملفات الأخيرة",raised,white){
            getSharedPreferences("vyntro_projects",MODE_PRIVATE).edit().clear().apply()
            Toast.makeText(this,"تم مسح القائمة",Toast.LENGTH_SHORT).show();render("settings")
        })
        gap(content,10)
        content.addView(label("إعدادات جودة التصدير والنسخ الاحتياطي ستضاف مع تنفيذ نظام المشاريع.",12,muted))
    }

    private fun pickAny()=picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
    private fun openEditor(uri:Uri){
        runCatching{contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)}
        val name=uri.lastPathSegment?.substringAfterLast('/')?:"ملف مستورد"
        val prefs=getSharedPreferences("vyntro_projects",MODE_PRIVATE)
        val recent=prefs.getStringSet("recent",emptySet()).orEmpty().toMutableSet()
        recent.add(uri.toString()+"|"+name.take(80))
        prefs.edit().putStringSet("recent",recent.toList().takeLast(30).toSet()).apply()
        startActivity(Intent(this,MainActivity::class.java).apply{data=uri;addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)})
    }
    private fun navButton(title:String,target:String)=action(title,if(page==target)accent else raised,if(page==target)bg else white){render(target)}.apply{
        layoutParams=LinearLayout.LayoutParams(0,dp(46),1f).apply{marginStart=dp(4);marginEnd=dp(4)};textSize=12f
    }
    private fun col()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
    private fun label(s:String,size:Int,color:Int,bold:Boolean=false)=TextView(this).apply{text=s;textSize=size.toFloat();setTextColor(color);if(bold)setTypeface(typeface,Typeface.BOLD);gravity=Gravity.CENTER_VERTICAL}
    private fun action(s:String,color:Int,fg:Int,run:()->Unit)=Button(this).apply{text=s;isAllCaps=false;textSize=14f;setTextColor(fg);background=shape(color,14);minHeight=dp(48);setOnClickListener{run()}}
    private fun shape(color:Int,radius:Int)=android.graphics.drawable.GradientDrawable().apply{setColor(color);cornerRadius=dp(radius).toFloat()}
    private fun gap(parent:LinearLayout,h:Int){parent.addView(View(this),LinearLayout.LayoutParams(1,dp(h)))}
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
}
