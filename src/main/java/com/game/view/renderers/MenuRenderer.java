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
        float r1 = (seleccion == 0) ? 0.98f : 0.4f;
        float g1 = (seleccion == 0) ? 0.85f : 0.4f;
        float b1 = (seleccion == 0) ? 0.20f : 0.4f;

        view.dibujarRect(0.0f, 0.3f, 0.6f, 0.15f, r1, g1, b1);
        view.dibujarTexto("1 JUGADOR", 0.0f, 0.31f, 0.005f, 0.0f, 0.0f, 0.0f); // Texto en negro

        // ==========================================
        // BOTÓN 2: MODO 2 JUGADORES
        // ==========================================
        float r2 = (seleccion == 1) ? 0.98f : 0.4f;
        float g2 = (seleccion == 1) ? 0.85f : 0.4f;
        float b2 = (seleccion == 1) ? 0.20f : 0.4f;

        view.dibujarRect(0.0f, 0.0f, 0.6f, 0.15f, r2, g2, b2);
        view.dibujarTexto("2 JUGADORES", 0.0f, 0.01f, 0.005f, 0.0f, 0.0f, 0.0f); // Texto en negro

        // ==========================================
        // BOTÓN 3: MODO 3 JUGADORES
        // ==========================================
        float r3 = (seleccion == 2) ? 0.98f : 0.4f;
        float g3 = (seleccion == 2) ? 0.85f : 0.4f;
        float b3 = (seleccion == 2) ? 0.20f : 0.4f;

        view.dibujarRect(0.0f, -0.3f, 0.6f, 0.15f, r3, g3, b3);
        view.dibujarTexto("3 JUGADORES", 0.0f, -0.29f, 0.005f, 0.0f, 0.0f, 0.0f); // Texto en negro

        view.dibujarTexto("Muevete con las Teclas de Direccion o W/S", 0.0f, -0.45f, 0.005f, 0.0f, 0.0f, 1.0f);
        view.dibujarTexto("Presiona ESPACIO para Seleccionar", 0.0f, -0.55f, 0.005f, 0.0f, 0.0f, 1.0f);

    }
}
