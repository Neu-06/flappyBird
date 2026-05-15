package com.game.view;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * TextureLoader:
 * Clase aislada encargada exclusivamente de cargar imágenes PNG desde el disco
 * y convertirlas en Texturas de OpenGL utilizables en el juego.
 * Esto mantiene el código organizado y evita ensuciar el Renderer o el
 * GameView.
 */
public class TextureLoader {

    /**
     * Carga una imagen PNG y devuelve el ID (entero) de la textura en OpenGL.
     * 
     * param filepath Ruta al archivo de imagen
     * return El ID de la textura generada por OpenGL.
     */
    public static int loadTexture(String filepath) {
        // Generar un ID de textura vacío en OpenGL
        int textureId = GL11.glGenTextures();

        // Hacer "bind" (seleccionar) esa textura para configurarla
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        // Configurar cómo se escala la imagen (GL_NEAREST mantiene los píxeles
        // nítidos, ideal para 2D)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        // Configurar qué pasa si nos salimos de los bordes de la imagen (repetir)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        // Preparar buffers para recibir el ancho, alto y canales de la imagen
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);

        // Voltear la imagen verticalmente al cargarla (las coordenadas UV de OpenGL
        // están invertidas respecto a las imágenes normales)
        STBImage.stbi_set_flip_vertically_on_load(true);

        // Cargar los píxeles reales de la imagen usando STBImage
        ByteBuffer image = STBImage.stbi_load(filepath, width, height, channels, 0);

        if (image != null) {
            // Si la imagen tiene transparencia (Canales = 4, RGBA) o no (Canales = 3, RGB)
            int format = (channels.get(0) == 4) ? GL11.GL_RGBA : GL11.GL_RGB;

            // Enviar los píxeles a la memoria de la Tarjeta Gráfica (GPU)
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

    /**
     * Crea una textura OpenGL directamente desde un texto generado con alta
     * calidad
     * usando el sistema de dibujo nativo de Java (Graphics2D).
     * Esto soluciona los problemas de STBEasyFont (píxeles y fuentes extrañas).
     * 
     * param texto El texto a convertir en textura.
     * return El ID de la textura OpenGL.
     */
    public static int createTextTexture(String texto) {
        // Usar una fuente estándar bonita y gruesa
        Font font = new Font("SansSerif", Font.BOLD, 64);

        // Crear una imagen temporal para calcular el tamaño del texto
        BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = tempImg.createGraphics();
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int width = fm.stringWidth(texto);
        int height = fm.getHeight();
        g2d.dispose();

        // Evitar anchos o altos en cero si el texto está vacío
        if (width == 0)
            width = 1;
        if (height == 0)
            height = 1;

        // Crear la imagen final con fondo transparente
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = img.createGraphics();

        // Activar Anti-Aliasing para que las letras se vean súper suaves
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setFont(font);

        // Dibujamos el texto en BLANCO para poder "teñirlo" luego con el uColor en el
        // shader
        g2d.setColor(Color.WHITE);
        g2d.drawString(texto, 0, fm.getAscent());
        g2d.dispose();

        // Extraer los píxeles (ARGB)
        int[] pixels = new int[width * height];
        img.getRGB(0, 0, width, height, pixels, 0, width);

        // Convertir al formato que OpenGL entiende (RGBA)
        // OJO: Iteramos la 'Y' desde height-1 hasta 0 porque OpenGL espera
        // las texturas de abajo hacia arriba (Bottom-Up).
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
                buffer.put((byte) ((pixel >> 8) & 0xFF)); // Green
                buffer.put((byte) (pixel & 0xFF)); // Blue
                buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
            }
        }
        buffer.flip();

        // Crear la textura en OpenGL
        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        // Usar GL_LINEAR para un escalado suave
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
                buffer);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        return textureId;
    }
}
