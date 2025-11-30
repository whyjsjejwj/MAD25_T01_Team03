package np.ict.mad.studybuddy.feature.motivation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SimpleExpandableCard(
    title: String,
    tips: List<Pair<String, String>>
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .clickable { expanded = !expanded },
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7A5633)
            )

            if (expanded) {
                tips.forEach { (header, desc) ->
                    Text("• $header", fontWeight = FontWeight.SemiBold)
                    Text(desc, color = Color.Gray)
                    Spacer(Modifier.height(6.dp))
                }
            } else {
                Text("Tap to expand…", color = Color.Gray)
            }
        }
    }
}
