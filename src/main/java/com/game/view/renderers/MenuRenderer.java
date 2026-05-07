package com.game.view.renderers;

import com.game.view.GameView;

/**
 * MenuRenderer:
 * Dibuja la pantalla de inicio con opciones de modo de juego.
 */
public class MenuRenderer {

    /**
     * Renderiza las opciones del menú principal.
     * 
     * @param view      acceso a OpenGL a través de GameView.
     * @param seleccion 0 para "1 Jugador", 1 para "2 Jugadores".
     */
    public void render(GameView view, int seleccion) {
        // fondo oscuro para el menú
        view.dibujarRect(0.0f, 0.0f, 2.0f, 2.0f, 0.1f, 0.15f, 0.2f);

        // ==========================================
        // BOTÓN 1: MODO 1 JUGADOR
        // ==========================================
        // Si la selección es 0, usamos un color amarillo brillante para resaltar.
        // Si no, usamos un color gris oscuro.
        float r1 = (seleccion == 0) ? 0.98f : 0.4f;
        float g1 = (seleccion == 0) ? 0.85f : 0.4f;
        float b1 = (seleccion == 0) ? 0.20f : 0.4f;

        // Dibujamos el rectángulo que representa el botón en la parte superior
        view.dibujarRect(0.0f, 0.2f, 0.6f, 0.15f, r1, g1, b1);
        // Dibujar el texto "1 JUGADOR" centrado aproximado
        view.dibujarTexto("1 JUGADOR", -0.12f, 0.21f, 0.005f, 0.0f, 0.0f, 0.0f); // Texto en negro

        // ==========================================
        // BOTÓN 2: MODO 2 JUGADORES
        // ==========================================
        // Si la selección es 1, usamos el color amarillo brillante.
        float r2 = (seleccion == 1) ? 0.98f : 0.4f;
        float g2 = (seleccion == 1) ? 0.85f : 0.4f;
        float b2 = (seleccion == 1) ? 0.20f : 0.4f;

        // Dibujamos el rectángulo en la parte inferior
        view.dibujarRect(0.0f, -0.2f, 0.6f, 0.15f, r2, g2, b2);
        // Dibujar el texto "2 JUGADORES" centrado aproximado
        view.dibujarTexto("2 JUGADORES", -0.15f, -0.19f, 0.005f, 0.0f, 0.0f, 0.0f); // Texto en negro

        view.dibujarTexto("Muevete con las Teclas de Direccion o W/S", -0.54f, -0.39f, 0.005f, 0.0f, 0.0f, 1.0f);
        view.dibujarTexto("Presiona ESPACIO para Seleccionar", -0.45f, -0.5f, 0.005f, 0.0f, 0.0f, 1.0f);

    }
}
