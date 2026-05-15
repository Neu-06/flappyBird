package com.game;

import com.game.core.GameEngine;

/**
 * Responsabilidad:instanciar el GameEngine y llamar a
 * GameEngine#run(), delegando toda la inicialización y el ciclo de
 * vida al motor.
 */
public class App {

    // Entry point.
    public static void main(String[] args) {
        new GameEngine().run();
    }
}
