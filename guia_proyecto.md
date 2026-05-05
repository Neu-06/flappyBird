# Guía de Referencia — Flappy Bird OpenGL (LWJGL 3.3)

---

## 1. Glosario de Términos y Abreviaciones

| Término / Abreviación | Significado completo | Contexto en el proyecto |
|---|---|---|
| **NDC** | *Normalized Device Coordinates* — Coordenadas normalizadas del dispositivo | El espacio de coordenadas que usa OpenGL por defecto. El eje X va de **-1.0** (izquierda) a **+1.0** (derecha); el eje Y va de **-1.0** (abajo) a **+1.0** (arriba). `BIRD_X = -0.45f` significa "45% hacia la izquierda del centro". |
| **VAO** | *Vertex Array Object* | Objeto de GPU que recuerda cómo están organizados los vértices: cuántos atributos hay, de qué tipo son, dónde empieza cada uno. Se crea una vez en `Renderer#crearQuadBase()`. |
| **VBO** | *Vertex Buffer Object* | Buffer en la GPU que almacena los datos en bruto de los vértices (las coordenadas XYZ del quad). El VAO le "apunta". |
| **Quad / Quad base** | Cuadrilátero unitario de -0.5 a +0.5 | El único mesh del proyecto. Se escala y traslada via uniforms para dibujar cualquier rectángulo. |
| **Shader** | Programa que corre en la GPU | Hay dos: **vertex shader** (calcula posición final de cada vértice) y **fragment shader** (calcula el color de cada pixel). |
| **GLSL** | *OpenGL Shading Language* | El lenguaje C-like en el que se escriben los shaders. El código `#version 330 core` al inicio indica que usa OpenGL 3.3. |
| **Uniform** | Variable que la CPU envía a la GPU | En el proyecto: `uOffset` (posición del rectángulo), `uScale` (tamaño), `uColor` (color RGB). Se setean con `glUniform2f` / `glUniform3f`. |
| **uOffset** | Uniform `vec2` de traslación | Desplaza el quad base hacia la posición X, Y deseada en NDC. |
| **uScale** | Uniform `vec2` de escala | Estira el quad al ancho y alto del objeto a dibujar. |
| **uColor** | Uniform `vec3` de color | Color RGB del objeto, componentes entre 0.0 y 1.0. |
| **AABB** | *Axis-Aligned Bounding Box* | Técnica de colisión que usa rectángulos alineados a los ejes. Si los rectángulos se superponen en X **y** en Y, hay colisión. Simple y rápida, sin rotación. |
| **dt / delta-time** | Diferencia de tiempo entre frames en segundos | Hace que la física sea independiente de la velocidad del PC. Si un frame tarda 16 ms, `dt ≈ 0.016`. La física multiplica siempre por `dt`. |
| **GLFW** | *Graphics Library FrameWork* | Librería que maneja: crear la ventana, contexto OpenGL, eventos de teclado/mouse. Es la capa entre Java y el sistema operativo. |
| **GL.createCapabilities()** | Carga funciones OpenGL en memoria | Debe llamarse justo después de `glfwMakeContextCurrent`. Sin esto, cualquier llamada `GL*` crashea. |
| **VSync / SwapInterval(1)** | Sincronización vertical | Limita los FPS a la tasa de refresco del monitor (60, 144 Hz…) para evitar *screen tearing*. |
| **glfwSwapBuffers** | Intercambia el buffer trasero con el frontal | El juego dibuja en un buffer oculto y luego lo muestra de golpe, evitando parpadeos (*double buffering*). |
| **glfwPollEvents** | Procesa los eventos del sistema operativo pendientes | Si no se llama, la ventana no responde al teclado ni al botón de cerrar. |
| **glfwGetTime** | Tiempo en segundos desde inicio de GLFW | Se usa para calcular `dt`. Es de alta resolución (nanosegundos internamente). |
| **Core Profile** | Perfil estricto de OpenGL 3.3+ | Desactiva las funciones antiguas (OpenGL 1.x / 2.x). Obliga a usar VAO/VBO/Shaders. |
| **GL_TRIANGLES** | Primitiva de dibujo | Cada 3 vértices forman un triángulo. El quad usa 6 vértices → 2 triángulos → 1 rectángulo. |
| **gap / gapCentroY** | Hueco entre las dos mitades de tubería | `gapCentroY` es el centro Y del hueco. `GAP_ALTO` es la altura total del hueco. |
| **flanco / edge-detection** | Detectar el momento exacto en que se presiona una tecla | `prevSpace` guarda el estado anterior. Si ahora está presionada (`true`) y antes no (`false`), es el "flanco de subida" → acción única. |
| **IMPULSO_SALTO** | Velocidad vertical inicial al saltar | Se asigna directamente a `velY`. La gravedad luego la va reduciendo hasta negativa. |
| **VELOCIDAD_MAX_CAIDA** | Límite inferior de `velY` | Evita que el pájaro acelere infinitamente hacia abajo, dando una sensación más jugable. |
| **timerSpawn** | Acumulador de tiempo para generar tuberías | Se suma `dt` cada frame. Cuando supera `TIEMPO_ENTRE_TUBERIAS`, genera una tubería nueva y se reinicia a 0. |
| **puntuada** | Flag booleano de `Tuberia` | Evita sumar el punto más de una vez por la misma tubería. Se marca `true` cuando el borde derecho de la tubería pasa al pájaro. |
| **overlapX** | Solapamiento horizontal en AABB | `true` si el pájaro y la tubería se superponen en el eje X. Solo si esto es `true` se evalúa el eje Y. |
| **RNG** | *Random Number Generator* | `java.util.Random`. Se usa para calcular `gapCentroY` de cada tubería nueva de forma aleatoria. |

