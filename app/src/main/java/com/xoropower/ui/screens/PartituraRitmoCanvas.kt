package com.xoropower.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xoropower.data.NotaPartituraVisual
import kotlin.math.roundToInt

@Composable
fun PartituraRitmoCanvas(
    notas: List<NotaPartituraVisual>,
    bpm: Int,
    compas: String = "4/4",
    beatsPerMeasure: Int = 4,
    finRepeticionMs: Long,
    playheadMs: Long = 0L,
    modoEditor: Boolean = true,
    pulsoActual: Int = 0,
    notaSeleccionada: NotaPartituraVisual? = null,
    enConteo: Boolean = false,
    conteoBeatActual: Int = -1,
    previewActivo: Boolean = false,
    onPulsoTocado: (Int) -> Unit = {},
    onNotaTocada: (NotaPartituraVisual?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val msPerBeat = (60_000L / bpm.coerceAtLeast(40)).toFloat()
    
    // Calcular el compás actual y filtrar notas para mostrar solo ese compás
    val compasActual = (pulsoActual / beatsPerMeasure) + 1
    val msInicioCompas = ((compasActual - 1) * beatsPerMeasure * msPerBeat).toLong()
    val msFinCompas = (compasActual * beatsPerMeasure * msPerBeat).toLong()
    val notasFiltradas = if (modoEditor) {
        notas.filter { it.ms >= msInicioCompas && it.ms < msFinCompas }
    } else {
        notas
    }

    // Animación de parpadeo para el countdown
    val blinkAlpha by rememberInfiniteTransition(label = "blink").animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("$compas COMPÁS", color = Color(0xFF888888), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("BPM $bpm", color = Color(0xFFFFD700), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp) // Altura aumentada para mayor holgura visual
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEFE4C9))
                .border(1.dp, Color(0xFFD4C8AD), RoundedCornerShape(12.dp))
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(modoEditor, notasFiltradas, beatsPerMeasure, finRepeticionMs) {
                        if (!modoEditor) return@pointerInput
                        
                        val anchoPx = size.width
                        val altoPx = size.height
                        val yMidPx = altoPx * 0.42f
                        val staffSpacePx = 28.dp.toPx()
                        
                        val hayDerechaPx = notas.any { it.carril == "derecha" }
                        val hayIzquierdaPx = notas.any { it.carril == "izquierda" }
                        val esCombinadoPx = hayDerechaPx && hayIzquierdaPx
                        
                        val yDerechaPx = if (esCombinadoPx) yMidPx - staffSpacePx else yMidPx
                        val yIzquierdaPx = if (esCombinadoPx) yMidPx + staffSpacePx else yMidPx
                        
                        val margenIzqPx = 130.dp.toPx()
                        val margenDerPx = anchoPx - 48.dp.toPx()
                        val areaNotasPx = margenDerPx - margenIzqPx
                        val totalBeats = (finRepeticionMs / msPerBeat).toInt().coerceAtLeast(beatsPerMeasure)

                        detectTapGestures { offset ->
                            val x = offset.x
                            val y = offset.y
                            
                            // 1. Detectar si tocó una nota existente (dentro de un radio de 24.dp)
                            var notaTocada: NotaPartituraVisual? = null
                            var minDistance = Float.MAX_VALUE
                            val touchRadius = 24.dp.toPx()
                            
                            notasFiltradas.forEach { nota ->
                                val nBeat = (nota.ms.toFloat() / msPerBeat).coerceAtLeast(1f)
                                val nX = margenIzqPx + (nBeat / totalBeats) * areaNotasPx
                                val nY = if (nota.carril == "derecha") yDerechaPx else yIzquierdaPx
                                
                                val dist = kotlin.math.hypot(x - nX, y - nY)
                                if (dist < touchRadius && dist < minDistance) {
                                    minDistance = dist
                                    notaTocada = nota
                                }
                            }
                            
                            if (notaTocada != null) {
                                onNotaTocada(notaTocada)
                            } else {
                                // 2. Si no tocó nota, mapear al pulso más cercano
                                val rawIndex = ((x - margenIzqPx) / areaNotasPx * totalBeats - 1f)
                                val index = rawIndex.roundToInt().coerceIn(0, totalBeats - 1)
                                onPulsoTocado(index)
                            }
                        }
                    }
            ) {
                val ancho = size.width
                val alto = size.height
                val yMid = alto * 0.42f
                val staffSpace = 28.dp.toPx()

                val cremaOscuro = Color(0xFFC4B89B)
                val negroRepeat = Color(0xFF222222)
                val rojoNota = Color(0xFFE84242)
                val azulNota = Color(0xFF3388E8)
                val margenIzq = 130.dp.toPx()
                val margenDer = ancho - 48.dp.toPx()
                val areaNotas = margenDer - margenIzq

                val paintBadge = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                }

                // Detectar tipo de ejercicio
                val hayDerecha = notas.any { it.carril == "derecha" }
                val hayIzquierda = notas.any { it.carril == "izquierda" }
                val esCombinado = hayDerecha && hayIzquierda

                // Posicionamiento según diseño
                val yDerecha = if (esCombinado) yMid - staffSpace else yMid
                val yIzquierda = if (esCombinado) yMid + staffSpace else yMid

                // Controlar opacidad de parpadeo durante conteo
                val currentAlpha = if (enConteo) blinkAlpha else 1.0f

                // Dibujar líneas del pentagrama
                val staffStartX = 20.dp.toPx()
                if (esCombinado) {
                    for (i in -2..2) {
                        val yLine = yMid + (i * staffSpace)
                        drawLine(cremaOscuro.copy(alpha = currentAlpha), Offset(staffStartX, yLine), Offset(ancho, yLine), 2.dp.toPx())
                    }
                    if (hayDerecha) {
                        drawLine(rojoNota.copy(0.35f * currentAlpha), Offset(staffStartX, yDerecha), Offset(ancho, yDerecha), 2.dp.toPx())
                    }
                    if (hayIzquierda) {
                        drawLine(azulNota.copy(0.35f * currentAlpha), Offset(staffStartX, yIzquierda), Offset(ancho, yIzquierda), 2.dp.toPx())
                    }
                } else {
                    for (i in -2..2) {
                        val yLine = yMid + (i * staffSpace)
                        drawLine(cremaOscuro.copy(alpha = currentAlpha), Offset(staffStartX, yLine), Offset(ancho, yLine), 2.dp.toPx())
                    }
                    val activeColor = if (hayDerecha) rojoNota else azulNota
                    drawLine(activeColor.copy(0.35f * currentAlpha), Offset(staffStartX, yMid), Offset(ancho, yMid), 2.dp.toPx())
                }

                val badgeRadius = 16.dp.toPx()
                val badgeX_4_4 = 30.dp.toPx()
                val badgeX_DI = if (esCombinado) 65.dp.toPx() else 35.dp.toPx()

                // Firma de tiempo 4/4
                if (esCombinado) {
                    drawIntoCanvas { canvas ->
                        val paintTimeSignature = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#222222")
                            textAlign = android.graphics.Paint.Align.CENTER
                            textSize = 34.sp.toPx()
                            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                            alpha = (255 * currentAlpha).toInt()
                        }
                        canvas.nativeCanvas.drawText("4", badgeX_4_4, yMid - 4.dp.toPx(), paintTimeSignature)
                        canvas.nativeCanvas.drawText("4", badgeX_4_4, yMid + staffSpace * 1.3f, paintTimeSignature)
                    }
                    drawLine(
                        color = Color(0xFF222222).copy(alpha = currentAlpha),
                        start = Offset(badgeX_4_4 - 14.dp.toPx(), yMid),
                        end = Offset(badgeX_4_4 + 14.dp.toPx(), yMid),
                        strokeWidth = 3.dp.toPx()
                    )
                }

                // Badges D e I
                if (hayDerecha) {
                    drawCircle(Color.Black.copy(alpha = 0.15f * currentAlpha), badgeRadius, Offset(badgeX_DI, yDerecha + 4.dp.toPx()))
                    drawCircle(rojoNota.copy(alpha = currentAlpha), badgeRadius, Offset(badgeX_DI, yDerecha))
                    drawCircle(rojoNota.copy(alpha = 0.6f * currentAlpha), badgeRadius + 3.dp.toPx(), Offset(badgeX_DI, yDerecha), style = Stroke(3.dp.toPx()))
                    drawIntoCanvas {
                        paintBadge.alpha = (255 * currentAlpha).toInt()
                        it.nativeCanvas.drawText("D", badgeX_DI, yDerecha + 5.dp.toPx(), paintBadge)
                    }
                }
                if (hayIzquierda) {
                    drawCircle(Color.Black.copy(alpha = 0.15f * currentAlpha), badgeRadius, Offset(badgeX_DI, yIzquierda + 4.dp.toPx()))
                    drawCircle(azulNota.copy(alpha = currentAlpha), badgeRadius, Offset(badgeX_DI, yIzquierda))
                    drawCircle(azulNota.copy(alpha = 0.6f * currentAlpha), badgeRadius + 3.dp.toPx(), Offset(badgeX_DI, yIzquierda), style = Stroke(3.dp.toPx()))
                    drawIntoCanvas {
                        paintBadge.alpha = (255 * currentAlpha).toInt()
                        it.nativeCanvas.drawText("I", badgeX_DI, yIzquierda + 5.dp.toPx(), paintBadge)
                    }
                }

                // Doble barra inicial
                val doubleBarX1 = 90.dp.toPx()
                val doubleBarX2 = doubleBarX1 + 13.dp.toPx()
                val fullBarTop = yMid - 2.2f * staffSpace
                val fullBarBottom = yMid + 2.2f * staffSpace

                drawLine(negroRepeat.copy(alpha = currentAlpha), Offset(doubleBarX1, fullBarTop), Offset(doubleBarX1, fullBarBottom), 8.dp.toPx())
                drawLine(negroRepeat.copy(alpha = currentAlpha), Offset(doubleBarX2, fullBarTop), Offset(doubleBarX2, fullBarBottom), 3.dp.toPx())

                // Signs of repetición inicio
                val repX1 = doubleBarX2 + 18.dp.toPx()
                val repX2 = repX1 + 12.dp.toPx()
                val dotX = repX2 + 12.dp.toPx()

                if (esCombinado) {
                    drawCircle(negroRepeat.copy(alpha = currentAlpha), 3.5.dp.toPx(), Offset(dotX, yDerecha - 0.25f * staffSpace))
                    drawCircle(negroRepeat.copy(alpha = currentAlpha), 3.5.dp.toPx(), Offset(dotX, yDerecha + 0.25f * staffSpace))
                    drawCircle(negroRepeat.copy(alpha = currentAlpha), 3.5.dp.toPx(), Offset(dotX, yIzquierda - 0.25f * staffSpace))
                    drawCircle(negroRepeat.copy(alpha = currentAlpha), 3.5.dp.toPx(), Offset(dotX, yIzquierda + 0.25f * staffSpace))

                    drawLine(negroRepeat.copy(alpha = currentAlpha), Offset(repX1, yDerecha - 0.7f * staffSpace), Offset(repX1, yDerecha + 0.7f * staffSpace), 4.dp.toPx())
                    drawLine(negroRepeat.copy(alpha = currentAlpha), Offset(repX2, yDerecha - 0.7f * staffSpace), Offset(repX2, yDerecha + 0.7f * staffSpace), 4.dp.toPx())
                    drawLine(negroRepeat.copy(alpha = currentAlpha), Offset(repX1, yIzquierda - 0.7f * staffSpace), Offset(repX1, yIzquierda + 0.7f * staffSpace), 4.dp.toPx())
                    drawLine(negroRepeat.copy(alpha = currentAlpha), Offset(repX2, yIzquierda - 0.7f * staffSpace), Offset(repX2, yIzquierda + 0.7f * staffSpace), 4.dp.toPx())
                } else {
                    drawCircle(negroRepeat.copy(alpha = currentAlpha), 3.5.dp.toPx(), Offset(dotX, yMid - 0.25f * staffSpace))
                    drawCircle(negroRepeat.copy(alpha = currentAlpha), 3.5.dp.toPx(), Offset(dotX, yMid + 0.25f * staffSpace))

                    val shortLineTop = yMid - 0.8f * staffSpace
                    val shortLineBottom = yMid + 0.8f * staffSpace
                    drawLine(negroRepeat.copy(alpha = currentAlpha), Offset(repX1, shortLineTop), Offset(repX1, shortLineBottom), 4.dp.toPx())
                    drawLine(negroRepeat.copy(alpha = currentAlpha), Offset(repX2, shortLineTop), Offset(repX2, shortLineBottom), 4.dp.toPx())
                }

                fun xPos(ms: Long): Float {
                    return if (modoEditor) {
                        val beat = (ms.toFloat() / msPerBeat).coerceAtLeast(1f)
                        val totalBeats = (finRepeticionMs.toFloat() / msPerBeat).coerceAtLeast(beatsPerMeasure.toFloat())
                        margenIzq + (beat / totalBeats) * areaNotas
                    } else {
                        val cursorX = 40.dp.toPx()
                        val pixelesPorMs = areaNotas / finRepeticionMs.coerceAtLeast(1)
                        cursorX + (ms - playheadMs) * pixelesPorMs
                    }
                }

                val totalBeats = (finRepeticionMs / msPerBeat).toInt().coerceAtLeast(beatsPerMeasure)
                val currentPlaybackBeat = (playheadMs / msPerBeat).toInt()
                val activeBeatIndex = if (previewActivo) {
                    if (enConteo) conteoBeatActual else currentPlaybackBeat
                } else {
                    pulsoActual
                }

                // Números de pulso
                for (b in 1..totalBeats) {
                    val ms = (b * msPerBeat).toLong()
                    val xBeat = xPos(ms)
                    
                    val isBeatActive = (b - 1) == (activeBeatIndex % beatsPerMeasure)
                    
                    val paintBeatCustom = android.graphics.Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        if (previewActivo && enConteo && isBeatActive) {
                            // Estilo num-conteo (yellow color, scale 1.6)
                            color = android.graphics.Color.parseColor("#E5A93C")
                            textSize = 40.sp.toPx()
                            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                        } else if (isBeatActive) {
                            // Estilo num-activo (negro tinta, scale 1.4)
                            color = android.graphics.Color.parseColor("#1A0F0A")
                            textSize = 34.sp.toPx()
                            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                        } else {
                            // Estilo normal (color gray con menor opacidad)
                            color = android.graphics.Color.parseColor("#261A0F0A") // ~15% opaco
                            textSize = 24.sp.toPx()
                            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
                        }
                    }
                    
                    drawIntoCanvas {
                        it.nativeCanvas.drawText(
                            ((b - 1) % beatsPerMeasure + 1).toString(),
                            xBeat,
                            yMid + 3.2f * staffSpace,
                            paintBeatCustom
                        )
                    }
                }

                // Líneas divisorias entre compases
                val numCompases = totalBeats / beatsPerMeasure
                for (c in 1 until numCompases) {
                    val msCompas = (c * beatsPerMeasure * msPerBeat).toLong() + (msPerBeat / 2).toLong()
                    val xCompas = xPos(msCompas)
                    drawLine(
                        cremaOscuro.copy(0.6f * currentAlpha),
                        Offset(xCompas, fullBarTop),
                        Offset(xCompas, fullBarBottom),
                        2.dp.toPx()
                    )
                }

                val primerNotaDerecha = notas.firstOrNull { it.tipo != "silencio" && it.carril == "derecha" }
                val primerNotaIzquierda = notas.firstOrNull { it.tipo != "silencio" && it.carril == "izquierda" }
                val ultimaNotaDerecha = notas.lastOrNull { it.tipo != "silencio" && it.carril == "derecha" }
                val ultimaNotaIzquierda = notas.lastOrNull { it.tipo != "silencio" && it.carril == "izquierda" }

                // Dibujar notas
                notasFiltradas.forEach { nota ->
                    val x = xPos(nota.ms)
                    val yPos = if (nota.carril == "derecha") yDerecha else yIzquierda
                    val color = if (nota.carril == "derecha") rojoNota else azulNota
                    
                    val direccionForzada = nota.texto
                    
                    val isSelected = notaSeleccionada != null && 
                                     nota.ms == notaSeleccionada.ms && 
                                     nota.carril == notaSeleccionada.carril
                    
                    val noteIndex = (nota.ms / msPerBeat).roundToInt() - 1
                    
                    // Zoom en la nota del pulso actual si está en play y no en conteo
                    val isCurrentPlayBeat = previewActivo && !enConteo && noteIndex == currentPlaybackBeat
                    val scale = if (isCurrentPlayBeat) 1.25f else 1.0f

                    withTransform({
                        if (scale != 1.0f) {
                            scale(scale, scale, pivot = Offset(x, yPos))
                        }
                    }) {
                        dibujarFigura(
                            nota.tipo,
                            color.copy(alpha = currentAlpha),
                            cremaOscuro.copy(alpha = currentAlpha),
                            x,
                            yPos,
                            direccionForzada,
                            selected = isSelected,
                            selectedAlpha = currentAlpha
                        )
                    }
                }

                // Final repetición :||
                val xRepeatEnd = xPos(finRepeticionMs + (msPerBeat / 2).toLong())
                dibujarRepeticionFinal(negroRepeat.copy(alpha = currentAlpha), xRepeatEnd, yMid, staffSpace, esCombinado, fullBarTop, fullBarBottom, hayDerecha)

                // Playhead (dorado ancho completo)
                if (previewActivo && !enConteo) {
                    val xPlay = xPos(playheadMs.coerceAtMost(finRepeticionMs))
                    val cursorWidth = 44.dp.toPx()
                    val cursorLeft = xPlay - cursorWidth / 2f
                    
                    // Fondo dorado translúcido
                    drawRect(
                        color = Color(0xFFFFD700).copy(alpha = 0.18f),
                        topLeft = Offset(cursorLeft, fullBarTop),
                        size = Size(cursorWidth, fullBarBottom - fullBarTop)
                    )
                    // Borde izquierdo sólido
                    drawLine(
                        color = Color(0xFFFFD700),
                        start = Offset(cursorLeft, fullBarTop),
                        end = Offset(cursorLeft, fullBarBottom),
                        strokeWidth = 3.5.dp.toPx()
                    )
                } else if (!previewActivo) {
                    // Si no está reproduciendo, dibujar el cursor sobre el pulso seleccionado (más sutil)
                    val xPlay = xPos(msPerBeat.toLong() * (pulsoActual + 1))
                    val cursorWidth = 44.dp.toPx()
                    val cursorLeft = xPlay - cursorWidth / 2f
                    
                    drawRect(
                        color = Color(0xFFFFD700).copy(alpha = 0.12f),
                        topLeft = Offset(cursorLeft, fullBarTop),
                        size = Size(cursorWidth, fullBarBottom - fullBarTop)
                    )
                    drawLine(
                        color = Color(0xFFFFD700).copy(alpha = 0.7f),
                        start = Offset(cursorLeft, fullBarTop),
                        end = Offset(cursorLeft, fullBarBottom),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.dibujarRepeticionFinal(
    color: Color, x: Float, yMid: Float, staffSpace: Float, esCombinado: Boolean, staffTop: Float, staffBottom: Float, hayDerecha: Boolean
) {
    if (esCombinado) {
        val dotX = x - 22.dp.toPx()
        val yDerecha = yMid - staffSpace
        val yIzquierda = yMid + staffSpace
        drawCircle(color, 3.5.dp.toPx(), Offset(dotX, yDerecha - 0.25f * staffSpace))
        drawCircle(color, 3.5.dp.toPx(), Offset(dotX, yDerecha + 0.25f * staffSpace))
        drawCircle(color, 3.5.dp.toPx(), Offset(dotX, yIzquierda - 0.25f * staffSpace))
        drawCircle(color, 3.5.dp.toPx(), Offset(dotX, yIzquierda + 0.25f * staffSpace))

        val fineLineX = x - 13.dp.toPx()
        drawLine(color, Offset(fineLineX, staffTop), Offset(fineLineX, staffBottom), 3.dp.toPx())

        val thickLineX = x
        drawLine(color, Offset(thickLineX, staffTop), Offset(thickLineX, staffBottom), 8.dp.toPx())
    } else {
        val dotX = x - 22.dp.toPx()
        val fineLineX = x - 13.dp.toPx()
        val thickLineX = x

        drawCircle(color, 3.5.dp.toPx(), Offset(dotX, yMid - 0.25f * staffSpace))
        drawCircle(color, 3.5.dp.toPx(), Offset(dotX, yMid + 0.25f * staffSpace))

        drawLine(color, Offset(fineLineX, staffTop), Offset(fineLineX, staffBottom), 3.dp.toPx())
        drawLine(color, Offset(thickLineX, staffTop), Offset(thickLineX, staffBottom), 8.dp.toPx())
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.dibujarFigura(
    tipo: String,
    color: Color,
    cremaOscuro: Color,
    x: Float,
    y: Float,
    direccion: String = "abajo",
    selected: Boolean = false,
    selectedAlpha: Float = 1.0f
) {
    if (selected) {
        // Resplandor/glow dorado detrás de la nota seleccionada
        drawOval(
            color = Color(0xFFFFD700).copy(alpha = 0.5f * selectedAlpha),
            topLeft = Offset(x - 17.dp.toPx(), y - 13.dp.toPx()),
            size = Size(34.dp.toPx(), 26.dp.toPx())
        )
        drawOval(
            color = Color(0xFFFFD700).copy(alpha = selectedAlpha),
            topLeft = Offset(x - 18.dp.toPx(), y - 14.dp.toPx()),
            size = Size(36.dp.toPx(), 28.dp.toPx()),
            style = Stroke(2.dp.toPx())
        )
    }

    when (tipo) {
        "silencio" -> {
            val w = 18.dp.toPx()
            val h = 6.dp.toPx()
            drawRect(color, topLeft = Offset(x - w / 2, y - h / 2), size = Size(w, h))
        }
        "blanca" -> {
            drawOval(
                color = color,
                topLeft = Offset(x - 10.dp.toPx(), y - 7.dp.toPx()),
                size = Size(20.dp.toPx(), 14.dp.toPx()),
                style = Stroke(3.dp.toPx())
            )
            val stemX = if (direccion == "arriba") x + 8.dp.toPx() else x - 8.dp.toPx()
            val stemEndY = if (direccion == "arriba") y - 28.dp.toPx() else y + 28.dp.toPx()
            drawLine(color, Offset(stemX, y), Offset(stemX, stemEndY), 2.5.dp.toPx())
        }
        else -> {
            drawOval(cremaOscuro.copy(0.5f * color.alpha), topLeft = Offset(x - 11.dp.toPx(), y - 8.dp.toPx()), size = Size(22.dp.toPx(), 16.dp.toPx()))
            drawOval(color, topLeft = Offset(x - 10.dp.toPx(), y - 7.dp.toPx()), size = Size(20.dp.toPx(), 14.dp.toPx()))
            val stemX = if (direccion == "arriba") x + 8.dp.toPx() else x - 8.dp.toPx()
            val stemEndY = if (direccion == "arriba") y - 28.dp.toPx() else y + 28.dp.toPx()
            drawLine(color, Offset(stemX, y), Offset(stemX, stemEndY), 2.5.dp.toPx())
        }
    }
}
