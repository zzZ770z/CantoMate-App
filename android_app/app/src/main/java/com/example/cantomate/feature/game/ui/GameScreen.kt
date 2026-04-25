package com.example.cantomate.feature.game.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cantomate.feature.game.model.GameWordItem
import com.example.cantomate.feature.game.viewmodel.GameViewModel

@Composable
fun GameScreen(viewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            "粵語配對挑戰",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "Master Cantonese through matching",
            color = Color.Gray,
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // 状态卡片
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A1A1A),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = viewModel.statusText,
                    color = Color(0xFF00B894),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                if (viewModel.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        color = Color(0xFF00B894),
                        trackColor = Color(0xFF262626)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.leftItems.isEmpty() && !viewModel.isLoading) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Button(
                    onClick = { viewModel.startGame() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B894)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(56.dp).fillMaxWidth()
                ) {
                    Text("開始挑戰", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Row(modifier = Modifier.weight(1f)) {
                // 左侧普通话
                Column(modifier = Modifier.weight(1f)) {
                    SectionHeader("普通話")
                    Spacer(modifier = Modifier.height(12.dp))
                    viewModel.leftItems.forEach { item ->
                        WordCard(
                            text = item.text,
                            isMatched = viewModel.matchedIds.contains(item.id),
                            isSelected = viewModel.selectedLeftId == item.id,
                            onClick = { viewModel.onLeftItemSelected(item.id, item.text) }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 右侧粤语
                Column(modifier = Modifier.weight(1f)) {
                    SectionHeader("粵語")
                    Spacer(modifier = Modifier.height(12.dp))
                    viewModel.rightItems.forEach { item ->
                        WordCard(
                            text = item.text,
                            isMatched = viewModel.matchedIds.contains(item.id),
                            isSelected = false,
                            onClick = { viewModel.onRightItemSelected(item) }
                        )
                    }
                }
            }

            if (viewModel.matchedIds.size == 5) {
                Button(
                    onClick = { viewModel.startGame() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("再玩一次", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Color.Gray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
fun WordCard(
    text: String,
    isMatched: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isMatched -> Color(0xFF2E7D32).copy(alpha = 0.2f)
        isSelected -> Color(0xFF0095F6)
        else -> Color(0xFF262626)
    }
    
    val borderColor = when {
        isMatched -> Color(0xFF2E7D32)
        isSelected -> Color(0xFF0095F6)
        else -> Color.Transparent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(enabled = !isMatched, onClick = onClick),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        border = if (isMatched || isSelected) androidx.compose.foundation.BorderStroke(2.dp, borderColor) else null
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (isMatched) Color(0xFF81C784) else Color.White,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
