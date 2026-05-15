package com.game.view;

import com.game.model.Bird;
import com.game.model.PipeManager;
import com.game.view.renderers.BackgroundRenderer;
import com.game.view.renderers.BirdRenderer;
import com.game.view.renderers.MenuRenderer;
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

    // Renderizador para el menú de inicio
    private final MenuRenderer menuRenderer;

    /**
     * param renderer instancia inicializada del Renderer.
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
     * param pipes gestor de tuberías con la lista activa.
     * param gameOver true para dibujar el overlay de game over.
     */
    public void renderScene(PipeManager pipes, boolean gameOver) {
        // Delegar dibujo del fondo
        backgroundRenderer.render(this);

        // Delegar dibujo de las tuberías
        pipeRenderer.render(this, pipes);

        // Dibujar el puntaje centrado en la parte superior
        String puntajeTexto = String.valueOf(pipes.getPuntaje());
        // Ajuste básico para centrar el texto según la cantidad de dígitos
        float offsetX = puntajeTexto.length() * 0.04f;
        // Color blanco brillante para que resalte
        dibujarTexto(puntajeTexto, -offsetX, 0.9f, 0.008f, 1.0f, 1.0f, 1.0f);

    }

    /**
     * Dibuja la pantalla de Game Over por encima de todo.
     * 
     * param pipes para obtener el puntaje.
     */
    public void renderGameOver(PipeManager pipes) {
        // Fondo oscuro semi-transparente para resaltar el texto
        dibujarRect(0.0f, 0.0f, 2.0f, 2.0f, 0.1f, 0.1f, 0.15f);

        // Título
        dibujarTexto("GAME OVER", -0.0f, 0.4f, 0.012f, 1.0f, 0.3f, 0.3f); // Rojo claro

        // Puntos totales
        String textoPuntos = "PUNTOS: " + pipes.getPuntaje();
        dibujarTexto(textoPuntos, -textoPuntos.length() * 0.0f, 0.1f, 0.01f, 1.0f, 1.0f, 0.0f); // Amarillo

        // Instrucciones
        dibujarTexto("ESPACIO para Reiniciar", -0.0f, -0.2f, 0.005f, 0.0f, 0.0f, 1.0f);
        dibujarTexto("R para volver al Menu", -0.0f, -0.4f, 0.005f, 0.0f, 0.0f, 1.0f);
    }

    /**
     * Renderiza el pájaro delegando a su renderizador específico.
     *
     * param bird modelo del pájaro.
     */
    public void renderBird(Bird bird) {
        birdRenderer.render(this, bird);
    }

    // ==========================================
    // DIBUJA EL MENÚ
    // ==========================================
    /**
     * Renderiza el menú inicial delegando la tarea al MenuRenderer.
     * 
     * param seleccion índice de la opción actualmente resaltada.
     */
    public void renderMenu(int seleccion) {
        menuRenderer.render(this, seleccion);
    }

    /**
     * Helper PÚBLICO de dibujo paramétrico de rectángulos.
     * Expuesto para que los renderizadores específicos (BirdRenderer, etc.)
     * puedan dibujar la geometría sin conocer los detalles de OpenGL internos.
     *
     * param x posición X del centro en NDC.
     * param y posición Y del centro en NDC.
     * param ancho ancho del rectángulo en NDC.
     * param alto alto del rectángulo en NDC.
     * param r componente rojo [0,1].
     * param g componente verde [0,1].
     * param b componente azul [0,1].
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
    // DIBUJAR IMÁGENES (TEXTURAS)
    // ==========================================
    /**
     * Dibuja una imagen cargada previamente con TextureLoader.
     * Es ideal para fondos, pájaros, tuberías, etc.
     * 
     * param textureId ID de la textura en OpenGL.
     * param x,y Posición central en NDC.
     * param ancho,alto Tamaño en NDC.
     */
    public void dibujarImagen(int textureId, float x, float y, float ancho, float alto) {
        // Activar el uso de texturas en el shader
        GL20.glUniform1i(renderer.getUUseTextureLocation(), 1);

        // Decirle al shader que lea la textura desde la unidad 0 (GL_TEXTURE0)
        GL20.glUniform1i(renderer.getUTextureLocation(), 0);

        // Traslacion y escala.
        GL20.glUniform2f(renderer.getUOffsetLocation(), x, y);
        GL20.glUniform2f(renderer.getUScaleLocation(), ancho, alto);
        GL20.glUniform1f(renderer.getURotationLocation(), 0.0f); // Sin rotación

        // Por defecto, dibujar la imagen sin teñirla (blanco puro)
        GL20.glUniform3f(renderer.getUColorLocation(), 1.0f, 1.0f, 1.0f);

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
    // Caché para no regenerar texturas idénticas cada frame
    private java.util.Map<String, Integer> cacheTextos = new java.util.HashMap<>();

    /**
     * Dibuja texto en pantalla con alta calidad.
     * 
     * param texto El texto a mostrar.
     * param x Posición X en NDC (ej. -0.5f).
     * param y Posición Y en NDC (ej. 0.2f).
     * param escala Tamaño del texto (ej. 0.008f para la nueva fuente).
     * param r,g,b Color RGB para teñir el texto.
     */
    public void dibujarTexto(String texto, float x, float y, float escala, float r, float g, float b) {
        // Obtener la textura del texto desde la caché o crearla
        if (!cacheTextos.containsKey(texto)) {
            cacheTextos.put(texto, TextureLoader.createTextTexture(texto));
        }
        int textureId = cacheTextos.get(texto);

        // Activar uso de textura y configurar uniforms
        GL20.glUniform1i(renderer.getUUseTextureLocation(), 1);
        GL20.glUniform1i(renderer.getUTextureLocation(), 0);

        // Ajustar el ancho en base a la longitud del texto (aproximación rápida)
        // Reducimos los multiplicadores para que coincidan mejor con la escala que
        // esperabas
        float ancho = texto.length() * escala * 6.0f;
        float alto = escala * 12.0f;

        // Transformaciones
        GL20.glUniform2f(renderer.getUOffsetLocation(), x, y);
        GL20.glUniform2f(renderer.getUScaleLocation(), ancho, alto);
        GL20.glUniform1f(renderer.getURotationLocation(), 0.0f);

        // Aplicar el color deseado al texto (el shader multiplicará la textura
        // blanca por este color)
        GL20.glUniform3f(renderer.getUColorLocation(), r, g, b);

        // Dibujar
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
}
