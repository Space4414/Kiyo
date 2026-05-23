package com.space4414.kiyo.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.space4414.kiyo.R
import com.space4414.kiyo.ui.theme.KiyoTeal

data class NavItem(
    val route: String,
    val label: String,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int,
)

val kiyoNavItems = listOf(
    NavItem("home",    "Home",      R.drawable.ic_kiyo_home_filled,           R.drawable.ic_kiyo_home_outline),
    NavItem("search",  "Search",    R.drawable.ic_kiyo_search_filled,         R.drawable.ic_kiyo_search_outline),
    NavItem("library", "Library",   R.drawable.ic_kiyo_library_music_filled,  R.drawable.ic_kiyo_library_music_outline),
    NavItem("dsp",     "Audio DSP", R.drawable.ic_kiyo_equalizer_filled,      R.drawable.ic_kiyo_equalizer_outline),
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
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
private fun NavBarItem(item: NavItem, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            painter = painterResource(if (selected) item.selectedIcon else item.unselectedIcon),
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
            Box(modifier = Modifier.size(width = 18.dp, height = 3.dp).clip(CircleShape).background(KiyoTeal))
        } else {
            Spacer(Modifier.height(3.dp))
        }
    }
}
