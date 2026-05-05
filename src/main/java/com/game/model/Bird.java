package com.game.model;

import com.game.core.Constants;

/**
 * Bird:
 * Contiene exclusivamente los datos de estado del jugador y la integración
 * de su física vertical. Extraído desde las variables de instancia y el
 * bloque de física de AppFlappyBird#actualizar(float).
 * La posición X del pájaro es fija en NDC y vive en Constants#BIRD_X;
 * no necesita estado en esta clase.
 */
public class Bird {

    /**
     * Posición vertical del pájaro en NDC.
     * Equivale al campo birdY original.
     */
    private float y;

    /**
     * Velocidad vertical del pájaro en NDC/s.
     * Equivale al campo birdVelY original.
     */
    private float velY;

    // -------------------------------------------------------------------------
    // API pública
    // -------------------------------------------------------------------------

    /**
     * Reinicia la posición y velocidad al estado inicial de partida.
     * Refleja la porción de AppFlappyBird#resetGame() que tocaba
     * birdY = 0 y birdVelY = 0.
     */
    public void reset() {
        y = 0.0f;
        velY = 0.0f;
    }

    /**
     * Aplica el impulso de salto.
     * Refleja la asignación birdVelY = IMPULSO_SALTO del original.
     */
    public void saltar() {
        velY = Constants.IMPULSO_SALTO;
    }

    /**
     * Actualiza la física vertical del pájaro para el frame actual.
     *
     * @param dt delta-time en segundos desde el frame anterior.
     */
    public void update(float dt) {
        // Integracion de fisica simple.
        velY += Constants.GRAVEDAD * dt;
        // Limitar velocidad de caida para sensacion jugable estable.
        if (velY < Constants.VELOCIDAD_MAX_CAIDA) {
            velY = Constants.VELOCIDAD_MAX_CAIDA;
        }
        y += velY * dt;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** @return posición vertical actual del pájaro en NDC. */
    public float getY() {
        return y;
    }

    /** @return velocidad vertical actual del pájaro en NDC/s. */
    public float getVelY() {
        return velY;
    }
}
