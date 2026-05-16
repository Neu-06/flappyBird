package com.game.input;

import com.game.core.GameEngine;

import org.lwjgl.glfw.GLFW;

/**
 * InputHandler:
 * Gestiona el polling de teclado en cada frame, incluyendo la detección de
 * flanco (edge-detection) para SPACE y R, que evita disparar múltiples
 * acciones mientras la tecla permanece presionada.
 *
 * Código migrado desde AppFlappyBird:
 * - Campos prevSpace y prevR.
 * - Método procesarInput() completo, trasladado sin alteraciones
 * de lógica. La única diferencia estructural es que las mutaciones de
 * estado de juego (gameOver, started, birdVelY,
 * resetGame()) se delegan a GameEngine mediante métodos
 * de callback específicos.
 *
 * Detección de flanco: El patrón if (ahoraPresionado && !prevEstado)
 * se conserva exactamente igual al original; GameEngine no participa
 * en esa lógica.
 */
public class InputHandler {

    /** Motor de juego al que se delegan las acciones. */
    private final GameEngine engine;

    /** Ventana GLFW sobre la que se hace polling. */
    private final long window;

    private boolean prevSpace = false;
    private boolean prevR = false;
    private boolean prevEnter = false;

    // Controles para navegación del menú
    private boolean prevUp = false;
    private boolean prevDown = false;

    // IDs para efectos de sonido
    private int sfxConfirm = -1;
    private int sfxPlayer1 = -1;
    private int sfxPlayer2 = -1;
    private int sfxPlayer3 = -1;

    /**
     * param engine referencia al motor de juego.
     * param window handle de la ventana GLFW.
     */
    public InputHandler(GameEngine engine, long window) {
        this.engine = engine;
        this.window = window;
    }

    // -------------------------------------------------------------------------
    // Procesado de entrada
    // -------------------------------------------------------------------------

    /**
     * Input del jugador:
     * - ESC: salir.
     * - SPACE: empezar/saltar.
     * - R: reset manual (solo en game over).
     *
     * Se usa deteccion de flanco (prevSpace/prevR) para no disparar
     * multiples acciones mientras tecla permanece presionada.
     */
    public void procesarInput() {
        // Carga perezosa (Lazy Load) de sonidos la primera vez que se procesa input
        if (sfxConfirm == -1) {
            sfxConfirm = com.game.audio.SoundManager.loadSound("src/main/resources/sounds/ready.ogg");
            sfxPlayer1 = com.game.audio.SoundManager.loadSound("src/main/resources/sounds/player_1.ogg");
            sfxPlayer2 = com.game.audio.SoundManager.loadSound("src/main/resources/sounds/player_2.ogg");
            sfxPlayer3 = com.game.audio.SoundManager.loadSound("src/main/resources/sounds/player_2.ogg"); // Reutilizamos
                                                                                                          // sonido por
                                                                                                          // ahora
        }

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }

        // Tecla ESPACIO
        boolean spaceAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;
        if (spaceAhora && !prevSpace) {
            // Reproducir 'ready' al confirmar en el menú o al reiniciar después de perder
            if (engine.isEnMenu() || engine.isGameOverTotal()) {
                com.game.audio.SoundManager.playSound(sfxConfirm);
            }
            engine.onSpacePressed();
        }
        prevSpace = spaceAhora;

        boolean rAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS;
        if (rAhora && !prevR) {
            engine.onRPressed();
        }
        prevR = rAhora;

        // ==========================================
        // TECLAS DE NAVEGACIÓN DEL MENÚ Y JUGADORES EXTRA
        // ==========================================

        // Tecla ENTER (Jugador 3)
        boolean enterAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ENTER) == GLFW.GLFW_PRESS;
        if (enterAhora && !prevEnter) {
            engine.onEnterPressed();
        }
        prevEnter = enterAhora;

        // Flecha ARRIBA o W
        boolean upAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS;
        if (upAhora && !prevUp) {
            engine.onUpPressed();
            if (engine.isEnMenu()) {
                // Al subir se selecciona "1 JUGADOR", suena player 1
                com.game.audio.SoundManager.playSound(sfxPlayer1);
            }
        }
        prevUp = upAhora;

        // Flecha ABAJO o S
        boolean downAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS;
        if (downAhora && !prevDown) {
            engine.onDownPressed();
            if (engine.isEnMenu()) {
                // Al bajar se selecciona "2 JUGADORES", suena player 2
                com.game.audio.SoundManager.playSound(sfxPlayer2);
            }
        }
        prevDown = downAhora;
    }
}
