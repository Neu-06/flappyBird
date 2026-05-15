package com.game.audio;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * SoundManager:
 * Archivo aislado encargado exclusivamente de reproducir audio (OpenAL).
 * Mantiene la lógica gráfica y de input limpia. Puedes usar sus métodos
 * para añadir sonidos al salto del pájaro, choques, etc.
 */
public class SoundManager {

    private static long device;
    private static long context;
    private static boolean isInitialized = false;

    /**
     * Inicializa el sistema de audio (OpenAL).
     * Se llama automáticamente la primera vez que cargas un sonido.
     */
    private static void init() {
        if (isInitialized)
            return;

        // Abrir el dispositivo de audio predeterminado
        String defaultDeviceName = ALC10.alcGetString(0, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);
        device = ALC10.alcOpenDevice(defaultDeviceName);

        // Crear un contexto de audio y activarlo
        int[] attributes = { 0 };
        context = ALC10.alcCreateContext(device, attributes);
        ALC10.alcMakeContextCurrent(context);

        // Crear capacidades de OpenAL
        ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
        AL.createCapabilities(alcCapabilities);

        isInitialized = true;
    }

    /**
     * Carga un archivo .ogg desde el disco y devuelve un ID de buffer.
     * 
     * param filepath Ruta al archivo OGG
     * return El ID del buffer de sonido en OpenAL, o -1 si falla.
     */
    public static int loadSound(String filepath) {
        if (!isInitialized)
            init();

        IntBuffer channelsBuffer = BufferUtils.createIntBuffer(1);
        IntBuffer sampleRateBuffer = BufferUtils.createIntBuffer(1);

        // STBVorbis hace todo el trabajo difícil de decodificar el OGG
        ShortBuffer rawAudioBuffer = STBVorbis.stb_vorbis_decode_filename(filepath, channelsBuffer, sampleRateBuffer);

        if (rawAudioBuffer == null) {
            System.err.println("ERROR: No se pudo cargar el sonido: " + filepath);
            return -1;
        }

        int channels = channelsBuffer.get(0);
        int sampleRate = sampleRateBuffer.get(0);

        int format = -1;
        if (channels == 1) {
            format = AL10.AL_FORMAT_MONO16;
        } else if (channels == 2) {
            format = AL10.AL_FORMAT_STEREO16;
        }

        // Generar un buffer de OpenAL y llenarlo con la información de audio
        int bufferId = AL10.alGenBuffers();
        AL10.alBufferData(bufferId, format, rawAudioBuffer, sampleRate);

        // Liberar la memoria temporal que usó STBVorbis
        MemoryUtil.memFree(rawAudioBuffer);

        return bufferId;
    }

    /**
     * Reproduce un sonido cargado previamente de forma rápida y sencilla.
     * 
     * @param bufferId El ID de sonido que devolvió loadSound().
     */
    public static void playSound(int bufferId) {
        if (bufferId == -1)
            return;

        // Generar una "fuente emisora" temporal y reproducir el buffer
        int sourceId = AL10.alGenSources();
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufferId);
        AL10.alSourcePlay(sourceId);

        // Nota para escalabilidad: En un motor grande aquí habría lógica
        // para destruir el sourceId una vez termine de sonar para no saturar
        // la memoria. Para sonidos cortos de menú de momento funciona bien.
    }

    /**
     * Limpia la memoria al cerrar el juego.
     */
    public static void cleanup() {
        if (!isInitialized)
            return;
        ALC10.alcMakeContextCurrent(0);
        ALC10.alcDestroyContext(context);
        ALC10.alcCloseDevice(device);
        isInitialized = false;
    }
}
