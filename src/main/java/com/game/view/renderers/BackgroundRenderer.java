package com.game.view.renderers;

import com.game.view.GameView;
import org.lwjgl.opengl.GL11;

/**
 * BackgroundRenderer:
 * Archivo de trabajo centralizado para el fondo del juego.
 * Aquí puedes añadir montañas, nubes, edificios o texturas PNG sin ensuciar
 * GameView.
 */
public class BackgroundRenderer {

    public void render(GameView view) {
        // Cielo (limpia el buffer con el color azul claro).
        GL11.glClearColor(0.52f, 0.80f, 0.92f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }
}
