package com.example.ui.theme

/**
 * Theme engine options exposed on the Settings screen.
 *
 * MATERIAL        - the default dynamic/teal Material 3 look.
 * M3_EXPRESSIVE   - same family, bolder shapes, larger radii, elevated motion.
 * LIQUID_GLASS    - dark instrument palette with a translucent glass navigation bar.
 */
enum class AppTheme(
    val label: String,
    val description: String
) {
    LIQUID_GLASS(
        label = "Liquid Glass",
        description = "Translucent nav bar, blurred device backdrop"
    ),
    MATERIAL(
        label = "Material",
        description = "Default dynamic palette, predictable density"
    ),
    M3_EXPRESSIVE(
        label = "Material 3 Expressive",
        description = "Bolder shapes and elevated motion"
    );

    companion object {
        fun fromName(name: String?): AppTheme =
            entries.firstOrNull { it.name == name } ?: MATERIAL
    }
}
