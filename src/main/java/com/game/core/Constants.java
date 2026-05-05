package com.game.core;

/**
 * Constants:
 * Incluye:
 * - Dimensiones de ventana.
 * - Parámetros de posición y tamaño del pájaro.
 * - Constantes de física vertical (gravedad, impulso, velocidad máxima).
 * - Parámetros de generación y movimiento de tuberías.
 */
public final class Constants {

    // Constructor privado: clase de utilidades, no instanciable.
    private Constants() {
    }

    // -------------------------------------------------------------------------
    // Tamano inicial de ventana.
    // -------------------------------------------------------------------------
    public static final int ANCHO = 900;
    public static final int ALTO = 700;

    // -------------------------------------------------------------------------
    // Posicion horizontal fija del pajaro en NDC.
    // -------------------------------------------------------------------------
    public static final float BIRD_X = -0.45f;
    // Tamano del pajaro.
    public static final float BIRD_ANCHO = 0.10f;
    public static final float BIRD_ALTO = 0.10f;

    // -------------------------------------------------------------------------
    // Fisica vertical.
    // -------------------------------------------------------------------------
    public static final float GRAVEDAD = -1.9f;
    public static final float IMPULSO_SALTO = 0.85f;
    public static final float VELOCIDAD_MAX_CAIDA = -1.8f;

    // -------------------------------------------------------------------------
    // Parametros de tuberias.
    // -------------------------------------------------------------------------
    public static final float TUBERIA_ANCHO = 0.18f;
    public static final float GAP_ALTO = 0.48f;
    public static final float VELOCIDAD_TUBERIAS = 0.62f;
    public static final float TIEMPO_ENTRE_TUBERIAS = 1.5f;
    public static final float GAP_MIN_CENTRO = -0.45f;
    public static final float GAP_MAX_CENTRO = 0.45f;
}
