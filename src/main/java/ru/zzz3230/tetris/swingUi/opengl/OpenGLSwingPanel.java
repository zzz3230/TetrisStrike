package ru.zzz3230.tetris.swingUi.opengl;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.TextureData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zzz3230.tetris.model.gameplay.GameplayContext;
import ru.zzz3230.tetris.model.gameplay.GameplayEventType;
import ru.zzz3230.tetris.model.gameplay.GameplayField;
import ru.zzz3230.tetris.model.gameplay.eventsData.LinesClearedEventData;
import ru.zzz3230.tetris.model.gameplay.eventsData.MergeEventData;
import ru.zzz3230.tetris.model.gameplay.eventsData.NotifyEventData;
import ru.zzz3230.tetris.utils.Utils;

import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.Texture;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

public class OpenGLSwingPanel extends JPanel implements GLEventListener {
    private static final Logger log = LoggerFactory.getLogger(OpenGLSwingPanel.class);
    private final int rows;
    private final int columns;
    private GLCanvas canvas;
    private int shaderProgram;
    private int vao;

    private int texturesID;
    private int timeID;
    private int resolutionID;

    private int texCellID;
    private int texPatternID;
    private int texFailID;

    private int failStartID;

    private int waveStartCellID;
    private int waveStartID;
    private int waveColorID;

    private int destroyStartCellsID;
    private int destroyStartID;
    private int destroyFadeTimeID;

    private int texFallingID;

    private final long startTime = System.currentTimeMillis();

    private Texture patternTexture;
    private Texture texture1;
    private Texture fallingTexture;
    private Texture failTexture;


    private void loadShaders(GL3 gl){
        // Компиляция шейдеров
        int vertexShader = createShader(gl, GL3.GL_VERTEX_SHADER, """
            #version 300 es
            
            precision highp float;
            precision highp sampler2D;
            layout (location = 0) in vec2 aPos;
             layout (location = 1) in vec2 aTexCoord;
        
             out vec2 uv;
        
             void main() {
                 gl_Position = vec4(aPos, 0.0, 1.0);
                 uv = aTexCoord;
             }
       """);

        int fragmentShader = 0;
        try {
            fragmentShader = createShader(gl, GL3.GL_FRAGMENT_SHADER,
                    Objects.requireNonNull(Utils.getResourceFileAsString("shaders/DefaultFragmentShader.glsl"))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Создание и линковка шейдерной программы
        shaderProgram = gl.glCreateProgram();
        gl.glAttachShader(shaderProgram, vertexShader);
        gl.glAttachShader(shaderProgram, fragmentShader);
        gl.glLinkProgram(shaderProgram);

        // Проверка ошибок линковки
        int[] linkStatus = new int[1];
        gl.glGetProgramiv(shaderProgram, GL3.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == GL.GL_FALSE) {
            System.err.println("Linking error!");
            System.err.println(gl.glGetError());
            byte[] log = new byte[512];
            gl.glGetProgramInfoLog(shaderProgram, log.length, (int[]) null, 0, log, 0);
            System.err.println(new String(log));
            return;
        }

        // Освобождаем шейдеры после линковки
        gl.glDeleteShader(vertexShader);
        gl.glDeleteShader(fragmentShader);
    }

    private void loadShaderVariables(GL3 gl){
        texCellID = gl.glGetUniformLocation(shaderProgram, "u_texture_0");
        texPatternID = gl.glGetUniformLocation(shaderProgram, "u_texture_1");
        texFallingID = gl.glGetUniformLocation(shaderProgram, "u_texture_2");

        texFailID = gl.glGetUniformLocation(shaderProgram, "u_texture_fail");
        failStartID = gl.glGetUniformLocation(shaderProgram, "u_failStart");


        resolutionID = gl.glGetUniformLocation(shaderProgram, "u_resolution");
        timeID = gl.glGetUniformLocation(shaderProgram, "u_time");

        waveStartCellID = gl.glGetUniformLocation(shaderProgram, "u_waveStartCell");
        waveStartID = gl.glGetUniformLocation(shaderProgram, "u_waveStart");
        waveColorID = gl.glGetUniformLocation(shaderProgram, "u_waveColor");

        destroyStartCellsID = gl.glGetUniformLocation(shaderProgram, "u_destroyStartCells");
        destroyStartID = gl.glGetUniformLocation(shaderProgram, "u_destroyStart");
        destroyFadeTimeID = gl.glGetUniformLocation(shaderProgram, "u_destroyFadeTime");

        if(destroyStartCellsID == -1 || destroyStartID == -1 || destroyFadeTimeID == -1){
            throw new RuntimeException("Failed to init Shader parameters");
        }

    }

    private void initVertex(GL3 gl){
        // Вершинные данные (позиции и цвета)
        float[] vertices = {
                -1.0f, -1.0f,  // Нижний левый
                3, -1.0f,  // Нижний правый (больше 1.0)
                -1.0f,  3.0f  // Верхний левый (больше 1.0)
        };

        float[] fullscreenQuad = {
                //  X      Y      U    V
                -1.0f, -1.0f,  0.0f, 0.0f,  // Левый нижний угол
                1.0f, -1.0f,  1.0f, 0.0f,  // Правый нижний угол
                1.0f,  1.0f,  1.0f, 1.0f,  // Правый верхний угол
                -1.0f,  1.0f,  0.0f, 1.0f   // Левый верхний угол
        };
        int[] indices = {0, 1, 2, 2, 3, 0};

        // Создание VAO (Vertex Array Object)
        int[] vaoArray = new int[1];
        gl.glGenVertexArrays(1, vaoArray, 0);
        vao = vaoArray[0];
        gl.glBindVertexArray(vao);

        // Создание VBO (Vertex Buffer Object)
        int[] vboArray = new int[1];
        gl.glGenBuffers(1, vboArray, 0);
        int vbo = vboArray[0];
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vbo);
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, fullscreenQuad.length * 4, FloatBuffer.wrap(fullscreenQuad), GL3.GL_STATIC_DRAW);


        int[] ebo = new int[1];
        gl.glGenBuffers(1, ebo, 0);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, ebo[0]);
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, indices.length * Integer.BYTES, IntBuffer.wrap(indices), GL.GL_STATIC_DRAW);

        // Включаем атрибут для координат позиции (location = 0)
        gl.glVertexAttribPointer(0, 2, GL.GL_FLOAT, false, 4 * Float.BYTES, 0);
        gl.glEnableVertexAttribArray(0);

