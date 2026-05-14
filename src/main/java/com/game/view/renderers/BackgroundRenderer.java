package com.game.view.renderers;

import com.game.view.GameView;
import com.game.view.TextureLoader;

/**
 * BackgroundRenderer:
 * Archivo de trabajo centralizado para el fondo del juego.
 * Muestra cómo colocar una imagen PNG en toda la pantalla fácilmente.
 */
public class BackgroundRenderer {

    // Variable para guardar el ID de la textura en la tarjeta gráfica.
    // Usamos -1 para saber que todavía no la hemos cargado.
    private int bgTexture = -1;

    public void render(GameView view) {
        // 1. Cargamos la imagen la primera vez que se dibuja el fondo (Lazy Loading).
        // Así nos aseguramos de que OpenGL ya está iniciado y listo.
        if (bgTexture == -1) {
            bgTexture = TextureLoader.loadTexture("src/main/resources/textures/background.png");
        }

        // 2. Ya no limpiamos el buffer aquí, eso se hace al inicio del frame en
        // Renderer.

        // 3. Dibujar la imagen sobre un rectángulo que cubra toda la pantalla
        if (bgTexture != -1) {
            // Centro en (0.0, 0.0). Ancho y Alto de 2.0 cubren todo NDC de -1.0 a 1.0.
            view.dibujarImagen(bgTexture, 0.0f, 0.0f, 2.0f, 2.0f);
        } else {
            // Si la imagen falla, podemos dibujar un rectángulo de color sólido
            view.dibujarRect(0.0f, 0.0f, 2.0f, 2.0f, 0.1f, 0.5f, 0.1f);
        }
    }
}
