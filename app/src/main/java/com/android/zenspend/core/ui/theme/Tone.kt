package com.android.zenspend.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * A "tone" is a named color pairing used by bento tiles, icon badges and chips.
 * Screens ask for a [ZenTone] (e.g. `ZenTone.Lime`) — never for a hex value —
 * and resolve it with `ZenSpendTheme.colors.tone(tone)`.
 */
enum class ZenTone {
    /** Hero tile: forest green (deeper forest in dark mode). */
    Ink,
    Lime,
    Coral,
    Leaf,
    Sky,
    Sun,
    Mint,

    /** Plain card surface (white in light mode). */
    Surface;

    companion object {
        /**
         * Pastel tones handed out to categories/wallets in rotation, so every
         * category gets a stable colour without any per-category hardcoding.
         */
        val Rotation: List<ZenTone> = listOf(Sun, Leaf, Sky, Mint, Coral, Lime)

        /** Stable tone for any id (category id, wallet id, hash of a name, ...). */
        fun forKey(key: Long): ZenTone = Rotation[Math.floorMod(key, Rotation.size.toLong()).toInt()]

        fun forKey(key: String): ZenTone = forKey(key.lowercase().hashCode().toLong())
    }
}

@Immutable
data class ToneColors(
    /** Fill of the tile / badge. */
    val container: Color,
    /** Primary content (text/icons) on [container]. */
    val content: Color,
    /** Secondary content (captions, labels) on [container]. */
    val contentMuted: Color,
    /** Subtle overlay used for chips/arrows sitting on [container]. */
    val overlay: Color,
    /** Track colour for progress bars drawn on [container]. */
    val track: Color
)

@Immutable
data class ZenTonePalette(
    val ink: ToneColors,
    val lime: ToneColors,
    val coral: ToneColors,
    val leaf: ToneColors,
    val sky: ToneColors,
    val sun: ToneColors,
    val mint: ToneColors,
    val surface: ToneColors
) {
    fun of(tone: ZenTone): ToneColors = when (tone) {
        ZenTone.Ink -> ink
        ZenTone.Lime -> lime
        ZenTone.Coral -> coral
        ZenTone.Leaf -> leaf
        ZenTone.Sky -> sky
        ZenTone.Sun -> sun
        ZenTone.Mint -> mint
        ZenTone.Surface -> surface
    }
}

/** Builds a pastel tone: fixed Ink text on a light fill (works in light and dark mode). */
internal fun pastelTone(container: Color, ink: Color) = ToneColors(
    container = container,
    content = ink,
    contentMuted = ink.copy(alpha = 0.62f),
    overlay = ink.copy(alpha = 0.10f),
    track = ink.copy(alpha = 0.12f)
)

internal fun lightTonePalette() = ZenTonePalette(
    ink = ToneColors(Forest, Bone, Bone.copy(alpha = 0.72f), Bone.copy(alpha = 0.14f), Bone.copy(alpha = 0.16f)),
    lime = pastelTone(Lime, Ink),
    coral = pastelTone(Coral, Ink),
    leaf = pastelTone(Leaf, Ink),
    sky = pastelTone(Sky, Ink),
    sun = pastelTone(Sun, Ink),
    mint = pastelTone(Mint, Ink),
    surface = ToneColors(Paper, Ink, InkSoft, Ink.copy(alpha = 0.06f), Ink.copy(alpha = 0.08f))
)

internal fun darkTonePalette() = ZenTonePalette(
    ink = ToneColors(ForestDeep, Bone, Bone.copy(alpha = 0.72f), Bone.copy(alpha = 0.14f), Bone.copy(alpha = 0.16f)),
    lime = pastelTone(Lime, Ink),
    coral = pastelTone(Coral, Ink),
    leaf = pastelTone(Leaf, Ink),
    sky = pastelTone(Sky, Ink),
    sun = pastelTone(Sun, Ink),
    mint = pastelTone(Mint, Ink),
    surface = ToneColors(NightRaised, Bone, BoneSoft, Bone.copy(alpha = 0.08f), Bone.copy(alpha = 0.10f))
)
