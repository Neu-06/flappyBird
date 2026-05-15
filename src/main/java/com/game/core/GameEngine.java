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
    // Jugador 1
    private final Bird bird1;
    private final PipeManager pipeManager1;
    private boolean gameOver1;

    // Jugador 2
    private final Bird bird2;
    private final PipeManager pipeManager2;
    private boolean gameOver2;

    private boolean modoDosJugadores;
    private boolean started;
    private InputHandler inputHandler; // Se construye tras init() del renderer.

    // Sonido de eventos (punto y game over)
    private int sfxPoint = -1;
    private int sfxGameOver = -1;

    // ==========================================
    // ESTADO DEL MENÚ
    // ==========================================
    // Determina si estamos viendo la pantalla inicial.
    private boolean enMenu = true;

    // Índice de la opción seleccionada.
    // 0 = 1 Jugador, 1 = 2 Jugadores .
    private int seleccionMenu = 0;

    /** return true si estamos en el menú principal */
    public boolean isEnMenu() {
        return enMenu;
    }

    /**
     * return true si estamos en estado Game Over total
     * (ambos jugadores muertos si aplica)
     */
    public boolean isGameOverTotal() {
        if (modoDosJugadores) {
            return gameOver1 && gameOver2;
        }
        return gameOver1;
    }

    /** return índice de la opción seleccionada en el menú */
    public int getSeleccionMenu() {
        return seleccionMenu;
    }

    /** Construye e interconecta todos los subsistemas. */
    public GameEngine() {
        renderer = new Renderer();
        bird1 = new Bird();
        pipeManager1 = new PipeManager();
        bird2 = new Bird();
        pipeManager2 = new PipeManager();
        gameView = new GameView(renderer);
        // inputHandler necesita la ventana → se crea en run() tras renderer.init().
    }

    // -------------------------------------------------------------------------
    // Flujo principal de la aplicacion.
    // -------------------------------------------------------------------------

    /**
     * Punto de entrada del motor.
     * Flujo: init → resetGame → loop → cleanup
     */
    public void run() {
        renderer.init();
        // InputHandler necesita el window handle disponible tras renderer.init().
        inputHandler = new InputHandler(this, renderer.getWindow());

        // Cargar los sonidos de eventos que gestiona el motor de juego
        sfxPoint = com.game.audio.SoundManager.loadSound("src/main/resources/sounds/point.ogg");
        sfxGameOver = com.game.audio.SoundManager.loadSound("src/main/resources/sounds/game_over.ogg");

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
     */
    private void resetGame() {
        bird1.reset();
        pipeManager1.reset();
        gameOver1 = false;

        bird2.reset();
        pipeManager2.reset();
        gameOver2 = false;

        started = false;
        renderer.actualizarTitulo(started, isGameOverTotal());
    }

    // -------------------------------------------------------------------------
    // Callbacks de input (llamados desde InputHandler)
    // -------------------------------------------------------------------------

    /**
     * Responde al flanco de subida de SPACE.
     */
    public void onSpacePressed() {
        // SI ESTAMOS EN EL MENÚ
        if (enMenu) {
            enMenu = false;
            modoDosJugadores = (seleccionMenu == 1);
            renderer.cambiarTamanoVentana(modoDosJugadores);
            resetGame();
            started = true;
            bird1.saltar(pipeManager1.getMultiplicadorDificultad());
            if (modoDosJugadores) {
                bird2.saltar(pipeManager2.getMultiplicadorDificultad());
            }
            return;
        }

        // Lógica de juego: REINICIO GLOBAL
        if (isGameOverTotal()) {
            resetGame();
            started = true;
            bird1.saltar(pipeManager1.getMultiplicadorDificultad());
            if (modoDosJugadores) {
                bird2.saltar(pipeManager2.getMultiplicadorDificultad());
            }
            return;
        }

        // SALTO JUGADOR 1
        if (started && !gameOver1) {
            bird1.saltar(pipeManager1.getMultiplicadorDificultad());
        }
    }

    /**
     * Responde al flanco de subida de R.
     */
    public void onRPressed() {
        if (isGameOverTotal()) {
            // Al presionar R, volvemos al menú y restauramos tamaño ---
            enMenu = true;
            renderer.cambiarTamanoVentana(false);
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
        } else if (started && modoDosJugadores && !gameOver2) {
            // SALTO JUGADOR 2
            bird2.saltar(pipeManager2.getMultiplicadorDificultad());
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

            // avisa que se va pintar un nuevo frame
            renderer.beginFrame();

            if (enMenu) {
                org.lwjgl.opengl.GL11.glViewport(0, 0, Constants.ANCHO, Constants.ALTO);
                gameView.renderMenu(seleccionMenu);
            } else {
                if (!modoDosJugadores) {
                    // Modo 1 Jugador: Pantalla completa
                    org.lwjgl.opengl.GL11.glViewport(0, 0, Constants.ANCHO, Constants.ALTO);
                    gameView.renderScene(pipeManager1, gameOver1);
                    gameView.renderBird(bird1);
                    if (gameOver1)
                        gameView.renderGameOver(pipeManager1);
                } else {
                    // Modo 2 Jugadores: Split Screen ancho (Doble de tamaño físico)
                    // Mitad izquierda (Jugador 1) usa el ancho de una pantalla normal
                    org.lwjgl.opengl.GL11.glViewport(0, 0, Constants.ANCHO, Constants.ALTO);
                    gameView.renderScene(pipeManager1, gameOver1);
                    gameView.renderBird(bird1);
                    if (gameOver1)
                        gameView.renderGameOver(pipeManager1);

                    // Mitad derecha (Jugador 2) desplazada una pantalla entera
                    org.lwjgl.opengl.GL11.glViewport(Constants.ANCHO, 0, Constants.ANCHO, Constants.ALTO);
                    gameView.renderScene(pipeManager2, gameOver2);
                    gameView.renderBird(bird2);
                    if (gameOver2)
                        gameView.renderGameOver(pipeManager2);

                    // Restaurar Viewport general para el resto del ciclo (limpieza, GUI futura,
                    // etc)
                    org.lwjgl.opengl.GL11.glViewport(0, 0, Constants.ANCHO * 2, Constants.ALTO);

                    // Dibujar una barra vertical en el centro para separar ambos juegos
                    gameView.dibujarRect(0.0f, 0.0f, 0.015f, 2.0f, 0.45f, 0.40f, 0.50f); // Negro
                }
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
     * param dt delta-time en segundos.
     */
    private void actualizar(float dt) {
        // No actualizar física si estamos en el menú
        if (enMenu) {
            return;
        }

        // Si aun no inicio o ya todos murieron, no avanza simulacion.
        if (!started || isGameOverTotal()) {
            return;
        }

        boolean sonidoPunto = false;
        boolean sonidoMuerte = false;

        // Actualización Jugador 1
        if (!gameOver1) {
            bird1.update(dt, pipeManager1.getMultiplicadorDificultad());

            float birdTop1 = bird1.getY() + (Constants.BIRD_ALTO * 0.5f);
            float birdBottom1 = bird1.getY() - (Constants.BIRD_ALTO * 0.5f);
            boolean colBordes1 = birdTop1 >= 1.0f || birdBottom1 <= -1.0f;

            int ptsAntes = pipeManager1.getPuntaje();
            boolean colTubos1 = pipeManager1.update(dt, bird1.getY());
            // colisiono con el techo, suelo o tuberias.
            if (colBordes1 || colTubos1) {
                gameOver1 = true;
                sonidoMuerte = true;
            } else if (pipeManager1.getPuntaje() > ptsAntes) {
                sonidoPunto = true; // sonidito si se gana puntos
            }
        }

        // Actualización Jugador 2
        if (modoDosJugadores && !gameOver2) {
            bird2.update(dt, pipeManager2.getMultiplicadorDificultad());

            float birdTop2 = bird2.getY() + (Constants.BIRD_ALTO * 0.5f);
            float birdBottom2 = bird2.getY() - (Constants.BIRD_ALTO * 0.5f);
            boolean colBordes2 = birdTop2 >= 1.0f || birdBottom2 <= -1.0f;

            int ptsAntes = pipeManager2.getPuntaje();
            boolean colTubos2 = pipeManager2.update(dt, bird2.getY());

            if (colBordes2 || colTubos2) {
                gameOver2 = true;
                sonidoMuerte = true;
            } else if (pipeManager2.getPuntaje() > ptsAntes) {
                sonidoPunto = true;
            }
        }

        // Títulos y sonidos
        if (sonidoMuerte) {
            com.game.audio.SoundManager.playSound(sfxGameOver);
            renderer.actualizarTitulo(started, isGameOverTotal());
        }
        if (sonidoPunto) {
            com.game.audio.SoundManager.playSound(sfxPoint);
            renderer.actualizarTitulo(started, isGameOverTotal());
        }
    }

    // Asegurarse de limpiar recursos del sonido al cerrar
    public void cleanup() {
        com.game.audio.SoundManager.cleanup();
    }
}
