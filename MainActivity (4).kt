package com.jk.music

import android.Manifest
import android.app.AlertDialog
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.media.MediaMetadataRetriever
import android.view.animation.LinearInterpolator
import android.graphics.Path
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.documentfile.provider.DocumentFile
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class MainActivity : ComponentActivity() {
  private lateinit var sp: SharedPreferences
  private var ctl: MediaController? = null
  private val names = ArrayList<String>()
  private val uris = ArrayList<String>()
  private val ui = Handler(Looper.getMainLooper())
  private lateinit var list: ListView
  private lateinit var titleV: TextView
  private lateinit var seek: SeekBar
  private lateinit var time: TextView
  private lateinit var playBox: FrameLayout
  private var drag = false
  private var accent = 0
  private var bg = 0
  private var playC = 0
  private var hasPc = false
  private var maxVol = 100
  private var vol = 70
  private var skin = 0
  private var shapeMode = 0
  private var target = ""
  private var playSize = 72
  private var wave: WaveView? = null
  private var cover: ImageView? = null
  private var coverAnim: ObjectAnimator? = null
  private var customTf: Typeface? = null
  private var fontI = 0
  private var cov = 1
  private val artists = ArrayList<String>()
  private val albums = ArrayList<String>()
  private val favs = HashSet<String>()
  private val plists = LinkedHashMap<String, ArrayList<String>>()
  private val qmap = ArrayList<Int>()
  private val rowText = ArrayList<String>()
  private val rowKind = ArrayList<Int>()
  private val rowIdx = ArrayList<Int>()
  private val groupNames = ArrayList<String>()
  private var tab = 0
  private var sub: String? = null
  private var heart: TextView? = null
  private var mc = 0xFF1E1B2E.toInt()
  private var ma = 92
  private var panelA = 100
  private var menuT = Color.WHITE
  private var curDlg: AlertDialog? = null
  private var curRoot: View? = null
  private val pal = intArrayOf(0xFF5EE0C0.toInt(), 0xFFFF7A90.toInt(), 0xFFFFC857.toInt(), 0xFF7AA2FF.toInt(), 0xFFC28CFF.toInt(), 0xFF9BE564.toInt(), 0xFFFFFFFF.toInt(), 0xFF17141F.toInt(), 0xFF000000.toInt(), 0xFF2B1A55.toInt())
  private val skinNames = arrayOf("Moderno", "iPod Nano", "iPod Clásico", "Consola", "Pixel morado", "Ventana clásica")
  private val devT = intArrayOf(0, 0xFF5B3A9E.toInt(), 0xFF9A9A9A.toInt(), 0xFF2A2A2E.toInt(), 0xFF4A2F86.toInt(), 0xFFD4D0C8.toInt())
  private val devB = intArrayOf(0, 0xFF2B1A55.toInt(), 0xFF4A4A4A.toInt(), 0xFF2A2A2E.toInt(), 0xFF1E0F40.toInt(), 0xFFD4D0C8.toInt())
  private val scrC = intArrayOf(0, 0xFF000000.toInt(), 0xFFE6E6E6.toInt(), 0xFF000000.toInt(), 0xFF1A0B33.toInt(), 0xFFFFFFFF.toInt())
  private val scrT = intArrayOf(0, 0xFFE9E2FF.toInt(), 0xFF1B1B1B.toInt(), 0xFFEEEEEE.toInt(), 0xFFD9C8FF.toInt(), 0xFF000000.toInt())
  private val whC = intArrayOf(0, 0xFF3A2468.toInt(), 0xFF222222.toInt(), 0xFF38383D.toInt(), 0, 0)
  private val whT = intArrayOf(0, 0xFFCBB8FF.toInt(), 0xFFEEEEEE.toInt(), 0xFFDDDDDD.toInt(), 0xFFCBB8FF.toInt(), 0xFF222222.toInt())
  private val ctrC = intArrayOf(0, 0xFF5A3D9C.toInt(), 0xFF6B6B6B.toInt(), 0xFF4A4A50.toInt(), 0xFF5A3D9C.toInt(), 0xFF6B6B6B.toInt())

  private val pickFiles = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { r -> addAll(r) }
  private val pickDir = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u -> if (u != null) scan(u) }
  private val pickFont = registerForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
    if (u != null) try {
      contentResolver.openInputStream(u)?.use { i -> File(filesDir, "font.ttf").outputStream().use { o -> i.copyTo(o) } }
      customTf = null
      Typeface.createFromFile(File(filesDir, "font.ttf"))
      fontI = 5; theme()
    } catch (e: Exception) { Toast.makeText(this, "Esa fuente no sirve", Toast.LENGTH_SHORT).show() }
  }
  private val pickImg = registerForActivityResult(ActivityResultContracts.OpenDocument()) { u -> if (u != null) saveImg(u) }

  override fun onCreate(b: Bundle?) {
    super.onCreate(b)
    sp = getSharedPreferences("p", 0)
    accent = sp.getInt("a", 0xFF5EE0C0.toInt())
    bg = sp.getInt("b", 0xFF17141F.toInt())
    hasPc = sp.contains("pc")
    playC = sp.getInt("pc", accent)
    vol = sp.getInt("v", 70)
    maxVol = sp.getInt("m", 100)
    skin = sp.getInt("skin", 0)
    shapeMode = sp.getInt("shape", 0)
    fontI = sp.getInt("font", 0)
    cov = sp.getInt("cov", 1)
    try {
      val a = JSONArray(sp.getString("list", "[]"))
      for (i in 0 until a.length()) { val o = a.getJSONObject(i); uris.add(o.getString("u")); names.add(o.getString("n")); artists.add(o.optString("ar", "")); albums.add(o.optString("al", "")) }
    } catch (e: Exception) {}
    try {
      val fv = JSONArray(sp.getString("favs", "[]"))
      for (i in 0 until fv.length()) favs.add(fv.getString(i))
      val pj = JSONObject(sp.getString("pl", "{}"))
      for (k in pj.keys()) { val ja = pj.getJSONArray(k); val l = ArrayList<String>(); for (i in 0 until ja.length()) l.add(ja.getString(i)); plists[k] = l }
    } catch (e: Exception) {}
    mc = sp.getInt("mc", mc); ma = sp.getInt("ma", 92); panelA = sp.getInt("pa", 100)
    setContentView(buildUi())
    if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
    val f = MediaController.Builder(this, SessionToken(this, ComponentName(this, PlayService::class.java))).buildAsync()
    f.addListener({
      val c = f.get()
      ctl = c
      c.addListener(lis)
      qmap.clear()
      if (c.mediaItemCount == 0 && uris.isNotEmpty()) { c.setMediaItems(uris.indices.map { item(uris[it], names[it]) }); c.prepare(); qmap.addAll(uris.indices) }
      else for (i in 0 until c.mediaItemCount) qmap.add(uris.indexOf(c.getMediaItemAt(i).localConfiguration?.uri?.toString()))
      applyVol(); refreshPlay(); upd(); fetchMeta()
    }, ContextCompat.getMainExecutor(this))
    ui.post(tick)
  }

  override fun onDestroy() { ui.removeCallbacks(tick); ctl?.release(); super.onDestroy() }

  private val lis = object : Player.Listener {
    override fun onIsPlayingChanged(p: Boolean) { refreshPlay() }
    override fun onMediaItemTransition(m: MediaItem?, r: Int) { upd() }
  }
  private val tick = object : Runnable {
    override fun run() {
      ctl?.let { c ->
        val d = c.duration
        if (d > 0 && !drag) seek.progress = (c.currentPosition * 1000 / d).toInt()
        time.text = fmt(c.currentPosition) + " / " + fmt(if (d > 0) d else 0)
      }
      ui.postDelayed(this, 500)
    }
  }

  private fun fmt(ms: Long): String { val s = ms / 1000; return String.format("%d:%02d", s / 60, s % 60) }
  private fun dp(x: Int) = (x * resources.displayMetrics.density).toInt()
  private fun on(c: Int) = if (ColorUtils.calculateLuminance(c) > 0.5) Color.BLACK else Color.WHITE
  private fun fg() = on(bg)
  private fun card() = if (fg() == Color.WHITE) 0x33FFFFFF else 0x22000000
  private fun st() = if (skin == 0) fg() else scrT[skin]
  private fun pcol() = if (hasPc) playC else if (skin == 0) accent else ctrC[skin]
  private fun item(u: String, n: String) = MediaItem.Builder().setUri(u).setMediaMetadata(MediaMetadata.Builder().setTitle(n).build()).build()
  private fun cur(): Int { val i = ctl?.currentMediaItemIndex ?: -1; return qmap.getOrNull(i) ?: -1 }
  private fun pa(c: Int) = ColorUtils.setAlphaComponent(c, Color.alpha(c) * panelA / 100)
  private fun applyVol() { ctl?.volume = vol / 100f * maxVol / 100f }
  private fun upd() { titleV.text = names.getOrNull(cur()) ?: "Nada en reproducción"; refreshList(); refreshCover(); updHeart() }
  private fun img(key: String): Bitmap? { val f = File(filesDir, "ic_" + key); return if (f.exists()) BitmapFactory.decodeFile(f.path) else null }

  private fun save() {
    val a = JSONArray()
    for (i in uris.indices) a.put(JSONObject().put("u", uris[i]).put("n", names[i]).put("ar", artists[i]).put("al", albums[i]))
    val e = sp.edit().putString("list", a.toString()).putInt("a", accent).putInt("b", bg).putInt("v", vol).putInt("m", maxVol).putInt("skin", skin).putInt("shape", shapeMode).putInt("font", fontI).putInt("cov", cov)
    e.putString("favs", JSONArray(favs.toList()).toString())
    val pj = JSONObject(); for ((k, v) in plists) pj.put(k, JSONArray(v)); e.putString("pl", pj.toString())
    e.putInt("mc", mc).putInt("ma", ma).putInt("pa", panelA)
    if (hasPc) e.putInt("pc", playC)
    e.apply()
  }
  private fun theme() { save(); setContentView(buildUi()); upd() }

  private fun saveImg(u: Uri) {
    try {
      val mx = if (target == "bg") 1280 else 256
      val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      contentResolver.openInputStream(u)?.use { BitmapFactory.decodeStream(it, null, o) }
      var s = 1
      while (o.outWidth / s > mx * 2 || o.outHeight / s > mx * 2) s *= 2
      val bmp = contentResolver.openInputStream(u)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = s }) } ?: return
      val k = minOf(1f, mx / maxOf(bmp.width, bmp.height).toFloat())
      val sc = Bitmap.createScaledBitmap(bmp, maxOf(1, (bmp.width * k).toInt()), maxOf(1, (bmp.height * k).toInt()), true)
      File(filesDir, "ic_" + target).outputStream().use { sc.compress(if (target == "bg") Bitmap.CompressFormat.JPEG else Bitmap.CompressFormat.PNG, 90, it) }
      sp.edit().remove("z_" + target).remove("x_" + target).remove("y_" + target).apply()
      theme()
    } catch (e: Exception) { Toast.makeText(this, "No se pudo leer esa imagen", Toast.LENGTH_SHORT).show() }
  }

  private fun addUri(u: Uri, n: String) {
    val name = n.substringBeforeLast('.')
    uris.add(u.toString()); names.add(name); artists.add(""); albums.add("")
  }
  private fun keep(u: Uri) { try { contentResolver.takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (e: Exception) {} }
  private fun addAll(r: List<Uri>) {
    for (u in r) { keep(u); addUri(u, DocumentFile.fromSingleUri(this, u)?.name ?: "Canción") }
    save(); upd(); fetchMeta()
  }
  private fun walk(d: DocumentFile) {
    for (f in d.listFiles()) {
      if (f.isDirectory) walk(f)
      else if ((f.type ?: "").startsWith("audio/")) addUri(f.uri, f.name ?: "Canción")
    }
  }
  private fun scan(t: Uri) {
    keep(t)
    DocumentFile.fromTreeUri(this, t)?.let { walk(it) }
    save(); upd(); fetchMeta()
  }

  private fun makeAdapter() = object : ArrayAdapter<String>(this, 0, rowText) {
    override fun getView(p: Int, v: View?, g: ViewGroup): View {
      val t = (v as? TextView) ?: TextView(context)
      t.text = rowText[p]; t.textSize = 16f; t.maxLines = 1; t.ellipsize = TextUtils.TruncateAt.END
      t.setPadding(dp(12), dp(12), dp(12), dp(12))
      if (fontI != 0) t.setTypeface(font(), Typeface.NORMAL)
      val hi = rowKind[p] == 0 && rowIdx[p] == cur()
      t.setTextColor(if (hi || rowKind[p] >= 2) accent else st())
      t.setBackgroundColor(if (hi) ((accent and 0x00FFFFFF) or 0x33000000) else 0)
      return t
    }
  }

  // Botones: color liso o imagen propia recortada en la forma elegida
  private fun bshape(c: Int, size: Int) = GradientDrawable().apply {
    if (shapeMode == 1) { shape = GradientDrawable.RECTANGLE; cornerRadius = dp(size) * 0.22f } else shape = GradientDrawable.OVAL
    setColor(c)
  }
  private fun fill(s: FrameLayout, key: String, def: String, size: Int, c: Int, tc: Int, f: (View) -> Unit) {
    s.removeAllViews(); s.background = null; s.clipToOutline = false
    s.setOnClickListener { v -> f(v) }
    val cus = img(key)
    val em = sp.getString("e_" + key, null)
    if (cus == null && em != null) {
      s.background = bshape(c, size)
      s.addView(TextView(this).apply { text = em; textSize = if (size > 60) 34f else 24f; gravity = Gravity.CENTER }, FrameLayout.LayoutParams(-1, -1))
      return
    }
    val bmp = cus ?: (if (skin >= 4) pixIcon(key, dp(size)) else null)
    if (bmp != null && cus == null) {
      s.addView(ImageView(this).apply { setImageBitmap(bmp); scaleType = ImageView.ScaleType.FIT_CENTER }, FrameLayout.LayoutParams(-1, -1))
      return
    }
    if (bmp == null) {
      s.background = bshape(c, size)
      s.addView(TextView(this).apply {
        text = def; textSize = if (size > 60) 26f else if (def.length > 2) 12f else 20f
        typeface = if (fontI != 0) Typeface.create(font(), Typeface.BOLD) else Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setTextColor(tc)
      }, FrameLayout.LayoutParams(-1, -1))
      return
    }
    val zm = sp.getInt("z_" + key, 100) / 100f
    val px = dp(size) * (zm - 1f) / 2f
    if (shapeMode != 2) { s.background = bshape(0x01FFFFFF, size); s.clipToOutline = true }
    s.addView(ImageView(this).apply {
      setImageBitmap(bmp)
      scaleType = if (shapeMode == 2) ImageView.ScaleType.FIT_CENTER else ImageView.ScaleType.CENTER_CROP
      scaleX = zm; scaleY = zm
      translationX = (50 - sp.getInt("x_" + key, 50)) / 50f * px
      translationY = (50 - sp.getInt("y_" + key, 50)) / 50f * px
    }, FrameLayout.LayoutParams(-1, -1))
  }
  // Iconos pixel: se dibujan en 16x16 y se amplían sin suavizar
  private fun pixIcon(key: String, px: Int): Bitmap? {
    if (key !in listOf("play", "pause", "prev", "next", "shuf", "rep")) return null
    val bm = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
    val cv = Canvas(bm)
    val p = Paint().apply { isAntiAlias = false; color = 0xFF8C6BD9.toInt(); strokeWidth = 2f }
    fun tri(ax: Float, bx: Float, t: Float, b: Float) {
      val path = Path(); path.moveTo(ax, (t + b) / 2); path.lineTo(bx, t); path.lineTo(bx, b); path.close()
      cv.drawPath(path, p)
    }
    when (key) {
      "prev" -> { tri(1f, 7.5f, 2f, 14f); tri(8f, 14.5f, 2f, 14f) }
      "next" -> { tri(7.5f, 1f, 2f, 14f); tri(14.5f, 8f, 2f, 14f) }
      "pause" -> { cv.drawRect(3f, 2f, 6.5f, 14f, p); cv.drawRect(9.5f, 2f, 13f, 14f, p) }
      "play" -> { p.color = 0xFF3E8BFF.toInt(); cv.drawCircle(8f, 8f, 6.5f, p); p.color = Color.WHITE; tri(11.5f, 6f, 4.5f, 11.5f) }
      "shuf" -> { cv.drawLine(1f, 12f, 10f, 4f, p); cv.drawLine(1f, 4f, 10f, 12f, p); tri(14.5f, 10f, 1.5f, 6.5f); tri(14.5f, 10f, 9.5f, 14.5f) }
      else -> { cv.drawLine(1f, 4f, 11f, 4f, p); tri(14.5f, 10.5f, 1f, 7f); cv.drawLine(4f, 12f, 14f, 12f, p); tri(1f, 5.5f, 8.5f, 14.5f) }
    }
    val a = IntArray(256); bm.getPixels(a, 0, 16, 0, 0, 16, 16)
    val o = a.copyOf()
    for (y in 0 until 16) for (x in 0 until 16) {
      if ((a[y * 16 + x] ushr 24) == 0) {
        val n = (x > 0 && (a[y * 16 + x - 1] ushr 24) != 0) || (x < 15 && (a[y * 16 + x + 1] ushr 24) != 0) ||
          (y > 0 && (a[(y - 1) * 16 + x] ushr 24) != 0) || (y < 15 && (a[(y + 1) * 16 + x] ushr 24) != 0)
        if (n) o[y * 16 + x] = 0xFF1E0F40.toInt()
      }
    }
    bm.setPixels(o, 0, 16, 0, 0, 16, 16)
    val s = maxOf(16, px / 16 * 16)
    return Bitmap.createScaledBitmap(bm, s, s, false)
  }
  private fun btn(key: String, def: String, size: Int, c: Int, tc: Int, f: (View) -> Unit) = FrameLayout(this).apply {
    layoutParams = LinearLayout.LayoutParams(dp(size), dp(size)).apply { setMargins(dp(6), 0, dp(6), 0) }
    fill(this, key, def, size, c, tc, f)
  }
  private fun togglePlay() {
    ctl?.let { if (it.isPlaying) it.pause() else { if (it.playbackState == Player.STATE_IDLE) it.prepare(); it.play() } }
  }
  private fun refreshPlay() {
    val p = ctl?.isPlaying == true
    wave?.active = p
    coverAnim?.let { if (p) it.resume() else it.pause() }
    fill(playBox, if (p) "pause" else "play", if (p) "⏸" else "▶", playSize, pcol(), on(pcol())) { _ -> togglePlay() }
  }

  private fun txt(t: String, f: () -> Unit) = TextView(this).apply {
    text = t; textSize = 22f; setTextColor(fg()); setPadding(dp(8), dp(8), dp(8), dp(8)); setOnClickListener { f() }
  }
  private fun sb(max: Int, p: Int, f: (Int) -> Unit) = SeekBar(this).apply {
    this.max = max; progress = p
    progressTintList = ColorStateList.valueOf(accent); thumbTintList = ColorStateList.valueOf(accent)
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(s: SeekBar?, v: Int, u: Boolean) { if (u) f(v) }
      override fun onStartTrackingTouch(s: SeekBar?) {}
      override fun onStopTrackingTouch(s: SeekBar?) {}
    })
  }
  private fun box(c: Int) = LinearLayout(this).apply {
    orientation = LinearLayout.VERTICAL; setPadding(dp(10), dp(10), dp(10), dp(10))
    background = GradientDrawable().apply { setColor(pa(c)); cornerRadius = dp(12).toFloat() }
  }

  private fun buildUi(): View {
    wave = null; cover = null; coverAnim?.cancel(); coverAnim = null
    val outer = FrameLayout(this).apply { setBackgroundColor(bg) }
    img("bg")?.let { b ->
      outer.addView(ImageView(this).apply { setImageBitmap(b); scaleType = ImageView.ScaleType.CENTER_CROP }, FrameLayout.LayoutParams(-1, -1))
      outer.addView(View(this).apply { setBackgroundColor(0x66000000) }, FrameLayout.LayoutParams(-1, -1))
    }
    val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; fitsSystemWindows = true; setPadding(dp(12), dp(8), dp(12), dp(12)) }
    outer.addView(root, FrameLayout.LayoutParams(-1, -1))
    val head = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
    head.addView(TextView(this).apply { text = "JKMUSICK"; textSize = 20f; setTextColor(fg()); typeface = Typeface.DEFAULT_BOLD }, LinearLayout.LayoutParams(0, -2, 1f))
    head.addView(txt("＋") { pickFiles.launch(arrayOf("audio/*")) })
    head.addView(txt("📁") { pickDir.launch(null) })
    head.addView(txt("⏲") { sleepDlg() })
    val hv = txt("♡") { val i = cur(); if (i in uris.indices) toggleFav(i) }
    heart = hv
    head.addView(hv)
    head.addView(txt("⚙") { settings() })
    root.addView(head)
    root.addView(group(arrayOf("Todas", "♥", "Álbumes", "Artistas", "Listas"), tab, fg()) { tab = it; sub = null; refreshList() })

    list = ListView(this).apply {
      divider = null
      adapter = makeAdapter()
      setOnItemClickListener { _, _, p, _ -> rowTap(p) }
      setOnItemLongClickListener { _, _, p, _ -> rowMenu(p); true }
    }
    titleV = TextView(this).apply { textSize = 17f; typeface = if (skin >= 4) Typeface.create(Typeface.MONOSPACE, Typeface.BOLD) else Typeface.DEFAULT_BOLD; setTextColor(st()); maxLines = 1; ellipsize = TextUtils.TruncateAt.END; setPadding(0, dp(8), 0, dp(4)); text = "Nada en reproducción" }
    seek = SeekBar(this).apply {
      max = 1000
      progressTintList = ColorStateList.valueOf(accent); thumbTintList = ColorStateList.valueOf(accent)
      setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(s: SeekBar?, v: Int, u: Boolean) {}
        override fun onStartTrackingTouch(s: SeekBar?) { drag = true }
        override fun onStopTrackingTouch(s: SeekBar?) { drag = false; ctl?.let { c -> if (c.duration > 0) c.seekTo(c.duration * (s?.progress ?: 0) / 1000) } }
      })
    }
    time = TextView(this).apply { setTextColor(st()); alpha = .7f; gravity = Gravity.END; text = "0:00 / 0:00" }

    val bc = if (skin == 0) card() else 0
    val bt = if (skin == 0) fg() else whT[skin]
    val ns = if (skin == 0) 52 else 56
    val sh = btn("shuf", "⤮", 44, bc, bt) { v -> ctl?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled; v.alpha = if (it.shuffleModeEnabled) 1f else .5f } }
    val pv = btn("prev", "⏮", ns, bc, bt) { ctl?.seekToPrevious() }
    val nx = btn("next", "⏭", ns, bc, bt) { ctl?.seekToNext() }
    val rp = btn("rep", "⟳", 44, bc, bt) { v ->
      ctl?.let {
        val m = when (it.repeatMode) { Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL; Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE; else -> Player.REPEAT_MODE_OFF }
        it.repeatMode = m; v.alpha = if (m == Player.REPEAT_MODE_OFF) .5f else 1f
        ((v as FrameLayout).getChildAt(0) as? TextView)?.text = if (m == Player.REPEAT_MODE_ONE) "①" else "⟳"
      }
    }
    sh.alpha = .5f; rp.alpha = .5f
    playSize = if (skin == 0 || skin >= 4) 72 else 84
    playBox = FrameLayout(this)
    val vr = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(8), 0, 0) }

    if (skin == 0) {
      root.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
      addCover(root, 120)
      root.addView(titleV); root.addView(seek); root.addView(time)
      val row = LinearLayout(this).apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, dp(8)) }
      row.addView(sh); row.addView(pv)
      row.addView(playBox, LinearLayout.LayoutParams(dp(72), dp(72)).apply { setMargins(dp(6), 0, dp(6), 0) })
      row.addView(nx); row.addView(rp)
      root.addView(row)
      vr.addView(TextView(this).apply { text = "🔊"; textSize = 18f; setPadding(0, 0, dp(8), 0) })
    } else if (skin <= 3) {
      val dev = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(14))
        background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(pa(devT[skin]), pa(devB[skin]))).apply { cornerRadius = dp(28).toFloat() }
      }
      if (skin == 3) {
        val a = box(scrC[skin]); addCover(a, 96); a.addView(titleV); a.addView(seek); a.addView(time)
        val l = box(scrC[skin]); l.addView(list, LinearLayout.LayoutParams(-1, -1))
        dev.addView(a)
        dev.addView(l, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(10) })
      } else {
        val s = box(scrC[skin]); addCover(s, 96); s.addView(titleV); s.addView(seek); s.addView(time)
        s.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
        dev.addView(s, LinearLayout.LayoutParams(-1, 0, 1f))
      }
      val wh = FrameLayout(this).apply {
        background = GradientDrawable().apply { setColor(whC[skin]); if (skin == 3) cornerRadius = dp(70).toFloat() else shape = GradientDrawable.OVAL }
      }
      val menuB = btn("menu", "MENU", 56, 0, bt) { settings() }
      wh.addView(menuB, FrameLayout.LayoutParams(dp(56), dp(56), Gravity.TOP or Gravity.CENTER_HORIZONTAL).apply { topMargin = dp(8) })
      wh.addView(pv, FrameLayout.LayoutParams(dp(56), dp(56), Gravity.START or Gravity.CENTER_VERTICAL).apply { marginStart = dp(8) })
      wh.addView(nx, FrameLayout.LayoutParams(dp(56), dp(56), Gravity.END or Gravity.CENTER_VERTICAL).apply { marginEnd = dp(8) })
      wh.addView(sh, FrameLayout.LayoutParams(dp(44), dp(44), Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL).apply { bottomMargin = dp(10) })
      wh.addView(playBox, FrameLayout.LayoutParams(dp(84), dp(84), Gravity.CENTER))
      dev.addView(wh, LinearLayout.LayoutParams(dp(220), dp(220)).apply { gravity = Gravity.CENTER_HORIZONTAL; topMargin = dp(12) })
      vr.addView(rp)
      dev.addView(vr)
      root.addView(dev, LinearLayout.LayoutParams(-1, 0, 1f))
    } else {
      val dev = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(10), dp(10), dp(10), dp(10))
        background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(pa(devT[skin]), pa(devB[skin]))).apply { cornerRadius = dp(if (skin == 4) 10 else 2).toFloat() }
      }
      if (skin == 5) {
        val tb = LinearLayout(this).apply {
          gravity = Gravity.CENTER_VERTICAL; setPadding(dp(8), dp(4), dp(4), dp(4))
          background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(0xFF0A246A.toInt(), 0xFF3A6EA5.toInt()))
        }
        tb.addView(TextView(this).apply { text = "JKMUSICK"; setTextColor(Color.WHITE); typeface = Typeface.DEFAULT_BOLD }, LinearLayout.LayoutParams(0, -2, 1f))
        for (g in arrayOf("_", "□", "✕")) tb.addView(TextView(this).apply {
          text = g; setTextColor(Color.BLACK); gravity = Gravity.CENTER; typeface = Typeface.DEFAULT_BOLD
          background = GradientDrawable().apply { setColor(0xFFD4D0C8.toInt()) }; setPadding(dp(8), 0, dp(8), 0)
        }, LinearLayout.LayoutParams(-2, dp(20)).apply { marginStart = dp(3) })
        dev.addView(tb, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
      }
      val s = box(scrC[skin]); addCover(s, 96); s.addView(titleV)
      if (skin == 4) { val w = WaveView(this).apply { col = accent }; wave = w; s.addView(w, LinearLayout.LayoutParams(-1, dp(64))) }
      s.addView(seek); s.addView(time)
      dev.addView(s)
      val l = box(scrC[skin]); l.addView(list, LinearLayout.LayoutParams(-1, -1))
      dev.addView(l, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(8) })
      val row = LinearLayout(this).apply { gravity = Gravity.CENTER; setPadding(0, dp(10), 0, dp(4)) }
      row.addView(sh); row.addView(pv)
      row.addView(playBox, LinearLayout.LayoutParams(dp(72), dp(72)).apply { setMargins(dp(6), 0, dp(6), 0) })
      row.addView(nx); row.addView(rp)
      dev.addView(row)
      vr.addView(TextView(this).apply { text = "🔊"; textSize = 18f; setPadding(0, 0, dp(8), 0) })
      dev.addView(vr)
      root.addView(dev, LinearLayout.LayoutParams(-1, 0, 1f))
    }
    vr.addView(sb(100, vol) { vol = it; applyVol(); save() }, LinearLayout.LayoutParams(0, -2, 1f))
    if (skin == 0) root.addView(vr)
    refreshPlay()
    refreshList(); updHeart()
    if (fontI != 0) applyFont(outer)
    return outer
  }

  // Fuentes, emojis y portadas
  private fun font(): Typeface = when (fontI) {
    1 -> Typeface.SERIF
    2 -> Typeface.MONOSPACE
    3 -> Typeface.create("cursive", Typeface.NORMAL)
    4 -> Typeface.create("casual", Typeface.NORMAL)
    5 -> { if (customTf == null) customTf = try { Typeface.createFromFile(File(filesDir, "font.ttf")) } catch (e: Exception) { Typeface.DEFAULT }; customTf!! }
    else -> Typeface.DEFAULT
  }
  private fun applyFont(v: View) {
    if (v is TextView) v.setTypeface(font(), if (v.typeface?.isBold == true) Typeface.BOLD else Typeface.NORMAL)
    if (v is ViewGroup) for (i in 0 until v.childCount) applyFont(v.getChildAt(i))
  }
  private fun askEmoji(label: String, key: String) {
    val et = EditText(this).apply { hint = "Escribe o pega un emoji"; textSize = 24f; setPadding(dp(20), dp(12), dp(20), dp(12)) }
    AlertDialog.Builder(this).setTitle("Emoji para «" + label + "»").setView(et)
      .setPositiveButton("Guardar") { _, _ -> val t = et.text.toString().trim(); if (t.isNotEmpty()) { sp.edit().putString("e_" + key, t).apply(); theme() } }
      .setNegativeButton("Cancelar", null).show()
  }
  private fun art(i: Int): Bitmap? {
    try {
      val r = MediaMetadataRetriever()
      r.setDataSource(this, Uri.parse(uris[i]))
      val b = r.embeddedPicture
      r.release()
      if (b != null) {
        val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(b, 0, b.size, o)
        val so = BitmapFactory.Options().apply { inSampleSize = maxOf(1, maxOf(o.outWidth, o.outHeight) / 512) }
        return BitmapFactory.decodeByteArray(b, 0, b.size, so)
      }
    } catch (e: Exception) {}
    return null
  }
  private fun genArt(seed: String): Bitmap {
    val bm = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
    val c = Canvas(bm)
    val h = (seed.hashCode() and 0x7fffffff) % 360
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.shader = LinearGradient(0f, 0f, 256f, 256f, Color.HSVToColor(floatArrayOf(h.toFloat(), .6f, .9f)), Color.HSVToColor(floatArrayOf(((h + 60) % 360).toFloat(), .8f, .4f)), Shader.TileMode.CLAMP)
    c.drawRect(0f, 0f, 256f, 256f, p)
    val t = Paint(Paint.ANTI_ALIAS_FLAG); t.color = 0xCCFFFFFF.toInt(); t.textSize = 130f; t.textAlign = Paint.Align.CENTER
    c.drawText("♪", 128f, 170f, t)
    return bm
  }
  private fun square(b: Bitmap, s: Int): Bitmap {
    val m = minOf(b.width, b.height)
    return Bitmap.createScaledBitmap(Bitmap.createBitmap(b, (b.width - m) / 2, (b.height - m) / 2, m, m), s, s, true)
  }
  private fun addCover(g: LinearLayout, size: Int) {
    if (cov == 0) return
    val v = ImageView(this).apply { scaleType = ImageView.ScaleType.FIT_CENTER }
    cover = v
    g.addView(v, LinearLayout.LayoutParams(dp(size), dp(size)).apply { gravity = Gravity.CENTER_HORIZONTAL; setMargins(0, dp(4), 0, dp(4)) })
  }
  private fun refreshCover() {
    val v = cover ?: return
    val i = cur()
    val src = (if (i in uris.indices) art(i) else null) ?: genArt(names.getOrNull(i) ?: "JKMUSICK")
    v.background = null; v.clipToOutline = false
    coverAnim?.cancel(); coverAnim = null; v.rotation = 0f
    when (cov) {
      2 -> {
        v.setImageBitmap(square(src, 400))
        v.background = GradientDrawable().apply { cornerRadius = dp(24).toFloat(); setColor(0x01FFFFFF) }
        v.clipToOutline = true
      }
      3 -> {
        val z = 400
        val out = Bitmap.createBitmap(z, z, Bitmap.Config.ARGB_8888)
        val c = Canvas(out); val p = Paint(Paint.ANTI_ALIAS_FLAG)
        c.drawCircle(z / 2f, z / 2f, z / 2f, p)
        p.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN); c.drawBitmap(square(src, z), 0f, 0f, p)
        p.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR); c.drawCircle(z / 2f, z / 2f, z * 0.07f, p)
        p.xfermode = null; p.style = Paint.Style.STROKE; p.strokeWidth = 8f; p.color = 0x66000000; c.drawCircle(z / 2f, z / 2f, z / 2f - 4f, p)
        v.setImageBitmap(out)
        coverAnim = ObjectAnimator.ofFloat(v, "rotation", 0f, 360f).apply {
          duration = 8000; repeatCount = ValueAnimator.INFINITE; interpolator = LinearInterpolator()
          start(); if (ctl?.isPlaying != true) pause()
        }
      }
      4 -> v.setImageBitmap(Bitmap.createScaledBitmap(Bitmap.createScaledBitmap(square(src, 400), 28, 28, true), 392, 392, false))
      else -> v.setImageBitmap(square(src, 400))
    }
  }

  // Biblioteca: favoritas, álbumes, artistas y listas
  private fun updHeart() {
    val f = cur() in uris.indices && favs.contains(uris[cur()])
    heart?.text = if (f) "♥" else "♡"; heart?.setTextColor(if (f) accent else fg())
  }
  private fun toggleFav(i: Int) { val u = uris[i]; if (!favs.remove(u)) favs.add(u); save(); refreshList(); updHeart() }
  private fun subSongs(s: String): List<Int> = when (tab) {
    2 -> uris.indices.filter { albums[it] == s }
    3 -> uris.indices.filter { artists[it] == s }
    else -> (plists[s] ?: ArrayList<String>()).mapNotNull { u -> uris.indexOf(u).takeIf { it >= 0 } }
  }
  private fun refreshList() {
    rowText.clear(); rowKind.clear(); rowIdx.clear(); groupNames.clear()
    fun song(i: Int) { rowText.add((if (favs.contains(uris[i])) "♥ " else "") + names[i]); rowKind.add(0); rowIdx.add(i) }
    val s = sub
    if (tab == 0) { for (i in uris.indices) song(i) }
    else if (tab == 1) { for (i in uris.indices) if (favs.contains(uris[i])) song(i) }
    else if (s == null) {
      if (tab == 4) { rowText.add("＋ Nueva lista"); rowKind.add(3); rowIdx.add(-1) }
      val g = LinkedHashMap<String, Int>()
      if (tab == 4) { for ((k, v) in plists) g[k] = v.size }
      else for (i in uris.indices) { val k = if (tab == 2) albums[i] else artists[i]; if (k.isNotEmpty()) g[k] = (g[k] ?: 0) + 1 }
      for (k in g.keys.sorted()) { groupNames.add(k); rowText.add(k + "  (" + g[k] + ")"); rowKind.add(1); rowIdx.add(groupNames.size - 1) }
    } else {
      rowText.add("‹ Volver"); rowKind.add(2); rowIdx.add(-1)
      for (i in subSongs(s)) song(i)
    }
    (list.adapter as? BaseAdapter)?.notifyDataSetChanged()
  }
  private fun rowTap(p: Int) {
    when (rowKind[p]) {
      0 -> {
        val ids = ArrayList<Int>(); var pos = 0
        for (k in rowKind.indices) if (rowKind[k] == 0) { if (k == p) pos = ids.size; ids.add(rowIdx[k]) }
        val c = ctl ?: return
        qmap.clear(); qmap.addAll(ids)
        c.setMediaItems(ids.map { item(uris[it], names[it]) }, pos, 0L); c.prepare(); c.play()
      }
      1 -> { sub = groupNames[rowIdx[p]]; refreshList() }
      2 -> { sub = null; refreshList() }
      else -> newList(null)
    }
  }
  private fun rowMenu(p: Int) {
    menuT = on(mc)
    val ops = ArrayList<String>(); val acts = ArrayList<() -> Unit>()
    if (rowKind[p] == 1 && tab == 4) {
      val n = groupNames[rowIdx[p]]
      ops.add("Borrar la lista"); acts.add { plists.remove(n); save(); refreshList() }
      showMenu(n, menuOptions(ops, acts)); return
    }
    if (rowKind[p] != 0) return
    val i = rowIdx[p]; val u = uris[i]; val s = sub
    ops.add(if (favs.contains(u)) "Quitar de favoritas" else "♥ Añadir a favoritas"); acts.add { toggleFav(i) }
    ops.add("Añadir a una lista…"); acts.add { addToList(i) }
    if (tab == 4 && s != null) { ops.add("Quitar de esta lista"); acts.add { plists[s]?.remove(u); save(); refreshList() } }
    ops.add("Quitar de la biblioteca"); acts.add { removeLib(i) }
    showMenu(names[i], menuOptions(ops, acts))
  }
  private fun removeLib(p: Int) {
    val u = uris[p]
    val j = qmap.indexOf(p)
    if (j >= 0) { qmap.removeAt(j); ctl?.removeMediaItem(j) }
    for (k in qmap.indices) if (qmap[k] > p) qmap[k] = qmap[k] - 1
    uris.removeAt(p); names.removeAt(p); artists.removeAt(p); albums.removeAt(p)
    favs.remove(u); for (l in plists.values) l.remove(u)
    save(); upd()
  }
  private fun addToList(i: Int) {
    menuT = on(mc)
    val u = uris[i]
    val ops = ArrayList<String>(); val acts = ArrayList<() -> Unit>()
    for (n in plists.keys.toList()) { ops.add(n); acts.add { val l = plists[n]; if (l != null && !l.contains(u)) l.add(u); save(); refreshList() } }
    ops.add("＋ Nueva lista"); acts.add { newList(u) }
    showMenu("Añadir a una lista", menuOptions(ops, acts))
  }
  private fun newList(u: String?) {
    menuT = on(mc)
    val et = EditText(this).apply { hint = "Nombre de la lista"; setTextColor(menuT); setHintTextColor(0x88888888.toInt()) }
    val v = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    v.addView(et)
    v.addView(act("Crear") {
      val n = et.text.toString().trim()
      if (n.isNotEmpty()) {
        val l = plists.getOrPut(n) { ArrayList() }
        if (u != null && !l.contains(u)) l.add(u)
        save(); curDlg?.dismiss(); refreshList()
      }
    })
    showMenu("Nueva lista", v)
  }
  private fun fetchMeta() {
    val todo = uris.indices.filter { artists[it].isEmpty() && albums[it].isEmpty() }.map { uris[it] }
    if (todo.isEmpty()) return
    Thread {
      val res = HashMap<String, Array<String>>()
      for (u in todo) {
        var ar = "Desconocido"; var al = "Desconocido"
        try {
          val r = MediaMetadataRetriever()
          r.setDataSource(this, Uri.parse(u))
          val a1 = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST); if (!a1.isNullOrBlank()) ar = a1.trim()
          val a2 = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM); if (!a2.isNullOrBlank()) al = a2.trim()
          r.release()
        } catch (e: Exception) {}
        res[u] = arrayOf(ar, al)
      }
      runOnUiThread {
        for ((u, m) in res) { val i = uris.indexOf(u); if (i >= 0) { artists[i] = m[0]; albums[i] = m[1] } }
        save(); refreshList()
      }
    }.start()
  }

  // Menús de color con transparencia
  private fun dlgBg() {
    curDlg?.window?.setBackgroundDrawable(GradientDrawable().apply { setColor(ColorUtils.setAlphaComponent(mc, 255 * ma / 100)); cornerRadius = dp(20).toFloat() })
  }
  private fun tintAll(v: View) {
    if (v is TextView && v.tag != "chip" && v !is EditText) v.setTextColor(menuT)
    if (v is ViewGroup) for (i in 0 until v.childCount) tintAll(v.getChildAt(i))
  }
  private fun menuOptions(ops: List<String>, acts: List<() -> Unit>): View {
    val l = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    for (i in ops.indices) l.addView(TextView(this).apply {
      text = ops[i]; textSize = 16f; setPadding(dp(4), dp(12), dp(4), dp(12))
      setOnClickListener { curDlg?.dismiss(); acts[i]() }
    })
    return l
  }
  private fun showMenu(title: String, v: View) {
    menuT = on(mc)
    val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(16), dp(20), dp(8)) }
    col.addView(TextView(this).apply { text = title; textSize = 18f; typeface = Typeface.DEFAULT_BOLD })
    col.addView(v)
    col.addView(act("Cerrar") { curDlg?.dismiss() })
    val sv = ScrollView(this).apply { addView(col) }
    tintAll(sv)
    if (fontI != 0) applyFont(sv)
    curRoot = sv
    val d = AlertDialog.Builder(this).setView(sv).create()
    curDlg = d
    d.show()
    dlgBg()
    d.window?.setDimAmount(if (ma < 60) 0.15f else 0.5f)
  }
  private fun sleepDlg() {
    menuT = on(mc)
    val ops = ArrayList<String>(); val acts = ArrayList<() -> Unit>()
    for (m in intArrayOf(15, 30, 45, 60, 90)) {
      ops.add(m.toString() + " minutos")
      acts.add { PlayService.sleep(m); Toast.makeText(this, "La música se detendrá en " + m + " min", Toast.LENGTH_SHORT).show() }
    }
    ops.add("Cancelar el temporizador")
    acts.add { PlayService.sleep(0); Toast.makeText(this, "Temporizador cancelado", Toast.LENGTH_SHORT).show() }
    showMenu("Temporizador para dormir", menuOptions(ops, acts))
  }
  private fun eqDlg() {
    val e = PlayService.eq
    if (e == null) { Toast.makeText(this, "El ecualizador aún no está listo. Reproduce una canción e inténtalo otra vez.", Toast.LENGTH_LONG).show(); return }
    menuT = on(mc)
    val n = e.numberOfBands.toInt()
    val lo = e.bandLevelRange[0].toInt(); val hi = e.bandLevelRange[1].toInt()
    val levels = FloatArray(n) { (sp.getInt("eq" + it, 0) - lo).toFloat() / (hi - lo) }
    val bars = EqView(this)
    bars.levels = levels; bars.active = ctl?.isPlaying == true
    val v = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    v.addView(bars, LinearLayout.LayoutParams(-1, dp(90)))
    val sbs = ArrayList<SeekBar>()
    fun setLevel(i: Int, l: Int) {
      sp.edit().putInt("eq" + i, l).apply(); PlayService.setBand(i, l)
      levels[i] = (l - lo).toFloat() / (hi - lo); bars.levels = levels
    }
    v.addView(group(arrayOf("Plano", "Graves", "Agudos", "Voz"), -1, menuT) { pr ->
      for (i in 0 until n) {
        val t = if (n > 1) i / (n - 1f) else 0f
        val g = when (pr) { 1 -> 6f * (1f - 2f * t); 2 -> 6f * (2f * t - 1f); 3 -> 5f * (1f - Math.abs(2f * t - 1f)); else -> 0f }
        val l = (maxOf(g, if (pr == 1 || pr == 2) -2f else g) * 100).toInt().coerceIn(lo, hi)
        setLevel(i, l); sbs[i].progress = (l - lo) / 100
      }
    })
    for (i in 0 until n) {
      val f = e.getCenterFreq(i.toShort()) / 1000
      v.addView(TextView(this).apply { text = if (f >= 1000) (f / 1000).toString() + " kHz" else f.toString() + " Hz" })
      val b = sb((hi - lo) / 100, (sp.getInt("eq" + i, 0) - lo) / 100) { setLevel(i, lo + it * 100) }
      sbs.add(b); v.addView(b)
    }
    showMenu("Ecualizador", v)
  }

  // Ajustes
  private fun lab(t: String) = TextView(this).apply { text = t; textSize = 15f; typeface = Typeface.DEFAULT_BOLD; setPadding(0, dp(14), 0, dp(2)); setTextColor(menuT) }
  private fun act(t: String, f: () -> Unit) = TextView(this).apply {
    text = t; tag = "chip"; setPadding(dp(12), dp(8), dp(12), dp(8)); setTextColor(menuT)
    background = GradientDrawable().apply { cornerRadius = dp(16).toFloat(); setColor(0x55888888) }
    layoutParams = LinearLayout.LayoutParams(-2, -2).apply { setMargins(dp(6), dp(4), 0, dp(4)) }
    setOnClickListener { f() }
  }
  private fun group(opts: Array<String>, cur: Int, tc: Int = menuT, f: (Int) -> Unit): View {
    val row = LinearLayout(this)
    val cs = ArrayList<TextView>()
    fun paint(sel: Int) {
      for (i in cs.indices) { cs[i].setTextColor(if (i == sel) on(accent) else tc); (cs[i].background as GradientDrawable).setColor(if (i == sel) accent else 0x33888888) }
    }
    for (i in opts.indices) {
      val t = TextView(this).apply {
        text = opts[i]; tag = "chip"; setPadding(dp(12), dp(8), dp(12), dp(8))
        background = GradientDrawable().apply { cornerRadius = dp(16).toFloat() }
        layoutParams = LinearLayout.LayoutParams(-2, -2).apply { setMargins(0, dp(6), dp(8), 0) }
      }
      t.setOnClickListener { paint(i); f(i) }
      cs.add(t); row.addView(t)
    }
    paint(cur)
    return HorizontalScrollView(this).apply { addView(row) }
  }
  private fun imgRow(label: String, key: String): View {
    val r = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
    r.addView(TextView(this).apply { text = label }, LinearLayout.LayoutParams(0, -2, 1f))
    r.addView(act("Elegir") { target = key; pickImg.launch(arrayOf("image/*")) })
    if (key != "bg") r.addView(act("Emoji") { askEmoji(label, key) })
    r.addView(act("Quitar") {
      File(filesDir, "ic_" + key).delete()
      sp.edit().remove("z_" + key).remove("x_" + key).remove("y_" + key).remove("e_" + key).apply(); theme()
    })
    return r
  }
  private fun sw(label: String, set: (Int) -> Unit): View {
    val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    col.addView(lab(label))
    val row = LinearLayout(this)
    for (c in pal) row.addView(View(this).apply {
      background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(c); setStroke(dp(1), 0x55888888) }
      layoutParams = LinearLayout.LayoutParams(dp(34), dp(34)).apply { setMargins(0, dp(6), dp(8), 0) }
      setOnClickListener { set(c) }
    })
    col.addView(HorizontalScrollView(this).apply { addView(row) })
    return col
  }

  private fun settings() {
    menuT = on(mc)
    val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(8), dp(20), dp(4)) }
    box.addView(act("Ecualizador…") { eqDlg() })
    box.addView(act("Temporizador para dormir…") { sleepDlg() })
    box.addView(lab("Estilo del reproductor"))
    box.addView(group(skinNames, skin) { skin = it; theme() })
    box.addView(lab("Portada de las canciones"))
    box.addView(group(arrayOf("Sin portada", "Cuadrada", "Redondeada", "Disco", "Pixel"), cov) { cov = it; theme() })
    box.addView(lab("Fuente de texto"))
    box.addView(group(arrayOf("Normal", "Serif", "Mono", "Cursiva", "Casual", "Mi fuente"), fontI) { fontI = it; theme() })
    box.addView(act("Elegir archivo de fuente (.ttf)") { pickFont.launch(arrayOf("*/*")) })
    box.addView(sw("Color principal") { accent = it; theme() })
    box.addView(sw("Color de fondo") { bg = it; theme() })
    box.addView(imgRow("Imagen de fondo", "bg"))
    box.addView(sw("Color del botón de reproducir") { playC = it; hasPc = true; theme() })
    box.addView(act("Automático") { hasPc = false; sp.edit().remove("pc").apply(); theme() })
    box.addView(sw("Color de los menús") { mc = it; menuT = on(mc); curRoot?.let { r -> tintAll(r) }; dlgBg(); save() })
    val mo = TextView(this).apply { text = "Opacidad de los menús: " + ma + "%"; setPadding(0, dp(10), 0, 0) }
    box.addView(mo)
    box.addView(sb(90, ma - 10) { ma = it + 10; mo.text = "Opacidad de los menús: " + ma + "%"; dlgBg(); save() })
    val po = TextView(this).apply { text = "Opacidad de los paneles: " + panelA + "%"; setPadding(0, dp(10), 0, 0) }
    box.addView(po)
    box.addView(sb(90, panelA - 10) { panelA = it + 10; po.text = "Opacidad de los paneles: " + panelA + "%"; theme() })
    box.addView(lab("Forma de los botones con imagen"))
    box.addView(group(arrayOf("Círculo", "Redondeado", "Sin recorte"), shapeMode) { shapeMode = it; theme() })
    box.addView(lab("Imágenes y emojis de los botones"))
    val keys = arrayOf("play", "pause", "prev", "next", "shuf", "rep", "menu")
    val labs = arrayOf("Reproducir", "Pausa", "Anterior", "Siguiente", "Aleatorio", "Repetir", "Menú")
    for (i in keys.indices) box.addView(imgRow(labs[i], keys[i]))
    box.addView(lab("Ajustar imagen: zoom, horizontal y vertical"))
    var ai = 0
    val sz = sb(200, 0) { sp.edit().putInt("z_" + keys[ai], it + 100).apply(); theme() }
    val sx = sb(100, 50) { sp.edit().putInt("x_" + keys[ai], it).apply(); theme() }
    val sy = sb(100, 50) { sp.edit().putInt("y_" + keys[ai], it).apply(); theme() }
    val spn = Spinner(this).apply {
      adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, labs)
      onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
          ai = pos
          sz.progress = sp.getInt("z_" + keys[pos], 100) - 100
          sx.progress = sp.getInt("x_" + keys[pos], 50)
          sy.progress = sp.getInt("y_" + keys[pos], 50)
        }
        override fun onNothingSelected(p: AdapterView<*>?) {}
      }
    }
    box.addView(spn); box.addView(sz); box.addView(sx); box.addView(sy)
    val ml = TextView(this).apply { text = "Volumen máximo: " + maxVol + "%"; setPadding(0, dp(14), 0, 0) }
    box.addView(ml)
    box.addView(sb(90, maxVol - 10) { maxVol = it + 10; ml.text = "Volumen máximo: " + maxVol + "%"; applyVol(); save() })
    showMenu("Personalizar", box)
  }
}

