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
    
    // Controles para navegación del menú
    private boolean prevUp = false;
    private boolean prevDown = false;

    // IDs para efectos de sonido
    private int sfxMove = -1;
    private int sfxConfirm = -1;

    /**
     * @param engine referencia al motor de juego.
     * @param window handle de la ventana GLFW.
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
     *
     * <p>
     * Extraído literalmente de {@code AppFlappyBird#procesarInput()};
     * las ramas de mutación de estado se realizan a través de
     * {@link GameEngine#onSpacePressed()} y {@link GameEngine#onRPressed()}.
     */
    public void procesarInput() {
        // Carga perezosa (Lazy Load) de sonidos la primera vez que se procesa input
        if (sfxMove == -1) {
            sfxMove = com.game.audio.SoundManager.loadSound("src/main/resources/sounds/switch_001.ogg");
            sfxConfirm = com.game.audio.SoundManager.loadSound("src/main/resources/sounds/confirmation_003.ogg");
        }

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }

        // Tecla ESPACIO
        boolean spaceAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;
        if (spaceAhora && !prevSpace) {
            // Si estábamos en el menú y damos espacio, es una confirmación
            if (engine.isEnMenu()) {
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
        // NUEVO: TECLAS DE NAVEGACIÓN DEL MENÚ
        // ==========================================
        
        // Flecha ARRIBA o W
        boolean upAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS || 
                          GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS;
        if (upAhora && !prevUp) {
            if (engine.isEnMenu()) {
                com.game.audio.SoundManager.playSound(sfxMove);
            }
            engine.onUpPressed();
        }
        prevUp = upAhora;

        // Flecha ABAJO o S
        boolean downAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS || 
                            GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS;
        if (downAhora && !prevDown) {
            if (engine.isEnMenu()) {
                com.game.audio.SoundManager.playSound(sfxMove);
            }
            engine.onDownPressed();
        }
        prevDown = downAhora;
    }
}

