package com.space4414.kiyo.ui.component

  import androidx.annotation.DrawableRes
  import androidx.compose.foundation.background
  import androidx.compose.foundation.clickable
  import androidx.compose.foundation.layout.*
  import androidx.compose.foundation.shape.CircleShape
  import androidx.compose.material3.Icon
  import androidx.compose.runtime.Composable
  import androidx.compose.ui.Alignment
  import androidx.compose.ui.Modifier
  import androidx.compose.ui.draw.clip
  import androidx.compose.ui.graphics.Color
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
      NavItem("library", "Library",   R.drawable.ic_kiyo_library_music_filled,  R.drawable.ic_kiyo_library_music_outline),
      NavItem("search",  "Search",    R.drawable.ic_kiyo_search_filled,         R.drawable.ic_kiyo_search_outline),
      NavItem("dsp",     "Audio DSP", R.drawable.ic_kiyo_equalizer_filled,      R.drawable.ic_kiyo_equalizer_outline),
  )

  /**
   * Poweramp-inspired bottom navigation: icon-only, no text labels, flat black bar.
   */
  @Composable
  fun BottomNavBar(
      currentRoute: String,
      onNavItemClick: (String) -> Unit,
      modifier: Modifier = Modifier,
  ) {
      Row(
          modifier = modifier
              .fillMaxWidth()
              .background(Color(0xFF0D0D0D))
              .padding(vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
          verticalAlignment = Alignment.CenterVertically,
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

  @Composable
  private fun NavBarItem(item: NavItem, selected: Boolean, onClick: () -> Unit) {
      Box(
          modifier = Modifier
              .clip(CircleShape)
              .clickable(onClick = onClick)
              .padding(12.dp),
          contentAlignment = Alignment.Center,
      ) {
          Icon(
              painter = painterResource(if (selected) item.selectedIcon else item.unselectedIcon),
              contentDescription = item.label,
              modifier = Modifier.size(24.dp),
              tint = if (selected) KiyoTeal else Color(0xFF6E6E73),
          )
      }
  }
  