private class WaveView(c: android.content.Context) : View(c) {
  var active = false
    set(v) { field = v; invalidate() }
  var col = 0xFFB79CFF.toInt()
  private val p = Paint()
  override fun onDraw(cv: Canvas) {
    val n = 40
    val w = width / n.toFloat()
    val t = System.currentTimeMillis() / 120.0
    p.color = col
    for (i in 0 until n) {
      val base = Math.abs(Math.sin(i * 1.7)) * 0.6 + 0.15
      val h = if (active) base * (0.6 + 0.4 * Math.abs(Math.sin(t + i * 0.9))) else base * 0.5
      val bh = (h * height).toFloat()
      cv.drawRect(i * w + w * 0.15f, (height - bh) / 2, (i + 1) * w - w * 0.15f, (height + bh) / 2, p)
    }
    if (active) postInvalidateOnAnimation()
  }
}

private class EqView(c: android.content.Context) : View(c) {
  var levels = FloatArray(0)
    set(v) { field = v; invalidate() }
  var active = false
  private val p = Paint()
  override fun onDraw(cv: Canvas) {
    val n = levels.size
    if (n == 0) return
    val w = width / n.toFloat()
    val rows = 12
    val bh = height / rows.toFloat()
    val t = System.currentTimeMillis() / 150.0
    for (i in 0 until n) {
      val lv = levels[i] * (if (active) 0.85f + 0.15f * Math.sin(t + i * 1.3).toFloat() else 1f)
      val lit = Math.round(lv * rows).coerceIn(1, rows)
      for (r in 0 until lit) {
        p.color = ColorUtils.blendARGB(0xFF8C6BD9.toInt(), 0xFF3E8BFF.toInt(), r / (rows - 1f))
        val top = height - (r + 1) * bh
        cv.drawRect(i * w + w * 0.12f, top + 2f, (i + 1) * w - w * 0.12f, top + bh - 1f, p)
      }
    }
    if (active) postInvalidateOnAnimation()
  }
}
