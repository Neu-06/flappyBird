package com.game.view.renderers;

import com.game.core.Constants;
import com.game.model.PipeManager;
import com.game.model.PipeManager.Tuberia;
import com.game.view.GameView;
import com.game.view.TextureLoader;

/**
 * PipeRenderer:
 * Archivo de trabajo centralizado para el dibujo de las tuberías.
 * Adaptado para cargar y dibujar texturas de forma aislada.
 */
public class PipeRenderer {

    // Variables para guardar los IDs de las texturas
    private int bodyTexture = -1;
    private int headerTexture = -1;

    public void render(GameView view, PipeManager pipes) {
        // Carga perezosa (Lazy) de las texturas. Solo ocurre la primera vez.
        if (bodyTexture == -1) {
            bodyTexture = TextureLoader.loadTexture("src/main/resources/textures/bodyPipe.png");
            headerTexture = TextureLoader.loadTexture("src/main/resources/textures/headerPipe.png");
        }

        // Definimos las dimensiones de la "cabeza" del tubo.
        float altoCabeza = 0.08f;
        float anchoCabeza = Constants.TUBERIA_ANCHO * 1.1f; // Ligeramente más ancha que el cuerpo

        for (Tuberia t : pipes.getTuberias()) {
            // Calcular limites verticales del hueco.
            float gapTop = t.gapCentroY + (Constants.GAP_ALTO * 0.5f);
            float gapBottom = t.gapCentroY - (Constants.GAP_ALTO * 0.5f);

            // --- TRAMO SUPERIOR ---
            float altoSuperior = 1.0f - gapTop;
            if (altoSuperior > 0.0f) {
                float yCentroSup = gapTop + (altoSuperior * 0.5f);
                
                if (bodyTexture != -1 && headerTexture != -1) {
                    // Dibujar el cuerpo del tubo cubriendo todo el largo
                    view.dibujarImagen(bodyTexture, t.x, yCentroSup, Constants.TUBERIA_ANCHO, altoSuperior);
                    // Dibujar la cabeza superpuesta en el borde del hueco
                    float yCentroCabezaSup = gapTop + (altoCabeza * 0.5f);
                    view.dibujarImagen(headerTexture, t.x, yCentroCabezaSup, anchoCabeza, altoCabeza);
                } else {
                    // Fallback a geometría original si fallan las texturas
                    view.dibujarRect(t.x, yCentroSup, Constants.TUBERIA_ANCHO, altoSuperior, 0.18f, 0.70f, 0.25f);
                }
            }

            // --- TRAMO INFERIOR ---
            float altoInferior = gapBottom + 1.0f;
            if (altoInferior > 0.0f) {
                float yCentroInf = -1.0f + (altoInferior * 0.5f);
                
                if (bodyTexture != -1 && headerTexture != -1) {
                    // Dibujar el cuerpo del tubo cubriendo todo el largo
                    view.dibujarImagen(bodyTexture, t.x, yCentroInf, Constants.TUBERIA_ANCHO, altoInferior);
                    // Dibujar la cabeza superpuesta en el borde del hueco
                    float yCentroCabezaInf = gapBottom - (altoCabeza * 0.5f);
                    view.dibujarImagen(headerTexture, t.x, yCentroCabezaInf, anchoCabeza, altoCabeza);
                } else {
                    // Fallback a geometría original
                    view.dibujarRect(t.x, yCentroInf, Constants.TUBERIA_ANCHO, altoInferior, 0.18f, 0.70f, 0.25f);
                }
            }
        }
    }
}

