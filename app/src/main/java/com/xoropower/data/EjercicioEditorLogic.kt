package com.xoropower.data

import androidx.compose.ui.graphics.Color
import com.xoropower.ui.screens.RhythmNote

enum class FiguraRitmica(val duracionPulsos: Int) {
    NEGRA(1),
    BLANCA(2),
    SILENCIO(1)
}

data class EditorSlot(
    val derecha: FiguraRitmica? = null,
    val izquierda: FiguraRitmica? = null,
    val direccionDerecha: String = "abajo", // "arriba" o "abajo"
    val direccionIzquierda: String = "abajo" // "arriba" o "abajo"
)

data class NotaPartituraVisual(
    val ms: Long,
    val carril: String,
    val tipo: String,
    val texto: String = "abajo"
)

object EjercicioEditorLogic {

    fun msPorPulso(bpm: Int): Long = 60_000L / bpm

    fun duracionTotalMs(bpm: Int, beatsPerMeasure: Int, numCompases: Int): Long =
        msPorPulso(bpm) * beatsPerMeasure * numCompases

    fun slotsVacios(beatsPerMeasure: Int, numCompases: Int): List<EditorSlot> =
        List(beatsPerMeasure * numCompases) { EditorSlot() }

    fun aplicarFigura(
        slots: List<EditorSlot>,
        indice: Int,
        mano: String,
        figura: FiguraRitmica,
        direccion: String = "abajo"
    ): List<EditorSlot> {
        if (indice !in slots.indices) return slots
        val mutable = slots.toMutableList()
        val slot = mutable[indice]
        mutable[indice] = if (mano == "derecha") {
            slot.copy(derecha = figura, direccionDerecha = direccion)
        } else {
            slot.copy(izquierda = figura, direccionIzquierda = direccion)
        }
        return mutable
    }

    fun indiceSiguientePulso(slots: List<EditorSlot>, pulsoActual: Int): Int {
        if (pulsoActual >= slots.lastIndex) return slots.lastIndex
        return (pulsoActual + 1).coerceAtMost(slots.lastIndex)
    }

    fun borrarNota(slots: List<EditorSlot>, indice: Int, mano: String): List<EditorSlot> {
        if (indice !in slots.indices) return slots
        val mutable = slots.toMutableList()
        val slot = mutable[indice]
        mutable[indice] = if (mano == "derecha") {
            slot.copy(derecha = null)
        } else {
            slot.copy(izquierda = null)
        }
        return mutable
    }

    fun aPartituraVisual(
        slots: List<EditorSlot>,
        bpm: Int,
        beatsPerMeasure: Int
    ): List<NotaPartituraVisual> {
        val msBeat = msPorPulso(bpm)
        val notas = mutableListOf<NotaPartituraVisual>()
        slots.forEachIndexed { index, slot ->
            val ms = msBeat * (index + 1)
            slot.derecha?.let { figura ->
                notas.add(NotaPartituraVisual(ms, "derecha", figura.name.lowercase(), slot.direccionDerecha))
            }
            slot.izquierda?.let { figura ->
                notas.add(NotaPartituraVisual(ms, "izquierda", figura.name.lowercase(), slot.direccionIzquierda))
            }
        }
        return notas
    }

    fun aRhythmNotes(slots: List<EditorSlot>, bpm: Int): List<RhythmNote> {
        val msBeat = msPorPulso(bpm)
        val notas = mutableListOf<RhythmNote>()
        var id = 0
        slots.forEachIndexed { index, slot ->
            val ms = msBeat * (index + 1)
            slot.derecha?.let { figura ->
                if (figura != FiguraRitmica.SILENCIO) {
                    notas.add(RhythmNote(id++, ms, "derecha", Color(0xFFF44336), slot.direccionDerecha, tipo = figura.name.lowercase()))
                }
            }
            slot.izquierda?.let { figura ->
                if (figura != FiguraRitmica.SILENCIO) {
                    notas.add(RhythmNote(id++, ms, "izquierda", Color(0xFF2196F3), slot.direccionIzquierda, tipo = figura.name.lowercase()))
                }
            }
        }
        return notas
    }

    fun aNotasDto(slots: List<EditorSlot>, bpm: Int, beatsPerMeasure: Int): List<NotaEjercicioDto> =
        aPartituraVisual(slots, bpm, beatsPerMeasure).map { visual ->
            NotaEjercicioDto(
                ms = visual.ms,
                mano = visual.carril,
                color = if (visual.carril == "derecha") "rojo" else "azul",
                texto = if (visual.tipo == "silencio") null else visual.texto,
                tipo = visual.tipo
            )
        }

    fun calcularFinRepeticionMs(bpm: Int, beatsPerMeasure: Int, numCompases: Int, notas: List<RhythmNote>): Long {
        val duracionCompas = duracionTotalMs(bpm, beatsPerMeasure, numCompases)
        val ultimaNota = notas.maxOfOrNull { it.ms } ?: 0L
        return maxOf(duracionCompas, ultimaNota + msPorPulso(bpm))
    }
}
