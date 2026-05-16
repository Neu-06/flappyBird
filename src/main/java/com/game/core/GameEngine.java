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
    private boolean gameOver2;

    // Jugador 3
    private final Bird bird3;
    private boolean gameOver3;

    // Puntajes finales registrados al morir
    private int puntajeFinal1 = 0;
    private int puntajeFinal2 = 0;
    private int puntajeFinal3 = 0;

    // Cantidad de jugadores (1, 2 o 3)
    private int numJugadores = 1;
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
        if (numJugadores == 3) {
            return gameOver1 && gameOver2 && gameOver3;
        } else if (numJugadores == 2) {
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
        bird3 = new Bird();
        gameView = new GameView(renderer);
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
        puntajeFinal1 = 0;

        bird2.reset();
        gameOver2 = false;
        puntajeFinal2 = 0;

        bird3.reset();
        gameOver3 = false;
        puntajeFinal3 = 0;

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
            numJugadores = seleccionMenu + 1; // 1, 2 o 3
            resetGame();
            started = true;
            bird1.saltar(pipeManager1.getMultiplicadorDificultad());
            if (numJugadores >= 2) {
                bird2.saltar(pipeManager1.getMultiplicadorDificultad());
            }
            if (numJugadores == 3) {
                bird3.saltar(pipeManager1.getMultiplicadorDificultad());
            }
            return;
        }

        // Lógica de juego: REINICIO GLOBAL
        if (isGameOverTotal()) {
            resetGame();
            started = true;
            bird1.saltar(pipeManager1.getMultiplicadorDificultad());
            if (numJugadores >= 2) {
                bird2.saltar(pipeManager1.getMultiplicadorDificultad());
            }
            if (numJugadores == 3) {
                bird3.saltar(pipeManager1.getMultiplicadorDificultad());
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
            // Al presionar R, volvemos al menú
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
            seleccionMenu--;
            if (seleccionMenu < 0)
                seleccionMenu = 0;
        } else if (started && numJugadores >= 2 && !gameOver2) {
            // SALTO JUGADOR 2
            bird2.saltar(pipeManager1.getMultiplicadorDificultad());
        }
    }

    /**
     * Responde a la tecla ABAJO o S para cambiar de opción en el menú.
     */
    public void onDownPressed() {
        if (enMenu) {
            seleccionMenu++;
            if (seleccionMenu > 2)
                seleccionMenu = 2; // Máximo 3 opciones
        }
    }

    /**
     * Responde a la tecla ENTER (Jugador 3)
     */
    public void onEnterPressed() {
        if (started && numJugadores == 3 && !gameOver3 && !isGameOverTotal() && !enMenu) {
            bird3.saltar(pipeManager1.getMultiplicadorDificultad());
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
                // Siempre renderizamos a pantalla completa, sea 1 o 2 jugadores
                org.lwjgl.opengl.GL11.glViewport(0, 0, Constants.ANCHO, Constants.ALTO);

                // Renderizar el escenario compartido (fondo y tuberías)
                gameView.renderScene(pipeManager1, isGameOverTotal());

                // Si el J1 no está muerto, se dibuja
                if (!gameOver1) {
                    gameView.renderBird(bird1);
                }

                // Si el J2 no está muerto, se dibuja en el mismo escenario
                if (numJugadores >= 2 && !gameOver2) {
                    gameView.renderBird(bird2);
                }

                // Si el J3 no está muerto, se dibuja en el mismo escenario
                if (numJugadores == 3 && !gameOver3) {
                    gameView.renderBird(bird3); // Nota: podemos cambiarle el color si queremos
                }

                // Pantalla de Game Over unificada al terminar
                if (isGameOverTotal()) {
                    gameView.renderGameOver(puntajeFinal1, puntajeFinal2, puntajeFinal3, numJugadores);
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

        boolean dtAplicado = false; // Indica si ya avanzamos las tuberías en este frame

        // Actualización Jugador 1
        if (!gameOver1) {
            bird1.update(dt, pipeManager1.getMultiplicadorDificultad());

            float birdTop1 = bird1.getY() + (Constants.BIRD_ALTO * 0.5f);
            float birdBottom1 = bird1.getY() - (Constants.BIRD_ALTO * 0.5f);
            boolean colBordes1 = birdTop1 >= 1.0f || birdBottom1 <= -1.0f;

            int ptsAntes = pipeManager1.getPuntaje();
            boolean colTubos1 = pipeManager1.update(dt, bird1.getY());
            dtAplicado = true; // El J1 ya hizo avanzar el escenario

            // colisiono con el techo, suelo o tuberias.
            if (colBordes1 || colTubos1 || ptsAntes == 5) {
                gameOver1 = true;
                puntajeFinal1 = pipeManager1.getPuntaje(); // Congelar puntaje J1
                sonidoMuerte = true;
            } else if (pipeManager1.getPuntaje() > ptsAntes) {
                sonidoPunto = true; // sonidito si se gana puntos
            }
        }

        // Actualización Jugador 2
        if (numJugadores >= 2 && !gameOver2) {
            bird2.update(dt, pipeManager1.getMultiplicadorDificultad());

            float birdTop2 = bird2.getY() + (Constants.BIRD_ALTO * 0.5f);
            float birdBottom2 = bird2.getY() - (Constants.BIRD_ALTO * 0.5f);
            boolean colBordes2 = birdTop2 >= 1.0f || birdBottom2 <= -1.0f;

            int ptsAntes = pipeManager1.getPuntaje();
            float dtTubos = dtAplicado ? 0.0f : dt;
            boolean colTubos2 = pipeManager1.update(dtTubos, bird2.getY());
            if (dtTubos > 0)
                dtAplicado = true; // Si avanzó tuberías, marcarlo

            if (colBordes2 || colTubos2 || ptsAntes == 8) {
                gameOver2 = true;
                puntajeFinal2 = pipeManager1.getPuntaje(); // Congelar puntaje J2
                sonidoMuerte = true;
            } else if (!dtAplicado && pipeManager1.getPuntaje() > ptsAntes) {
                sonidoPunto = true;
            }
        }

        // Actualización Jugador 3
        if (numJugadores == 3 && !gameOver3) {
            bird3.update(dt, pipeManager1.getMultiplicadorDificultad());

            float birdTop3 = bird3.getY() + (Constants.BIRD_ALTO * 0.5f);
            float birdBottom3 = bird3.getY() - (Constants.BIRD_ALTO * 0.5f);
            boolean colBordes3 = birdTop3 >= 1.0f || birdBottom3 <= -1.0f;

            int ptsAntes = pipeManager1.getPuntaje();
            // Solo avanzamos tuberías si ni J1 ni J2 lo hicieron
            float dtTubos = dtAplicado ? 0.0f : dt;
            boolean colTubos3 = pipeManager1.update(dtTubos, bird3.getY());
            if (dtTubos > 0)
                dtAplicado = true;

            if (colBordes3 || colTubos3) {
                gameOver3 = true;
                puntajeFinal3 = pipeManager1.getPuntaje(); // Congelar puntaje J3
                sonidoMuerte = true;
            } else if (!dtAplicado && pipeManager1.getPuntaje() > ptsAntes) {
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
