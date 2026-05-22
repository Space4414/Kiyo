package com.space4414.kiyo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Equalizer
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.space4414.kiyo.ui.theme.KiyoTeal

data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

val kiyoNavItems = listOf(
    NavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NavItem("search", "Search", Icons.Filled.Search, Icons.Outlined.Search),
    NavItem("library", "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    NavItem("dsp", "Audio DSP", Icons.Filled.Equalizer, Icons.Outlined.Equalizer),
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FrostedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        cornerRadius = 32.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            kiyoNavItems.forEach { item ->
                NavBarItem(
                    item = item,
                    selected = currentRoute == item.route,
                    onClick = { onNavItemClick(item.route) },
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            modifier = Modifier.size(22.dp),
            tint = if (selected) KiyoTeal else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) KiyoTeal else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .size(width = 18.dp, height = 3.dp)
                    .clip(CircleShape)
                    .background(KiyoTeal),
            )
        } else {
            Spacer(Modifier.height(3.dp))
        }
    }
}
