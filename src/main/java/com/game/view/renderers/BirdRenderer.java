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
        // Actualmente es un simple cubo. Modifica esto libremente.
        view.dibujarRect(Constants.BIRD_X, bird.getY(),
                Constants.BIRD_ANCHO, Constants.BIRD_ALTO,
                0.98f, 0.85f, 0.20f);
    }
}
