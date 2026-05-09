package com.game.view;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * TextureLoader:
 * Clase aislada encargada exclusivamente de cargar imágenes PNG desde el disco
 * y convertirlas en Texturas de OpenGL utilizables en el juego.
 * Esto mantiene el código organizado y evita ensuciar el Renderer o el GameView.
 */
public class TextureLoader {

    /**
     * Carga una imagen PNG y devuelve el ID (entero) de la textura en OpenGL.
     * 
     * @param filepath Ruta al archivo de imagen (ej: "src/main/resources/textures/background.png")
     * @return El ID de la textura generada por OpenGL.
     */
    public static int loadTexture(String filepath) {
        // 1. Generar un ID de textura vacío en OpenGL
        int textureId = GL11.glGenTextures();

        // 2. Hacer "bind" (seleccionar) esa textura para configurarla
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        // 3. Configurar cómo se escala la imagen (GL_NEAREST mantiene los píxeles nítidos, ideal para 2D)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        // Configurar qué pasa si nos salimos de los bordes de la imagen (repetir)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        // 4. Preparar buffers para recibir el ancho, alto y canales de la imagen
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        // Voltear la imagen verticalmente al cargarla (las coordenadas UV de OpenGL están invertidas respecto a las imágenes normales)
        STBImage.stbi_set_flip_vertically_on_load(true);

        // 5. Cargar los píxeles reales de la imagen usando STBImage
        ByteBuffer image = STBImage.stbi_load(filepath, width, height, channels, 0);

        if (image != null) {
            // Si la imagen tiene transparencia (Canales = 4, RGBA) o no (Canales = 3, RGB)
            int format = (channels.get(0) == 4) ? GL11.GL_RGBA : GL11.GL_RGB;

            // 6. Enviar los píxeles a la memoria de la Tarjeta Gráfica (GPU)
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, format, width.get(0), height.get(0), 
                              0, format, GL11.GL_UNSIGNED_BYTE, image);

            // Generar Mipmaps (versiones más pequeñas de la imagen para cuando se aleja)
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

            // Liberar la imagen de la memoria RAM principal porque ya está en la GPU
            STBImage.stbi_image_free(image);
        } else {
            System.err.println("ERROR: No se pudo cargar la imagen: " + filepath);
            System.err.println("Motivo: " + STBImage.stbi_failure_reason());
        }

        // Deseleccionar la textura
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        return textureId;
    }
}
