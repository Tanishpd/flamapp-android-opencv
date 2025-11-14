#include "renderer.h"
#include <android/log.h>
#include <cstring>

#define LOG_TAG "GLRenderer"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

const char* GLRendererNative::vertexShaderSource = R"(
attribute vec4 aPosition;
attribute vec2 aTexCoord;
varying vec2 vTexCoord;

void main() {
    gl_Position = aPosition;
    vTexCoord = aTexCoord;
}
)";

const char* GLRendererNative::fragmentShaderSource = R"(
precision mediump float;
varying vec2 vTexCoord;
uniform sampler2D uTexture;

void main() {
    gl_FragColor = texture2D(uTexture, vTexCoord);
}
)";

GLRendererNative::GLRendererNative() 
    : program(0), textureId(0), vbo(0), 
      positionLoc(-1), texCoordLoc(-1), textureLoc(-1) {
}

GLRendererNative::~GLRendererNative() {
    cleanup();
}

bool GLRendererNative::initialize() {
    // Create shader program
    program = createProgram(vertexShaderSource, fragmentShaderSource);
    if (program == 0) {
        LOGE("Failed to create shader program");
        return false;
    }
    
    // Get attribute and uniform locations
    positionLoc = glGetAttribLocation(program, "aPosition");
    texCoordLoc = glGetAttribLocation(program, "aTexCoord");
    textureLoc = glGetUniformLocation(program, "uTexture");
    
    if (positionLoc < 0 || texCoordLoc < 0 || textureLoc < 0) {
        LOGE("Failed to get shader locations");
        return false;
    }
    
    // Create VBO for vertices and texture coordinates
    GLfloat vertices[] = {
        // Position      // TexCoords
        -1.0f, -1.0f,    0.0f, 1.0f,
         1.0f, -1.0f,    1.0f, 1.0f,
        -1.0f,  1.0f,    0.0f, 0.0f,
         1.0f,  1.0f,    1.0f, 0.0f
    };
    
    glGenBuffers(1, &vbo);
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    
    createTexture();
    
    LOGD("GL Renderer initialized successfully");
    return true;
}

void GLRendererNative::createTexture() {
    glGenTextures(1, &textureId);
    glBindTexture(GL_TEXTURE_2D, textureId);
    
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    
    glBindTexture(GL_TEXTURE_2D, 0);
    
    LOGD("Texture created: %u", textureId);
}

void GLRendererNative::updateTexture(const unsigned char* data, int width, int height) {
    if (data == nullptr || width <= 0 || height <= 0) {
        LOGE("Invalid texture data");
        return;
    }
    
    glBindTexture(GL_TEXTURE_2D, textureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, 
                 GL_RGBA, GL_UNSIGNED_BYTE, data);
    glBindTexture(GL_TEXTURE_2D, 0);
}

void GLRendererNative::render() {
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);
    
    glUseProgram(program);
    
    // Bind texture
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, textureId);
    glUniform1i(textureLoc, 0);
    
    // Bind VBO
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    
    // Set vertex attributes
    glEnableVertexAttribArray(positionLoc);
    glVertexAttribPointer(positionLoc, 2, GL_FLOAT, GL_FALSE, 
                         4 * sizeof(GLfloat), (void*)0);
    
    glEnableVertexAttribArray(texCoordLoc);
    glVertexAttribPointer(texCoordLoc, 2, GL_FLOAT, GL_FALSE, 
                         4 * sizeof(GLfloat), (void*)(2 * sizeof(GLfloat)));
    
    // Draw
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    
    // Cleanup
    glDisableVertexAttribArray(positionLoc);
    glDisableVertexAttribArray(texCoordLoc);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindTexture(GL_TEXTURE_2D, 0);
}

void GLRendererNative::cleanup() {
    if (textureId != 0) {
        glDeleteTextures(1, &textureId);
        textureId = 0;
    }
    
    if (vbo != 0) {
        glDeleteBuffers(1, &vbo);
        vbo = 0;
    }
    
    if (program != 0) {
        glDeleteProgram(program);
        program = 0;
    }
    
    LOGD("GL Renderer cleaned up");
}

GLuint GLRendererNative::createShader(GLenum shaderType, const char* source) {
    GLuint shader = glCreateShader(shaderType);
    if (shader == 0) {
        LOGE("Failed to create shader");
        return 0;
    }
    
    glShaderSource(shader, 1, &source, nullptr);
    glCompileShader(shader);
    
    GLint compiled;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    
    if (!compiled) {
        GLint infoLen = 0;
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
        
        if (infoLen > 1) {
            char* infoLog = new char[infoLen];
            glGetShaderInfoLog(shader, infoLen, nullptr, infoLog);
            LOGE("Shader compilation failed: %s", infoLog);
            delete[] infoLog;
        }
        
        glDeleteShader(shader);
        return 0;
    }
    
    return shader;
}

GLuint GLRendererNative::createProgram(const char* vertexSource, const char* fragmentSource) {
    GLuint vertexShader = createShader(GL_VERTEX_SHADER, vertexSource);
    if (vertexShader == 0) {
        return 0;
    }
    
    GLuint fragmentShader = createShader(GL_FRAGMENT_SHADER, fragmentSource);
    if (fragmentShader == 0) {
        glDeleteShader(vertexShader);
        return 0;
    }
    
    GLuint program = glCreateProgram();
    if (program == 0) {
        LOGE("Failed to create program");
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        return 0;
    }
    
    glAttachShader(program, vertexShader);
    glAttachShader(program, fragmentShader);
    glLinkProgram(program);
    
    GLint linked;
    glGetProgramiv(program, GL_LINK_STATUS, &linked);
    
    if (!linked) {
        GLint infoLen = 0;
        glGetProgramiv(program, GL_INFO_LOG_LENGTH, &infoLen);
        
        if (infoLen > 1) {
            char* infoLog = new char[infoLen];
            glGetProgramInfoLog(program, infoLen, nullptr, infoLog);
            LOGE("Program linking failed: %s", infoLog);
            delete[] infoLog;
        }
        
        glDeleteProgram(program);
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        return 0;
    }
    
    glDeleteShader(vertexShader);
    glDeleteShader(fragmentShader);
    
    LOGD("Program created successfully: %u", program);
    return program;
}
