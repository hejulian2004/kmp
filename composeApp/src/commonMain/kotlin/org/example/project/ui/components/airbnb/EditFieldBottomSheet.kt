/**
 * @File: EditFieldBottomSheet.kt
 * @Package: org.example.project.ui.components.airbnb
 * @Description: Airbnb 属性字段编辑底部弹窗组件
 * @Author: 何聚敛
 * @Date: 2026-08-05
 */
package org.example.project.ui.components.airbnb

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.ui.theme.airbnb.Accent
import org.example.project.ui.theme.airbnb.CardBg
import org.example.project.ui.theme.airbnb.DividerColor
import org.example.project.ui.theme.airbnb.TextPrimary
import org.example.project.ui.theme.airbnb.TextSecondary

enum class ProfileField(
    val label: String,
    val hint: String,
    val maxLength: Int,
) {
    NAME("房东姓名", "", 40),
    OCCUPATION("职业", "例如：设计师、工程师", 40),
    LIVES_IN("住在哪里", "例如：香港、上海", 40),
    LANGUAGES("语言", "例如：中文、English", 60),
    HOBBIES_ADD("添加兴趣爱好", "输入一个新的兴趣爱好", 40),
}

fun getFieldQuestion(field: ProfileField): String = when (field) {
    ProfileField.NAME -> "你的名字是？"
    ProfileField.OCCUPATION -> "你的工作是？"
    ProfileField.LIVES_IN -> "你住在哪里？"
    ProfileField.LANGUAGES -> "你会说哪些语言？"
    ProfileField.HOBBIES_ADD -> "你的兴趣爱好？"
}

fun getFieldDescription(field: ProfileField): String = when (field) {
    ProfileField.NAME -> "这是房客在你的名片和公开主页上看到的名称。"
    ProfileField.OCCUPATION -> "分享你的职业，让房客更好地了解你。"
    ProfileField.LIVES_IN -> "告诉房客你常住的城市或地区。"
    ProfileField.LANGUAGES -> "列出你能够用于沟通交流的语言。"
    ProfileField.HOBBIES_ADD -> "添加一个能够在个人页展示的趣味标签。"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFieldBottomSheet(
    field: ProfileField,
    currentValue: String,
    onDismiss: () -> Unit = {},
    onSave: (String) -> Unit = {},
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var text by remember { mutableStateOf(currentValue) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBg,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = TextPrimary,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = getFieldQuestion(field),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = getFieldDescription(field),
                fontSize = 15.sp,
                color = TextSecondary,
                lineHeight = 22.sp,
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= field.maxLength) text = it },
                placeholder = {
                    if (field.hint.isNotEmpty()) {
                        Text(field.hint, color = TextSecondary)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = DividerColor,
                    focusedLabelColor = TextPrimary,
                    cursorColor = Accent,
                ),
            )

            Text(
                text = "${text.length}/${field.maxLength} 个字符",
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp),
                fontSize = 12.sp,
                color = TextSecondary,
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onSave(text) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    "保存",
                    color = Color.White,
                    fontSize = 16.sp,
                )
            }
        }
    }
}
