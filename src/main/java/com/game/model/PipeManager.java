package com.game.model;

import com.game.core.Constants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * PipeManager:
 * Encapsula la lista de obstáculos activos, su generación periódica,
 * su movimiento horizontal y la detección de colisión AABB con el pájaro.
 *
 * Código migrado desde AppFlappyBird:
 * - Clase interna Tuberia (ahora pública para que GameView pueda iterar sobre
 * ella).
 * - Campos tuberias, random, timerSpawn, puntaje.
 * - Métodos spawnTuberia() y colisionaConTuberia(Tuberia).
 * - El bucle de actualización de tuberías que estaba dentro de
 * AppFlappyBird#actualizar(float).
 */
public class PipeManager {

    // -------------------------------------------------------------------------
    // Modelo de una tuberia:
    // x: posicion horizontal comun para parte superior/inferior,
    // gapCentroY: centro vertical del hueco,
    // puntuada: evita sumar dos veces la misma tuberia.
    // -------------------------------------------------------------------------
    /**
     * Modelo de una tuberia:
     * x: posicion horizontal comun para parte superior/inferior,
     * gapCentroY: centro vertical del hueco,
     * puntuada: evita sumar dos veces la misma tuberia.
     */
    public static class Tuberia {
        public float x;
        public float gapCentroY;
        public boolean puntuada;

        Tuberia(float x, float gapCentroY) {
            this.x = x;
            this.gapCentroY = gapCentroY;
        }
    }

    // Lista de obstaculos activos.
    private final List<Tuberia> tuberias = new ArrayList<>();
    // RNG para variar la posicion del gap.
    private final Random random = new Random();

    /** Equivale a timerSpawn de AppFlappyBird. */
    private float timerSpawn;
    /** Equivale a puntaje de AppFlappyBird. */
    private int puntaje;

    // -------------------------------------------------------------------------
    // Dificultad Dinámica
    // -------------------------------------------------------------------------
    private float getVelocidadActual() {
        // Incrementa la velocidad 0.025f por cada punto ganado.
        // Velocidad base = 0.62f. Límite máximo = 1.30f (el doble de rápido).
        return Math.min(Constants.VELOCIDAD_TUBERIAS + (puntaje * 0.025f), 1.30f);
    }

    private float getTiempoSpawnActual() {
        // Mantiene la distancia física constante entre tuberías aunque vayan más rápido.
        // Distancia = Velocidad Base * Tiempo Base
        float distanciaFija = Constants.VELOCIDAD_TUBERIAS * Constants.TIEMPO_ENTRE_TUBERIAS;
        return distanciaFija / getVelocidadActual();
    }

    // -------------------------------------------------------------------------
    // API pública
    // -------------------------------------------------------------------------

    /** @return multiplicador de velocidad actual (1.0f base, sube progresivamente) */
    public float getMultiplicadorDificultad() {
        return getVelocidadActual() / Constants.VELOCIDAD_TUBERIAS;
    }

    /**
     * Reinicia el gestor al estado inicial de partida.
     * Refleja la porción de AppFlappyBird#resetGame() que tocaba
     * timerSpawn = 0, puntaje = 0 y tuberias.clear().
     */
    public void reset() {
        timerSpawn = 0.0f;
        puntaje = 0;
        tuberias.clear();
    }

    /**
     * Actualiza la lógica de todas las tuberías para el frame actual.
     *
     * Código original extraído de AppFlappyBird#actualizar(float):
     *
     * @param dt    delta-time en segundos.
     * @param birdY posición vertical actual del pájaro (para colisión AABB).
     * @return true si hay colisión con alguna tubería (→ game over).
     */
    public boolean update(float dt, float birdY) {
        // Temporizador para generar nuevas tuberias adaptado a la velocidad actual.
        timerSpawn += dt;
        if (timerSpawn >= getTiempoSpawnActual()) {
            timerSpawn = 0.0f;
            spawnTuberia();
        }

        Iterator<Tuberia> it = tuberias.iterator();
        while (it.hasNext()) {
            Tuberia t = it.next();
            // Avance horizontal de obstaculos adaptado a la dificultad.
            t.x -= getVelocidadActual() * dt;

            // Puntuar cuando la tuberia ya quedo atras del pajaro.
            if (t.x + (Constants.TUBERIA_ANCHO * 0.5f) < Constants.BIRD_X && !t.puntuada) {
                t.puntuada = true;
                puntaje++;
            }

            if (colisionaConTuberia(t, birdY)) {
                return true;
            }

            // Remover tuberias fuera de pantalla para no acumular memoria.
            if (t.x + (Constants.TUBERIA_ANCHO * 0.5f) < -1.3f) {
                it.remove();
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Métodos privados
    // -------------------------------------------------------------------------

    // Crea tuberia nueva en borde derecho con gap vertical aleatorio.
    private void spawnTuberia() {
        float gapCentro = Constants.GAP_MIN_CENTRO
                + random.nextFloat() * (Constants.GAP_MAX_CENTRO - Constants.GAP_MIN_CENTRO);
        tuberias.add(new Tuberia(1.2f, gapCentro));
    }

    /**
     * Colision AABB simplificada:
     * 1) Si no hay overlap horizontal, no colisiona.
     * 2) Si hay overlap horizontal, colisiona si el pajaro esta fuera del gap.
     *
     * Código original de AppFlappyBird#colisionaConTuberia(Tuberia),
     * trasladado sin cambios; únicamente se añade birdY como parámetro
     * porque ya no es un campo de instancia accesible directamente.
     *
     * @param t     tubería a evaluar.
     * @param birdY posición vertical actual del pájaro.
     * @return true si hay colisión.
     */
    private boolean colisionaConTuberia(Tuberia t, float birdY) {
        // Se ajusta el hitbox sumando márgenes para incluir las nuevas figuras:
        // - Cola (izquierda): aprox 0.04f extra.
        // - Pico (derecha): aprox 0.03f extra.
        float birdLeft = Constants.BIRD_X - (Constants.BIRD_ANCHO * 0.5f) - 0.04f;
        float birdRight = Constants.BIRD_X + (Constants.BIRD_ANCHO * 0.5f) + 0.03f;
        
        // Vertialmente el cuerpo base es suficiente, no sobresale mucho más.
        float birdBottom = birdY - (Constants.BIRD_ALTO * 0.5f);
        float birdTop = birdY + (Constants.BIRD_ALTO * 0.5f);

        float pipeLeft = t.x - (Constants.TUBERIA_ANCHO * 0.5f);
        float pipeRight = t.x + (Constants.TUBERIA_ANCHO * 0.5f);
        boolean overlapX = birdRight > pipeLeft && birdLeft < pipeRight;
        if (!overlapX) {
            return false;
        }

        float gapTop = t.gapCentroY + (Constants.GAP_ALTO * 0.5f);
        float gapBottom = t.gapCentroY - (Constants.GAP_ALTO * 0.5f);
        return birdTop > gapTop || birdBottom < gapBottom;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** @return vista no modificable de la lista de tuberías activas. */
    public List<Tuberia> getTuberias() {
        return tuberias;
    }

    /** @return puntaje acumulado en la partida actual. */
    public int getPuntaje() {
        return puntaje;
    }
}
