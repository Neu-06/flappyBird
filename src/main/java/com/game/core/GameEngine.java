package com.game.core;

import com.game.input.InputHandler;
import com.game.model.Bird;
import com.game.model.PipeManager;
import com.game.view.GameView;
import com.game.view.Renderer;

import org.lwjgl.glfw.GLFW;

/**
 * GameEngine:
 * Clase central que orquesta todos los subsistemas. Contiene el bucle
 * principal, la gestión del delta-time y las variables de estado de partida
 * (started, gameOver).
 *
 * Código migrado desde AppFlappyBird:
 * Campos started y gameOver.
 * Método run() → mismo flujo: init → resetGame → loop → cleanup.
 * Método resetGame() → delega reset a cada subsistema.
 * Método actualizar(float dt) → gestiona colisión contra bordes
 * y delega al PipeManager.
 * Método loop() → bucle while con cálculo de dt y cap de 0.033 s.
 * 
 * Las mutaciones de estado provocadas por el input (SPACE / R) se exponen
 * como callbacks públicos que InputHandler invoca: preservando la
 * lógica original de procesarInput() sin mezclarla con el bucle.
 */
public class GameEngine {

    // Subsistemas
    private final Renderer renderer;
    private final GameView gameView;
    private final Bird bird;
    private final PipeManager pipeManager;
    private InputHandler inputHandler; // Se construye tras init() del renderer.

    // Estado del jugador/juego (equivale a started / gameOver originales).
    private boolean started;
    private boolean gameOver;

    // ==========================================
    // ESTADO DEL MENÚ
    // ==========================================
    // Determina si estamos viendo la pantalla inicial.
    private boolean enMenu = true;

    // Índice de la opción seleccionada.
    // 0 = 1 Jugador, 1 = 2 Jugadores .
    private int seleccionMenu = 0;

    /** @return true si estamos en el menú principal */
    public boolean isEnMenu() {
        return enMenu;
    }

    /** @return índice de la opción seleccionada en el menú */
    public int getSeleccionMenu() {
        return seleccionMenu;
    }

    /** Construye e interconecta todos los subsistemas. */
    public GameEngine() {
        renderer = new Renderer();
        bird = new Bird();
        pipeManager = new PipeManager();
        gameView = new GameView(renderer);
        // inputHandler necesita la ventana → se crea en run() tras renderer.init().
    }

    // -------------------------------------------------------------------------
    // Flujo principal de la aplicacion.
    // -------------------------------------------------------------------------

    /**
     * Punto de entrada del motor.
     * Flujo: {@code init → resetGame → loop → cleanup}, idéntico a
     * {@code AppFlappyBird#run()}.
     */
    public void run() {
        renderer.init();
        // InputHandler necesita el window handle disponible tras renderer.init().
        inputHandler = new InputHandler(this, renderer.getWindow());
        // Estado inicial listo para jugar.
        enMenu = true;
        resetGame();
        loop();
        cleanup();
        renderer.cleanup();
    }

    // -------------------------------------------------------------------------
    // Gestión de estado
    // -------------------------------------------------------------------------

    /**
     * Reinicia estado de partida.
     * Se usa al iniciar app y al reiniciar tras game over.
     * Código extraído de {@code AppFlappyBird#resetGame()}.
     */
    private void resetGame() {
        bird.reset();
        pipeManager.reset();
        started = false;
        gameOver = false;
        renderer.actualizarTitulo(started, gameOver);
    }

    // -------------------------------------------------------------------------
    // Callbacks de input (llamados desde InputHandler)
    // -------------------------------------------------------------------------

    /**
     * Responde al flanco de subida de SPACE.
     *
     * Código original (rama SPACE de AppFlappyBird#procesarInput():
     */
    public void onSpacePressed() {
        // SI ESTAMOS EN EL MENÚ
        if (enMenu) {
            // Confirmamos la selección actual
            if (seleccionMenu == 0) {
                // Seleccionó 1 Jugador: Empezamos el juego normal
                enMenu = false;
                resetGame();
                started = true;
                bird.saltar();
            } else {
                // Seleccionó 2 Jugadores: De momento no hace nada
                System.out.println("Modo 2 jugadores aun no implementado.");
            }
            return;
        }

        // Lógica original de juego
        if (gameOver) {
            resetGame();
            started = true;
            bird.saltar();
        } else {
            started = true;
            bird.saltar();
        }
    }

