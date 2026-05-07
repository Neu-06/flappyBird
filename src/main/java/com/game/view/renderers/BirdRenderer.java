package com.game.view.renderers;

import com.game.core.Constants;
import com.game.model.Bird;
import com.game.view.GameView;

/**
 * BirdRenderer:
 * Archivo de trabajo centralizado para el dibujo del pájaro.
 * Aquí puedes añadir tus capas de geometría (ojos, alas, pico) o cargar
 * texturas PNG.
 */
public class BirdRenderer {

    public void render(GameView view, Bird bird) {
        float x = Constants.BIRD_X;
        float y = bird.getY();
        float bW = Constants.BIRD_ANCHO;
        float bH = Constants.BIRD_ALTO;

        // COLA (Triángulo muy notable apuntando hacia atrás)
        view.dibujarTriangulo(x - 0.06f, y, 0.07f, 0.05f, (float) (Math.PI * 0.75), 0.98f, 0.60f, 0.0f);
        // CUERPO REDONDEADO (Círculo amarillo principal)
        view.dibujarCirculo(x, y, bW, bH, 0.98f, 0.85f, 0.20f);
        // ALA (Triángulo lateral apuntando hacia abajo/atrás)
        view.dibujarTriangulo(x - 0.02f, y - 0.01f, 0.07f, 0.04f, (float) Math.PI / 28, 1.0f, 1.0f, 0.8f);
        // PICO (Triángulo naranja apuntando hacia adelante)
        view.dibujarTriangulo(x + 0.048f, y - 0.01f, 0.06f, 0.02f, (float) (Math.PI / 4), 1.0f, 0.5f, 0.0f);
        // OJO (Blanco)
        view.dibujarCirculo(x + 0.025f, y + 0.02f, 0.035f, 0.035f, 1.0f, 1.0f, 1.0f);
        // PUPILA (Negra)
        view.dibujarCirculo(x + 0.032f, y + 0.02f, 0.012f, 0.012f, 0.0f, 0.0f, 0.0f);
    }
}
