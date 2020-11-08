#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"

#include <jni.h>
#include <GLES/gl.h>

JNIEXPORT void JNICALL Java_zame_game_engine_graphics_Renderer_renderTriangles(
        JNIEnv* env,
        jclass type,
        jfloatArray vertexBuffer,
        jfloatArray colorsBuffer,
        jintArray textureBuffer,
        jshortArray indicesBuffer,
        jint indicesBufferPos) {

    jfloat* nativeVertexBuffer = (*env)->GetFloatArrayElements(env, vertexBuffer, NULL);
    jfloat* nativeColorsBuffer = (*env)->GetFloatArrayElements(env, colorsBuffer, NULL);
    jint* nativeTextureBuffer = (textureBuffer == NULL ? NULL : (*env)->GetIntArrayElements(env, textureBuffer, NULL));
    jshort* nativeIndicesBuffer = (*env)->GetShortArrayElements(env, indicesBuffer, NULL);

    glVertexPointer(3, GL_FLOAT, 0, nativeVertexBuffer);
    glColorPointer(4, GL_FLOAT, 0, nativeColorsBuffer);

    if (nativeTextureBuffer != NULL) {
        glTexCoordPointer(2, GL_FIXED, 0, nativeTextureBuffer);
    }

    glDrawElements(GL_TRIANGLES, indicesBufferPos, GL_UNSIGNED_SHORT, nativeIndicesBuffer);

    (*env)->ReleaseShortArrayElements(env, indicesBuffer, nativeIndicesBuffer, 0);

    if (nativeTextureBuffer != NULL) {
        (*env)->ReleaseIntArrayElements(env, textureBuffer, nativeTextureBuffer, 0);
    }

    (*env)->ReleaseFloatArrayElements(env, colorsBuffer, nativeColorsBuffer, 0);
    (*env)->ReleaseFloatArrayElements(env, vertexBuffer, nativeVertexBuffer, 0);
}

JNIEXPORT void JNICALL Java_zame_game_engine_graphics_Renderer_renderLines(
        JNIEnv* env,
        jclass type,
        jfloatArray vertexBuffer,
        jfloatArray colorsBuffer,
        jint vertexCount) {

    jfloat* nativeVertexBuffer = (*env)->GetFloatArrayElements(env, vertexBuffer, NULL);
    jfloat* nativeColorsBuffer = (*env)->GetFloatArrayElements(env, colorsBuffer, NULL);

    glVertexPointer(2, GL_FLOAT, 0, nativeVertexBuffer);
    glColorPointer(4, GL_FLOAT, 0, nativeColorsBuffer);
    glDrawArrays(GL_LINES, 0, vertexCount);

    (*env)->ReleaseFloatArrayElements(env, colorsBuffer, nativeColorsBuffer, 0);
    (*env)->ReleaseFloatArrayElements(env, vertexBuffer, nativeVertexBuffer, 0);
}

#pragma clang diagnostic pop