    /**
     * Responde al flanco de subida de R.
     *
     * Código original (rama R de AppFlappyBird#procesarInput():
     */
    public void onRPressed() {
        if (gameOver) {
            // Al presionar R, volvemos al menú ---
            enMenu = true;
            resetGame();
        }
    }

    // ==========================================
    // MÉTODOS DE NAVEGACIÓN DEL MENÚ
    // ==========================================

    /**
     * Responde a la tecla ARRIBA o W para cambiar de opción en el menú.
     */
    public void onUpPressed() {
        if (enMenu) {
            seleccionMenu = 0; // Sube al primer botón (1 Jugador)
        }
    }

    /**
     * Responde a la tecla ABAJO o S para cambiar de opción en el menú.
     */
    public void onDownPressed() {
        if (enMenu) {
            seleccionMenu = 1; // Baja al segundo botón (2 Jugadores)
        }
    }

    // -------------------------------------------------------------------------
    // Bucle principal
    // -------------------------------------------------------------------------

    /**
     * Bucle principal:
     * - calcula dt,
     * - procesa input,
     * - actualiza logica,
     * - renderiza,
     * - swap/poll.
     *
     * Extraído de AppFlappyBird#loop().
     */
    private void loop() {
        float ultimoTiempo = (float) GLFW.glfwGetTime();
        while (!GLFW.glfwWindowShouldClose(renderer.getWindow())) {
            float ahora = (float) GLFW.glfwGetTime();
            float dt = ahora - ultimoTiempo;
            ultimoTiempo = ahora;
            // Limite de dt para evitar "saltos" grandes si el frame se congela.
            if (dt > 0.033f) {
                dt = 0.033f;
            }

            inputHandler.procesarInput();
            actualizar(dt);

            // Activar pipeline y malla base antes de los métodos de GameView.
            renderer.beginFrame();

            if (enMenu) {
                // Dibujar solo el menú inicial si estamos en la pantalla de inicio
                gameView.renderMenu(seleccionMenu);
            } else {
                // Dibujo original del juego si ya empezamos a jugar
                gameView.renderScene(pipeManager, gameOver);
                gameView.renderBird(bird);
            }

            // Presentar frame y leer eventos.
            renderer.endFrame();
        }
    }

    // -------------------------------------------------------------------------
    // Lógica de actualización
    // -------------------------------------------------------------------------

    /**
     * Actualizacion de logica por frame (dt en segundos):
     * - fisica vertical,
     * - spawn y movimiento de tuberias,
     * - puntaje y colisiones.
     *
     * Extraído de AppFlappyBird#actualizar(float).
     *
     * @param dt delta-time en segundos.
     */
    private void actualizar(float dt) {
        // No actualizar física si estamos en el menú
        if (enMenu) {
            return;
        }

        // Si aun no inicio o ya termino, no avanza simulacion.
        if (!started || gameOver) {
            return;
        }

        bird.update(dt);

        // Colision contra techo/suelo NDC.
        float birdTop = bird.getY() + (Constants.BIRD_ALTO * 0.5f);
        float birdBottom = bird.getY() - (Constants.BIRD_ALTO * 0.5f);
        if (birdTop >= 1.0f || birdBottom <= -1.0f) {
            gameOver = true;
            renderer.actualizarTitulo(started, gameOver);
            return;
        }

        // Guardar puntaje previo para detectar si cambió (necesario para
        // actualizarTitulo).
        int puntajeAntes = pipeManager.getPuntaje();

        boolean colision = pipeManager.update(dt, bird.getY());

        // Puntuar cuando la tuberia ya quedo atras del pajaro (el PipeManager lo
        // incrementa
        // internamente; aquí solo se detecta el cambio para refrescar el título).
        if (pipeManager.getPuntaje() != puntajeAntes) {
            renderer.actualizarTitulo(started, gameOver);
        }

        if (colision) {
            gameOver = true;
            renderer.actualizarTitulo(started, gameOver);
        }
    }

    // Asegurarse de limpiar recursos del sonido al cerrar
    public void cleanup() {
        com.game.audio.SoundManager.cleanup();
    }
}
