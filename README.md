# Flappy Bird (OpenGL / LWJGL en Java)

Un motor gráfico estilo Flappy Bird construido utilizando Java y OpenGL a través de la librería LWJGL. Incluye sistema de texturizado, colisiones y un motor de audio integrado (OpenAL).

## 📋 Requisitos Previos (Windows)
Asegúrate de tener instaladas las siguientes herramientas en tu sistema antes de continuar:
1. **Java Development Kit (JDK)**: Versión 17 o superior.
2. **Apache Maven**: Gestor de dependencias y construcción de proyectos en Java.
3. **Git**: Para clonar el repositorio.

## 🚀 Cómo instalar y correr el proyecto

### 1. Clonar el repositorio
Abre tu terminal (Símbolo del sistema, PowerShell o Git Bash) y ejecuta:
```bash
git clone <URL_DEL_REPOSITORIO>
cd flappyBird
```

### 2. Compilar el código
Una vez dentro de la carpeta del proyecto (`flappyBird`), usa Maven para que se encargue automáticamente de descargar todas las dependencias necesarias (gráficos, sonido, ventanas) y compilar el código:
```bash
mvn clean compile
```

### 3. Ejecutar el juego
Tienes dos opciones principales para iniciar la aplicación:

**Opción A: Desde la consola (con Maven)**
Puedes decirle a Maven que ejecute la clase principal directamente con el siguiente comando:
```bash
mvn exec:java -D"exec.mainClass"="com.game.App"
```

**Opción B: Desde tu IDE (Recomendado)**
1. Abre la carpeta `flappyBird` en tu entorno de desarrollo preferido (Visual Studio Code, IntelliJ IDEA o Eclipse).
2. Espera unos segundos a que el IDE reconozca el archivo `pom.xml` e importe las dependencias.
3. Dirígete al archivo principal en la ruta: `src/main/java/com/game/App.java`.
4. Ejecuta el método `main` (haciendo clic en "Run").

## 🎮 Controles Básicos
- **W / Flecha Arriba**: Subir en el menú.
- **S / Flecha Abajo**: Bajar en el menú.
- **Barra Espaciadora**: Confirmar selección (Menú) / Aletear (Durante el juego).
- **R**: Volver al menú principal (Al perder).
- **ESC**: Salir del juego en cualquier momento.
