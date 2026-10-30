package com.jk.music

import android.Manifest
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
  private val pal = intArrayOf(0xFF5EE0C0.toInt(), 0xFFFF7A90.toInt(), 0xFFFFC857.toInt(), 0xFF7AA2FF.toInt(), 0xFFC28CFF.toInt(), 0xFF9BE564.toInt(), 0xFFFFFFFF.toInt(), 0xFF17141F.toInt(), 0xFF000000.toInt(), 0xFF2B1A55.toInt())
  private val skinNames = arrayOf("Moderno", "iPod Nano", "iPod Clásico", "Consola")
  private val devT = intArrayOf(0, 0xFF5B3A9E.toInt(), 0xFF9A9A9A.toInt(), 0xFF2A2A2E.toInt())
  private val devB = intArrayOf(0, 0xFF2B1A55.toInt(), 0xFF4A4A4A.toInt(), 0xFF2A2A2E.toInt())
  private val scrC = intArrayOf(0, 0xFF000000.toInt(), 0xFFE6E6E6.toInt(), 0xFF000000.toInt())
  private val scrT = intArrayOf(0, 0xFFE9E2FF.toInt(), 0xFF1B1B1B.toInt(), 0xFFEEEEEE.toInt())
  private val whC = intArrayOf(0, 0xFF3A2468.toInt(), 0xFF222222.toInt(), 0xFF38383D.toInt())
  private val whT = intArrayOf(0, 0xFFCBB8FF.toInt(), 0xFFEEEEEE.toInt(), 0xFFDDDDDD.toInt())
  private val ctrC = intArrayOf(0, 0xFF5A3D9C.toInt(), 0xFF6B6B6B.toInt(), 0xFF4A4A50.toInt())

  private val pickFiles = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { r -> addAll(r) }
  private val pickDir = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u -> if (u != null) scan(u) }
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
    try {
      val a = JSONArray(sp.getString("list", "[]"))
      for (i in 0 until a.length()) { val o = a.getJSONObject(i); uris.add(o.getString("u")); names.add(o.getString("n")) }
    } catch (e: Exception) {}
    setContentView(buildUi())
    if (Build.VERSION.SDK_INT >= 33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
    val f = MediaController.Builder(this, SessionToken(this, ComponentName(this, PlayService::class.java))).buildAsync()
    f.addListener({
      val c = f.get()
      ctl = c
      c.addListener(lis)
      if (c.mediaItemCount == 0 && uris.isNotEmpty()) { c.setMediaItems(uris.indices.map { item(uris[it], names[it]) }); c.prepare() }
      applyVol(); refreshPlay(); upd()
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
  private fun cur() = ctl?.currentMediaItemIndex ?: -1
  private fun applyVol() { ctl?.volume = vol / 100f * maxVol / 100f }
  private fun upd() { titleV.text = names.getOrNull(cur()) ?: "Nada en reproducción"; (list.adapter as? BaseAdapter)?.notifyDataSetChanged() }
  private fun img(key: String): Bitmap? { val f = File(filesDir, "ic_" + key); return if (f.exists()) BitmapFactory.decodeFile(f.path) else null }

  private fun save() {
    val a = JSONArray()
    for (i in uris.indices) a.put(JSONObject().put("u", uris[i]).put("n", names[i]))
    val e = sp.edit().putString("list", a.toString()).putInt("a", accent).putInt("b", bg).putInt("v", vol).putInt("m", maxVol).putInt("skin", skin).putInt("shape", shapeMode)
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
    uris.add(u.toString()); names.add(name)
    ctl?.addMediaItem(item(u.toString(), name))
  }
  private fun keep(u: Uri) { try { contentResolver.takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (e: Exception) {} }
  private fun addAll(r: List<Uri>) {
    for (u in r) { keep(u); addUri(u, DocumentFile.fromSingleUri(this, u)?.name ?: "Canción") }
    save(); upd(); ctl?.prepare()
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
    save(); upd(); ctl?.prepare()
  }

  private fun makeAdapter() = object : ArrayAdapter<String>(this, 0, names) {
    override fun getView(p: Int, v: View?, g: ViewGroup): View {
      val t = (v as? TextView) ?: TextView(context)
      t.text = names[p]; t.textSize = 16f; t.maxLines = 1; t.ellipsize = TextUtils.TruncateAt.END
      t.setPadding(dp(12), dp(12), dp(12), dp(12))
      t.setTextColor(if (p == cur()) accent else st())
      t.setBackgroundColor(if (p == cur()) ((accent and 0x00FFFFFF) or 0x33000000) else 0)
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
    val bmp = img(key)
    if (bmp == null) {
      s.background = bshape(c, size)
      s.addView(TextView(this).apply {
        text = def; textSize = if (size > 60) 26f else if (def.length > 2) 12f else 20f
        typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; setTextColor(tc)
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
  private fun btn(key: String, def: String, size: Int, c: Int, tc: Int, f: (View) -> Unit) = FrameLayout(this).apply {
    layoutParams = LinearLayout.LayoutParams(dp(size), dp(size)).apply { setMargins(dp(6), 0, dp(6), 0) }
    fill(this, key, def, size, c, tc, f)
  }
  private fun togglePlay() {
    ctl?.let { if (it.isPlaying) it.pause() else { if (it.playbackState == Player.STATE_IDLE) it.prepare(); it.play() } }
  }
  private fun refreshPlay() {
    val p = ctl?.isPlaying == true
    fill(playBox, if (p) "pause" else "play", if (p) "⏸" else "▶", playSize, pcol(), on(pcol())) { _ -> togglePlay() }
  }

  private fun txt(t: String, f: () -> Unit) = TextView(this).apply {
    text = t; textSize = 22f; setTextColor(fg()); setPadding(dp(12), dp(8), dp(12), dp(8)); setOnClickListener { f() }
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
    background = GradientDrawable().apply { setColor(c); cornerRadius = dp(12).toFloat() }
  }

  private fun buildUi(): View {
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
    head.addView(txt("⚙") { settings() })
    root.addView(head)

    list = ListView(this).apply {
      divider = null
      adapter = makeAdapter()
      setOnItemClickListener { _, _, p, _ -> ctl?.let { c -> c.seekTo(p, 0); c.prepare(); c.play() } }
      setOnItemLongClickListener { _, _, p, _ ->
        AlertDialog.Builder(this@MainActivity).setMessage("¿Quitar «" + names[p] + "» de la lista?")
          .setPositiveButton("Quitar") { _, _ -> uris.removeAt(p); names.removeAt(p); ctl?.removeMediaItem(p); save(); upd() }
          .setNegativeButton("Cancelar", null).show()
        true
      }
    }
    titleV = TextView(this).apply { textSize = 17f; typeface = Typeface.DEFAULT_BOLD; setTextColor(st()); maxLines = 1; ellipsize = TextUtils.TruncateAt.END; setPadding(0, dp(8), 0, dp(4)); text = "Nada en reproducción" }
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
    playSize = if (skin == 0) 72 else 84
    playBox = FrameLayout(this)
    val vr = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(0, dp(8), 0, 0) }

    if (skin == 0) {
      root.addView(list, LinearLayout.LayoutParams(-1, 0, 1f))
      root.addView(titleV); root.addView(seek); root.addView(time)
      val row = LinearLayout(this).apply { gravity = Gravity.CENTER; setPadding(0, dp(8), 0, dp(8)) }
      row.addView(sh); row.addView(pv)
      row.addView(playBox, LinearLayout.LayoutParams(dp(72), dp(72)).apply { setMargins(dp(6), 0, dp(6), 0) })
      row.addView(nx); row.addView(rp)
      root.addView(row)
      vr.addView(TextView(this).apply { text = "🔊"; textSize = 18f; setPadding(0, 0, dp(8), 0) })
    } else {
      val dev = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(14))
        background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(devT[skin], devB[skin])).apply { cornerRadius = dp(28).toFloat() }
      }
      if (skin == 3) {
        val a = box(scrC[skin]); a.addView(titleV); a.addView(seek); a.addView(time)
        val l = box(scrC[skin]); l.addView(list, LinearLayout.LayoutParams(-1, -1))
        dev.addView(a)
        dev.addView(l, LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(10) })
      } else {
        val s = box(scrC[skin]); s.addView(titleV); s.addView(seek); s.addView(time)
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
    }
    vr.addView(sb(100, vol) { vol = it; applyVol(); save() }, LinearLayout.LayoutParams(0, -2, 1f))
    if (skin == 0) root.addView(vr)
    refreshPlay()
    return outer
  }

  // Ajustes
  private fun lab(t: String) = TextView(this).apply { text = t; textSize = 15f; typeface = Typeface.DEFAULT_BOLD; setPadding(0, dp(14), 0, dp(2)) }
  private fun act(t: String, f: () -> Unit) = TextView(this).apply {
    text = t; setPadding(dp(12), dp(8), dp(12), dp(8)); setTextColor(Color.WHITE)
    background = GradientDrawable().apply { cornerRadius = dp(16).toFloat(); setColor(0x55888888) }
    layoutParams = LinearLayout.LayoutParams(-2, -2).apply { setMargins(dp(6), dp(4), 0, dp(4)) }
    setOnClickListener { f() }
  }
  private fun group(opts: Array<String>, cur: Int, f: (Int) -> Unit): View {
    val row = LinearLayout(this)
    val cs = ArrayList<TextView>()
    fun paint(sel: Int) {
      for (i in cs.indices) { cs[i].setTextColor(if (i == sel) on(accent) else Color.WHITE); (cs[i].background as GradientDrawable).setColor(if (i == sel) accent else 0x33888888) }
    }
    for (i in opts.indices) {
      val t = TextView(this).apply {
        text = opts[i]; setPadding(dp(12), dp(8), dp(12), dp(8))
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
    r.addView(act("Quitar") {
      File(filesDir, "ic_" + key).delete()
      sp.edit().remove("z_" + key).remove("x_" + key).remove("y_" + key).apply(); theme()
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
    val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(8), dp(20), dp(4)) }
    box.addView(lab("Estilo del reproductor"))
    box.addView(group(skinNames, skin) { skin = it; theme() })
    box.addView(sw("Color principal") { accent = it; theme() })
    box.addView(sw("Color de fondo") { bg = it; theme() })
    box.addView(imgRow("Imagen de fondo", "bg"))
    box.addView(sw("Color del botón de reproducir") { playC = it; hasPc = true; theme() })
    box.addView(act("Automático") { hasPc = false; sp.edit().remove("pc").apply(); theme() })
    box.addView(lab("Forma de los botones con imagen"))
    box.addView(group(arrayOf("Círculo", "Redondeado", "Sin recorte"), shapeMode) { shapeMode = it; theme() })
    box.addView(lab("Imágenes de los botones"))
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
    AlertDialog.Builder(this).setTitle("Personalizar").setView(ScrollView(this).apply { addView(box) }).setPositiveButton("Listo", null).show()
  }
}
