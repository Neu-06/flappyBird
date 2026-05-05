package com.game;

import com.game.core.GameEngine;

/**
 * App:
 * Único punto de entrada de la aplicación en el paquete raíz com.game.
 *
 * Responsabilidad exclusiva: instanciar el GameEngine y llamar a
 * GameEngine#run(), delegando toda la inicialización y el ciclo de
 * vida al motor.
 * Equivale al método main original de AppFlappyBird.
 */
public class App {

    // Entry point.
    public static void main(String[] args) {
        new GameEngine().run();
    }
}
