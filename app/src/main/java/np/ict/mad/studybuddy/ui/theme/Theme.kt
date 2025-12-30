package np.ict.mad.studybuddy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Good readable text color for warm themes
private val WarmTextDark = Color(0xFF2B1A00)

// LIGHT MODE COLORS (High contrast)
private val LightColorScheme = lightColorScheme(
    // Make CTA (buttons) dark so white text is readable
    primary = PurpleGrey40,          // Brown
    onPrimary = Color.White,

    // Use your warm yellow as accent, with dark text on top
    secondary = Purple40,            // Warm Yellow
    onSecondary = WarmTextDark,

    tertiary = Pink40,               // Cream
    onTertiary = WarmTextDark,

    background = Color(0xFFFFFCF5),
    onBackground = WarmTextDark,

    surface = Color(0xFFFFFFFF),
    onSurface = WarmTextDark
)

// DARK MODE COLORS (High contrast)
private val DarkColorScheme = darkColorScheme(
    // Gold CTA works nicely in dark mode with dark text
    primary = Purple80,              // Deep Yellow/Gold
    onPrimary = Color(0xFF1C1B18),   // near your dark background (better than pure black)

    secondary = PurpleGrey80,        // Deep Brown
    onSecondary = Color(0xFFFFF4CE), // Cream text

    tertiary = Pink80,               // Warm muted cream
    onTertiary = Color(0xFF1C1B18),

    background = Color(0xFF1C1B18),
    onBackground = Color(0xFFE8DEBD),

    surface = Color(0xFF2A2925),
    onSurface = Color(0xFFE8DEBD)
)

@Composable
fun StudyBuddyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // disabled
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
