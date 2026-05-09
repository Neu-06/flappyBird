package com.game.view;

import com.game.model.Bird;
import com.game.model.PipeManager;
import com.game.view.renderers.BackgroundRenderer;
import com.game.view.renderers.BirdRenderer;
import com.game.view.renderers.MenuRenderer;
import com.game.view.renderers.PipeRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBEasyFont;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * GameView:
 * El área de dibujo principal de la escena. Funciona como orquestador delegando
 * el trabajo detallado a renderizadores específicos para cada entidad, lo que
 * permite
 * modificar su aspecto gráfico (ej: añadir PNGs o más geometría) de forma
 * aislada.
 */
public class GameView {

    private final Renderer renderer;
    private final BackgroundRenderer backgroundRenderer;
    private final PipeRenderer pipeRenderer;
    private final BirdRenderer birdRenderer;

    // --- NUEVO: Renderizador para el menú de inicio ---
    private final MenuRenderer menuRenderer;

    /**
     * @param renderer instancia inicializada del {@link Renderer}.
     */
    public GameView(Renderer renderer) {
        this.renderer = renderer;
        this.backgroundRenderer = new BackgroundRenderer();
        this.pipeRenderer = new PipeRenderer();
        this.birdRenderer = new BirdRenderer();

        // Inicializamos el nuevo renderizador del menú
        this.menuRenderer = new MenuRenderer();
    }

    /**
     * Render del frame (fondo y tuberías).
     *
     * @param pipes    gestor de tuberías con la lista activa.
     * @param gameOver true para dibujar el overlay de game over.
     */
    public void renderScene(PipeManager pipes, boolean gameOver) {
        // Delegar dibujo del fondo
        backgroundRenderer.render(this);

        // Delegar dibujo de las tuberías
        pipeRenderer.render(this, pipes);

        // Overlay simple de game over (sin texto en framebuffer).
        if (gameOver) {
            dibujarRect(0.0f, 0.0f, 2.0f, 0.22f, 0.15f, 0.18f, 0.22f);
        }
    }

    /**
     * Renderiza el pájaro delegando a su renderizador específico.
     *
     * @param bird modelo del pájaro.
     */
    public void renderBird(Bird bird) {
        birdRenderer.render(this, bird);
    }

    // ==========================================
    // NUEVO MÉTODO PARA DIBUJAR EL MENÚ
    // ==========================================
    /**
     * Renderiza el menú inicial delegando la tarea al MenuRenderer.
     * 
     * @param seleccion índice de la opción actualmente resaltada.
     */
    public void renderMenu(int seleccion) {
        menuRenderer.render(this, seleccion);
    }

    /**
     * Helper PÚBLICO de dibujo paramétrico de rectángulos.
     * Expuesto para que los renderizadores específicos (BirdRenderer, etc.)
     * puedan dibujar la geometría sin conocer los detalles de OpenGL internos.
     *
     * @param x     posición X del centro en NDC.
     * @param y     posición Y del centro en NDC.
     * @param ancho ancho del rectángulo en NDC.
     * @param alto  alto del rectángulo en NDC.
     * @param r     componente rojo [0,1].
     * @param g     componente verde [0,1].
     * @param b     componente azul [0,1].
     */
    public void dibujarRect(float x, float y, float ancho, float alto,
            float r, float g, float b) {
        // Traslacion del quad.
        GL20.glUniform2f(renderer.getUOffsetLocation(), x, y);
        // Escala del quad.
        GL20.glUniform2f(renderer.getUScaleLocation(), ancho, alto);
        // Rotación a 0 (vital para no heredar rotaciones de los triángulos)
        GL20.glUniform1f(renderer.getURotationLocation(), 0.0f);
        // Desactivar textura para usar color sólido
        GL20.glUniform1i(renderer.getUUseTextureLocation(), 0);
        // Color.
        GL20.glUniform3f(renderer.getUColorLocation(), r, g, b);
        // Dibujar 2 triangulos.
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

    // Metodo para dibujar un circulo
    public void dibujarCirculo(float x, float y, float ancho, float alto, float r, float g, float b) {
        GL20.glUniform2f(renderer.getUOffsetLocation(), x, y);
        GL20.glUniform2f(renderer.getUScaleLocation(), ancho, alto);
        // Desactivar textura para usar color sólido
        GL20.glUniform1i(renderer.getUUseTextureLocation(), 0);
        GL20.glUniform3f(renderer.getUColorLocation(), r, g, b);

        // Cambiamos el VAO al del círculo antes de dibujar
        GL30.glBindVertexArray(renderer.getVaoCirculo());
        GL11.glDrawArrays(GL11.GL_TRIANGLE_FAN, 0, 32 + 2);

        // Regresamos al VAO del quad para no romper otros dibujos
        GL30.glBindVertexArray(renderer.getVao());
    }

    // Metodo para dibujar un triangulo
    public void dibujarTriangulo(float x, float y, float ancho, float alto, float rotacion,
            float r, float g, float b) {
        // Traslacion del quad.
        GL20.glUniform2f(renderer.getUOffsetLocation(), x, y);
        // Escala del quad.
        GL20.glUniform2f(renderer.getUScaleLocation(), ancho, alto);
        // Rotacion
        GL20.glUniform1f(renderer.getURotationLocation(), rotacion);
        // Desactivar textura para usar color sólido
        GL20.glUniform1i(renderer.getUUseTextureLocation(), 0);
        // Color.
        GL20.glUniform3f(renderer.getUColorLocation(), r, g, b);
        // Dibujar 1 triangulo.
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
    }

    // ==========================================
    // NUEVO: DIBUJAR IMÁGENES (TEXTURAS)
    // ==========================================
    /**
     * Dibuja una imagen cargada previamente con TextureLoader.
     * Es ideal para fondos, pájaros, tuberías, etc.
     * 
     * @param textureId ID de la textura en OpenGL.
     * @param x,y       Posición central en NDC.
     * @param ancho,alto Tamaño en NDC.
     */
    public void dibujarImagen(int textureId, float x, float y, float ancho, float alto) {
        // Activar el uso de texturas en el shader
        GL20.glUniform1i(renderer.getUUseTextureLocation(), 1);

        // Traslacion y escala.
        GL20.glUniform2f(renderer.getUOffsetLocation(), x, y);
        GL20.glUniform2f(renderer.getUScaleLocation(), ancho, alto);
        GL20.glUniform1f(renderer.getURotationLocation(), 0.0f); // Sin rotación

        // Enlazar la textura en la unidad 0
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        // Dibujar el quad (los UVs se generan dinámicamente en el shader)
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);

        // Desenlazar textura para no ensuciar
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }


