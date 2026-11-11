package com.jk.music

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.audiofx.Equalizer
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

// ---------------------------------------------------------------------------
// Extracción del color dominante de una portada.
// Reduce la imagen a 48x48, agrupa los píxeles por color aproximado y elige el
// grupo más "pesado" (favoreciendo colores saturados). El resultado se oscurece
// un poco para que el texto blanco siempre se lea bien encima.
// ---------------------------------------------------------------------------
fun dominantColor(b: Bitmap): Int {
  val sm = Bitmap.createScaledBitmap(b, 48, 48, true)
  val px = IntArray(48 * 48)
  sm.getPixels(px, 0, 48, 0, 0, 48, 48)
  val buckets = HashMap<Int, DoubleArray>()
  val hsv = FloatArray(3)
  for (c in px) {
    val r = Color.red(c); val g = Color.green(c); val bl = Color.blue(c)
    Color.RGBToHSV(r, g, bl, hsv)
    if (hsv[2] < 0.15f || (hsv[1] < 0.12f && hsv[2] > 0.9f)) continue
    val key = ((r shr 5) shl 6) or ((g shr 5) shl 3) or (bl shr 5)
    val w = 0.3 + hsv[1]
    val a = buckets.getOrPut(key) { DoubleArray(4) }
    a[0] += w; a[1] += r * w; a[2] += g * w; a[3] += bl * w
  }
  val best = buckets.values.maxByOrNull { it[0] } ?: return 0xFF2A1A3A.toInt()
  val c = Color.rgb((best[1] / best[0]).toInt(), (best[2] / best[0]).toInt(), (best[3] / best[0]).toInt())
  val h = FloatArray(3)
  Color.colorToHSV(c, h)
  h[2] = minOf(h[2], 0.55f)
  return Color.HSVToColor(h)
}

// ---------------------------------------------------------------------------
// Barra flotante sobre otras apps. Muestra portada, título, artista y controles.
// Su fondo toma el color dominante de la portada y cambia con un fundido suave.
// ---------------------------------------------------------------------------
class FloatBar(private val ctx: Context, private val player: Player, private val onClose: () -> Unit) {
  private val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
  private var root: LinearLayout? = null
  private var cover: ImageView? = null
  private var title: TextView? = null
  private var artist: TextView? = null
  private var play: IconView? = null
  private val bg = GradientDrawable()
  private var cur = 0xFF2A1A3A.toInt()
  private var anim: ValueAnimator? = null
  private var lastArt: ByteArray? = null
  private val lis = object : Player.Listener {
    override fun onMediaMetadataChanged(m: MediaMetadata) { refresh() }
    override fun onIsPlayingChanged(p: Boolean) { refresh() }
  }
  private fun dp(x: Int) = (x * ctx.resources.displayMetrics.density).toInt()