// Включаем атрибут для текстурных координат (location = 1)
        gl.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        gl.glEnableVertexAttribArray(1);
    }

    private Texture loadTexture(String assetName){
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try {
            var stream = classLoader.getResourceAsStream(assetName);
            if(stream == null){
                throw new RuntimeException("Failed to load asset: " + assetName);
            }
            return TextureIO.newTexture(stream, false, "png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadTextures(){
        patternTexture = loadTexture("assets/tetris_gamestate.png");
        fallingTexture = loadTexture("assets/tetris_gamestate.png");

        texture1 = loadTexture("assets/cell.png");

        failTexture = loadTexture("assets/fail.png");
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();

        gl.glClearColor(0.1f, 0.1f, 0.1f, .0f);

        loadShaders(gl);

        initVertex(gl);

        loadShaderVariables(gl);

        loadTextures();

        System.out.println("Finished init");

        gl.glBindVertexArray(0);
    }

    private void updateFieldTexture(GL3 gl, GameplayField.GameplayFieldFragment field, Texture texture){
        int width = field.getCols();
        int height = field.getRows();

        byte[] buffer1 = new byte[width * height * 4];
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                var color = field.getCell(y, x).getColor();
                if(color != null){
                    //buffer.setRGB(x, height - y - 1, color.getRGB());
                    int basePos = ((height - y - 1) * width + x) * 4;
                    buffer1[basePos + 0] = (byte) color.getRed();
                    buffer1[basePos + 1] = (byte) color.getGreen();
                    buffer1[basePos + 2] = (byte) color.getBlue();
                    buffer1[basePos + 3] = (byte) color.getAlpha();
                }
            }
        }

        texture.updateImage(gl,
                new TextureData(
                        gl.getGLProfile(),
                        GL3.GL_RGBA,
                        width,
                        height,
                        0,
                        GL3.GL_RGBA,
                        GL3.GL_UNSIGNED_BYTE,
                        false,
                        false,
                        false,
                        ByteBuffer.wrap(buffer1),
                        null
                )
        );
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();
        gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);

        if(!fieldData.isEmpty()){

            updateFieldTexture(gl, fieldData.getFirst().gameplayField().getStaticBlocks(), patternTexture);
            updateFieldTexture(gl, fieldData.getFirst().gameplayField().getFallingBlock(), fallingTexture);

            while(!fieldData.isEmpty()){
                var dat = fieldData.pop();
                if(dat.eventData() instanceof MergeEventData(Point mergePoint, Color color)){
                    gl.glUniform2f(waveStartCellID, mergePoint.y, dat.gameplayField().getRows() - mergePoint.x - 1);
                    gl.glUniform1f(waveStartID, (System.currentTimeMillis() - startTime) / 1000.0f);
                    gl.glUniform4f(waveColorID,
                            color.getRed() / 256.0f,
                            color.getGreen() / 256.0f,
                            color.getBlue() / 256.0f,
                            color.getAlpha() / 256.0f
                    );
                }
                else if(dat.eventData() instanceof LinesClearedEventData(int[] indexes)){
                    gl.glUniform1f(destroyStartID, (System.currentTimeMillis() - startTime) / 1000.0f);
                    //gl.glUniform1f(destroyFadeTimeID, 1.0f);
                    gl.glUniform1f(destroyFadeTimeID, dat.iterationDelay() * 2);

                    float[] data = new float[8];
                    Arrays.fill(data, -1f);

                     if(indexes.length > 4){
                         log.error("Too many indexes, max 4");
                     }

                    int elementsToFill = Math.min(indexes.length, 4);
                    for(int row = 0; row < elementsToFill; row++) {
                        data[row] = rows - indexes[row] - 1;
                    }

                    gl.glUniform4f(destroyStartCellsID, data[0], data[1], data[2], data[3]);
                }
                else if(dat.eventData() instanceof NotifyEventData(GameplayEventType type)){
                    if(type == GameplayEventType.STATIC_BLOCKS_MOVED){
                        gl.glUniform1f(destroyStartID, -1); // Disable destroy animation
                    }
                    else if(type == GameplayEventType.GAME_OVER){
                        gl.glUniform1f(failStartID, (System.currentTimeMillis() - startTime) / 1000.0f);
                        gl.glUniform1f(destroyStartID, -1); // Disable destroy animation
                    }
                }
            }

        }


        gl.glActiveTexture(GL3.GL_TEXTURE0);
        texture1.bind(gl);
        gl.glUniform1i(texCellID, 0);

        gl.glActiveTexture(GL3.GL_TEXTURE1);
        patternTexture.bind(gl);
        gl.glUniform1i(texPatternID, 1);

        gl.glActiveTexture(GL3.GL_TEXTURE2);
        fallingTexture.bind(gl);
        gl.glUniform1i(texFallingID, 2);

        gl.glActiveTexture(GL3.GL_TEXTURE3);
        failTexture.bind(gl);
        gl.glUniform1i(texFailID, 3);

        gl.glUseProgram(shaderProgram);
        gl.glUniform1f(timeID,  (System.currentTimeMillis() - startTime) / 1000.0f);
        gl.glUniform2f(resolutionID, width, height);

        gl.glBindVertexArray(vao);
        gl.glDrawElements(GL.GL_TRIANGLES, 6, GL.GL_UNSIGNED_INT, 0);
        gl.glBindVertexArray(0);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL3 gl = drawable.getGL().getGL3();
        gl.glDeleteProgram(shaderProgram);
    }

    int width;
    int height;
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL3 gl = drawable.getGL().getGL3();

        Component canvas = (Component) drawable;

        double widthCoef = canvas.getGraphicsConfiguration().getDefaultTransform().getScaleY();
        double heightCoef = canvas.getGraphicsConfiguration().getDefaultTransform().getScaleX();

        gl.glViewport(0, 0, (int)(width * widthCoef), (int)(height * heightCoef));
        this.width = width;
        this.height = height;
    }

    private int createShader(GL3 gl, int type, String source) {
        source = source.replace("\r", "");
        int shader = gl.glCreateShader(type);
        String[] sources = new String[]{ source };
        int[] lengths = new int[]{ source.length() * 2 };
        gl.glShaderSource(shader, 1, sources, lengths, 0);
        gl.glCompileShader(shader);

        // Проверка ошибок компиляции
        int[] compiled = new int[1];
        gl.glGetShaderiv(shader, GL3.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == GL.GL_FALSE) {
            System.err.println("Compiling error! ");
            System.err.println(gl.glGetError());
        }

        byte[] log = new byte[512];
        gl.glGetShaderInfoLog(shader, log.length, (int[]) null, 0, log, 0);

        System.out.println("Shader log: " + new String(log));
        return shader;
    }
    public GLJPanel panel;
    public OpenGLSwingPanel(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;

        SwingUtilities.invokeLater(() -> {
            GLProfile.initSingleton();

            GLProfile profile = GLProfile.getGL2GL3();
            System.out.println(profile.getName());
            GLCapabilities capabilities = new GLCapabilities(profile);
            canvas = new GLCanvas(capabilities);
            canvas.addGLEventListener(this);

            setLayout(new BorderLayout());

            add(canvas);
            rootAnimator = new FPSAnimator(canvas, 60);
            rootAnimator.start();
        });
    }
    FPSAnimator rootAnimator;

    public void dispose(){
        rootAnimator.stop();
        canvas.destroy();
    }

    private final ConcurrentLinkedDeque<GameplayContext> fieldData = new ConcurrentLinkedDeque<>();

    public void setFieldData(GameplayContext context) {
        fieldData.add(context);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        //canvas.repaint();
    }
}