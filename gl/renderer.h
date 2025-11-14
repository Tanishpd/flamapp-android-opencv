#ifndef GL_RENDERER_H
#define GL_RENDERER_H

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <EGL/egl.h>
#include <string>

class GLRendererNative {
public:
    GLRendererNative();
    ~GLRendererNative();
    
    bool initialize();
    void createTexture();
    void updateTexture(const unsigned char* data, int width, int height);
    void render();
    void cleanup();
    
private:
    GLuint createShader(GLenum shaderType, const char* source);
    GLuint createProgram(const char* vertexSource, const char* fragmentSource);
    
    GLuint program;
    GLuint textureId;
    GLuint vbo;
    GLint positionLoc;
    GLint texCoordLoc;
    GLint textureLoc;
    
    static const char* vertexShaderSource;
    static const char* fragmentShaderSource;
};

#endif // GL_RENDERER_H
