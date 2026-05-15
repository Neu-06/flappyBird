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
     */
    private float y;

    /**
     * Velocidad vertical del pájaro en NDC/s.
     */
    private float velY;

    /**
     * Reinicia la posición y velocidad al estado inicial de partida.
     */
    public void reset() {
        y = 0.0f;
        velY = 0.0f;
    }

    /**
     * Aplica el impulso de salto, mientras mayor sea el multiplicador mayor sera el
     * impulso.
     * multiplicador para compensar la velocidad horizontal.
     */
    public void saltar(float multiplicador) {
        velY = Constants.IMPULSO_SALTO * multiplicador;
    }

    /**
     * Actualiza la física vertical del pájaro para el frame actual,
     * escalando la gravedad cuadráticamente para alinearla con la velocidad.
     *
     * dt: delta-time en segundos desde el frame anterior.
     * multiplicador: para compensar la velocidad acelerada
     */
    public void update(float dt, float multiplicador) {
        // La gravedad se escala al cuadrado del multiplicador para mantener
        // la misma altura de salto pero ejecutada en menos tiempo.
        float gravedadActual = Constants.GRAVEDAD * multiplicador * multiplicador;
        velY += gravedadActual * dt;

        // El límite de caída también se escala
        float maxCaidaActual = Constants.VELOCIDAD_MAX_CAIDA * multiplicador;
        if (velY < maxCaidaActual) {
            velY = maxCaidaActual;
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
