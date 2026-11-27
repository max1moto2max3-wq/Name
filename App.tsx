import React, { useState } from 'react';
import { Alert, Image, Pressable, SafeAreaView, ScrollView, StatusBar, StyleSheet, Text, View, ActivityIndicator } from 'react-native';
import * as ImagePicker from 'expo-image-picker';
import * as ImageManipulator from 'expo-image-manipulator';
import * as MediaLibrary from 'expo-media-library';
import Slider from '@react-native-community/slider';
import { StatusBar as ExpoStatusBar } from 'expo-status-bar';
import { VideoView, useVideoPlayer } from 'expo-video';

type Asset = { uri: string; type: 'image' | 'video'; name?: string };

function VideoPreview({ uri }: { uri: string }) {
  const player = useVideoPlayer(uri, p => { p.loop = true; p.play(); });
  return <VideoView player={player} style={styles.media} nativeControls contentFit="contain" />;
}

function ToolButton({ icon, label, onPress, active = false }: { icon: string; label: string; onPress: () => void; active?: boolean }) {
  return (
    <Pressable onPress={onPress} style={({ pressed }) => [styles.toolButton, active && styles.toolActive, pressed && { opacity: 0.7 }]}>
      <Text style={styles.toolIcon}>{icon}</Text>
      <Text style={styles.toolLabel}>{label}</Text>
    </Pressable>
  );
}