---

## 2. Mejorar Visuales: Ave, Paisaje, Tuberías con Imágenes

Actualmente todo se dibuja con rectángulos de color sólido (`glDrawArrays`). Para añadir texturas/imágenes se necesita:

### Dónde crear los archivos nuevos

```
src/
└── main/
    ├── java/com/game/
    │   └── view/
    │       └── TextureLoader.java   ← NUEVO: carga PNG → textura OpenGL
    └── resources/                   ← NUEVA CARPETA de assets
        ├── textures/
        │   ├── bird.png
        │   ├── pipe.png
        │   └── background.png
        └── sounds/
            ├── flap.wav
            └── hit.wav
```

### Qué archivos tocar y cuáles casi no

| Archivo | Nivel de cambio | Qué hacer |
|---|---|---|
| `view/TextureLoader.java` | **CREAR** | Cargar PNG con `STBImage` (ya incluido en LWJGL) y subirla a la GPU con `glTexImage2D`. |
| `view/Renderer.java` | **Moderado** | Añadir `crearShaderTextura()`: los shaders necesitan `in vec2 aTexCoord` y `uniform sampler2D uTextura` en GLSL. También crear un VBO con coordenadas UV. |
| `view/GameView.java` | **El más tocado** | Reemplazar `dibujarRect(...)` por `dibujarTextura(texturaId, x, y, ancho, alto)`. Llamar `glBindTexture` antes de `glDrawArrays`. |
| `model/Bird.java` | Sin cambios | Solo es datos. |
| `model/PipeManager.java` | Sin cambios | Solo es lógica. |
| `core/GameEngine.java` | Mínimo | Solo ajustar el orden de carga de texturas en `run()`. |
| `core/Constants.java` | Sin cambios | Quizás añadir rutas de recursos como constantes. |
| `input/InputHandler.java` | Sin cambios | No tiene nada visual. |

> **Nota:** LWJGL ya incluye `stb_image` sin necesidad de librerías externas. Se usa con `org.lwjgl.stb.STBImage`.

---

## 3. Añadir Sonidos

### Nueva clase sugerida

```
view/SoundManager.java   ← Carga y reproduce archivos WAV/OGG
```

### Librería disponible sin agregar dependencias

LWJGL incluye **OpenAL** (`org.lwjgl.openal.*`). Permite:
- Cargar un buffer de audio desde un WAV.
- Reproducir fuentes (sources) de audio: flap, colisión, punto.

### Dónde llamar los sonidos

| Evento | Dónde llamarlo |
|---|---|
| Sonido de salto | `GameEngine#onSpacePressed()` |
| Sonido de colisión | `GameEngine#actualizar()`, cuando `gameOver = true` |
| Sonido de punto | `GameEngine#actualizar()`, cuando `puntaje` cambia |
| Música de fondo | `GameEngine#run()`, antes del bucle |

---

## 4. Modo 2 Jugadores (Pantalla Dividida — Estilo Mario)

### Concepto: Viewports

OpenGL permite definir **en qué región de la ventana** se dibuja con `glViewport(x, y, ancho, alto)`. Para pantalla dividida:

- **Jugador 1**: `glViewport(0, 0, ANCHO/2, ALTO)` — mitad izquierda.
- **Jugador 2**: `glViewport(ANCHO/2, 0, ANCHO/2, ALTO)` — mitad derecha.

Cada llamada a `glViewport` redefine el NDC para esa región. Así, el mismo código de dibujo "cabe" en su mitad sin calcular nada extra.

### Estructura de archivos nueva / modificada

```
model/
└── Bird.java          ← Sin cambios al modelo; se crean DOS instancias.

core/
└── GameEngine.java    ← Crear bird1, bird2, pipeManager1, pipeManager2.
                          En el bucle: actualizar ambos, dibujar en viewport 1 y viewport 2.

input/
└── InputHandler.java  ← Añadir teclas para el P2 (ej. W o flecha arriba).
```

### Teclas sugeridas

