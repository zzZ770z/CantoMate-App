package com.example.cantomate.jyutping

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun JyutpingChartScreen(
    viewModel: JyutpingChartViewModel = viewModel(),
    context: Context
) {
    val tabTitles = listOf("声母", "韵母", "声调")
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })

    LaunchedEffect(selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
    }
    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE63946))
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "粤语粤拼发音看板",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.White,
            contentColor = Color(0xFFE63946)
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }


        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> JyutpingGridList(dataList = jyutpingInitials) {
                    viewModel.playJyutpingAudio(context, it.exampleWord)
                }
                1 -> JyutpingGridList(dataList = jyutpingFinals) {
                    viewModel.playJyutpingAudio(context, it.exampleWord)
                }
                2 -> ToneVisualScreen(dataList = jyutpingTones) {
                    viewModel.playJyutpingAudio(context, it.exampleWord)
                }
            }
        }
    }
}

@Composable
private fun JyutpingGridList(
    dataList: List<JyutpingItem>,
    onCardClick: (JyutpingItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(dataList) { item ->
            JyutpingCard(item = item, onCardClick = onCardClick)
        }
    }
}

@Composable
private fun JyutpingCard(
    item: JyutpingItem,
    onCardClick: (JyutpingItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .clickable { onCardClick(item) }
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = item.code,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE63946)
            )
            Text(
                text = item.exampleChar,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D3142)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "例词：${item.exampleWord}",
            fontSize = 14.sp,
            color = Color(0xFF4F5D75)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.mouthTip,
            fontSize = 12.sp,
            color = Color(0xFF8D99AE),
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun ToneVisualScreen(
    dataList: List<JyutpingItem>,
    onCardClick: (JyutpingItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "粤语6声调高低变化对照",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3142),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "高音", fontSize = 10.sp, color = Color(0xFF8D99AE))
                Text(text = "中音", fontSize = 10.sp, color = Color(0xFF8D99AE))
                Text(text = "低音", fontSize = 10.sp, color = Color(0xFF8D99AE))
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 30.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                dataList.forEach { tone ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(when (tone.code) {
                                    "1" -> 80.dp
                                    "2" -> 50.dp
                                    "3" -> 40.dp
                                    "4" -> 20.dp
                                    "5" -> 35.dp
                                    "6" -> 25.dp
                                    else -> 40.dp
                                })
                                .background(Color(0xFFE63946), shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${tone.code}声",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dataList) { item ->
                JyutpingCard(item = item, onCardClick = onCardClick)
            }
        }
    }
}