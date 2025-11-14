package com.flamapp;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer {
    
    private Context context;
    private int program;
    private int textureId;
    
    private FloatBuffer vertexBuffer;
    private FloatBuffer texCoordBuffer;
    
    private int positionHandle;
    private int texCoordHandle;
    private int textureHandle;
    
    private ByteBuffer imageData;
    private int imageWidth;
    private int imageHeight;
    private boolean needsTextureUpdate = false;
    
    private static final float[] VERTICES = {
        -1.0f, -1.0f,
         1.0f, -1.0f,
        -1.0f,  1.0f,
         1.0f,  1.0f
    };
    
    private static final float[] TEX_COORDS = {
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f
    };
    
    private static final String VERTEX_SHADER =
        "attribute vec4 aPosition;\n" +
        "attribute vec2 aTexCoord;\n" +
        "varying vec2 vTexCoord;\n" +
        "void main() {\n" +
        "    gl_Position = aPosition;\n" +
        "    vTexCoord = aTexCoord;\n" +
        "}\n";
    
    private static final String FRAGMENT_SHADER =
        "precision mediump float;\n" +
        "varying vec2 vTexCoord;\n" +
        "uniform sampler2D uTexture;\n" +
        "void main() {\n" +
        "    gl_FragColor = texture2D(uTexture, vTexCoord);\n" +
        "}\n";
    
    public GLRenderer(Context context) {
        this.context = context;
        
        vertexBuffer = ByteBuffer.allocateDirect(VERTICES.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTICES);
        vertexBuffer.position(0);
        
        texCoordBuffer = ByteBuffer.allocateDirect(TEX_COORDS.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEX_COORDS);
        texCoordBuffer.position(0);
    }
    
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord");
        textureHandle = GLES20.glGetUniformLocation(program, "uTexture");
        
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureId = textures[0];
        
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }
    
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }
    
    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        
        if (needsTextureUpdate && imageData != null) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 
                    imageWidth, imageHeight, 0, GLES20.GL_RGBA, 
                    GLES20.GL_UNSIGNED_BYTE, imageData);
            needsTextureUpdate = false;
        }
        
        GLES20.glUseProgram(program);
        
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(textureHandle, 0);
        
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        
        GLES20.glEnableVertexAttribArray(texCoordHandle);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }
    
    public synchronized void updateTexture(byte[] data, int width, int height) {
        if (data != null && data.length > 0) {
            imageData = ByteBuffer.allocateDirect(data.length)
                    .order(ByteOrder.nativeOrder())
                    .put(data);
            imageData.position(0);
            imageWidth = width;
            imageHeight = height;
            needsTextureUpdate = true;
        }
    }
    
    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
