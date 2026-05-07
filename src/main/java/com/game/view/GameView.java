package com.game.view;

import com.game.model.Bird;
import com.game.model.PipeManager;
import com.game.view.renderers.BackgroundRenderer;
import com.game.view.renderers.BirdRenderer;
import com.game.view.renderers.PipeRenderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

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

    /**
     * @param renderer instancia inicializada del {@link Renderer}.
     */
    public GameView(Renderer renderer) {
        this.renderer = renderer;
        this.backgroundRenderer = new BackgroundRenderer();
        this.pipeRenderer = new PipeRenderer();
        this.birdRenderer = new BirdRenderer();
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
        // Color.
        GL20.glUniform3f(renderer.getUColorLocation(), r, g, b);
        // Dibujar 2 triangulos.
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

    // Metodo para dibujar un circulo
    public void dibujarCirculo(float x, float y, float ancho, float alto, float r, float g, float b) {
        GL20.glUniform2f(renderer.getUOffsetLocation(), x, y);
        GL20.glUniform2f(renderer.getUScaleLocation(), ancho, alto);
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
        // Color.
        GL20.glUniform3f(renderer.getUColorLocation(), r, g, b);
        // Dibujar 1 triangulo.
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
    }
}