| Jugador | Saltar | Reset |
|---|---|---|
| P1 | `SPACE` | `R` |
| P2 | `W` o `↑` | `T` |

### Archivos que NO necesitan cambios

- `view/Renderer.java` — El cambio de viewport es solo una línea en el bucle.
- `model/PipeManager.java` — Se instancian dos `PipeManager` independientes.
- `core/Constants.java` — Las constantes de física y tamaño son iguales para ambos.

---

## 5. Pantalla de Menú Inicial (1 jugador / 2 jugadores / Salir)

### Concepto: Máquina de Estados de Pantalla

Se añade un enum `GameState` con los estados posibles:

```
MENU → JUGANDO_1P → GAME_OVER_1P → MENU
MENU → JUGANDO_2P → GAME_OVER_2P → MENU
```

### Archivos a crear / modificar

| Archivo | Acción |
|---|---|
| `core/GameState.java` | **CREAR** enum: `MENU`, `PLAYING_1P`, `PLAYING_2P`, `GAME_OVER`. |
| `view/MenuView.java` | **CREAR** Dibuja las opciones del menú usando `dibujarRect` (o texturas). Cada opción es un rectángulo en posición NDC. |
| `core/GameEngine.java` | **Modificar** el bucle: dependiendo del `currentState`, llama a `menuView.render()` o `gameView.render()`. |
| `input/InputHandler.java` | **Modificar** Añadir navegación de menú (flechas arriba/abajo, ENTER para seleccionar). |

### Texto en pantalla (limitación actual)

El proyecto actual **no renderiza texto** (es un requisito costoso en OpenGL puro). Las opciones para el menú son:

1. **Rectángulos de colores** como botones visuales (ya funciona con el código actual).
2. **Texturas de texto prerenderizado** (crear imágenes PNG con el texto y cargarlas como texturas).
3. **Librería de fuentes**: LWJGL incluye `stb_truetype` para renderizar fuentes TrueType directamente en OpenGL.

---

## 6. Estadísticas Dentro del Juego (Bajar del Título de la Ventana)

### Problema actual

El puntaje vive en el título de la ventana (`glfwSetWindowTitle`). Es fácil de implementar pero no escalable para 2 jugadores ni visualmente atractivo.

### Solución: HUD (Heads-Up Display)

Un HUD es una capa de rectángulos y texto que se dibuja **encima** de la escena, en posiciones fijas en NDC.

### Dónde hacerlo

| Archivo | Qué añadir |
|---|---|
| `view/HudView.java` | **CREAR** Métodos `renderPuntaje(int p1, int p2)`, `renderVidas()`, etc. Dibuja rectángulos en las esquinas del NDC. |
| `view/GameView.java` | Llamar a `hudView.render(...)` al final de cada frame, después de tuberías y pájaro. |
| `core/GameEngine.java` | Pasar puntajes al HUD. Eliminar `renderer.actualizarTitulo(...)` o conservarlo como debug. |

### Para 2 jugadores

- **Jugador 1**: Puntaje en la esquina superior izquierda de su viewport.
- **Jugador 2**: Puntaje en la esquina superior derecha de su viewport.
- Se llama a `glViewport(...)` antes de `hudView.renderP1(puntaje1)`, luego se cambia el viewport y se llama `hudView.renderP2(puntaje2)`.

---

## 7. Resumen: Mapa de Responsabilidades Futuras

```
┌──────────────────────────────────────────────────────────────────┐
│                    DÓNDE TOCA CADA MEJORA                        │
├────────────────────┬─────────────────────────────────────────────┤
│ Cambiar colores    │ core/Constants.java (o GameView directamente)│
│ Añadir imágenes    │ view/TextureLoader.java (CREAR)              │
│                    │ view/Renderer.java (shaders UV)              │
│                    │ view/GameView.java (dibujarTextura)          │
│ Añadir sonidos     │ view/SoundManager.java (CREAR)               │
│                    │ core/GameEngine.java (llamadas de eventos)   │
│ 2 jugadores        │ core/GameEngine.java (2 instancias Bird/Pipe)│
│                    │ view/Renderer.java (glViewport)              │
│                    │ input/InputHandler.java (teclas P2)          │
│ Menú inicial       │ core/GameState.java (CREAR enum)             │
│                    │ view/MenuView.java (CREAR)                   │
│                    │ core/GameEngine.java (switch de estado)      │
│ HUD / puntaje      │ view/HudView.java (CREAR)                    │
│ en pantalla        │ view/GameView.java (llamar HUD)              │
│ Texto / fuentes    │ view/FontRenderer.java (CREAR, usa stb_ttf)  │
├────────────────────┴─────────────────────────────────────────────┤
│ NUNCA tocar para estas mejoras:                                  │
│   model/Bird.java · model/PipeManager.java · core/Constants.java │
│   (a menos que cambies la física o las dimensiones del juego)    │
└──────────────────────────────────────────────────────────────────┘
```
