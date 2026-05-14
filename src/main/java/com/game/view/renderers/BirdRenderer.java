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
        view.dibujarTriangulo(x - 0.06f, y, 0.07f, 0.05f, (float) (Math.PI * 0.75), 0.08f, 0.08f, 0.10f);
        // CUERPO REDONDEADO (Círculo amarillo principal)
        view.dibujarCirculo(x, y, bW, bH, 0.15f, 0.15f, 0.18f);
        // ALA (Triángulo lateral)
        // Animación dinámica basada en física:
        // Al saltar (velY positivo), el ala se inclina fuertemente hacia abajo.
        // Al caer (velY negativo), la resistencia del aire empuja el ala hacia arriba.
        // ALA (Triángulo lateral)
        // 1. Obtener el tiempo continuo del motor para la oscilación
        float tiempo = (float) org.lwjgl.glfw.GLFW.glfwGetTime();

        // 2. Definir el "estado" del pájaro según su velocidad
        boolean estaSubiendo = bird.getVelY() > 0;

        // 3. Dinámica del aleteo:
        // Si sube: aletea rápido (35.0f) y con un movimiento amplio (0.5f).
        // Si cae: planea suavemente, el aleteo es muy lento (10.0f) y casi
        // imperceptible (0.15f).
        float velocidadAleteo = estaSubiendo ? 35.0f : 10.0f;
        float amplitudAleteo = estaSubiendo ? 0.5f : 0.15f;

        // 4. Inclinación base (aerodinámica):
        // El viento empuja el ala hacia arriba cuando cae.
        float inclinacionBase = -bird.getVelY() * 0.25f;

        // Ecuación final: Inclinación base + (Onda Senoidal * Amplitud)
        float rotacionAla = inclinacionBase + (float) (Math.sin(tiempo * velocidadAleteo) * amplitudAleteo);

        view.dibujarTriangulo(x - 0.02f, y - 0.01f, 0.07f, 0.04f, rotacionAla, 0.95f, 0.0f, 0.10f);
        // PICO (Triángulo naranja apuntando hacia adelante)
        view.dibujarTriangulo(x + 0.048f, y - 0.01f, 0.06f, 0.02f, (float) (Math.PI / 4), 1.0f, 0.98f, 0.95f);
        // OJO (Blanco)
        view.dibujarCirculo(x + 0.025f, y + 0.02f, 0.035f, 0.035f, 1.0f, 0.0f, 0.0f);
        // PUPILA (Negra)
        view.dibujarCirculo(x + 0.032f, y + 0.02f, 0.012f, 0.012f, 0.0f, 0.0f, 0.0f);
    }
}
