package com.srilakshmikanthanp.clipbirdroid.ui.gui.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tabbed Composable that takes a list of tabs and a content composable
 */
@Composable
fun GTab(
  tabs: List<String>,
  selectedTab: Int,
  onTabClick: (Int) -> Unit,
) {
  TabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent, divider = { }) {
    tabs.forEachIndexed { index, title ->
      Tab(onClick = { onTabClick(index) }, selected = (selectedTab == index)) {
        Text(text = title, fontSize = 16.sp, modifier = Modifier.padding(16.dp))
      }
    }
  }
}
