package com.bhaskar.pixelwalls.presentation.ai.ui_components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bhaskar.pixelwalls.domain.model.PromptTemplate

@Composable
fun PromptText(
    template: PromptTemplate,
    selection: Map<String, Int>,
    onVariableClick: (String) -> Unit
) {
    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        val variablePattern = """\{(\w+)\}""".toRegex()

        variablePattern.findAll(template.template).forEach { match ->
            append(template.template.substring(currentIndex, match.range.first))

            val variableKey = match.groupValues[1]
            val variable = template.variables[variableKey]
            val selectedIndex = selection[variableKey] ?: 0
            val value = variable?.options?.getOrNull(selectedIndex)?.value ?: ""

            withLink(
                link = LinkAnnotation.Clickable(
                    tag = variableKey,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.ExtraBold,
                            textDecoration = TextDecoration.Underline
                        )
                    ),
                    linkInteractionListener = {
                        onVariableClick(variableKey)
                    }
                )
            ) {
                append(value)
            }

            currentIndex = match.range.last + 1
        }

        if (currentIndex < template.template.length) {
            append(template.template.substring(currentIndex))
        }
    }

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.headlineSmall.copy(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    )
}