export default function App() {
  const [asset, setAsset] = useState<Asset | null>(null);
  const [rotation, setRotation] = useState(0);
  const [flipH, setFlipH] = useState(false);
  const [flipV, setFlipV] = useState(false);
  const [brightness, setBrightness] = useState(50);
  const [contrast, setContrast] = useState(50);
  const [saturation, setSaturation] = useState(50);
  const [saving, setSaving] = useState(false);

  const pickMedia = async () => {
    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (!permission.granted) {
      Alert.alert('إذن الوصول مطلوب', 'اسمح للتطبيق بالوصول إلى مكتبة الوسائط لاختيار ملف.');
      return;
    }
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ['images', 'videos'],
      allowsEditing: false,
      quality: 1,
      selectionLimit: 1,
    });
    if (result.canceled || !result.assets.length) return;
    const selected = result.assets[0];
    setAsset({ uri: selected.uri, type: selected.type === 'video' ? 'video' : 'image', name: selected.fileName ?? undefined });
    resetTools();
  };

  const resetTools = () => {
    setRotation(0); setFlipH(false); setFlipV(false);
    setBrightness(50); setContrast(50); setSaturation(50);
  };

  const saveImage = async () => {
    if (!asset || asset.type !== 'image') {
      Alert.alert('لا توجد صورة', 'استورد صورة أولاً لحفظها.');
      return;
    }
    setSaving(true);
    try {
      const permission = await MediaLibrary.requestPermissionsAsync();
      if (!permission.granted) {
        Alert.alert('إذن الحفظ مطلوب', 'اسمح للتطبيق بحفظ الصورة في معرض الجهاز.');
        return;
      }
      const actions: ImageManipulator.Action[] = [];
      if (rotation) actions.push({ rotate: rotation });
      if (flipH) actions.push({ flip: ImageManipulator.FlipType.Horizontal });
      if (flipV) actions.push({ flip: ImageManipulator.FlipType.Vertical });
      const result = await ImageManipulator.manipulateAsync(asset.uri, actions, {
        compress: 0.95, format: ImageManipulator.SaveFormat.JPEG,
      });
      await MediaLibrary.saveToLibraryAsync(result.uri);
      Alert.alert('تم الحفظ', 'تم حفظ الصورة في معرض الجهاز.');
    } catch {
      Alert.alert('تعذّر الحفظ', 'حدثت مشكلة أثناء معالجة الصورة. حاول مرة أخرى.');
    } finally {
      setSaving(false);
    }
  };

  const rotate = () => setRotation(value => (value + 90) % 360);
  const reset = () => { resetTools(); };

  return (
    <SafeAreaView style={styles.safe}>
      <ExpoStatusBar style="light" />
      <StatusBar barStyle="light-content" backgroundColor="#0B0D12" />
      <ScrollView contentContainerStyle={styles.page} showsVerticalScrollIndicator={false}>
        <View style={styles.header}>
          <View>
            <Text style={styles.brand}>لَمْعَة<Text style={styles.brandDot}> ✦</Text></Text>
            <Text style={styles.headerSub}>استوديو إبداعي للصور والفيديو</Text>
          </View>
          <View style={styles.version}><Text style={styles.versionText}>استوديو  3.0</Text></View>
        </View>

        <View style={styles.hero}>
          <Text style={styles.eyebrow}>مساحتك الإبداعية</Text>
          <Text style={styles.headline}>خلّ لحظاتك{"\n"}تلمع بطريقتك.</Text>
          <Text style={styles.heroSub}>استورد، عدّل، واحتفظ بأجمل تفاصيلك.</Text>
        </View>

        <View style={styles.previewCard}>
          <View style={styles.previewTop}>
            <Text style={styles.sectionEyebrow}>معاينة العمل</Text>
            <View style={styles.liveTag}><View style={styles.liveDot} /><Text style={styles.liveText}>{asset ? (asset.type === 'video' ? 'فيديو محدد' : 'صورة محددة') : 'جاهز للبدء'}</Text></View>
          </View>
          <View style={styles.canvas}>
            {asset ? (
              asset.type === 'image' ? (
                <Image source={{ uri: asset.uri }} resizeMode="contain" style={[styles.media, { transform: [
                  { rotate: `${rotation}deg` }, { scaleX: flipH ? -1 : 1 }, { scaleY: flipV ? -1 : 1 },
                ] }]} />
              ) : <VideoPreview uri={asset.uri} />
            ) : (
              <View style={styles.emptyState}>
                <View style={styles.sparkle}><Text style={styles.sparkleText}>✦</Text></View>
                <Text style={styles.emptyTitle}>هنا تبدأ الحكاية</Text>
                <Text style={styles.emptySub}>اختر صورة أو فيديو لعرضه في مساحة العمل</Text>
              </View>
            )}
          </View>
          <View style={styles.fileRow}>
            <Text numberOfLines={1} style={styles.fileName}>{asset?.name ?? (asset ? 'ملف من مكتبة الوسائط' : 'لم يتم اختيار أي ملف')}</Text>
            <Text style={styles.fileMeta}>{asset ? (asset.type === 'image' ? 'صورة' : 'فيديو') : '—'}</Text>
          </View>
        </View>

        <Pressable onPress={pickMedia} style={({ pressed }) => [styles.importButton, pressed && { opacity: 0.8 }]}>
          <Text style={styles.importPlus}>＋</Text><Text style={styles.importText}>استيراد صورة أو فيديو</Text>
        </Pressable>

        <View style={styles.sectionHeader}>
          <View><Text style={styles.sectionTitle}>أدوات التحرير</Text><Text style={styles.sectionSub}>تحكم في تكوين صورتك</Text></View>
          <Text style={styles.sectionMark}>01 / EDIT</Text>
        </View>

        <View style={styles.panel}>
          <Text style={styles.panelTitle}>التحويل والتكوين</Text>
          <View style={styles.toolsRow}>
            <ToolButton icon="↻" label="تدوير" onPress={rotate} />
            <ToolButton icon="⇆" label="قلب أفقي" active={flipH} onPress={() => setFlipH(v => !v)} />
            <ToolButton icon="⇅" label="قلب رأسي" active={flipV} onPress={() => setFlipV(v => !v)} />
            <ToolButton icon="⟲" label="إعادة" onPress={reset} />
          </View>
        </View>

        <View style={styles.panel}>
          <View style={styles.panelTitleRow}><Text style={styles.panelTitle}>ضبط الصورة</Text><Text style={styles.comingSoon}>قريباً</Text></View>
          <Adjustment label="الإضاءة" value={brightness} onChange={setBrightness} />
          <Adjustment label="التباين" value={contrast} onChange={setContrast} />
          <Adjustment label="التشبّع" value={saturation} onChange={setSaturation} />
          <Text style={styles.helperText}>أدوات الضبط التفصيلي قيد التطوير؛ لا تُطبّق هذه المؤشرات على الملف المحفوظ بعد.</Text>
        </View>

        <View style={styles.sectionHeader}>
          <View><Text style={styles.sectionTitle}>جاهز للحفظ؟</Text><Text style={styles.sectionSub}>احتفظ بنسخة من تعديلاتك</Text></View>
          <Text style={styles.sectionMark}>02 / EXPORT</Text>
        </View>
        <Pressable onPress={saveImage} disabled={saving} style={({ pressed }) => [styles.saveButton, pressed && { opacity: 0.8 }, saving && { opacity: 0.6 }]}>
          {saving ? <ActivityIndicator color="#17121F" /> : <Text style={styles.saveIcon}>↓</Text>}
          <Text style={styles.saveText}>{saving ? 'جارٍ الحفظ...' : 'حفظ الصورة في المعرض'}</Text>
        </Pressable>
        <Text style={styles.footer}>صُنع للإبداع • لَمْعَة</Text>
      </ScrollView>
    </SafeAreaView>
  );
}

