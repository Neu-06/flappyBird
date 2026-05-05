package com.game.view;

import com.game.core.Constants;
import com.game.model.Bird;
import com.game.model.PipeManager;
import com.game.model.PipeManager.Tuberia;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 * GameView:
 * El área de dibujo de la escena. Contiene los métodos que emiten llamadas
 * a glUniform* y glDrawArrays para renderizar cada elemento
 * del juego usando el quad base configurado en Renderer.
 *
 * Depende de Renderer únicamente para obtener las locations de
 * uniforms; no duplica ningún estado de GPU.
 */
public class GameView {

    /** Referencia al renderer para acceder a las locations de uniforms. */
    private final Renderer renderer;

    /**
     * @param renderer instancia inicializada del {@link Renderer}.
     */
    public GameView(Renderer renderer) {
        this.renderer = renderer;
    }

    // -------------------------------------------------------------------------
    // API pública de dibujo
    // -------------------------------------------------------------------------

    /**
     * Render del frame:
     * - fondo,
     * - tuberias,
     * - franja central en game over.
     *
     * Contiene el código de AppFlappyBird#render() exceptuando el
     * dibujado del pájaro (delegado a renderBird(Bird)) y las llamadas
     * de activación del shader/VAO (delegadas a Renderer#beginFrame()).
     *
     * @param pipes    gestor de tuberías con la lista activa.
     * @param gameOver true para dibujar el overlay de game over.
     */
    public void renderScene(PipeManager pipes, boolean gameOver) {
        // Cielo.
        GL11.glClearColor(0.52f, 0.80f, 0.92f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        for (Tuberia t : pipes.getTuberias()) {
            // Calcular limites verticales del hueco.
            float gapTop = t.gapCentroY + (Constants.GAP_ALTO * 0.5f);
            float gapBottom = t.gapCentroY - (Constants.GAP_ALTO * 0.5f);

            // Tramo superior de tuberia.
            float altoSuperior = 1.0f - gapTop;
            if (altoSuperior > 0.0f) {
                float yCentroSup = gapTop + (altoSuperior * 0.5f);
                dibujarRect(t.x, yCentroSup, Constants.TUBERIA_ANCHO, altoSuperior, 0.18f, 0.70f, 0.25f);
            }

            // Tramo inferior de tuberia.
            float altoInferior = gapBottom + 1.0f;
            if (altoInferior > 0.0f) {
                float yCentroInf = -1.0f + (altoInferior * 0.5f);
                dibujarRect(t.x, yCentroInf, Constants.TUBERIA_ANCHO, altoInferior, 0.18f, 0.70f, 0.25f);
            }
        }

        // Overlay simple de game over (sin texto en framebuffer).
        if (gameOver) {
            dibujarRect(0.0f, 0.0f, 2.0f, 0.22f, 0.15f, 0.18f, 0.22f);
        }
    }

    /**
     * Dibujar pajaro.
     *
     * Corresponde a la línea:
     * dibujarRect(BIRD_X, birdY, BIRD_ANCHO, BIRD_ALTO, 0.98f, 0.85f, 0.20f);
     * del método AppFlappyBird#render().
     *
     * @param bird modelo del pájaro (se usa únicamente su posición Y).
     */
    public void renderBird(Bird bird) {
        // Dibujar pajaro.
        dibujarRect(Constants.BIRD_X, bird.getY(),
                Constants.BIRD_ANCHO, Constants.BIRD_ALTO,
                0.98f, 0.85f, 0.20f);
    }

    // -------------------------------------------------------------------------
    // Helper privado
    // -------------------------------------------------------------------------

    /**
     * Helper de dibujo parametrico de rectangulos.
     * Extraído literalmente de AppFlappyBird#dibujarRect().
     *
     * @param x     posición X del centro en NDC.
     * @param y     posición Y del centro en NDC.
     * @param ancho ancho del rectángulo en NDC.
     * @param alto  alto del rectángulo en NDC.
     * @param r     componente rojo [0,1].
     * @param g     componente verde [0,1].
     * @param b     componente azul [0,1].
     */
    private void dibujarRect(float x, float y, float ancho, float alto,
            float r, float g, float b) {
        // Traslacion del quad.
        GL20.glUniform2f(renderer.getUOffsetLocation(), x, y);
        // Escala del quad.
        GL20.glUniform2f(renderer.getUScaleLocation(), ancho, alto);
        // Color.
        GL20.glUniform3f(renderer.getUColorLocation(), r, g, b);
        // Dibujar 2 triangulos.
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }
}
