package com.jk.music

import android.Manifest
import android.app.AlertDialog
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.ComponentName
import android.content.ContentUris
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
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
import android.view.MotionEvent
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
  private var query = ""
  private var searching = false
  private var searchBox: EditText? = null
  private val plays = HashMap<String, Int>()
  private val mixU = ArrayList<String>()
  private var totalMs = 0L
  private var lastIdx = -2
  private var acc = 0L
  private var counted = false
  private var sub: String? = null
  private var heart: IconView? = null
  private var appName = "JKMUSICK"
  private var wmpHue = 275
  private var wmpTab = 0
  private var wmpViz = 0
  private var muted = false
  private var wmpShow: ((Int) -> Unit)? = null
  private var wmpMax: (() -> Unit)? = null
  private var heart2: IconView? = null
  private var thumb: ImageView? = null
  private var devV: LinearLayout? = null
  private var vuV: VuView? = null
  private var caseC = 0xFFC9CDD1.toInt()
  private var floatOn = false
  private var npAnim: ValueAnimator? = null
  private var sortMode = 2
  private val addedMap = HashMap<String, Long>()
  private val nk = HashMap<String, String>()
  private var npOpen = false
  private var outerV: FrameLayout? = null
  private var npView: View? = null
  private var npCover: ImageView? = null
  private var npTitle: TextView? = null
  private var npArtist: TextView? = null
  private var npLine: TextView? = null
  private var npSeek: SeekBar? = null
  private var npCur: TextView? = null
  private var npDur: TextView? = null
  private var npPlay: FrameLayout? = null
  private var npHeart: IconView? = null
  private var npShuf: IconView? = null
  private var npBg = 0xFF2A1A3A.toInt()
  private var npDrag = false
  private var autoNp = true
  private var artBusy: String? = null
  private var mc = 0xFF1E1B2E.toInt()
  private var ma = 92
  private var panelA = 100
  private var menuT = Color.WHITE
  private var curDlg: AlertDialog? = null
  private var curRoot: View? = null
  private val pal = intArrayOf(0xFF5EE0C0.toInt(), 0xFFFF7A90.toInt(), 0xFFFFC857.toInt(), 0xFF7AA2FF.toInt(), 0xFFC28CFF.toInt(), 0xFF9BE564.toInt(), 0xFFFFFFFF.toInt(), 0xFF17141F.toInt(), 0xFF000000.toInt(), 0xFF2B1A55.toInt())
  private val skinNames = arrayOf("Moderno", "iPod Nano", "iPod Clásico", "Consola", "Pixel morado", "Ventana clásica", "Winamp retro", "AIMP azul", "Retro morado", "Widget minimalista", "Windows Media Player")
  private val devT = intArrayOf(0, 0xFF5B3A9E.toInt(), 0xFF9A9A9A.toInt(), 0xFF2A2A2E.toInt(), 0xFF4A2F86.toInt(), 0xFFD4D0C8.toInt(), 0xFFE9ECEC.toInt(), 0xFFD9DEE6.toInt(), 0xFF6B3FA0.toInt(), 0xFFDCEBFA.toInt(), 0xFF4B2A7A.toInt())
  private val devB = intArrayOf(0, 0xFF2B1A55.toInt(), 0xFF4A4A4A.toInt(), 0xFF2A2A2E.toInt(), 0xFF1E0F40.toInt(), 0xFFD4D0C8.toInt(), 0xFFB4BABB.toInt(), 0xFF98A2B2.toInt(), 0xFF2F1A55.toInt(), 0xFFC5DDF3.toInt(), 0xFF2F1A55.toInt())
  private val scrC = intArrayOf(0, 0xFF000000.toInt(), 0xFFE6E6E6.toInt(), 0xFF000000.toInt(), 0xFF1A0B33.toInt(), 0xFFFFFFFF.toInt(), 0xFF9DB6A7.toInt(), 0xFF1B3556.toInt(), 0xFF1A1030.toInt(), 0xFFBFD9F0.toInt(), 0xFF000000.toInt())
  private val scrT = intArrayOf(0, 0xFFE9E2FF.toInt(), 0xFF1B1B1B.toInt(), 0xFFEEEEEE.toInt(), 0xFFD9C8FF.toInt(), 0xFF000000.toInt(), 0xFF1B2B22.toInt(), 0xFFDDE9FF.toInt(), 0xFFE8DCFF.toInt(), 0xFF14325A.toInt(), 0xFF201030.toInt())
  private val whC = intArrayOf(0, 0xFF3A2468.toInt(), 0xFF222222.toInt(), 0xFF38383D.toInt(), 0, 0, 0, 0, 0, 0, 0)
  private val whT = intArrayOf(0, 0xFFCBB8FF.toInt(), 0xFFEEEEEE.toInt(), 0xFFDDDDDD.toInt(), 0xFFCBB8FF.toInt(), 0xFF222222.toInt(), 0xFF1F2A7A.toInt(), 0xFF1B3A6B.toInt(), 0xFFE8DCFF.toInt(), 0xFF1B3A6B.toInt(), 0xFF2F1A55.toInt())
  private val ctrC = intArrayOf(0, 0xFF5A3D9C.toInt(), 0xFF6B6B6B.toInt(), 0xFF4A4A50.toInt(), 0xFF5A3D9C.toInt(), 0xFF6B6B6B.toInt(), 0xFFE4E7E7.toInt(), 0xFFC7CFDB.toInt(), 0xFF4B2A7A.toInt(), 0xFF1B3A6B.toInt(), 0xFFD9D2E9.toInt())

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
  private val askAudio = registerForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
    if (ok) scanDevice() else Toast.makeText(this, "Sin permiso no puedo buscar tu música", Toast.LENGTH_LONG).show()
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
    autoNp = sp.getBoolean("anp", true); sortMode = sp.getInt("sort", 2)
    caseC = sp.getInt("case", 0xFFC9CDD1.toInt()); floatOn = sp.getBoolean("fbar", false)
    appName = sp.getString("aname", "JKMUSICK") ?: "JKMUSICK"; wmpHue = sp.getInt("wmph", 275); wmpViz = sp.getInt("wviz", 0)
    try { val ad = JSONObject(sp.getString("added", "{}")); for (k in ad.keys()) addedMap[k] = ad.getLong(k) } catch (e: Exception) {}
    shapeMode = sp.getInt("shape", 0)
    fontI = sp.getInt("font", 0)
    cov = sp.getInt("cov", 1)
    try {
      val a = JSONArray(sp.getString("list", "[]"))
      for (i in 0 until a.length()) { val o = a.getJSONObject(i); uris.add(o.getString("u")); names.add(cleanName(o.getString("n"))); artists.add(o.optString("ar", "")); albums.add(o.optString("al", "")) }
    } catch (e: Exception) {}
    try {
      val fv = JSONArray(sp.getString("favs", "[]"))
      for (i in 0 until fv.length()) favs.add(fv.getString(i))
      val pj = JSONObject(sp.getString("pl", "{}"))
      for (k in pj.keys()) { val ja = pj.getJSONArray(k); val l = ArrayList<String>(); for (i in 0 until ja.length()) l.add(ja.getString(i)); plists[k] = l }
    } catch (e: Exception) {}
    try { val pc = JSONObject(sp.getString("plays", "{}")); for (k in pc.keys()) plays[k] = pc.getInt(k) } catch (e: Exception) {}
    totalMs = sp.getLong("tms", 0L)
    mc = sp.getInt("mc", mc); ma = sp.getInt("ma", 92); panelA = sp.getInt("pa", 100)
    setContentView(buildUi())
    onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
      override fun handleOnBackPressed() { if (npOpen) closeNp() else if (searching) toggleSearch() else moveTaskToBack(true) }
    })
    if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
    if (uris.isEmpty() && !sp.getBoolean("asked", false)) ui.postDelayed({
      sp.edit().putBoolean("asked", true).apply()
      AlertDialog.Builder(this).setMessage("¿Quieres que busque todas las canciones de este dispositivo?").setPositiveButton("Buscar") { _, _ -> scanDeviceAsk() }.setNegativeButton("Ahora no", null).show()
    }, 1500)
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
        if (npOpen) {
          if (d > 0 && !npDrag) npSeek?.progress = (c.currentPosition * 1000 / d).toInt()
          npCur?.text = fmt(c.currentPosition); npDur?.text = fmt(if (d > 0) d else 0)
        }
        if (c.isPlaying) {
          val ci = cur()
          if (ci != lastIdx || (c.currentPosition < 1500 && acc > 5000)) { lastIdx = ci; acc = 0; counted = false }
          acc += 500; totalMs += 500
          if (!counted && acc >= 20000 && ci in uris.indices) {
            counted = true; plays[uris[ci]] = (plays[uris[ci]] ?: 0) + 1; save()
            if (tab == 5) refreshList()
          }
        }
      }
      ui.postDelayed(this, 500)
    }
  }

  private fun fmt(ms: Long): String { val s = ms / 1000; return String.format("%d:%02d", s / 60, s % 60) }
  private fun dp(x: Int) = (x * resources.displayMetrics.density).toInt()
  private fun on(c: Int) = if (ColorUtils.calculateLuminance(c) > 0.5) Color.BLACK else Color.WHITE
  private fun fg() = on(bg)
  private fun card() = if (fg() == Color.WHITE) 0x33FFFFFF else 0x22000000
  private fun st() = if (skin == 0 || skin == 9) fg() else if (skin == 10) 0xFF201030.toInt() else scrT[skin]
  private fun pcol() = if (hasPc) playC else if (skin == 0) accent else ctrC[skin]
  private fun item(u: String, n: String) = MediaItem.Builder().setUri(u).setMediaMetadata(MediaMetadata.Builder().setTitle(n).build()).build()
  private fun cur(): Int { val i = ctl?.currentMediaItemIndex ?: -1; return qmap.getOrNull(i) ?: -1 }
  private fun pa(c: Int) = ColorUtils.setAlphaComponent(c, Color.alpha(c) * panelA / 100)
  private fun applyVol() { ctl?.volume = if (muted) 0f else vol / 100f * maxVol / 100f }
  private fun upd() { titleV.text = names.getOrNull(cur())?.let { if (skin == 10) "Playing: " + it else it } ?: (if (skin == 10) "Ready" else "Nada en reproducción"); refreshList(); refreshCover(); updHeart(); refreshNp(); pushArt(); refreshThumb() }
  private fun img(key: String): Bitmap? { val f = File(filesDir, "ic_" + key); return if (f.exists()) BitmapFactory.decodeFile(f.path) else null }

  private fun save() {
    val a = JSONArray()
    for (i in uris.indices) a.put(JSONObject().put("u", uris[i]).put("n", names[i]).put("ar", artists[i]).put("al", albums[i]))
    val e = sp.edit().putString("list", a.toString()).putInt("a", accent).putInt("b", bg).putInt("v", vol).putInt("m", maxVol).putInt("skin", skin).putInt("shape", shapeMode).putInt("font", fontI).putInt("cov", cov)
    e.putString("favs", JSONArray(favs.toList()).toString())
    val pj = JSONObject(); for ((k, v) in plists) pj.put(k, JSONArray(v)); e.putString("pl", pj.toString())
    val pl2 = JSONObject(); for ((k, v) in plays) pl2.put(k, v)
    e.putString("plays", pl2.toString()).putLong("tms", totalMs)
    val ad2 = JSONObject(); for ((k, v) in addedMap) ad2.put(k, v)
    e.putString("added", ad2.toString()).putInt("sort", sortMode)
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
    val name = cleanName(n)
    uris.add(u.toString()); names.add(name); artists.add(""); albums.add("")
    addedMap[u.toString()] = System.currentTimeMillis()
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
      t.text = (if ((skin == 7 || skin == 8) && rowKind[p] == 0) "" + (p + 1) + ".  " else "") + rowText[p]; t.textSize = 16f; t.maxLines = 1; t.ellipsize = TextUtils.TruncateAt.END
      t.setPadding(dp(12), dp(12), dp(12), dp(12))
      if (fontI != 0) t.setTypeface(font(), Typeface.NORMAL)
      val hi = rowKind[p] == 0 && rowIdx[p] == cur()
      if (skin == 10) {
        t.setTextColor(if (hi) Color.WHITE else if (rowKind[p] >= 2) wc(.6f, .35f) else 0xFF201030.toInt())
        t.setBackgroundColor(if (hi) wc(.50f, .46f) else Color.WHITE)
        return t
      }
      t.setTextColor(if (hi || rowKind[p] >= 2) accent else st())
      t.setBackgroundColor(if (hi) ((accent and 0x00FFFFFF) or 0x33000000) else 0)
      return t
    }
  }

  // Botones: color liso o imagen propia recortada en la forma elegida
  private fun bshape(c: Int, size: Int) = GradientDrawable().apply {
    if (shapeMode == 1) { shape = GradientDrawable.RECTANGLE; cornerRadius = dp(size) * 0.22f } else shape = GradientDrawable.OVAL
    setColor(c)
    if (skin == 10 && c != 0) {   // botones plateados del estilo Windows Media Player
      setGradientType(GradientDrawable.RADIAL_GRADIENT); gradientRadius = dp(size) * 0.85f; setGradientCenter(.35f, .3f)
      setColors(intArrayOf(Color.WHITE, c, ColorUtils.blendARGB(c, Color.BLACK, .35f))); setStroke(dp(1), wc(.48f, .20f))
    }
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
    val bmp = cus ?: (if (skin == 4 || skin == 5) pixIcon(key, dp(size)) else null)
    if (bmp != null && cus == null) {
      s.addView(ImageView(this).apply { setImageBitmap(bmp); scaleType = ImageView.ScaleType.FIT_CENTER }, FrameLayout.LayoutParams(-1, -1))
      return
    }
    if (bmp == null) {
      s.background = bshape(c, size)
      if (key in listOf("play", "pause", "prev", "next", "shuf", "rep")) {
        s.addView(IconView(this, key, tc).apply { val q = dp(size) / 4; setPadding(q, q, q, q) }, FrameLayout.LayoutParams(-1, -1))
        return
      }
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
    layoutParams = LinearLayout.LayoutParams(dp(size), dp(size)).apply { val mg = if (skin == 9) 2 else 6; setMargins(dp(mg), 0, dp(mg), 0) }
    fill(this, key, def, size, c, tc, f)
  }
  private fun togglePlay() {
    ctl?.let { if (it.isPlaying) it.pause() else { if (it.playbackState == Player.STATE_IDLE) it.prepare(); it.play() } }
  }
  private fun refreshPlay() {
    val p = ctl?.isPlaying == true
    wave?.active = p
    coverAnim?.let { if (p) it.resume() else it.pause() }
    vuV?.active = p
    npPlayIcon()
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
    wave = null; cover = null; coverAnim?.cancel(); coverAnim = null; thumb = null; vuV = null; devV = null; heart2 = null
    val outer = FrameLayout(this).apply { setBackgroundColor(bg) }
    img("bg")?.let { b ->
      outer.addView(ImageView(this).apply { setImageBitmap(b); scaleType = ImageView.ScaleType.CENTER_CROP }, FrameLayout.LayoutParams(-1, -1))
      outer.addView(View(this).apply { setBackgroundColor(0x66000000) }, FrameLayout.LayoutParams(-1, -1))
    }
    val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; fitsSystemWindows = true; setPadding(dp(12), dp(8), dp(12), dp(12)) }
    outer.addView(root, FrameLayout.LayoutParams(-1, -1))
    val head = headBar(if (skin == 10) Color.WHITE else fg(), skin != 10)
    if (skin != 10) root.addView(head)
    val tabsV = group(arrayOf("Todas", "Favoritas", "Álbumes", "Artistas", "Listas", "Top", "Mix"), tab, if (skin == 10) wc(.7f, .14f) else fg()) { tab = it; sub = null; refreshList() }
    if (skin != 10) root.addView(tabsV)
    val sbx = EditText(this).apply {
      hint = "Buscar canción, artista o álbum"; setSingleLine(true); setTextColor(fg()); setHintTextColor(0x88888888.toInt())
      setText(query); visibility = if (searching) View.VISIBLE else View.GONE
      addTextChangedListener(object : android.text.TextWatcher {
        override fun afterTextChanged(e: android.text.Editable?) { val q = e?.toString() ?: ""; if (q != query) { query = q; refreshList() } }
        override fun beforeTextChanged(t: CharSequence?, a: Int, b: Int, c: Int) {}
        override fun onTextChanged(t: CharSequence?, a: Int, b: Int, c: Int) {}
      })
    }
    searchBox = sbx
    if (skin != 10) root.addView(sbx)

    list = ListView(this).apply {
      divider = null
      adapter = makeAdapter()
      setOnItemClickListener { _, _, p, _ -> rowTap(p) }
      setOnItemLongClickListener { _, _, p, _ -> rowMenu(p); true }
    }
    titleV = TextView(this).apply { textSize = 17f; typeface = if (skin in 4..8) Typeface.create(Typeface.MONOSPACE, Typeface.BOLD) else Typeface.DEFAULT_BOLD; setTextColor(st()); maxLines = 1; ellipsize = TextUtils.TruncateAt.END; setPadding(0, dp(8), 0, dp(4)); text = if (skin == 10) "Ready" else "Nada en reproducción" }
    seek = SeekBar(this).apply {
      max = 1000
      progressTintList = ColorStateList.valueOf(accent); thumbTintList = ColorStateList.valueOf(accent)
      setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(s: SeekBar?, v: Int, u: Boolean) {}
        override fun onStartTrackingTouch(s: SeekBar?) { drag = true }
        override fun onStopTrackingTouch(s: SeekBar?) { drag = false; ctl?.let { c -> if (c.duration > 0) c.seekTo(c.duration * (s?.progress ?: 0) / 1000) } }
      })
    }
    time = TextView(this).apply {
      setTextColor(st()); alpha = .7f; gravity = Gravity.END; text = "0:00 / 0:00"
      if (skin in 6..8) { textSize = 30f; typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD); gravity = Gravity.START; alpha = 1f }
    }
    titleV.setOnClickListener { openNp() }

    val bc = if (skin == 9) 0x221B3A6B else if (skin == 0) card() else if (skin >= 6) ctrC[skin] else 0
    val bt = if (skin == 0) fg() else whT[skin]
    val ns = if (skin == 0) 52 else if (skin == 9) 40 else 56
    val ss = if (skin == 9) 34 else 44
    val sh = btn("shuf", "⤮", ss, bc, bt) { v -> ctl?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled; v.alpha = if (it.shuffleModeEnabled) 1f else .5f } }
    val pv = btn("prev", "⏮", ns, bc, bt) { ctl?.seekToPrevious() }
    val nx = btn("next", "⏭", ns, bc, bt) { ctl?.seekToNext() }
    val rp = btn("rep", "⟳", ss, bc, bt) { v ->
      ctl?.let {
        val m = when (it.repeatMode) { Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL; Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE; else -> Player.REPEAT_MODE_OFF }
        it.repeatMode = m; v.alpha = if (m == Player.REPEAT_MODE_OFF) .5f else 1f
        val w0 = (v as FrameLayout).getChildAt(0)
        if (w0 is TextView) w0.text = if (m == Player.REPEAT_MODE_ONE) "①" else "⟳" else if (w0 is IconView) w0.kind = if (m == Player.REPEAT_MODE_ONE) "rep1" else "rep"
      }
    }
    sh.alpha = .5f; rp.alpha = .5f
    playSize = if (skin == 10) 50 else if (skin == 9) 56 else if (skin == 0 || skin >= 4) 72 else 84
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
      vr.addView(IconView(this, "vol", bt).apply { setPadding(0, 0, dp(6), 0) }, LinearLayout.LayoutParams(dp(30), dp(24)))
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
    } else if (skin == 9) {
      // Widget minimalista: tarjeta flotante con LCD azul claro, portada a la izquierda y controles circulares
      root.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
      val card = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(14), dp(16), dp(12)); elevation = dp(8).toFloat()
        background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(pa(devT[9]), pa(devB[9]))).apply { cornerRadius = dp(28).toFloat() }
      }
      val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
      val th = ImageView(this).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP; clipToOutline = true
        background = GradientDrawable().apply { cornerRadius = dp(18).toFloat(); setColor(0x331B3A6B) }
      }
      thumb = th
      top.addView(th, LinearLayout.LayoutParams(dp(72), dp(72)))
      val lcd = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(8), dp(14), dp(8))
        background = GradientDrawable().apply { cornerRadius = dp(16).toFloat(); setColor(scrC[9]) }
      }
      titleV.setTextColor(scrT[9]); time.setTextColor(scrT[9]); time.alpha = .8f; time.gravity = Gravity.START
      lcd.addView(titleV); lcd.addView(time)
      top.addView(lcd, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(12) })
      card.addView(top)
      card.addView(seek, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6) })
      val row = LinearLayout(this).apply { gravity = Gravity.CENTER; setPadding(0, dp(4), 0, dp(4)) }
      val hv2 = IconView(this, "heart", 0xFF1B3A6B.toInt()).apply { setPadding(dp(8), dp(8), dp(8), dp(8)); setOnClickListener { val i = cur(); if (i in uris.indices) toggleFav(i) } }
      heart2 = hv2
      val add = IconView(this, "plus", 0xFF1B3A6B.toInt()).apply { setPadding(dp(8), dp(8), dp(8), dp(8)); setOnClickListener { val i = cur(); if (i in uris.indices) addToList(i) } }
      row.addView(rp); row.addView(sh); row.addView(pv)
      row.addView(playBox, LinearLayout.LayoutParams(dp(56), dp(56)).apply { setMargins(dp(2), 0, dp(2), 0) })
      row.addView(nx)
      row.addView(hv2, LinearLayout.LayoutParams(dp(36), dp(36))); row.addView(add, LinearLayout.LayoutParams(dp(36), dp(36)))
      card.addView(row)
      vr.addView(IconView(this, "vol", bt).apply { setPadding(0, 0, dp(6), 0) }, LinearLayout.LayoutParams(dp(30), dp(24)))
      card.addView(vr)
      root.addView(card, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
    } else if (skin == 10) {
      // Estilo Windows Media Player (skin morado): ventana con barra de título, menús, pestañas y controles
      outer.addView(LeopardView(this, wmpHue), 0, FrameLayout.LayoutParams(-1, -1))
      val win = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(2), dp(2), dp(2), dp(2)); elevation = dp(8).toFloat(); clipToOutline = true
        background = GradientDrawable().apply { setColor(wc(.36f, .33f)); setStroke(dp(2), wc(.48f, .20f)); cornerRadius = dp(8).toFloat() }
      }
      val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(wc(.36f, .33f)) }
      // Barra de título: icono (tu imagen o la esfera azul), nombre y botones de ventana
      val tb = LinearLayout(this).apply {
        gravity = Gravity.CENTER_VERTICAL; setPadding(dp(6), dp(4), dp(4), dp(4))
        background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(wc(.50f, .52f), wc(.48f, .20f)))
      }
      val logo = img("logo")
      tb.addView(if (logo != null) ImageView(this).apply {
        setImageBitmap(square(logo, 96)); scaleType = ImageView.ScaleType.CENTER_CROP; clipToOutline = true
        background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(0x01FFFFFF) }
      } else OrbView(this), LinearLayout.LayoutParams(dp(24), dp(24)))
      tb.addView(TextView(this).apply {
        text = appName; textSize = 15f; setTextColor(Color.WHITE); typeface = Typeface.DEFAULT_BOLD; maxLines = 1; ellipsize = TextUtils.TruncateAt.END
        setShadowLayer(1f, 1f, 1f, 0x88000000.toInt()); setPadding(dp(8), 0, 0, 0)
      }, LinearLayout.LayoutParams(0, -2, 1f))
      val bMin = WinBtn(this, "min", wmpHue); val bMax = WinBtn(this, "max", wmpHue); val bX = WinBtn(this, "close", wmpHue)
      for (b in listOf(bMin, bMax, bX)) tb.addView(b, LinearLayout.LayoutParams(dp(28), dp(24)).apply { marginStart = dp(3) })
      win.addView(tb)
      var mini = false; var full = false
      bMin.setOnClickListener {
        mini = !mini; body.visibility = if (mini) View.GONE else View.VISIBLE
        win.layoutParams = (if (mini) LinearLayout.LayoutParams(-1, -2) else LinearLayout.LayoutParams(-1, 0, 1f)).apply { setMargins(dp(8), dp(4), dp(8), dp(8)) }
      }
      val toggleMax = {
        full = !full
        (win.layoutParams as LinearLayout.LayoutParams).apply { if (full) setMargins(-dp(8), -dp(4), -dp(8), -dp(8)) else setMargins(dp(8), dp(4), dp(8), dp(8)) }
        win.requestLayout()
      }
      wmpMax = toggleMax
      bMax.setOnClickListener { toggleMax() }
      bX.setOnClickListener { moveTaskToBack(true) }
      // Menús
      val mb = LinearLayout(this).apply { setPadding(dp(4), dp(2), dp(4), dp(2)); setBackgroundColor(wc(.45f, .90f)) }
      for (n in arrayOf("File", "View", "Play", "Tools", "Help")) mb.addView(TextView(this).apply {
        text = android.text.SpannableString(n).apply { setSpan(android.text.style.UnderlineSpan(), 0, 1, 0) }
        setTextColor(wc(.7f, .14f)); textSize = 14f; setPadding(dp(10), dp(8), dp(10), dp(8)); setOnClickListener { wmpMenu(n, this) }
      })
      body.addView(mb)
      // Barra de progreso plateada
      seek.progressTintList = ColorStateList.valueOf(0xFFD0D0D8.toInt()); seek.progressBackgroundTintList = ColorStateList.valueOf(wc(.40f, .26f)); seek.thumbTintList = ColorStateList.valueOf(Color.WHITE)
      val sr = LinearLayout(this).apply { setBackgroundColor(wc(.50f, .46f)); setPadding(dp(3), dp(2), dp(3), dp(2)) }
      sr.addView(seek, LinearLayout.LayoutParams(-1, -2))
      body.addView(sr)
      // Pestañas: Música actual / Biblioteca
      val tabsRow = LinearLayout(this).apply { setPadding(dp(5), dp(5), dp(5), 0) }
      val tv = arrayOf(TextView(this), TextView(this))
      tv[0].text = "Música actual"; tv[1].text = "Biblioteca"
      for (t in tv) { t.setPadding(dp(14), dp(7), dp(14), dp(7)); tabsRow.addView(t, LinearLayout.LayoutParams(-2, -2).apply { marginEnd = dp(2) }) }
      body.addView(tabsRow)
      val pane0 = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = GradientDrawable().apply { setColor(Color.BLACK); setStroke(dp(2), wc(.30f, .40f)) }
      }
      val wv = WaveView(this).apply { col = ColorUtils.HSLToColor(floatArrayOf((wmpHue + 25f) % 360f, 1f, .72f)); mode = wmpViz }
      wave = wv
      pane0.addView(wv, LinearLayout.LayoutParams(-1, 0, 1f))
      val stat = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setBackgroundColor(0xFF08040F.toInt()); setPadding(dp(10), dp(4), dp(10), dp(4)) }
      titleV.textSize = 12f; titleV.typeface = Typeface.DEFAULT; titleV.setPadding(0, 0, 0, 0); titleV.setTextColor(wc(.6f, .62f))
      time.textSize = 12f; time.setTextColor(wc(.6f, .62f)); time.alpha = 1f
      stat.addView(titleV, LinearLayout.LayoutParams(0, -2, 1f)); stat.addView(time)
      pane0.addView(stat)
      val pane1 = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(wc(.45f, .90f)); visibility = View.GONE }
      head.setBackgroundColor(wc(.50f, .46f)); head.setPadding(dp(4), 0, dp(4), 0)
      pane1.addView(head); pane1.addView(tabsV)
      sbx.setTextColor(wc(.7f, .14f)); pane1.addView(sbx)
      list.setBackgroundColor(Color.WHITE)
      pane1.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
      val panes = FrameLayout(this).apply { setPadding(dp(5), 0, dp(5), dp(5)) }
      panes.addView(pane0, FrameLayout.LayoutParams(-1, -1)); panes.addView(pane1, FrameLayout.LayoutParams(-1, -1))
      fun paint() {
        for (k in 0..1) {
          val on = wmpTab == k; val r = dp(5).toFloat()
          tv[k].setTextColor(if (on) wc(.7f, .14f) else 0xFFE8DCFF.toInt()); tv[k].typeface = if (on) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
          tv[k].background = GradientDrawable().apply { setColor(if (on) wc(.45f, .90f) else wc(.34f, .27f)); setStroke(dp(1), wc(.48f, .20f)); cornerRadii = floatArrayOf(r, r, r, r, 0f, 0f, 0f, 0f) }
        }
      }
      // Cambio de pestaña con un efecto escalonado, como las ventanas de los 2000
      val show = { k: Int ->
        wmpTab = k
        pane0.visibility = if (k == 0) View.VISIBLE else View.GONE; pane1.visibility = if (k == 1) View.VISIBLE else View.GONE
        paint()
        val pn = if (k == 0) pane0 else pane1
        pn.scaleX = .965f; pn.scaleY = .965f; pn.alpha = .2f
        pn.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(220).setInterpolator(android.animation.TimeInterpolator { t -> (t * 4).toInt() / 4f }).start()
      }
      wmpShow = show
      tv[0].setOnClickListener { show(0) }; tv[1].setOnClickListener { show(1) }
      if (wmpTab == 1) { pane0.visibility = View.GONE; pane1.visibility = View.VISIBLE }
      paint()
      body.addView(panes, LinearLayout.LayoutParams(-1, 0, 1f))
      // Barra de controles inferior: play circular, stop, anterior, siguiente, silencio y volumen
      body.addView(View(this).apply { setBackgroundColor(wc(.60f, .72f)) }, LinearLayout.LayoutParams(-1, dp(2)))
      val cbar = LinearLayout(this).apply {
        gravity = Gravity.CENTER_VERTICAL; setPadding(dp(8), dp(8), dp(8), dp(9))
        background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(wc(.50f, .50f), wc(.48f, .20f)))
      }
      cbar.addView(playBox, LinearLayout.LayoutParams(dp(50), dp(50)))
      cbar.addView(wmpBtn("stop", 22, 34) { ctl?.let { it.pause(); it.seekTo(0) } })
      cbar.addView(wmpBtn("prev", 32, 22) { ctl?.seekToPrevious() })
      cbar.addView(wmpBtn("next", 32, 22) { ctl?.seekToNext() })
      cbar.addView(wmpBtn(if (muted) "mute" else "vol", 32, 22) { v -> muted = !muted; applyVol(); ((v as FrameLayout).getChildAt(0) as IconView).kind = if (muted) "mute" else "vol" })
      cbar.addView(sb(100, vol) { vol = it; applyVol(); save() }, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(6) })
      body.addView(cbar)
      win.addView(body, LinearLayout.LayoutParams(-1, 0, 1f))
      root.addView(win, LinearLayout.LayoutParams(-1, 0, 1f).apply { setMargins(dp(8), dp(4), dp(8), dp(8)) })
    } else {
      val dev = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(10), dp(10), dp(10), dp(10))
        background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(pa(devT[skin]), pa(devB[skin]))).apply { cornerRadius = dp(if (skin == 5) 2 else 16).toFloat() }
      }
      if (skin == 6) { devV = dev; dev.background = caseDrawable() }
      if (skin == 5 || skin == 7) {
        val tb = LinearLayout(this).apply {
          gravity = Gravity.CENTER_VERTICAL; setPadding(dp(8), dp(4), dp(4), dp(4))
          background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(0xFF0A246A.toInt(), 0xFF3A6EA5.toInt()))
        }
        tb.addView(TextView(this).apply { text = appName; setTextColor(Color.WHITE); typeface = Typeface.DEFAULT_BOLD }, LinearLayout.LayoutParams(0, -2, 1f))
        for (g in arrayOf("_", "□", "✕")) tb.addView(TextView(this).apply {
          text = g; setTextColor(Color.BLACK); gravity = Gravity.CENTER; typeface = Typeface.DEFAULT_BOLD
          background = GradientDrawable().apply { setColor(0xFFD4D0C8.toInt()) }; setPadding(dp(8), 0, dp(8), 0)
        }, LinearLayout.LayoutParams(-2, dp(20)).apply { marginStart = dp(3) })
        dev.addView(tb, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
      }
      val s = box(scrC[skin]); addCover(s, 96); s.addView(titleV)
      if (skin == 4 || skin == 6 || skin == 8) { val w = WaveView(this).apply { col = if (skin == 6) 0xFF1B2B22.toInt() else accent }; wave = w; s.addView(w, LinearLayout.LayoutParams(-1, dp(64))) }
      if (skin == 6) { val vu = VuView(this); vuV = vu; s.addView(vu, LinearLayout.LayoutParams(-1, dp(22))) }
      s.addView(seek); s.addView(time)
      dev.addView(s)
      if (skin == 6) {
        val eb = box(scrC[6]); eb.addView(EqSliders(this, sp, 0xFF1B2B22.toInt()), LinearLayout.LayoutParams(-1, dp(110)))
        dev.addView(eb, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
      }
      val l = box(scrC[skin]); l.addView(list, LinearLayout.LayoutParams(-1, -1))
      dev.addView(l, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(8) })
      val row = LinearLayout(this).apply { gravity = Gravity.CENTER; setPadding(0, dp(10), 0, dp(4)) }
      row.addView(sh); row.addView(pv)
      row.addView(playBox, LinearLayout.LayoutParams(dp(72), dp(72)).apply { setMargins(dp(6), 0, dp(6), 0) })
      row.addView(nx); row.addView(rp)
      dev.addView(row)
      vr.addView(IconView(this, "vol", bt).apply { setPadding(0, 0, dp(6), 0) }, LinearLayout.LayoutParams(dp(30), dp(24)))
      dev.addView(vr)
      root.addView(dev, LinearLayout.LayoutParams(-1, 0, 1f))
    }
    vr.addView(sb(100, vol) { vol = it; applyVol(); save() }, LinearLayout.LayoutParams(0, -2, 1f))
    if (skin == 0) root.addView(vr)
    refreshPlay()
    refreshList(); updHeart()
    outerV = outer
    if (npOpen) { val nv = buildNp(); npView = nv; outer.addView(nv, FrameLayout.LayoutParams(-1, -1)); refreshNp(); npPlayIcon() }
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
    heart?.filled = f; heart?.col = if (f) accent else (if (skin == 10) Color.WHITE else fg())
    heart2?.filled = f; heart2?.col = if (f) accent else 0xFF1B3A6B.toInt()
  }
  private fun toggleFav(i: Int) { val u = uris[i]; if (!favs.remove(u)) favs.add(u); save(); refreshList(); updHeart() }
  private fun subSongs(s: String): List<Int> = when (tab) {
    2 -> uris.indices.filter { albums[it] == s }
    3 -> uris.indices.filter { artists[it] == s }
    else -> (plists[s] ?: ArrayList<String>()).mapNotNull { u -> uris.indexOf(u).takeIf { it >= 0 } }
  }
  private fun refreshList() {
    rowText.clear(); rowKind.clear(); rowIdx.clear(); groupNames.clear()
    fun song(i: Int) { rowText.add((if (favs.contains(uris[i])) "♥ " else "") + names[i] + (if (tab == 5) "  ·  " + (plays[uris[i]] ?: 0) + "×" else "")); rowKind.add(0); rowIdx.add(i) }
    val s = sub
    if (query.isNotBlank()) {
      val q = norm(query)
      for (i in ordered(uris.indices.filter { norm(names[it]).contains(q) || norm(artists[it]).contains(q) || norm(albums[it]).contains(q) })) song(i)
      if (rowText.isEmpty()) { rowText.add("Sin resultados para «" + query + "»"); rowKind.add(5); rowIdx.add(-1) }
    }
    else if (tab == 0) { for (i in ordered(uris.indices.toList())) song(i) }
    else if (tab == 1) { for (i in ordered(uris.indices.filter { favs.contains(uris[it]) })) song(i) }
    else if (tab == 5) {
      for (i in uris.indices.filter { (plays[uris[it]] ?: 0) > 0 }.sortedByDescending { plays[uris[it]] ?: 0 }) song(i)
      if (rowText.isEmpty()) { rowText.add("Aún no hay datos: una canción cuenta cuando suena 20 segundos."); rowKind.add(5); rowIdx.add(-1) }
    }
    else if (tab == 6) {
      if (mixU.isEmpty()) genMix()
      rowText.add("↻ Generar otro mix"); rowKind.add(4); rowIdx.add(-1)
      for (u in mixU) { val i = uris.indexOf(u); if (i >= 0) song(i) }
    }
    else if (s == null) {
      if (tab == 4) { rowText.add("＋ Nueva lista"); rowKind.add(3); rowIdx.add(-1) }
      val g = LinkedHashMap<String, Int>()
      if (tab == 4) { for ((k, v) in plists) g[k] = v.size }
      else for (i in uris.indices) { val k = if (tab == 2) albums[i] else artists[i]; if (k.isNotEmpty()) g[k] = (g[k] ?: 0) + 1 }
      for (k in g.keys.sorted()) { groupNames.add(k); rowText.add(k + "  (" + g[k] + ")"); rowKind.add(1); rowIdx.add(groupNames.size - 1) }
    } else {
      rowText.add("‹ Volver"); rowKind.add(2); rowIdx.add(-1)
      for (i in (if (tab == 4) subSongs(s) else ordered(subSongs(s)))) song(i)
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
        if (autoNp) openNp()
      }
      1 -> { sub = groupNames[rowIdx[p]]; refreshList() }
      2 -> { sub = null; refreshList() }
      4 -> { genMix(); refreshList() }
      5 -> {}
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
  // Descarga de un archivo de audio desde un enlace directo (https)
  private fun linkDlg() {
    menuT = on(mc)
    val et = EditText(this).apply { hint = "https://… enlace directo a un archivo de audio"; setTextColor(menuT); setHintTextColor(0x88888888.toInt()); setSingleLine(true) }
    val st = TextView(this).apply { text = "Sirve para .mp3, .m4a, .ogg, .wav o .flac de tu propia nube, podcasts o sitios con música libre." ; textSize = 13f }
    val v = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    v.addView(et); v.addView(act("Descargar") { download(et.text.toString().trim(), st) }); v.addView(st)
    showMenu("Añadir desde un enlace", v)
  }
  private fun download(link: String, st: TextView) {
    if (!link.startsWith("https://")) { st.text = "El enlace debe empezar por https://"; return }
    st.text = "Descargando…"
    Thread {
      var tmp: File? = null
      try {
        val c = java.net.URL(link).openConnection() as java.net.HttpURLConnection
        c.connectTimeout = 15000; c.readTimeout = 30000; c.instanceFollowRedirects = true
        c.setRequestProperty("User-Agent", "JKMUSICK")
        if (c.responseCode !in 200..299) throw Exception("El servidor respondió " + c.responseCode)
        val type = (c.contentType ?: "").lowercase()
        var name = java.net.URLDecoder.decode(link.substringBefore('?').substringAfterLast('/'), "UTF-8")
        val okExt = Regex("\\.(mp3|m4a|aac|ogg|oga|opus|wav|flac)$", RegexOption.IGNORE_CASE).containsMatchIn(name)
        if (!type.startsWith("audio/") && !okExt) throw Exception("Ese enlace no es un archivo de audio directo (.mp3, .m4a, .ogg, .wav o .flac).")
        if (name.isBlank() || !okExt) name = "Descarga " + System.currentTimeMillis() + (if (type.contains("mpeg")) ".mp3" else if (type.contains("ogg")) ".ogg" else if (type.contains("wav")) ".wav" else if (type.contains("flac")) ".flac" else ".m4a")
        val dir = File(filesDir, "music"); dir.mkdirs()
        var f = File(dir, name.replace(Regex("[^\\p{L}\\p{N}._ -]"), "_"))
        if (f.exists()) f = File(dir, System.currentTimeMillis().toString() + "_" + f.name)
        tmp = f
        val total = c.contentLengthLong
        var done = 0L; var lastP = -1
        c.inputStream.use { i -> f.outputStream().use { o ->
          val buf = ByteArray(32768)
          while (true) {
            val n = i.read(buf); if (n < 0) break
            o.write(buf, 0, n); done += n
            if (done > 300L * 1024 * 1024) throw Exception("El archivo es demasiado grande (máximo 300 MB).")
            if (total > 0) { val p = (done * 100 / total).toInt(); if (p != lastP) { lastP = p; runOnUiThread { st.text = "Descargando… " + p + "%" } } }
          }
        } }
        runOnUiThread {
          addUri(Uri.fromFile(f), f.name); save(); upd(); fetchMeta()
          st.text = "Listo: «" + f.nameWithoutExtension + "» ya está en tu lista."
        }
      } catch (e: Exception) {
        try { tmp?.delete() } catch (x: Exception) {}
        runOnUiThread { st.text = e.message ?: "No se pudo descargar" }
      }
    }.start()
  }

  // Buscar toda la música del dispositivo (MediaStore)
  private fun audioPerm() = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
  private fun scanDeviceAsk() {
    if (ContextCompat.checkSelfPermission(this, audioPerm()) == PackageManager.PERMISSION_GRANTED) scanDevice() else askAudio.launch(audioPerm())
  }
  private fun scanDevice() {
    Toast.makeText(this, "Buscando canciones…", Toast.LENGTH_SHORT).show()
    Thread {
      val found = ArrayList<Array<String>>()
      try {
        val proj = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DATE_ADDED)
        val sel = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " + MediaStore.Audio.Media.DURATION + " >= 30000"
        contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj, sel, null, MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC")?.use { c ->
          while (c.moveToNext()) {
            val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, c.getLong(0)).toString()
            val t = cleanName(c.getString(2) ?: c.getString(1) ?: "Canción")
            var ar = c.getString(3) ?: ""; if (ar.isBlank() || ar == "<unknown>") ar = "Desconocido"
            var al = c.getString(4) ?: ""; if (al.isBlank() || al == "<unknown>") al = "Desconocido"
            found.add(arrayOf(uri, t, ar, al, (c.getLong(5) * 1000).toString()))
          }
        }
      } catch (e: Exception) {}
      runOnUiThread {
        val have = HashSet<String>(uris); var n = 0
        for (f in found) if (have.add(f[0])) { uris.add(f[0]); names.add(f[1]); artists.add(f[2]); albums.add(f[3]); addedMap[f[0]] = f[4].toLong(); n++ }
        save(); upd()
        val msg = if (found.isEmpty()) "No encontré canciones en el dispositivo." else n.toString() + " canciones nuevas añadidas (" + found.size + " en total)."
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
      }
    }.start()
  }

  // Estadísticas y mix inteligente
  private fun playIds(ids: List<Int>, pos: Int) {
    val c = ctl ?: return
    qmap.clear(); qmap.addAll(ids)
    c.setMediaItems(ids.map { item(uris[it], names[it]) }, pos, 0L); c.prepare(); c.play()
  }
  private fun genMix() {
    mixU.clear()
    val n = uris.size
    if (n == 0) return
    val rnd = java.util.Random()
    val want = minOf(30, n)
    val top = uris.indices.filter { (plays[uris[it]] ?: 0) > 0 || favs.contains(uris[it]) }
    val fresh = uris.indices.filter { (plays[uris[it]] ?: 0) == 0 && !favs.contains(uris[it]) }
    val chosen = LinkedHashSet<Int>()
    chosen.addAll(top.shuffled(rnd).take(want * 4 / 10))
    chosen.addAll(fresh.shuffled(rnd).take(want * 3 / 10))
    for (x in uris.indices.shuffled(rnd)) { if (chosen.size >= want) break; chosen.add(x) }
    val rest = ArrayList<Int>(chosen.shuffled(rnd)); val out = ArrayList<Int>()
    while (rest.isNotEmpty()) {
      val last = if (out.isEmpty()) "" else artists[out[out.size - 1]]
      var k = rest.indexOfFirst { artists[it].isEmpty() || artists[it] == "Desconocido" || artists[it] != last }
      if (k < 0) k = 0
      out.add(rest.removeAt(k))
    }
    for (i in out) mixU.add(uris[i])
  }
  private fun statsDlg() {
    menuT = on(mc)
    val mins = totalMs / 60000
    val v = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    v.addView(TextView(this).apply {
      text = "Canciones: " + uris.size + "\nReproducciones: " + plays.values.sum() + "\nTiempo escuchado: " + (mins / 60) + " h " + (mins % 60) + " min\nFavoritas: " + favs.size
      textSize = 15f
    })
    val top = uris.indices.filter { (plays[uris[it]] ?: 0) > 0 }.sortedByDescending { plays[uris[it]] ?: 0 }.take(10)
    v.addView(lab("Más escuchadas"))
    if (top.isEmpty()) v.addView(TextView(this).apply { text = "Todavía no hay datos. Una canción cuenta cuando suena 20 segundos." })
    else v.addView(menuOptions(top.mapIndexed { k, i -> (k + 1).toString() + ". " + names[i] + "  ·  " + (plays[uris[i]] ?: 0) + "×" }, top.mapIndexed { k, _ -> { playIds(top, k) } }))
    val ar = HashMap<String, Int>()
    for (i in uris.indices) { val c = plays[uris[i]] ?: 0; if (c > 0 && artists[i].isNotEmpty() && artists[i] != "Desconocido") ar[artists[i]] = (ar[artists[i]] ?: 0) + c }
    if (ar.isNotEmpty()) {
      v.addView(lab("Artistas más escuchados"))
      v.addView(TextView(this).apply { text = ar.entries.sortedByDescending { it.value }.take(5).joinToString("\n") { it.key + "  ·  " + it.value + "×" }; textSize = 15f })
    }
    showMenu("Estadísticas", v)
  }


  // Orden, menú de añadir e íconos
  private fun nn(t: String) = nk.getOrPut(t) { norm(t) }
  private val sortNames = arrayOf("Añadidas recientemente", "Añadidas primero (más antiguas)", "Nombre A → Z", "Nombre Z → A", "Artista", "Álbum", "Más escuchadas")
  private fun ordered(l: List<Int>): List<Int> = when (sortMode) {
    0 -> l.sortedWith(compareByDescending<Int> { addedMap[uris[it]] ?: 0L }.thenByDescending { it })
    1 -> l.sortedWith(compareBy<Int> { addedMap[uris[it]] ?: 0L }.thenBy { it })
    2 -> l.sortedBy { nn(names[it]) }
    3 -> l.sortedByDescending { nn(names[it]) }
    4 -> l.sortedWith(compareBy<Int> { nn(artists[it]) }.thenBy { nn(names[it]) })
    5 -> l.sortedWith(compareBy<Int> { nn(albums[it]) }.thenBy { nn(names[it]) })
    else -> l.sortedByDescending { plays[uris[it]] ?: 0 }
  }
  private fun sortDlg() {
    menuT = on(mc)
    showMenu("Ordenar canciones", menuOptions(sortNames.indices.map { (if (it == sortMode) "●  " else "○  ") + sortNames[it] }, sortNames.indices.map { k -> { sortMode = k; save(); refreshList() } }))
  }
  private fun addMenu() {
    menuT = on(mc)
    showMenu("Añadir música", menuOptions(
      listOf("Archivos del dispositivo", "Carpeta", "Buscar toda la música del dispositivo", "Desde un enlace"),
      listOf({ pickFiles.launch(arrayOf("audio/*")) }, { pickDir.launch(null) }, { scanDeviceAsk() }, { linkDlg() })))
  }
  private fun ico(kind: String, col: Int = fg(), f: () -> Unit) = IconView(this, kind, col).apply {
    setPadding(dp(8), dp(8), dp(8), dp(8)); layoutParams = LinearLayout.LayoutParams(dp(40), dp(40)); setOnClickListener { f() }
  }

  // Portada en la notificación y pantalla de reproducción
  private fun pushArt() {
    val c = ctl ?: return
    val i = cur(); if (i !in uris.indices) return
    if (c.mediaMetadata.artworkData != null) return
    val u = uris[i]; if (artBusy == u) return
    artBusy = u
    val idx = c.currentMediaItemIndex
    Thread {
      var data: ByteArray? = null
      try {
        val bo = java.io.ByteArrayOutputStream()
        square(art(i) ?: genArt(names[i]), 400).compress(Bitmap.CompressFormat.JPEG, 85, bo)
        data = bo.toByteArray()
      } catch (e: Exception) {}
      val dd = data
      runOnUiThread {
        artBusy = null
        val c2 = ctl
        try {
          if (dd != null && c2 != null && idx < c2.mediaItemCount && c2.getMediaItemAt(idx).localConfiguration?.uri.toString() == u && i < names.size) {
            val md = MediaMetadata.Builder().setTitle(names[i]).setArtist(artists[i].takeIf { it != "Desconocido" })
              .setAlbumTitle(albums[i].takeIf { it != "Desconocido" }).setArtworkData(dd, MediaMetadata.PICTURE_TYPE_FRONT_COVER).build()
            c2.replaceMediaItem(idx, c2.getMediaItemAt(idx).buildUpon().setMediaMetadata(md).build())
          }
        } catch (e: Exception) {}
      }
    }.start()
  }
  private fun npIco(kind: String, size: Int, f: () -> Unit) = IconView(this, kind, 0xFFF2EAFF.toInt()).apply {
    val q = dp(size) / 5; setPadding(q, q, q, q); layoutParams = LinearLayout.LayoutParams(dp(size), dp(size)); setOnClickListener { f() }
  }
  private fun buildNp(): View {
    val f = FrameLayout(this)
    f.setBackgroundColor(npBg); f.isClickable = true
    val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; fitsSystemWindows = true; setPadding(dp(24), dp(8), dp(24), dp(16)) }
    f.addView(col, FrameLayout.LayoutParams(-1, -1))
    val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
    top.addView(npIco("down", 44) { closeNp() })
    col.addView(top)
    val dm = resources.displayMetrics
    val sz = minOf(dm.widthPixels - dp(48), (dm.heightPixels * 0.40f).toInt())
    val cv = ImageView(this).apply {
      scaleType = ImageView.ScaleType.CENTER_CROP
      background = GradientDrawable().apply { cornerRadius = dp(28).toFloat(); setColor(0x01FFFFFF) }; clipToOutline = true
    }
    npCover = cv
    col.addView(cv, LinearLayout.LayoutParams(sz, sz).apply { gravity = Gravity.CENTER_HORIZONTAL; topMargin = dp(4) })
    val tt = TextView(this).apply { textSize = 28f; typeface = Typeface.DEFAULT_BOLD; setTextColor(0xFFF2EAFF.toInt()); maxLines = 1; ellipsize = TextUtils.TruncateAt.END; setPadding(0, dp(20), 0, 0) }
    npTitle = tt; col.addView(tt)
    val ar = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
    val at = TextView(this).apply { textSize = 18f; setTextColor(0xB3F2EAFF.toInt()); maxLines = 1; ellipsize = TextUtils.TruncateAt.END }
    npArtist = at; ar.addView(at, LinearLayout.LayoutParams(0, -2, 1f))
    val hv = npIco("heart", 44) { val i = cur(); if (i in uris.indices) toggleFav(i) }
    npHeart = hv; ar.addView(hv)
    ar.addView(npIco("more", 44) { val i = cur(); if (i in uris.indices) npMenu(i) })
    col.addView(ar)
    col.addView(View(this), LinearLayout.LayoutParams(-1, 0, 1f))
    val sk = SeekBar(this).apply {
      max = 1000; progressTintList = ColorStateList.valueOf(0xFFF2EAFF.toInt()); thumbTintList = ColorStateList.valueOf(0xFFF2EAFF.toInt())
      setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(s: SeekBar?, v: Int, u: Boolean) {}
        override fun onStartTrackingTouch(s: SeekBar?) { npDrag = true }
        override fun onStopTrackingTouch(s: SeekBar?) { npDrag = false; ctl?.let { c -> if (c.duration > 0) c.seekTo(c.duration * (s?.progress ?: 0) / 1000) } }
      })
    }
    npSeek = sk; col.addView(sk)
    val tr = LinearLayout(this)
    val t1 = TextView(this).apply { setTextColor(0xB3F2EAFF.toInt()); text = "0:00" }
    val t2 = TextView(this).apply { setTextColor(0xB3F2EAFF.toInt()); text = "0:00"; gravity = Gravity.END }
    npCur = t1; npDur = t2
    tr.addView(t1, LinearLayout.LayoutParams(0, -2, 1f)); tr.addView(t2, LinearLayout.LayoutParams(0, -2, 1f))
    col.addView(tr)
    val cr = LinearLayout(this).apply { gravity = Gravity.CENTER; setPadding(0, dp(10), 0, dp(10)) }
    cr.addView(npIco("prev", 72) { ctl?.seekToPrevious() })
    val pb = FrameLayout(this); npPlay = pb
    pb.setOnClickListener { togglePlay() }
    cr.addView(pb, LinearLayout.LayoutParams(dp(96), dp(96)).apply { setMargins(dp(12), 0, dp(12), 0) })
    cr.addView(npIco("next", 72) { ctl?.seekToNext() })
    col.addView(cr)
    val br = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
    br.addView(npIco("eq", 44) { eqDlg() })
    val ln = TextView(this).apply { textSize = 15f; setTextColor(0xFFF2EAFF.toInt()); gravity = Gravity.CENTER; maxLines = 1; ellipsize = TextUtils.TruncateAt.END }
    npLine = ln; br.addView(ln, LinearLayout.LayoutParams(0, -2, 1f))
    val sf = npIco("shuf", 44) { ctl?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled; npShuf?.alpha = if (it.shuffleModeEnabled) 1f else .5f } }
    npShuf = sf; br.addView(sf)
    col.addView(br)
    return f
  }
  private fun npPlayIcon() {
    val b = npPlay ?: return
    val p = ctl?.isPlaying == true
    b.removeAllViews()
    b.addView(IconView(this, if (p) "pause" else "play", 0xFFF2EAFF.toInt()).apply { val q = dp(96) / 5; setPadding(q, q, q, q) }, FrameLayout.LayoutParams(-1, -1))
  }
  private fun refreshNp() {
    if (!npOpen) return
    val i = cur()
    npTitle?.text = names.getOrNull(i) ?: "Nada en reproducción"
    val a = artists.getOrNull(i) ?: ""
    npArtist?.text = if (a == "Desconocido") "" else a
    val al = albums.getOrNull(i) ?: ""
    npLine?.text = if (al == "Desconocido") "" else al
    npHeart?.filled = i in uris.indices && favs.contains(uris[i])
    npShuf?.alpha = if (ctl?.shuffleModeEnabled == true) 1f else .5f
    if (i in uris.indices) Thread {
      val src = art(i) ?: genArt(names[i])
      val sq = square(src, 600)
      val dom = dominantColor(src)
      runOnUiThread {
        if (npOpen && cur() == i) {
          npCover?.setImageBitmap(sq)
          fadeNp(dom)
        }
      }
    }.start()
  }
  private fun openNp() {
    if (npOpen) return
    val o = outerV ?: return
    npOpen = true
    val v = buildNp(); npView = v
    o.addView(v, FrameLayout.LayoutParams(-1, -1))
    v.translationY = resources.displayMetrics.heightPixels.toFloat()
    v.animate().translationY(0f).setDuration(240).start()
    refreshNp(); npPlayIcon()
    if (fontI != 0) applyFont(v)
  }
  private fun closeNp() {
    if (!npOpen) return
    npOpen = false
    val v = npView; npView = null
    npCover = null; npTitle = null; npArtist = null; npLine = null; npSeek = null; npCur = null; npDur = null; npPlay = null; npHeart = null; npShuf = null
    if (v != null) v.animate().translationY(resources.displayMetrics.heightPixels.toFloat()).setDuration(200).withEndAction { (v.parent as? ViewGroup)?.removeView(v) }.start()
  }
  private fun npMenu(i: Int) {
    menuT = on(mc)
    val u = uris[i]
    showMenu(names[i], menuOptions(
      listOf(if (favs.contains(u)) "Quitar de favoritas" else "Añadir a favoritas", "Añadir a una lista…", "Quitar de la biblioteca"),
      listOf({ toggleFav(i) }, { addToList(i) }, { removeLib(i) })))
  }


  // Carcasa metálica del estilo Winamp: degradado a partir del color elegido más un brillo diagonal
  private fun caseDrawable(): android.graphics.drawable.Drawable {
    val top = ColorUtils.blendARGB(caseC, Color.WHITE, 0.35f)
    val bot = ColorUtils.blendARGB(caseC, Color.BLACK, 0.28f)
    val base = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(pa(top), pa(bot))).apply { cornerRadius = dp(16).toFloat() }
    val sheen = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(0x44FFFFFF, 0x00FFFFFF, 0x26000000)).apply { cornerRadius = dp(16).toFloat() }
    return android.graphics.drawable.LayerDrawable(arrayOf<android.graphics.drawable.Drawable>(base, sheen))
  }
  // Miniatura de la portada (estilo widget minimalista)
  private fun refreshThumb() {
    val t = thumb ?: return
    val i = cur()
    Thread {
      val src = (if (i in uris.indices) art(i) else null) ?: genArt(names.getOrNull(i) ?: "JKMUSICK")
      val sq = square(src, 200)
      runOnUiThread { if (thumb === t) t.setImageBitmap(sq) }
    }.start()
  }
  // Transición suave del color de fondo de la pantalla de reproducción
  private fun fadeNp(to: Int) {
    npAnim?.cancel()
    npAnim = ValueAnimator.ofObject(ArgbEvaluator(), npBg, to).apply {
      duration = 600
      addUpdateListener { npBg = it.animatedValue as Int; npView?.setBackgroundColor(npBg) }
      start()
    }
  }
  override fun onStart() { super.onStart(); PlayService.showFloat(false) }
  override fun onStop() { super.onStop(); if (floatOn) PlayService.showFloat(true) }


  // Color del estilo Windows Media Player a partir de un solo matiz (HSL)
  private fun wc(s: Float, l: Float) = ColorUtils.HSLToColor(floatArrayOf(wmpHue.toFloat(), s, l))

  // Barra superior reutilizable: logo + nombre (opcional) e íconos de acciones
  private fun headBar(col: Int, showTitle: Boolean): LinearLayout {
    val head = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
    if (showTitle) {
      img("logo")?.let { b ->
        head.addView(ImageView(this).apply {
          setImageBitmap(square(b, 96)); scaleType = ImageView.ScaleType.CENTER_CROP; clipToOutline = true
          background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(0x01FFFFFF) }
        }, LinearLayout.LayoutParams(dp(32), dp(32)).apply { marginEnd = dp(10) })
      }
      head.addView(TextView(this).apply { text = appName; textSize = 20f; setTextColor(col); typeface = Typeface.DEFAULT_BOLD }, LinearLayout.LayoutParams(0, -2, 1f))
    } else head.addView(View(this), LinearLayout.LayoutParams(0, 0, 1f))
    head.addView(ico("plus", col) { addMenu() })
    head.addView(ico("search", col) { toggleSearch() })
    head.addView(ico("sort", col) { sortDlg() })
    head.addView(ico("timer", col) { sleepDlg() })
    val hv = ico("heart", col) { val i = cur(); if (i in uris.indices) toggleFav(i) }
    heart = hv
    head.addView(hv)
    head.addView(ico("tune", col) { settings() })
    return head
  }

  // Botón plateado del estilo Windows Media Player (píldora o círculo)
  private fun wmpBtn(kind: String, w: Int, h: Int, f: (View) -> Unit): FrameLayout {
    val fl = FrameLayout(this)
    fl.background = GradientDrawable().apply {
      cornerRadius = dp(minOf(w, h) / 2).toFloat()
      setGradientType(GradientDrawable.RADIAL_GRADIENT); gradientRadius = dp(w) * 0.9f; setGradientCenter(.35f, .3f)
      setColors(intArrayOf(Color.WHITE, 0xFFD9D2E9.toInt(), ColorUtils.blendARGB(0xFFD9D2E9.toInt(), Color.BLACK, .35f))); setStroke(dp(1), wc(.48f, .20f))
    }
    fl.addView(IconView(this, kind, wc(.48f, .20f)).apply { val q = dp(minOf(w, h)) / 5; setPadding(q, q, q, q) }, FrameLayout.LayoutParams(-1, -1))
    fl.layoutParams = LinearLayout.LayoutParams(dp(w), dp(h)).apply { marginStart = dp(4) }
    fl.setOnClickListener { v -> f(v) }
    return fl
  }

  // Menú desplegable con aspecto de Windows (lavanda con borde morado)
  private fun dropdown(anchor: View, items: List<Pair<String, (() -> Unit)?>>, checks: Set<Int>) {
    val col = LinearLayout(this).apply {
      orientation = LinearLayout.VERTICAL; setPadding(dp(2), dp(2), dp(2), dp(2))
      background = GradientDrawable().apply { setColor(wc(.45f, .90f)); setStroke(dp(1), wc(.48f, .20f)) }
    }
    var pw: android.widget.PopupWindow? = null
    items.forEachIndexed { k, (label, f) ->
      if (f == null) {
        col.addView(View(this).apply { setBackgroundColor(wc(.30f, .50f)) }, LinearLayout.LayoutParams(-1, dp(1)).apply { setMargins(dp(4), dp(3), dp(4), dp(3)) })
        return@forEachIndexed
      }
      col.addView(TextView(this).apply {
        text = (if (k in checks) "●  " else "     ") + label; setTextColor(wc(.7f, .14f)); textSize = 15f
        setPadding(dp(14), dp(10), dp(26), dp(10)); setOnClickListener { pw?.dismiss(); f() }
      })
    }
    val p = android.widget.PopupWindow(col, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
    p.elevation = dp(6).toFloat()
    pw = p
    p.showAsDropDown(anchor)
  }
  private fun wmpMenu(name: String, anchor: View) {
    val items = ArrayList<Pair<String, (() -> Unit)?>>()
    val checks = HashSet<Int>()
    fun add(l: String, checked: Boolean = false, f: (() -> Unit)?) { if (checked) checks.add(items.size); items.add(Pair(l, f)) }
    fun viz(n: Int) { wmpViz = n; sp.edit().putInt("wviz", n).apply(); wave?.mode = n }
    when (name) {
      "File" -> {
        add("Open files…") { pickFiles.launch(arrayOf("audio/*")) }; add("Open folder…") { pickDir.launch(null) }
        add("Scan device") { scanDeviceAsk() }; add("Open link…") { linkDlg() }
        add("-", false, null); add("Exit") { moveTaskToBack(true) }
      }
      "View" -> {
        add("Bars", wmpViz == 0) { viz(0) }; add("Mirror", wmpViz == 1) { viz(1) }; add("Waveform", wmpViz == 2) { viz(2) }
        add("-", false, null)
        add("Now playing") { wmpShow?.invoke(0) }; add("Library") { wmpShow?.invoke(1) }
        add("-", false, null); add("Full screen") { wmpMax?.invoke() }
      }
      "Play" -> {
        add("Play / Pause") { togglePlay() }; add("Stop") { ctl?.let { it.pause(); it.seekTo(0) } }
        add("Previous") { ctl?.seekToPrevious() }; add("Next") { ctl?.seekToNext() }
        add("-", false, null)
        add("Shuffle", ctl?.shuffleModeEnabled == true) { ctl?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled } }
        add("Repeat", (ctl?.repeatMode ?: Player.REPEAT_MODE_OFF) != Player.REPEAT_MODE_OFF) { ctl?.let { it.repeatMode = if (it.repeatMode == Player.REPEAT_MODE_OFF) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF } }
      }
      "Tools" -> {
        for ((n, h) in listOf("Morado" to 275, "Violeta eléctrico" to 262, "Magenta" to 315, "Azul medianoche" to 232))
          add("Skin: " + n, wmpHue == h) { wmpHue = h; sp.edit().putInt("wmph", h).apply(); theme() }
        add("-", false, null)
        add("Sort…") { sortDlg() }; add("Equalizer…") { eqDlg() }; add("Sleep timer…") { sleepDlg() }; add("Settings…") { settings() }
      }
      else -> add("About") { AlertDialog.Builder(this).setTitle(appName).setMessage("Reproductor de la música de tu dispositivo con el estilo Windows Media Player.").setPositiveButton("OK", null).show() }
    }
    dropdown(anchor, items, checks)
  }

  // Nombre e imagen del reproductor
  private fun nameDlg() {
    menuT = on(mc)
    val et = EditText(this).apply { setText(appName); setSingleLine(true); setTextColor(menuT); hint = "Nombre del reproductor" }
    AlertDialog.Builder(this).setTitle("Nombre del reproductor").setView(et)
      .setPositiveButton("Guardar") { _, _ ->
        val n = et.text.toString().trim().take(24)
        appName = if (n.isEmpty()) "JKMUSICK" else n
        sp.edit().putString("aname", appName).apply(); theme()
      }
      .setNegativeButton("Cancelar", null).show()
  }
  private fun roundBmp(b: Bitmap): Bitmap {
    val o = Bitmap.createBitmap(b.width, b.height, Bitmap.Config.ARGB_8888)
    val c = Canvas(o); val p = Paint(Paint.ANTI_ALIAS_FLAG)
    c.drawCircle(b.width / 2f, b.height / 2f, b.width / 2f, p)
    p.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
    c.drawBitmap(b, 0f, 0f, p)
    return o
  }
  // Acceso directo en la pantalla de inicio con tu nombre e imagen (Android no deja cambiar el ícono de la app instalada)
  private fun pinShortcut() {
    if (Build.VERSION.SDK_INT < 26) { Toast.makeText(this, "Tu Android no permite crear accesos directos así", Toast.LENGTH_LONG).show(); return }
    val sm = getSystemService(android.content.pm.ShortcutManager::class.java)
    if (sm == null || !sm.isRequestPinShortcutSupported) { Toast.makeText(this, "Tu pantalla de inicio no permite accesos directos", Toast.LENGTH_LONG).show(); return }
    val src = img("logo") ?: genArt(appName)
    val si = android.content.pm.ShortcutInfo.Builder(this, "jk-" + System.currentTimeMillis())
      .setShortLabel(appName).setLongLabel(appName)
      .setIcon(android.graphics.drawable.Icon.createWithBitmap(roundBmp(square(src, 192))))
      .setIntent(Intent(this, MainActivity::class.java).setAction(Intent.ACTION_MAIN)).build()
    sm.requestPinShortcut(si, null)
  }

  private fun removeLib(p: Int) {
    val u = uris[p]
    if (u.startsWith("file:")) try { File(Uri.parse(u).path ?: "").delete() } catch (e: Exception) {}
    val j = qmap.indexOf(p)
    if (j >= 0) { qmap.removeAt(j); ctl?.removeMediaItem(j) }
    for (k in qmap.indices) if (qmap[k] > p) qmap[k] = qmap[k] - 1
    uris.removeAt(p); names.removeAt(p); artists.removeAt(p); albums.removeAt(p)
    favs.remove(u); plays.remove(u); mixU.remove(u); for (l in plists.values) l.remove(u)
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
    val redo = !sp.getBoolean("tt1", false)
    val todo = uris.indices.filter { redo || (artists[it].isEmpty() && albums[it].isEmpty()) }.map { uris[it] }
    if (todo.isEmpty()) return
    Thread {
      val res = HashMap<String, Array<String>>()
      for (u in todo) {
        var ar = "Desconocido"; var al = "Desconocido"; var ti = ""
        try {
          val r = MediaMetadataRetriever()
          r.setDataSource(this, Uri.parse(u))
          val a1 = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST); if (!a1.isNullOrBlank()) ar = a1.trim()
          val a2 = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM); if (!a2.isNullOrBlank()) al = a2.trim()
          val a3 = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE); if (!a3.isNullOrBlank()) ti = cleanName(a3)
          r.release()
        } catch (e: Exception) {}
        res[u] = arrayOf(ar, al, ti)
      }
      runOnUiThread {
        for ((u, m) in res) {
          val i = uris.indexOf(u); if (i < 0) continue
          if (m[0] != "Desconocido" || artists[i].isEmpty()) artists[i] = m[0]
          if (m[1] != "Desconocido" || albums[i].isEmpty()) albums[i] = m[1]
          if (m[2].isNotEmpty()) names[i] = m[2]
          else if (artists[i] == "Desconocido" && names[i].contains(" - ")) {
            val p = names[i].split(" - ", limit = 2)
            if (p[0].isNotBlank() && p[1].isNotBlank()) { artists[i] = p[0].trim(); names[i] = p[1].trim() }
          }
        }
        sp.edit().putBoolean("tt1", true).apply()
        save(); refreshList()
      }
    }.start()
  }

  // Nombres limpios y búsqueda
  private fun cleanName(raw: String): String {
    var s = raw.trim()
    s = s.replace(Regex("\\.(mp3|m4a|aac|ogg|oga|opus|wav|flac|wma)$", RegexOption.IGNORE_CASE), "")
    s = s.replace('_', ' ')
    s = s.replace(Regex("\\s*[\\[(][^\\])]*\\b(official|oficial|video|vídeo|lyrics?|letra|audio|hd|hq|4k|mv|visualizer|remaster(ed)?|explicit)\\b[^\\])]*[\\])]", RegexOption.IGNORE_CASE), "")
    s = s.replace(Regex("\\s*\\[[A-Za-z0-9_-]{11}\\]$"), "")
    s = s.replace(Regex("^\\s*\\(?\\d{1,3}\\)?\\s*[-.)]\\s+"), "")
    s = s.replace(Regex("\\s+"), " ").trim()
    return if (s.isEmpty()) raw.trim() else s
  }
  private fun norm(t: String) = java.text.Normalizer.normalize(t, java.text.Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "").lowercase()
  private fun toggleSearch() {
    searching = !searching
    val b = searchBox ?: return
    val im = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
    if (searching) { b.visibility = View.VISIBLE; b.requestFocus(); im.showSoftInput(b, 0) }
    else { b.setText(""); query = ""; b.visibility = View.GONE; im.hideSoftInputFromWindow(b.windowToken, 0); refreshList() }
  }

  // Menús de color con transparencia
  private fun dlgBg() {
    val r = GradientDrawable().apply { setColor(ColorUtils.setAlphaComponent(mc, 255 * ma / 100)); cornerRadius = dp(28).toFloat() }
    curDlg?.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT))
    curRoot?.let { it.background = r; it.clipToOutline = true }
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
    val an = act("") { }
    an.text = "Pantalla de reproducción al elegir canción: " + (if (autoNp) "sí" else "no")
    an.setOnClickListener { autoNp = !autoNp; sp.edit().putBoolean("anp", autoNp).apply(); an.text = "Pantalla de reproducción al elegir canción: " + (if (autoNp) "sí" else "no") }
    box.addView(an)
    box.addView(lab("Nombre e imagen del reproductor"))
    box.addView(act("Cambiar nombre (" + appName + ")") { curDlg?.dismiss(); nameDlg() })
    box.addView(imgRow("Imagen del reproductor", "logo", false))
    box.addView(act("Crear acceso directo en la pantalla de inicio") { pinShortcut() })
    box.addView(lab("Color del estilo Windows Media Player"))
    box.addView(sb(360, wmpHue) { h -> wmpHue = h; sp.edit().putInt("wmph", h).apply(); theme() })
    box.addView(lab("Carcasa del estilo Winamp retro"))
    box.addView(sw("Color de la carcasa") { caseC = it; sp.edit().putInt("case", caseC).apply(); devV?.background = caseDrawable() })
    val hh = FloatArray(3); Color.colorToHSV(caseC, hh)
    box.addView(TextView(this).apply { text = "Matiz (se ve al instante)"; setPadding(0, dp(8), 0, 0) })
    box.addView(sb(360, hh[0].toInt()) { h -> caseC = Color.HSVToColor(floatArrayOf(h.toFloat(), 0.55f, 0.85f)); sp.edit().putInt("case", caseC).apply(); devV?.background = caseDrawable() })
    box.addView(act("Plata") { caseC = 0xFFC9CDD1.toInt(); sp.edit().putInt("case", caseC).apply(); devV?.background = caseDrawable() })
    val fb = act("") { }
    fun fbLabel() = "Barra flotante sobre otras apps: " + (if (floatOn) "sí" else "no")
    fb.text = fbLabel()
    fb.setOnClickListener {
      if (!floatOn && !android.provider.Settings.canDrawOverlays(this)) {
        Toast.makeText(this, "Activa «Permitir sobre otras apps» para JKMUSICK y vuelve", Toast.LENGTH_LONG).show()
        startActivity(Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName)))
        floatOn = true
      } else floatOn = !floatOn
      sp.edit().putBoolean("fbar", floatOn).apply(); fb.text = fbLabel()
    }
    box.addView(fb)
    box.addView(act("Buscar canciones en el dispositivo") { curDlg?.dismiss(); scanDeviceAsk() })
    box.addView(act("Estadísticas…") { curDlg?.dismiss(); statsDlg() })
    box.addView(act("Añadir desde un enlace…") { curDlg?.dismiss(); linkDlg() })
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
  var mode = 1          // 0 barras desde abajo, 1 espejo (centradas), 2 onda
  private val p = Paint()
  override fun onDraw(cv: Canvas) {
    val n = 40
    val w = width / n.toFloat()
    val t = System.currentTimeMillis() / 120.0
    p.color = col
    if (mode == 2) {
      p.style = Paint.Style.STROKE; p.strokeWidth = 3f
      var px = 0f; var py = height / 2f
      for (i in 0..n) {
        val amp = if (active) 0.34 else 0.05
        val y = (height / 2f + Math.sin(t * 2 + i * 0.5) * amp * height * Math.abs(Math.sin(i * 0.9 + t * 0.3))).toFloat()
        val x = i * w
        if (i > 0) cv.drawLine(px, py, x, y, p)
        px = x; py = y
      }
      p.style = Paint.Style.FILL
    } else for (i in 0 until n) {
      val base = Math.abs(Math.sin(i * 1.7)) * 0.6 + 0.15
      val h = if (active) base * (0.6 + 0.4 * Math.abs(Math.sin(t + i * 0.9))) else base * 0.5
      val bh = (h * height).toFloat()
      if (mode == 0) cv.drawRect(i * w + w * 0.15f, height - bh, (i + 1) * w - w * 0.15f, height.toFloat(), p)
      else cv.drawRect(i * w + w * 0.15f, (height - bh) / 2, (i + 1) * w - w * 0.15f, (height + bh) / 2, p)
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


class IconView(c: android.content.Context, k: String, color: Int) : View(c) {
  var kind = k
    set(v) { field = v; invalidate() }
  var col = color
    set(v) { field = v; invalidate() }
  var filled = false
    set(v) { field = v; invalidate() }
  private val p = Paint(Paint.ANTI_ALIAS_FLAG)
  private val pa = Path()
  private val rf = RectF()
  override fun onDraw(cv: Canvas) {
    val w = width - paddingLeft - paddingRight
    val h = height - paddingTop - paddingBottom
    val s = minOf(w, h) / 24f
    if (s <= 0f) return
    cv.save()
    cv.translate(paddingLeft + (w - 24 * s) / 2f, paddingTop + (h - 24 * s) / 2f)
    cv.scale(s, s)
    p.color = col; p.strokeWidth = 2f; p.strokeCap = Paint.Cap.ROUND; p.strokeJoin = Paint.Join.ROUND
    p.style = Paint.Style.STROKE
    pa.reset()
    when (kind) {
      "search" -> { cv.drawCircle(10.5f, 10.5f, 6.5f, p); cv.drawLine(15.5f, 15.5f, 20f, 20f, p) }
      "plus" -> { cv.drawLine(12f, 5f, 12f, 19f, p); cv.drawLine(5f, 12f, 19f, 12f, p) }
      "timer" -> { cv.drawCircle(12f, 13.5f, 7f, p); cv.drawLine(12f, 13.5f, 12f, 9.5f, p); cv.drawLine(9.5f, 3f, 14.5f, 3f, p) }
      "sort" -> { cv.drawLine(4f, 7f, 20f, 7f, p); cv.drawLine(4f, 12f, 15f, 12f, p); cv.drawLine(4f, 17f, 10f, 17f, p) }
      "tune" -> {
        cv.drawLine(4f, 8f, 8f, 8f, p); cv.drawLine(15f, 8f, 20f, 8f, p); cv.drawCircle(11.5f, 8f, 3.2f, p)
        cv.drawLine(4f, 16f, 9f, 16f, p); cv.drawLine(16f, 16f, 20f, 16f, p); cv.drawCircle(12.5f, 16f, 3.2f, p)
      }
      "eq" -> {
        cv.drawLine(6f, 4f, 6f, 10f, p); cv.drawLine(6f, 17f, 6f, 20f, p); cv.drawCircle(6f, 13.5f, 3.2f, p)
        cv.drawLine(12f, 4f, 12f, 5f, p); cv.drawLine(12f, 12f, 12f, 20f, p); cv.drawCircle(12f, 8.5f, 3.2f, p)
        cv.drawLine(18f, 4f, 18f, 12f, p); cv.drawLine(18f, 19f, 18f, 20f, p); cv.drawCircle(18f, 15.5f, 3.2f, p)
      }
      "heart" -> {
        pa.moveTo(12f, 20.5f); pa.cubicTo(12f, 20.5f, 3f, 14.5f, 3f, 8.5f); pa.cubicTo(3f, 5.5f, 5.3f, 3.5f, 7.8f, 3.5f)
        pa.cubicTo(9.6f, 3.5f, 11.2f, 4.5f, 12f, 6f); pa.cubicTo(12.8f, 4.5f, 14.4f, 3.5f, 16.2f, 3.5f)
        pa.cubicTo(18.7f, 3.5f, 21f, 5.5f, 21f, 8.5f); pa.cubicTo(21f, 14.5f, 12f, 20.5f, 12f, 20.5f); pa.close()
        if (filled) p.style = Paint.Style.FILL_AND_STROKE
        cv.drawPath(pa, p)
      }
      "vol" -> {
        pa.moveTo(4f, 9.5f); pa.lineTo(8f, 9.5f); pa.lineTo(13f, 5f); pa.lineTo(13f, 19f); pa.lineTo(8f, 14.5f); pa.lineTo(4f, 14.5f); pa.close()
        cv.drawPath(pa, p)
        rf.set(9f, 8f, 17f, 16f); cv.drawArc(rf, -50f, 100f, false, p)
        rf.set(6f, 4.5f, 22f, 19.5f); cv.drawArc(rf, -50f, 100f, false, p)
      }
      "play" -> { p.style = Paint.Style.FILL_AND_STROKE; pa.moveTo(8f, 5f); pa.lineTo(19f, 12f); pa.lineTo(8f, 19f); pa.close(); cv.drawPath(pa, p) }
      "pause" -> {
        p.style = Paint.Style.FILL_AND_STROKE
        rf.set(6.5f, 5f, 10f, 19f); cv.drawRoundRect(rf, 1.5f, 1.5f, p)
        rf.set(14f, 5f, 17.5f, 19f); cv.drawRoundRect(rf, 1.5f, 1.5f, p)
      }
      "prev" -> {
        p.style = Paint.Style.FILL_AND_STROKE
        pa.moveTo(11f, 5.5f); pa.lineTo(3f, 12f); pa.lineTo(11f, 18.5f); pa.close()
        pa.moveTo(20.5f, 5.5f); pa.lineTo(12.5f, 12f); pa.lineTo(20.5f, 18.5f); pa.close()
        cv.drawPath(pa, p)
      }
      "next" -> {
        p.style = Paint.Style.FILL_AND_STROKE
        pa.moveTo(13f, 5.5f); pa.lineTo(21f, 12f); pa.lineTo(13f, 18.5f); pa.close()
        pa.moveTo(3.5f, 5.5f); pa.lineTo(11.5f, 12f); pa.lineTo(3.5f, 18.5f); pa.close()
        cv.drawPath(pa, p)
      }
      "shuf" -> {
        pa.moveTo(4f, 17f); pa.lineTo(9f, 17f); pa.lineTo(15f, 7f); pa.lineTo(19f, 7f)
        pa.moveTo(4f, 7f); pa.lineTo(9f, 7f); pa.lineTo(15f, 17f); pa.lineTo(19f, 17f)
        pa.moveTo(16.5f, 4.5f); pa.lineTo(19.5f, 7f); pa.lineTo(16.5f, 9.5f)
        pa.moveTo(16.5f, 14.5f); pa.lineTo(19.5f, 17f); pa.lineTo(16.5f, 19.5f)
        cv.drawPath(pa, p)
      }
      "rep", "rep1" -> {
        pa.moveTo(4f, 12f); pa.lineTo(4f, 10f); pa.quadTo(4f, 7f, 7f, 7f); pa.lineTo(19f, 7f)
        pa.moveTo(16f, 4f); pa.lineTo(19f, 7f); pa.lineTo(16f, 10f)
        pa.moveTo(20f, 12f); pa.lineTo(20f, 14f); pa.quadTo(20f, 17f, 17f, 17f); pa.lineTo(5f, 17f)
        pa.moveTo(8f, 14f); pa.lineTo(5f, 17f); pa.lineTo(8f, 20f)
        cv.drawPath(pa, p)
        if (kind == "rep1") { p.style = Paint.Style.FILL; p.textSize = 9f; p.textAlign = Paint.Align.CENTER; cv.drawText("1", 12f, 15f, p) }
      }
      "more" -> { p.style = Paint.Style.FILL; cv.drawCircle(12f, 5f, 1.8f, p); cv.drawCircle(12f, 12f, 1.8f, p); cv.drawCircle(12f, 19f, 1.8f, p) }
      "down" -> { cv.drawLine(5f, 9f, 12f, 16f, p); cv.drawLine(12f, 16f, 19f, 9f, p) }
      "close" -> { cv.drawLine(6f, 6f, 18f, 18f, p); cv.drawLine(18f, 6f, 6f, 18f, p) }
      "stop" -> { p.style = Paint.Style.FILL_AND_STROKE; rf.set(6.5f, 6.5f, 17.5f, 17.5f); cv.drawRoundRect(rf, 1.5f, 1.5f, p) }
      "mute" -> {
        pa.moveTo(4f, 9.5f); pa.lineTo(8f, 9.5f); pa.lineTo(13f, 5f); pa.lineTo(13f, 19f); pa.lineTo(8f, 14.5f); pa.lineTo(4f, 14.5f); pa.close()
        cv.drawPath(pa, p); cv.drawLine(16f, 9f, 21f, 15f, p); cv.drawLine(21f, 9f, 16f, 15f, p)
      }
    }
    cv.restore()
  }
}


// Ecualizador gráfico: deslizadores verticales conectados a las bandas reales del ecualizador
class EqSliders(c: android.content.Context, private val sp: android.content.SharedPreferences, private val tint: Int) : View(c) {
  private val p = Paint(Paint.ANTI_ALIAS_FLAG)
  private val rf = RectF()
  private val d = c.resources.displayMetrics.density
  private fun top() = 8f * d
  private fun bot() = height - 20f * d
  override fun onDraw(cv: Canvas) {
    val e = PlayService.eq
    if (e == null) {
      p.color = tint; p.textSize = 13f * d; p.textAlign = Paint.Align.CENTER
      cv.drawText("Ecualizador no disponible", width / 2f, height / 2f, p); return
    }
    val n = e.numberOfBands.toInt()
    val lo = e.bandLevelRange[0].toInt(); val hi = e.bandLevelRange[1].toInt()
    val colW = width / n.toFloat()
    for (i in 0 until n) {
      val cx = colW * i + colW / 2
      val lv = e.getBandLevel(i.toShort()).toInt()
      val y = bot() - (lv - lo).toFloat() / (hi - lo) * (bot() - top())
      p.style = Paint.Style.FILL; p.color = 0x33000000
      rf.set(cx - 3 * d, top(), cx + 3 * d, bot()); cv.drawRoundRect(rf, 3 * d, 3 * d, p)
      p.color = tint
      rf.set(cx - 3 * d, y, cx + 3 * d, bot()); cv.drawRoundRect(rf, 3 * d, 3 * d, p)
      p.color = 0xFFE9ECEC.toInt()
      rf.set(cx - 12 * d, y - 6 * d, cx + 12 * d, y + 6 * d); cv.drawRoundRect(rf, 4 * d, 4 * d, p)
      p.style = Paint.Style.STROKE; p.strokeWidth = 1.5f * d; p.color = 0xFF55605A.toInt(); cv.drawRoundRect(rf, 4 * d, 4 * d, p)
      p.style = Paint.Style.FILL; p.color = tint; p.textSize = 10f * d; p.textAlign = Paint.Align.CENTER
      val f = e.getCenterFreq(i.toShort()) / 1000
      cv.drawText(if (f >= 1000) (f / 1000).toString() + "k" else f.toString(), cx, height - 5f * d, p)
    }
  }
  override fun onTouchEvent(ev: MotionEvent): Boolean {
    val e = PlayService.eq ?: return true
    parent?.requestDisallowInterceptTouchEvent(true)
    if (ev.actionMasked == MotionEvent.ACTION_DOWN || ev.actionMasked == MotionEvent.ACTION_MOVE) {
      val n = e.numberOfBands.toInt()
      val i = (ev.x / (width / n.toFloat())).toInt().coerceIn(0, n - 1)
      val lo = e.bandLevelRange[0].toInt(); val hi = e.bandLevelRange[1].toInt()
      val y = ev.y.coerceIn(top(), bot())
      val lv = (lo + (bot() - y) / (bot() - top()) * (hi - lo)).toInt()
      PlayService.setBand(i, lv)
      sp.edit().putInt("eq" + i, lv).apply()
      invalidate()
    }
    return true
  }
}

// Vúmetro estéreo (L/R). Es una animación visual: sube con la música y baja al pausar
class VuView(c: android.content.Context) : View(c) {
  var active = false
    set(v) { field = v; invalidate() }
  private val p = Paint()
  private val lv = FloatArray(2)
  override fun onDraw(cv: Canvas) {
    val t = System.currentTimeMillis() / 140.0
    val segs = 22
    val sw = width / segs.toFloat(); val h = height / 2f
    for (ch in 0..1) {
      val target = if (active) (0.30 + 0.60 * Math.abs(Math.sin(t * (1.0 + ch * 0.37) + ch * 1.7))).toFloat() else 0.03f
      lv[ch] += (target - lv[ch]) * 0.35f
      for (k in 0 until segs) {
        p.color = if (k < lv[ch] * segs) (if (k > 17) 0xFFB5412F.toInt() else 0xFF1B2B22.toInt()) else 0x22000000
        cv.drawRect(k * sw + 1f, ch * h + 2f, (k + 1) * sw - 1f, (ch + 1) * h - 2f, p)
      }
    }
    if (active) postInvalidateOnAnimation()
  }
}


// Estampado de leopardo morado para el "escritorio" del estilo Windows Media Player
class LeopardView(c: android.content.Context, private val hue: Int) : View(c) {
  private val p = Paint(Paint.ANTI_ALIAS_FLAG)
  private val rf = RectF()
  override fun onDraw(cv: Canvas) {
    val d = resources.displayMetrics.density
    cv.drawColor(ColorUtils.HSLToColor(floatArrayOf(hue.toFloat(), .42f, .60f)))
    val tile = 260f * d
    var seed = 7L
    fun r(): Float { seed = (seed * 16807) % 2147483647L; return seed / 2147483647f }
    val spots = ArrayList<FloatArray>()
    for (i in 0 until 15) spots.add(floatArrayOf(r() * tile, r() * tile, (13 + r() * 14) * d, (9 + r() * 11) * d, r() * 180f, (8 + r() * 6) * d))
    val fill = ColorUtils.HSLToColor(floatArrayOf(hue.toFloat(), .55f, .66f))
    val cols = (width / tile).toInt() + 1
    val rows = (height / tile).toInt() + 1
    for (tx in 0..cols) for (ty in 0..rows) for (s in spots) {
      val cx = tx * tile + s[0]; val cy = ty * tile + s[1]
      cv.save(); cv.rotate(s[4], cx, cy)
      rf.set(cx - s[2], cy - s[3], cx + s[2], cy + s[3])
      p.style = Paint.Style.FILL; p.color = fill; p.pathEffect = null
      cv.drawOval(rf, p)
      p.style = Paint.Style.STROKE; p.strokeWidth = s[5]; p.color = 0xFF07030F.toInt(); p.strokeCap = Paint.Cap.ROUND
      p.pathEffect = android.graphics.DashPathEffect(floatArrayOf(s[2] * 2.3f, s[2] * 0.9f), 0f)
      cv.drawOval(rf, p)
      cv.restore()
    }
  }
}

// Esfera azul del icono de Windows Media Player
class OrbView(c: android.content.Context) : View(c) {
  private val p = Paint(Paint.ANTI_ALIAS_FLAG)
  private val pa = Path()
  override fun onDraw(cv: Canvas) {
    val s = minOf(width, height) / 2f; val cx = width / 2f; val cy = height / 2f
    p.shader = android.graphics.RadialGradient(cx - s * .3f, cy - s * .4f, s * 1.6f,
      intArrayOf(0xFFFFFFFF.toInt(), 0xFF8FB8FF.toInt(), 0xFF1C3F9E.toInt(), 0xFF0A1A55.toInt()), floatArrayOf(0f, .3f, .7f, 1f), android.graphics.Shader.TileMode.CLAMP)
    cv.drawCircle(cx, cy, s, p); p.shader = null
    p.color = Color.WHITE
    pa.reset(); pa.moveTo(cx - s * .25f, cy - s * .45f); pa.lineTo(cx + s * .5f, cy); pa.lineTo(cx - s * .25f, cy + s * .45f); pa.close()
    cv.drawPath(pa, p)
  }
}

// Botones de ventana (minimizar, maximizar, cerrar) con bisel claro
class WinBtn(c: android.content.Context, private val kind: String, private val hue: Int) : View(c) {
  private val p = Paint(Paint.ANTI_ALIAS_FLAG)
  private val rf = RectF()
  override fun onDraw(cv: Canvas) {
    val d = resources.displayMetrics.density
    rf.set(1f, 1f, width - 1f, height - 1f)
    p.style = Paint.Style.FILL
    p.shader = android.graphics.LinearGradient(0f, 0f, 0f, height.toFloat(), ColorUtils.HSLToColor(floatArrayOf(hue.toFloat(), .7f, .9f)), ColorUtils.HSLToColor(floatArrayOf(hue.toFloat(), .45f, .62f)), android.graphics.Shader.TileMode.CLAMP)
    cv.drawRoundRect(rf, 3 * d, 3 * d, p); p.shader = null
    p.style = Paint.Style.STROKE; p.strokeWidth = d; p.color = 0xCCFFFFFF.toInt()
    cv.drawRoundRect(rf, 3 * d, 3 * d, p)
    p.color = ColorUtils.HSLToColor(floatArrayOf(hue.toFloat(), .48f, .2f)); p.strokeWidth = 2.2f * d; p.strokeCap = Paint.Cap.ROUND
    val cx = width / 2f; val cy = height / 2f; val k = 4f * d
    when (kind) {
      "min" -> cv.drawLine(cx - k, cy + k, cx + k * .6f, cy + k, p)
      "max" -> { p.strokeWidth = 1.8f * d; cv.drawRect(cx - k, cy - k, cx + k, cy + k, p) }
      else -> { cv.drawLine(cx - k, cy - k, cx + k, cy + k, p); cv.drawLine(cx + k, cy - k, cx - k, cy + k, p) }
    }
  }
}