function Adjustment({ label, value, onChange }: { label: string; value: number; onChange: (value: number) => void }) {
  return (
    <View style={styles.adjustment}>
      <View style={styles.adjustmentTop}><Text style={styles.adjustmentLabel}>{label}</Text><Text style={styles.adjustmentValue}>{value}</Text></View>
      <Slider minimumValue={0} maximumValue={100} value={value} onValueChange={onChange}
        minimumTrackTintColor="#B79AFF" maximumTrackTintColor="#343442" thumbTintColor="#D7C7FF" />
    </View>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: '#0B0D12' },
  page: { paddingHorizontal: 22, paddingTop: 18, paddingBottom: 36 },
  header: { flexDirection: 'row-reverse', justifyContent: 'space-between', alignItems: 'center' },
  brand: { color: '#F5F1FC', fontSize: 28, fontWeight: '900', letterSpacing: 0.4, textAlign: 'right' },
  brandDot: { color: '#B79AFF', fontSize: 17 },
  headerSub: { color: '#858494', fontSize: 11, marginTop: 1, textAlign: 'right' },
  version: { backgroundColor: '#211D2D', borderRadius: 20, paddingHorizontal: 12, paddingVertical: 8 },
  versionText: { color: '#C9B7F7', fontSize: 10, fontWeight: '700' },
  hero: { marginTop: 34, marginBottom: 22, alignItems: 'flex-end' },
  eyebrow: { color: '#B79AFF', fontSize: 11, fontWeight: '800', marginBottom: 8 },
  headline: { color: '#F5F1FC', fontSize: 34, fontWeight: '900', lineHeight: 43, textAlign: 'right' },
  heroSub: { color: '#858494', fontSize: 12, marginTop: 9, textAlign: 'right' },
  previewCard: { backgroundColor: '#171821', borderRadius: 23, padding: 12, borderWidth: 1, borderColor: '#242531' },
  previewTop: { flexDirection: 'row-reverse', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 4, paddingBottom: 12 },
  sectionEyebrow: { color: '#B7B4C4', fontSize: 11, fontWeight: '700' },
  liveTag: { flexDirection: 'row-reverse', alignItems: 'center', gap: 6, backgroundColor: '#252232', borderRadius: 20, paddingHorizontal: 9, paddingVertical: 6 },
  liveDot: { width: 6, height: 6, borderRadius: 4, backgroundColor: '#B79AFF' },
  liveText: { color: '#C8B9E8', fontSize: 9 },
  canvas: { height: 245, borderRadius: 16, overflow: 'hidden', backgroundColor: '#0D0E14', alignItems: 'center', justifyContent: 'center' },
  media: { width: '100%', height: '100%' },
  emptyState: { alignItems: 'center', paddingHorizontal: 25 },
  sparkle: { width: 54, height: 54, borderRadius: 18, backgroundColor: '#211D2D', alignItems: 'center', justifyContent: 'center', marginBottom: 15 },
  sparkleText: { color: '#C9B7F7', fontSize: 27 },
  emptyTitle: { color: '#F5F1FC', fontSize: 16, fontWeight: '800' },
  emptySub: { color: '#777687', fontSize: 11, marginTop: 7, textAlign: 'center', lineHeight: 18 },
  fileRow: { flexDirection: 'row-reverse', justifyContent: 'space-between', alignItems: 'center', paddingHorizontal: 4, paddingTop: 12 },
  fileName: { color: '#A7A4B3', fontSize: 10, flex: 1, textAlign: 'right' },
  fileMeta: { color: '#B79AFF', fontSize: 10, marginLeft: 10 },
  importButton: { backgroundColor: '#B79AFF', borderRadius: 16, minHeight: 54, marginTop: 14, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 9 },
  importPlus: { color: '#17121F', fontSize: 23, fontWeight: '300', marginTop: -2 },
  importText: { color: '#17121F', fontSize: 14, fontWeight: '900' },
  sectionHeader: { marginTop: 30, marginBottom: 13, flexDirection: 'row-reverse', justifyContent: 'space-between', alignItems: 'center' },
  sectionTitle: { color: '#F5F1FC', fontSize: 18, fontWeight: '900', textAlign: 'right' },
  sectionSub: { color: '#858494', fontSize: 11, marginTop: 4, textAlign: 'right' },
  sectionMark: { color: '#625B75', fontSize: 9, fontWeight: '800', letterSpacing: 1 },
  panel: { backgroundColor: '#171821', borderRadius: 19, padding: 16, borderWidth: 1, borderColor: '#242531', marginBottom: 10 },
  panelTitle: { color: '#F2EFF8', fontSize: 13, fontWeight: '800', textAlign: 'right', marginBottom: 14 },
  toolsRow: { flexDirection: 'row-reverse', justifyContent: 'space-between', gap: 7 },
  toolButton: { flex: 1, minHeight: 77, backgroundColor: '#22232E', borderRadius: 13, alignItems: 'center', justifyContent: 'center', gap: 7, borderWidth: 1, borderColor: '#2C2D39' },
  toolActive: { backgroundColor: '#302742', borderColor: '#8F76C2' },
  toolIcon: { color: '#D5C5F7', fontSize: 23 },
  toolLabel: { color: '#B8B4C4', fontSize: 9, fontWeight: '700' },
  panelTitleRow: { flexDirection: 'row-reverse', justifyContent: 'space-between', alignItems: 'center' },
  comingSoon: { color: '#B79AFF', fontSize: 9, backgroundColor: '#292338', paddingHorizontal: 9, paddingVertical: 5, borderRadius: 10 },
  adjustment: { marginTop: 5, marginBottom: 8 },
  adjustmentTop: { flexDirection: 'row-reverse', justifyContent: 'space-between', alignItems: 'center' },
  adjustmentLabel: { color: '#C7C3D0', fontSize: 11 },
  adjustmentValue: { color: '#B79AFF', fontSize: 10, fontWeight: '700' },
  helperText: { color: '#777687', fontSize: 9, textAlign: 'right', lineHeight: 15, marginTop: 4 },
  saveButton: { backgroundColor: '#D4C3FF', borderRadius: 16, minHeight: 54, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 9 },
  saveIcon: { color: '#17121F', fontSize: 21, fontWeight: '900' },
  saveText: { color: '#17121F', fontSize: 13, fontWeight: '900' },
  footer: { color: '#5F5D6A', fontSize: 10, textAlign: 'center', marginTop: 22 },
});