  fun show() {
    if (root != null) return
    bg.cornerRadius = dp(28).toFloat(); bg.setColor(cur)
    val r = LinearLayout(ctx).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(10), dp(8), dp(10), dp(8)); background = bg }
    val cv = ImageView(ctx).apply {
      scaleType = ImageView.ScaleType.CENTER_CROP; clipToOutline = true
      background = GradientDrawable().apply { cornerRadius = dp(14).toFloat(); setColor(0x22FFFFFF) }
    }
    cover = cv
    r.addView(cv, LinearLayout.LayoutParams(dp(46), dp(46)))
    val col = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL }
    val t = TextView(ctx).apply { setTextColor(Color.WHITE); textSize = 14f; typeface = Typeface.DEFAULT_BOLD; maxLines = 1; ellipsize = TextUtils.TruncateAt.END }
    val a = TextView(ctx).apply { setTextColor(0xB3FFFFFF.toInt()); textSize = 12f; maxLines = 1; ellipsize = TextUtils.TruncateAt.END }
    title = t; artist = a
    col.addView(t); col.addView(a)
    r.addView(col, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(10); marginEnd = dp(4) })
    fun ib(kind: String, f: () -> Unit) = IconView(ctx, kind, Color.WHITE).apply { setPadding(dp(8), dp(8), dp(8), dp(8)); setOnClickListener { f() } }
    r.addView(ib("prev") { player.seekToPrevious() }, LinearLayout.LayoutParams(dp(40), dp(40)))
    val pl = ib("play") { if (player.isPlaying) player.pause() else player.play() }
    play = pl
    r.addView(pl, LinearLayout.LayoutParams(dp(44), dp(44)))
    r.addView(ib("next") { player.seekToNext() }, LinearLayout.LayoutParams(dp(40), dp(40)))
    r.addView(ib("close") { hide(); onClose() }, LinearLayout.LayoutParams(dp(36), dp(36)))
    r.setOnClickListener {
      ctx.startActivity(Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP))
    }
    val lp = WindowManager.LayoutParams(
      ctx.resources.displayMetrics.widthPixels - dp(24), WindowManager.LayoutParams.WRAP_CONTENT,
      WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
    )
    lp.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
    lp.y = dp(64)
    try { wm.addView(r, lp) } catch (e: Exception) { return }
    root = r
    player.addListener(lis)
    refresh()
  }

  fun hide() {
    anim?.cancel()
    root?.let { try { wm.removeView(it) } catch (e: Exception) {} }
    root = null
    try { player.removeListener(lis) } catch (e: Exception) {}
  }

  private fun refresh() {
    val md = player.mediaMetadata
    title?.text = md.title ?: (ctx.getSharedPreferences("p", 0).getString("aname", "JKMUSICK") ?: "JKMUSICK")
    artist?.text = md.artist ?: ""
    play?.kind = if (player.isPlaying) "pause" else "play"
    val bytes = md.artworkData
    if (bytes != null) {
      if (bytes !== lastArt) {
        lastArt = bytes
        val b = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        if (b != null) { cover?.setImageBitmap(b); fadeTo(dominantColor(b)) }
      }
    } else {
      if (lastArt != null) { lastArt = null; fadeTo(0xFF2A1A3A.toInt()) }
      // Sin portada: se muestra la imagen que elegiste para el reproductor (si hay)
      val lf = java.io.File(ctx.filesDir, "ic_logo")
      if (lf.exists()) cover?.setImageBitmap(BitmapFactory.decodeFile(lf.path)) else cover?.setImageDrawable(null)
    }
  }

  // Fundido suave entre el color anterior y el nuevo
  private fun fadeTo(to: Int) {
    anim?.cancel()
    anim = ValueAnimator.ofObject(ArgbEvaluator(), cur, to).apply {
      duration = 600
      addUpdateListener { cur = it.animatedValue as Int; bg.setColor(cur) }
      start()
    }
  }
}

class PlayService : MediaSessionService() {
  private var session: MediaSession? = null
  private var bar: FloatBar? = null
  private var dismissed = false

  companion object {
    @Volatile var eq: Equalizer? = null
    private var player: ExoPlayer? = null
    private var inst: PlayService? = null
    private val h = Handler(Looper.getMainLooper())
    private val stopper = Runnable { player?.pause() }
    fun sleep(min: Int) { h.removeCallbacks(stopper); if (min > 0) h.postDelayed(stopper, min * 60000L) }
    fun setBand(i: Int, l: Int) { try { eq?.setBandLevel(i.toShort(), l.toShort()) } catch (e: Exception) {} }
    // La app llama a esto al pasar a segundo plano (true) o volver (false)
    fun showFloat(on: Boolean) { inst?.floatBar(on) }
  }

  override fun onCreate() {
    super.onCreate()
    inst = this
    val attrs = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build()
    val p = ExoPlayer.Builder(this).setAudioAttributes(attrs, true).setHandleAudioBecomingNoisy(true).setWakeMode(C.WAKE_MODE_LOCAL).build()
    player = p
    try {
      val sid = (getSystemService(AUDIO_SERVICE) as AudioManager).generateAudioSessionId()
      p.audioSessionId = sid
      val e = Equalizer(0, sid)
      e.enabled = true
      val sp = getSharedPreferences("p", 0)
      for (i in 0 until e.numberOfBands.toInt()) e.setBandLevel(i.toShort(), sp.getInt("eq" + i, 0).toShort())
      eq = e
    } catch (e: Exception) { eq = null }
    session = MediaSession.Builder(this, p).build()
  }

  private fun floatBar(on: Boolean) {
    val p = player ?: return
    if (!on) { dismissed = false; bar?.hide(); return }
    if (dismissed || !Settings.canDrawOverlays(this) || p.currentMediaItem == null) return
    if (bar == null) bar = FloatBar(this, p) { dismissed = true }
    bar?.show()
  }

  override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

  override fun onDestroy() {
    h.removeCallbacks(stopper)
    bar?.hide(); bar = null; inst = null
    try { eq?.release() } catch (e: Exception) {}
    eq = null; player = null
    session?.let { it.player.release(); it.release() }
    session = null
    super.onDestroy()
  }
}
