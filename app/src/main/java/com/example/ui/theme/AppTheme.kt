package com.example.ui.theme

/**
 * Theme options exposed on the Settings screen.
 *
 * MATERIAL       - the default Material You dynamic palette from the wallpaper.
 * M3_EXPRESSIVE  - same family, bolder shapes, larger radii, springier motion.
 * MONO_CONSOLE   - flat dark surfaces tuned for long flash sessions.
 */
enum class AppTheme(
    val label: String,
    val description: String
) {
    MATERIAL(
        label = "Material",
        description = "Default dynamic palette from your wallpaper"
    ),
    M3_EXPRESSIVE(
        label = "Material 3 Expressive",
        description = "Bolder shapes, larger controls, springier motion"
    ),
    MONO_CONSOLE(
        label = "Mono Console",
        description = "Flat dark surfaces tuned for long flash sessions"
    );

    companion object {
        fun fromName(name: String?): AppTheme = when (name) {
            // Legacy value saved by earlier builds.
            "LIQUID_GLASS" -> MONO_CONSOLE
            null -> MATERIAL
            else -> entries.firstOrNull { it.name == name } ?: MATERIAL
        }
    }
}
