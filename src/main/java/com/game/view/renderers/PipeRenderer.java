package com.game.view.renderers;

import com.game.core.Constants;
import com.game.model.PipeManager;
import com.game.model.PipeManager.Tuberia;
import com.game.view.GameView;

/**
 * PipeRenderer:
 * Archivo de trabajo centralizado para el dibujo de las tuberías.
 * Aquí puedes añadirles bordes (lips), brillos o usar texturas PNG.
 */
public class PipeRenderer {

    public void render(GameView view, PipeManager pipes) {
        for (Tuberia t : pipes.getTuberias()) {
            // Calcular limites verticales del hueco.
            float gapTop = t.gapCentroY + (Constants.GAP_ALTO * 0.5f);
            float gapBottom = t.gapCentroY - (Constants.GAP_ALTO * 0.5f);

            // --- TRAMO SUPERIOR ---
            float altoSuperior = 1.0f - gapTop;
            if (altoSuperior > 0.0f) {
                float yCentroSup = gapTop + (altoSuperior * 0.5f);
                view.dibujarRect(t.x, yCentroSup, Constants.TUBERIA_ANCHO, altoSuperior, 0.18f, 0.70f, 0.25f);
            }

            // --- TRAMO INFERIOR ---
            float altoInferior = gapBottom + 1.0f;
            if (altoInferior > 0.0f) {
                float yCentroInf = -1.0f + (altoInferior * 0.5f);
                view.dibujarRect(t.x, yCentroInf, Constants.TUBERIA_ANCHO, altoInferior, 0.18f, 0.70f, 0.25f);

            }
        }
    }
}
