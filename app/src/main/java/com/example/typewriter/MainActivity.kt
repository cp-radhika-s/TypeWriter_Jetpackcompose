package com.example.typewriter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.typewriter.ui.theme.TypewriterTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TypewriterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background
                ) {

                }
            }
        }
    }
}

@Composable
fun Typewriter(
    baseText: String,
    highlightedText: String,
    parts: List<String>
) {

    var partIndex by remember { mutableStateOf(0) }
    val highlightStart = baseText.indexOf(highlightedText)


    var partText by remember { mutableStateOf("") }

    val textToDisplay = "$baseText$partText"
    var selectedPartRects by remember { mutableStateOf(listOf<Rect>()) }

    LaunchedEffect(key1 = parts) {
        while (partIndex <= parts.size) {

            val part = parts[partIndex]

            part.forEachIndexed { charIndex, _ ->
                partText = part.substring(startIndex = 0, endIndex = charIndex + 1)
                delay(100)
            }

            delay(1000)

            part.forEachIndexed { charIndex, _ ->
                partText = part
                    .substring(startIndex = 0, endIndex = part.length - (charIndex + 1))
                delay(30)
            }

            delay(500)

            partIndex = (partIndex + 1) % parts.size
        }
    }

    Text(
        text = textToDisplay,
        style = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 40.sp,
            letterSpacing = -(1.6).sp,
            lineHeight = 52.sp,
            color = Color.White
        ),
        modifier = Modifier.drawBehind {
            val borderSize = 20.sp.toPx()

            selectedPartRects.forEach { rect ->
                val selectedRect = rect.translate(0f, -borderSize / 1.5f)
                drawLine(  //85586F
                    color = Color(0XFF85586F).copy(1f),
                    start = Offset(selectedRect.left, selectedRect.bottom),
                    end = selectedRect.bottomRight,
                    strokeWidth = borderSize
                )
            }
        },
        onTextLayout = { layoutResult ->
            val start = baseText.length
            val end = textToDisplay.count()
            selectedPartRects = if (start < end) {
                layoutResult
                    .getBoundingBoxesForRange(
                        start = start,
                        end = end - 1
                    )
            } else {
                emptyList()
            }

            if (highlightStart >= 0) {
                selectedPartRects = selectedPartRects + layoutResult
                    .getBoundingBoxesForRange(
                        start = highlightStart,
                        end = highlightStart + highlightedText.length
                    )
            }
        }

    )

}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val animatePartsList = remember {
        listOf(
            "reach your goals.",
            "achieve your dreams.",
            "be happy.",
            "be healthy.",
            "get rid of depression."
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(20.dp),
        contentAlignment = Alignment.Center,

    ) {
        TypewriterTheme {
            Typewriter(
                "Everything you need to ",
                "Everything", animatePartsList
            )
        }
    }
}

fun TextLayoutResult.getBoundingBoxesForRange(start: Int, end: Int): List<Rect> {
    var prevRect: Rect? = null
    var firstLineCharRect: Rect? = null
    val boundingBoxes = mutableListOf<Rect>()
    for (i in start..end) {
        val rect = getBoundingBox(i)
        val isLastRect = i == end

        // single char case
        if (isLastRect && firstLineCharRect == null) {
            firstLineCharRect = rect
            prevRect = rect
        }

        // `rect.right` is zero for the last space in each line
        // looks like an issue to me, reported: https://issuetracker.google.com/issues/197146630
        if (!isLastRect && rect.right == 0f) continue

        if (firstLineCharRect == null) {
            firstLineCharRect = rect
        } else if (prevRect != null) {
            if (prevRect.bottom != rect.bottom || isLastRect) {
                boundingBoxes.add(
                    firstLineCharRect.copy(right = prevRect.right)
                )
                firstLineCharRect = rect
            }
        }
        prevRect = rect
    }
    return boundingBoxes
}
