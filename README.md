# MediaPro Studio

تطبيق Android أصلي لتحرير الصور والفيديو، قيد التطوير باستخدام Kotlin وJetpack Compose وMaterial 3.

## التقنية الحالية
- Kotlin 2.0.21 وAndroid Gradle Plugin 8.7.3
- Jetpack Compose وMaterial 3
- Navigation Compose
- MVVM باستخدام ViewModel وStateFlow
- Hilt لحقن الاعتماديات
- فصل أولي بين domain وdata وui
- Android Photo Picker لاستيراد الصور والفيديو دون طلب صلاحيات مكتبة الوسائط العامة

## متطلبات البناء
- Android Studio حديث يدعم AGP 8.7.3
- JDK 17
- Android SDK 35
- الحد الأدنى الحالي: Android 10 (API 29)

## الحالة
هذه هي المرحلة التأسيسية للانتقال من واجهات Android Views القديمة إلى Compose. تتضمن التنقل الأساسي واستيراد الوسائط وقائمة جلسة مؤقتة للملفات المستوردة.

لا تتضمن هذه المرحلة بعد محرر الصور المتقدم أو الخط الزمني متعدد المسارات أو التصدير الاحترافي. قائمة المشاريع الحالية داخل الذاكرة فقط، وستُنقل إلى Room في مرحلة إدارة المشاريع.

## البناء
من Android Studio افتح المجلد ثم شغّل مهمة :app:assembleDebug.
