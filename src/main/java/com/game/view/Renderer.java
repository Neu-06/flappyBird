package com.game.view;

import com.game.core.Constants;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Renderer:
 * Encapsula toda la infraestructura de bajo nivel de OpenGL/GLFW: creación
 * de la ventana, compilación y enlazado de shaders GLSL, creación del VAO/VBO
 * del "quad base", y resolución de uniforms.
 *
 * GameView recibe una referencia a esta clase para poder activar
 * el programa y el VAO antes de emitir llamadas de dibujo.
 */
public class Renderer {

    // -------------------------------------------------------------------------
    // Recursos OpenGL basicos.
    // -------------------------------------------------------------------------
    private long window;
    private int programa;
    private int vao;
    private int vbo;

    // Uniforms de transformacion y color.
    private int uOffsetLocation;
    private int uScaleLocation;
    private int uColorLocation;

    // -------------------------------------------------------------------------
    // Inicialización
    // -------------------------------------------------------------------------

    /**
     * Inicializa GLFW/OpenGL + shaders + geometria base.
     * Código extraído literalmente de AppFlappyBird#init().
     */
    public void init() {
        // Arranque de GLFW.
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("No se pudo iniciar GLFW");
        }

        // Config de ventana/contexto.
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        // Crear ventana.
        window = GLFW.glfwCreateWindow(Constants.ANCHO, Constants.ALTO, "Flappy Bird OpenGL", 0, 0);
        if (window == 0) {
            throw new RuntimeException("No se pudo crear la ventana");
        }

        // Contexto + VSync + mostrar.
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);
        // Cargar funciones OpenGL.
        GL.createCapabilities();

        // Crear pipeline y quad unitario reutilizable.
        crearShaders();
        crearQuadBase();
    }

    /**
     * Crea shaders 2D:
     * - Vertex: transforma quad base con escala y offset.
     * - Fragment: color uniforme.
     *
     * Extraído AppFlappyBird#crearShaders()
     */
    private void crearShaders() {
        String vertexSrc = """
                #version 330 core
                layout (location = 0) in vec3 aPos;
                uniform vec2 uOffset;
                uniform vec2 uScale;
                void main() {
                    vec2 finalPos = aPos.xy * uScale + uOffset;
                    gl_Position = vec4(finalPos, aPos.z, 1.0);
                }
                """;

        // Color solido por objeto.
        String fragmentSrc = """
                #version 330 core
                uniform vec3 uColor;
                out vec4 fragColor;
                void main() {
                    fragColor = vec4(uColor, 1.0);
                }
                """;

        // Compilar vertex shader.
        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexSrc);
        GL20.glCompileShader(vertexShader);
        comprobarShader(vertexShader, "Vertex");

        // Compilar fragment shader.
        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentSrc);
        GL20.glCompileShader(fragmentShader);
        comprobarShader(fragmentShader, "Fragment");

        // Link de programa.
        programa = GL20.glCreateProgram();
        GL20.glAttachShader(programa, vertexShader);
        GL20.glAttachShader(programa, fragmentShader);
        GL20.glLinkProgram(programa);

        if (GL20.glGetProgrami(programa, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error al enlazar programa: " + GL20.glGetProgramInfoLog(programa));
        }

        // Resolver uniforms.
        uOffsetLocation = GL20.glGetUniformLocation(programa, "uOffset");
        uScaleLocation = GL20.glGetUniformLocation(programa, "uScale");
        uColorLocation = GL20.glGetUniformLocation(programa, "uColor");
        if (uOffsetLocation == -1 || uScaleLocation == -1 || uColorLocation == -1) {
            throw new RuntimeException("No se pudieron obtener uniforms del shader");
        }

        // Limpiar objetos shader temporales.
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    // Verificacion de compilacion GLSL.
    private void comprobarShader(int shader, String tipo) {
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException(tipo + " shader: " + GL20.glGetShaderInfoLog(shader));
        }
    }

    /**
     * Crea un rectangulo unitario centrado en origen:
     * - Rango x,y de -0.5 a +0.5.
     * - 2 triangulos (6 vertices).
     * Cualquier objeto 2D se dibuja escalando y moviendo este quad.
     *
     * Extraído AppFlappyBird#crearQuadBase()
     */
    private void crearQuadBase() {
        float[] vertices = {
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                0.5f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, 0.5f, 0.0f,
                -0.5f, 0.5f, 0.0f
        };

        // VAO.
        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        // VBO.
        vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        // Subida de vertices.
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        // Atributo posicion.
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        // Desbind.
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    // -------------------------------------------------------------------------
    // Utilidades de frame
    // -------------------------------------------------------------------------

    /**
     * Activa el programa de shader y el VAO del quad base para el frame actual.
     * Debe llamarse al inicio de cada frame de dibujo, antes de las llamadas
     * de {@code GameView}.
     */
    public void beginFrame() {
        // Activar pipeline y malla base.
        GL20.glUseProgram(programa);
        GL30.glBindVertexArray(vao);
    }

    /**
     * Actualiza feedback visual en barra de titulo.
     * Extraído de AppFlappyBird#actualizarTitulo().
     *
     * @param started  si el juego ya ha arrancado.
     * @param gameOver si la partida ha terminado.
     * @param puntaje  puntuación a mostrar.
     */
    public void actualizarTitulo(boolean started, boolean gameOver, int puntaje) {
        String tituloBase = "Flappy Bird OpenGL | Puntos: " + puntaje;
        if (!started) {
            GLFW.glfwSetWindowTitle(window, tituloBase + " | SPACE para empezar");
        } else if (gameOver) {
            GLFW.glfwSetWindowTitle(window, tituloBase + " | GAME OVER - SPACE o R para reiniciar");
        } else {
            GLFW.glfwSetWindowTitle(window, tituloBase);
        }
    }

    /**
     * Presenta el frame renderizado e interpola eventos.
     * Equivale al bloque al final del bucle en AppFlappyBird#loop().
     */
    public void endFrame() {
        // Presentar frame y leer eventos.
        GLFW.glfwSwapBuffers(window);
        GLFW.glfwPollEvents();
    }

    // -------------------------------------------------------------------------
    // Liberacion de recursos.
    // -------------------------------------------------------------------------

    /**
     * Libera todos los recursos de GPU y termina GLFW.
     * Extraído de AppFlappyBird#cleanup().
     */
    public void cleanup() {
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        GL20.glDeleteProgram(programa);
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** @return handle de ventana GLFW. */
    public long getWindow() {
        return window;
    }

    /** @return location del uniform {@code uOffset}. */
    public int getUOffsetLocation() {
        return uOffsetLocation;
    }

    /** @return location del uniform {@code uScale}. */
    public int getUScaleLocation() {
        return uScaleLocation;
    }

    /** @return location del uniform {@code uColor}. */
    public int getUColorLocation() {
        return uColorLocation;
    }
}