    // ==========================================
    // DIBUJO DE TEXTO
    // ==========================================
    /**
     * Dibuja texto en pantalla utilizando STBEasyFont de LWJGL.
     * Como STBEasyFont genera quads (no soportados en OpenGL moderno),
     * este método los convierte a triángulos en tiempo real.
     * 
     * @param texto  El texto a mostrar.
     * @param x      Posición X en NDC (ej. -0.5f).
     * @param y      Posición Y en NDC (ej. 0.2f).
     * @param escala Tamaño del texto (ej. 0.003f).
     * @param r,g,b  Color RGB.
     */
    public void dibujarTexto(String texto, float x, float y, float escala, float r, float g, float b) {
        // 1. Generar la geometría del texto con STB
        ByteBuffer charBuffer = BufferUtils.createByteBuffer(texto.length() * 270);
        int quads = STBEasyFont.stb_easy_font_print(0, 0, texto, null, charBuffer);

        // 2. Convertir Quads (4 vértices) a Triángulos (6 vértices)
        int numVertices = quads * 6;
        FloatBuffer vertexData = BufferUtils.createFloatBuffer(numVertices * 3);

        for (int i = 0; i < quads; i++) {
            // STB genera vértices de 16 bytes: x(float), y(float), z(float), color(4 bytes)
            int offset = i * 64;

            float v0x = charBuffer.getFloat(offset);
            float v0y = charBuffer.getFloat(offset + 4);
            float v0z = charBuffer.getFloat(offset + 8);

            float v1x = charBuffer.getFloat(offset + 16);
            float v1y = charBuffer.getFloat(offset + 20);
            float v1z = charBuffer.getFloat(offset + 24);

            float v2x = charBuffer.getFloat(offset + 32);
            float v2y = charBuffer.getFloat(offset + 36);
            float v2z = charBuffer.getFloat(offset + 40);

            float v3x = charBuffer.getFloat(offset + 48);
            float v3y = charBuffer.getFloat(offset + 52);
            float v3z = charBuffer.getFloat(offset + 56);

            // Triángulo 1 (v0, v1, v2)
            vertexData.put(v0x).put(v0y).put(v0z);
            vertexData.put(v1x).put(v1y).put(v1z);
            vertexData.put(v2x).put(v2y).put(v2z);

            // Triángulo 2 (v0, v2, v3)
            vertexData.put(v0x).put(v0y).put(v0z);
            vertexData.put(v2x).put(v2y).put(v2z);
            vertexData.put(v3x).put(v3y).put(v3z);
        }
        vertexData.flip();

        // 3. Crear un VAO y VBO temporal para dibujar el texto
        int tempVao = GL30.glGenVertexArrays();
        int tempVbo = GL15.glGenBuffers();

        GL30.glBindVertexArray(tempVao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tempVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexData, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(0);

        // 4. Configurar uniforms (el shader lo escalará y posicionará)
        GL20.glUniform2f(renderer.getUOffsetLocation(), x, y);
        // STBEasyFont dibuja "al revés" verticalmente, así que invertimos Y en la
        // escala
        GL20.glUniform2f(renderer.getUScaleLocation(), escala, -escala);
        GL20.glUniform1f(renderer.getURotationLocation(), 0.0f);
        GL20.glUniform1i(renderer.getUUseTextureLocation(), 0); // Desactivar textura para dibujar letras sólidas
        GL20.glUniform3f(renderer.getUColorLocation(), r, g, b);

        // 5. Dibujar
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, numVertices);

        // 6. Limpiar y volver al VAO original
        GL30.glBindVertexArray(renderer.getVao());
        GL30.glDeleteVertexArrays(tempVao);
        GL15.glDeleteBuffers(tempVbo);
    }
}
