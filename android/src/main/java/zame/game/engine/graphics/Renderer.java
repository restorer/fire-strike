package zame.game.engine.graphics;

import android.graphics.Bitmap;
import android.opengl.GLUtils;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11ExtensionPack;

import zame.game.core.util.Common;
import zame.game.engine.Config;
import zame.game.engine.Engine;
import zame.game.engine.EngineObject;

// https://stackoverflow.com/questions/1848886/jni-c-library-passing-byte-ptr

// Native buffers (aka ByteBuffer, ShortBuffer and FloatBuffer) suck in DalvikVM. It is terribly slow.
// So native code used to render. It's up to 4x faster than java code with native buffers.

// According to qualcomm docs, you need to glclear after every glbindframebuffer,
// this is a problem related to tiled architecture, if you are switching framebuffers,
// data need to get copied from fastmem to normal memory to save current framebuffer
// and from slowmem to fast mem to get contents of newly binded frame, in case you are
// clearing just after glbind no data is copied from slowmem to fastmem and you are saving time,
// but you need to redesign your render pipeline often, so it will avoid reading data back
// and forth between slow and fast memory, so try to do glclear after each bind

@SuppressWarnings("MagicNumber")
public class Renderer implements EngineObject {
    private static final int MAX_QUADS = 64 * 64 * 2; // (64*64*2 * (12*4 + 16*4 + 8*4 + 6*2 + 4*4 + 8*4))

    private static final int SIZE_RTT_FBO = 512;
    private static final int SIZE_RTT_FALLBACK = 256;

    public static final int SIZE_TILE = 128;
    private static final int SIZE_TILE_MON = 256;

    private static final int TEX_CELL_X = ((SIZE_TILE + 2) << 16) / 2048;
    private static final int TEX_CELL_Y = ((SIZE_TILE + 2) << 16) / 2048;
    private static final int TEX_MON_CELL_X = ((SIZE_TILE_MON + 2) << 16) / 2048;
    private static final int TEX_MON_CELL_Y = ((SIZE_TILE_MON + 2) << 16) / 2048;
    private static final int TEX_1PX_X = (1 << 16) / 2048;
    private static final int TEX_1PX_Y = (1 << 16) / 2048;
    private static final int TEX_SIZE_X = (SIZE_TILE << 16) / 2048;
    private static final int TEX_SIZE_Y = (SIZE_TILE << 16) / 2048;
    private static final int TEX_MON_SIZE_X = (SIZE_TILE_MON << 16) / 2048;
    private static final int TEX_MON_SIZE_Y = (SIZE_TILE_MON << 16) / 2048;
    private static final int TEX_SIZE_X_D2 = ((SIZE_TILE / 2) << 16) / 2048;
    private static final int TEX_SIZE_Y_D2 = ((SIZE_TILE / 2) << 16) / 2048;
    private static final int TEX_SIZE_X_D2_1PX = TEX_SIZE_X_D2 + TEX_1PX_X;
    private static final int TEX_SIZE_Y_D2_1PX = TEX_SIZE_Y_D2 + TEX_1PX_Y;
    private static final int TEX_SIZE_X_X2 = ((SIZE_TILE * 2) << 16) / 2048;
    private static final int TEX_SIZE_Y_X2 = ((SIZE_TILE * 2) << 16) / 2048;
    private static final int TEX_EXTR_X_FROM = ((SIZE_TILE / 2 - SIZE_TILE / 16 / 2) << 16) / 2048;
    private static final int TEX_EXTR_X_SIZE = ((SIZE_TILE / 16) << 16) / 2048;
    // private static final int TEX_SIZE_X_X4 = ((SIZE_TILE * 4) << 16) / 2048;

    // ----

    public static final int TEXTURE_LOADING = 0;
    static final int TEXTURE_LABELS = TEXTURE_LOADING + 1;
    public static final int TEXTURE_MAIN = TEXTURE_LABELS + 1;
    public static final int TEXTURE_KNIFE = TEXTURE_MAIN + 1;
    public static final int TEXTURE_PISTOL = TEXTURE_KNIFE + 4;
    public static final int TEXTURE_DBLPISTOL = TEXTURE_PISTOL + 4;
    public static final int TEXTURE_SHTG = TEXTURE_DBLPISTOL + 4;
    public static final int TEXTURE_AK47 = TEXTURE_SHTG + 5;
    public static final int TEXTURE_TMP = TEXTURE_AK47 + 4;
    public static final int TEXTURE_GRENADE = TEXTURE_TMP + 4;
    public static final int TEXTURE_MONSTERS = TEXTURE_GRENADE + 8;
    public static final int TEXTURE_SKY = TEXTURE_MONSTERS + 2;
    public static final int TEXTURE_RTT = TEXTURE_SKY + 1;
    private static final int TEXTURE_LAST = TEXTURE_RTT + 1;

    // ----

    public static final int FLAG_CULL = 1;
    public static final int FLAG_DEPTH = 2;
    public static final int FLAG_ALPHA = 4;
    public static final int FLAG_ALPHA_LOWER = 8;
    public static final int FLAG_BLEND = 16;
    public static final int FLAG_BLEND_GAMMA = 32;
    public static final int FLAG_SMOOTH = 64;
    public static final int FLAG_STENCIL_REPLACE = 128;
    public static final int FLAG_STENCIL_KEEP = 256;
    public static final int FLAG_TEX_REPEAT = 512;

    private static final int MASK_FLAG_TEX = FLAG_TEX_REPEAT;

    // ----

    private Engine engine;
    private Config config;

    public GL10 gl;
    private GL11ExtensionPack gl11ep;
    @SuppressWarnings("BooleanVariableAlwaysNegated") private boolean isFboSupported;
    private boolean isTexturesCreated;
    private final int[] textures = new int[TEXTURE_LAST];
    private boolean isFramebufferPrepared;
    private int lastRenderFlags = -1;
    private int lastTexture = -1;
    private int lastTextureFlags = -1;
    private final int[] frameBuffers = new int[1];
    private final int[] depthBuffers = new int[1];

    private final float[] vertexBuffer = new float[MAX_QUADS * 12];
    private final float[] colorsBuffer = new float[MAX_QUADS * 16];
    private final int[] textureBuffer = new int[MAX_QUADS * 8];
    private final short[] indicesBuffer = new short[MAX_QUADS * 6];
    private final float[] lineVertexBuffer = new float[MAX_QUADS * 4];
    private final float[] lineColorsBuffer = new float[MAX_QUADS * 8];

    private short vertexCount;
    private short lineVertexCount;

    private int vertexBufferPos;
    private int colorsBufferPos;
    private int textureBufferPos;
    private int indicesBufferPos;
    private int lineVertexBufferPos;
    private int lineColorsBufferPos;

    // ----

    public float x1;
    public float y1;
    public float z1;
    public int u1;
    public int v1;
    public float r1;
    public float g1;
    public float b1;
    public float a1;

    public float x2;
    public float y2;
    public float z2;
    public int u2;
    public int v2;
    public float r2;
    public float g2;
    public float b2;
    public float a2;

    public float x3;
    public float y3;
    public float z3;
    private int u3;
    public int v3;
    public float r3;
    public float g3;
    public float b3;
    public float a3;

    public float x4;
    public float y4;
    public float z4;
    private int u4;
    public int v4;
    public float r4;
    public float g4;
    public float b4;
    public float a4;

    // ----

    static {
        System.loadLibrary("renderer");
    }

    private static native void renderTriangles(
            float[] vertexBuf,
            float[] colorsBuf,
            int[] textureBuf,
            short[] indicesBuf,
            int indicesBufPos);

    private static native void renderLines(float[] vertexBuf, float[] colorsBuf, int vertexCount);

    // ----

    @Override
    public void onCreate(Engine engine) {
        this.engine = engine;
        this.config = engine.config;
    }

    public void onSurfaceCreated(GL10 gl) {
        onSurfaceChanged(gl);

        if (isTexturesCreated) {
            gl.glDeleteTextures(TEXTURE_LAST, textures, 0);
        }

        gl.glGenTextures(TEXTURE_LAST, textures, 0);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        isTexturesCreated = true;
        isFboSupported = ((" " + gl.glGetString(GL10.GL_EXTENSIONS) + " ").contains(" GL_OES_framebuffer_object "));
    }

    public void onSurfaceChanged(GL10 gl) {
        onDrawFrame(gl);
        isFramebufferPrepared = false;
    }

    public void onDrawFrame(GL10 gl) {
        this.gl = gl;
        this.gl11ep = (GL11ExtensionPack)gl;

        lastRenderFlags = -1;
        lastTexture = -1;
        lastTextureFlags = -1;
    }

    public void prepareFramebuffer() {
        if (!isFboSupported || gl11ep == null) {
            return;
        }

        bindTexture(TEXTURE_RTT);

        gl.glTexImage2D(
                GL10.GL_TEXTURE_2D,
                0,
                GL10.GL_RGBA,
                SIZE_RTT_FBO,
                SIZE_RTT_FBO,
                0,
                GL10.GL_RGBA,
                GL10.GL_UNSIGNED_BYTE,
                null);

        // ----

        gl11ep.glGenFramebuffersOES(1, frameBuffers, 0);
        gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, frameBuffers[0]);
        clear();

        gl11ep.glGenRenderbuffersOES(1, depthBuffers, 0);
        gl11ep.glBindRenderbufferOES(GL11ExtensionPack.GL_RENDERBUFFER_OES, depthBuffers[0]);

        gl11ep.glRenderbufferStorageOES(
                GL11ExtensionPack.GL_RENDERBUFFER_OES,
                GL11ExtensionPack.GL_DEPTH_COMPONENT16,
                SIZE_RTT_FBO,
                SIZE_RTT_FBO);

        gl11ep.glFramebufferRenderbufferOES(
                GL11ExtensionPack.GL_FRAMEBUFFER_OES,
                GL11ExtensionPack.GL_DEPTH_ATTACHMENT_OES,
                GL11ExtensionPack.GL_RENDERBUFFER_OES,
                depthBuffers[0]);

        gl11ep.glFramebufferTexture2DOES(
                GL11ExtensionPack.GL_FRAMEBUFFER_OES,
                GL11ExtensionPack.GL_COLOR_ATTACHMENT0_OES,
                GL10.GL_TEXTURE_2D,
                textures[TEXTURE_RTT],
                0);

        if (gl11ep.glCheckFramebufferStatusOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES)
                != GL11ExtensionPack.GL_FRAMEBUFFER_COMPLETE_OES) {

            Bitmap img = Common.createBitmap(SIZE_RTT_FALLBACK,
                    SIZE_RTT_FALLBACK, "Can't alloc bitmap for render buffer");

            bitmapToTexture(textures[TEXTURE_RTT], img);
            img.recycle();

            //noinspection UnusedAssignment
            img = null;

            return;
        }

        isFramebufferPrepared = true;

        gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);
        clear();
    }

    public int getRenderToTextureSize() {
        return (isFramebufferPrepared ? SIZE_RTT_FBO : SIZE_RTT_FALLBACK);
    }

    public void useViewport(int width, int height) {
        gl.glViewport(0, 0, width, height);
    }

    public void clear() {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT | GL10.GL_STENCIL_BUFFER_BIT);
    }

    void bitmapToTexture(int tex, Bitmap bitmap) {
        bindTexture(tex);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
    }

    public void startRenderToTexture() {
        if (isFramebufferPrepared) {
            gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, frameBuffers[0]);
        }

        clear();
    }

    public void finishRenderToTexture(int width, int height) {
        if (isFramebufferPrepared) {
            gl11ep.glBindFramebufferOES(GL11ExtensionPack.GL_FRAMEBUFFER_OES, 0);
            clear();
        } else {
            bindTexture(TEXTURE_RTT);
            gl.glCopyTexImage2D(GL10.GL_TEXTURE_2D, 0, GL10.GL_RGB, 0, 0, width, height, 0);
        }
    }

    public void useOrtho(
            float left,
            float right,
            float bottom,
            float top,
            float near,
            float far) {

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        if (!engine.inWallpaperMode && config.rotateScreen) {
            gl.glOrthof(right, left, top, bottom, near, far);
        } else {
            gl.glOrthof(left, right, bottom, top, near, far);
        }

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public void useFrustum(
            float left,
            float right,
            float bottom,
            float top,
            @SuppressWarnings("SameParameterValue") float near,
            @SuppressWarnings("SameParameterValue") float far) {

        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        if (!engine.inWallpaperMode && config.rotateScreen) {
            gl.glFrustumf(right, left, top, bottom, near, far);
        } else {
            gl.glFrustumf(left, right, bottom, top, near, far);
        }
    }

    public void pushModelviewMatrix() {
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glPushMatrix();
    }

    public void popModelviewMatrix() {
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glPopMatrix();
    }

    public void startBatch() {
        vertexCount = 0;
        lineVertexCount = 0;

        vertexBufferPos = 0;
        colorsBufferPos = 0;
        textureBufferPos = 0;
        indicesBufferPos = 0;
        lineVertexBufferPos = 0;
        lineColorsBufferPos = 0;
    }

    public void renderBatch(int flags) {
        renderBatch(flags, -1);
    }

    public void renderBatch(int flags, int tex) {
        applyRenderFlags(flags);
        applyTexture(tex, flags);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        if (indicesBufferPos != 0) {
            if (tex >= 0) {
                gl.glEnable(GL10.GL_TEXTURE_2D);
                gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            } else {
                gl.glDisable(GL10.GL_TEXTURE_2D);
                gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
            }

            renderTriangles(
                    vertexBuffer,
                    colorsBuffer,
                    (tex >= 0 ? textureBuffer : null),
                    indicesBuffer,
                    indicesBufferPos);
        }

        if (lineVertexCount != 0) {
            gl.glDisable(GL10.GL_TEXTURE_2D);
            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

            renderLines(lineVertexBuffer, lineColorsBuffer, lineVertexCount);
        }
    }

    private void applyTexture(int tex, int flags) {
        int textureFlags = (flags & MASK_FLAG_TEX);

        if (tex != lastTexture || textureFlags != lastTextureFlags) {
            bindTexture(tex, textureFlags);
        }
    }

    private void bindTexture(int tex) {
        bindTexture(tex, -1);
    }

    private void bindTexture(int tex, int flags) {
        if (tex < 0) {
            lastTexture = -1;
            lastTextureFlags = -1;
            return;
        }

        gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[tex]);

        if (flags >= 0) {
            int wrapValue = ((flags & FLAG_TEX_REPEAT) != 0) ? GL10.GL_REPEAT : GL10.GL_CLAMP_TO_EDGE;

            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, wrapValue);
            gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, wrapValue);

            lastTexture = tex;
            lastTextureFlags = flags;
        } else {
            lastTexture = -1;
            lastTextureFlags = -1;
        }
    }

    private void applyRenderFlags(int flags) {
        if (flags == lastRenderFlags) {
            return;
        }

        boolean useCullFace = (flags & FLAG_CULL) != 0;
        boolean useDepthTest = (flags & FLAG_DEPTH) != 0;
        boolean useAlphaTest = (flags & FLAG_ALPHA) != 0;
        boolean useAlphaRefLower = (flags & FLAG_ALPHA_LOWER) != 0;
        boolean useBlend = (flags & FLAG_BLEND) != 0;
        boolean useBlendFuncGamma = (flags & FLAG_BLEND_GAMMA) != 0;
        boolean useSmoothShadeModel = (flags & FLAG_SMOOTH) != 0;
        boolean useStencilReplace = (flags & FLAG_STENCIL_REPLACE) != 0;
        boolean useStencilKeep = (flags & FLAG_STENCIL_KEEP) != 0;

        if (lastRenderFlags < 0) {
            gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
            gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
            gl.glDisable(GL10.GL_DITHER);
            gl.glDepthFunc(GL10.GL_LESS);
            gl.glFrontFace(GL10.GL_CCW);
            gl.glCullFace(GL10.GL_BACK);
        }

        toggleCapacity(GL10.GL_CULL_FACE, useCullFace);
        toggleCapacity(GL10.GL_DEPTH_TEST, useDepthTest);
        toggleCapacity(GL10.GL_ALPHA_TEST, useAlphaTest);
        toggleCapacity(GL10.GL_BLEND, useBlend);
        toggleCapacity(GL10.GL_STENCIL_TEST, useStencilReplace | useStencilKeep);

        if (useAlphaTest) {
            gl.glAlphaFunc(GL10.GL_GREATER, useAlphaRefLower ? 0.1f : 0.5f);
        }

        if (useBlend) {
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, useBlendFuncGamma ? GL10.GL_ONE : GL10.GL_ONE_MINUS_SRC_ALPHA);
        }

        gl.glShadeModel(useSmoothShadeModel ? GL10.GL_SMOOTH : GL10.GL_FLAT);

        if (useStencilReplace) {
            gl.glStencilFunc(GL10.GL_ALWAYS, 1, 0xff);
            gl.glStencilOp(GL10.GL_REPLACE, GL10.GL_REPLACE, GL10.GL_REPLACE);
            gl.glStencilMask(0xff);

            gl.glClear(GL10.GL_STENCIL_BUFFER_BIT);
        }

        if (useStencilKeep) {
            gl.glStencilFunc(GL10.GL_EQUAL, 1, 0xff);
            gl.glStencilOp(GL10.GL_KEEP, GL10.GL_KEEP, GL10.GL_KEEP);
            gl.glStencilMask(0);
        }

        lastRenderFlags = flags;
    }

    private void toggleCapacity(int cap, boolean isEnabled) {
        if (isEnabled) {
            gl.glEnable(cap);
        } else {
            gl.glDisable(cap);
        }
    }

    // ----

    // In-game:
    //
    //    ^
    //	1 | 4
    // ---+--->
    //	2 | 3
    //
    // Ortho:
    //
    //    ^
    //  2 | 3
    // ---+--->
    //  1 | 4
    //
    public void batchQuad() {
        int vertexBufferPosL = vertexBufferPos;
        int colorsBufferPosL = colorsBufferPos;
        int textureBufferPosL = textureBufferPos;
        int indicesBufferPosL = indicesBufferPos;
        short vertexCountL = vertexCount;

        float[] vertexBufferL = vertexBuffer;
        float[] colorsBufferL = colorsBuffer;
        int[] textureBufferL = textureBuffer;
        short[] indicesBufferL = indicesBuffer;

        vertexBufferL[vertexBufferPosL] = x1;
        vertexBufferL[vertexBufferPosL + 1] = y1;
        vertexBufferL[vertexBufferPosL + 2] = z1;
        vertexBufferL[vertexBufferPosL + 3] = x2;
        vertexBufferL[vertexBufferPosL + 4] = y2;
        vertexBufferL[vertexBufferPosL + 5] = z2;
        vertexBufferL[vertexBufferPosL + 6] = x3;
        vertexBufferL[vertexBufferPosL + 7] = y3;
        vertexBufferL[vertexBufferPosL + 8] = z3;
        vertexBufferL[vertexBufferPosL + 9] = x4;
        vertexBufferL[vertexBufferPosL + 10] = y4;
        vertexBufferL[vertexBufferPosL + 11] = z4;

        colorsBufferL[colorsBufferPosL] = r1;
        colorsBufferL[colorsBufferPosL + 1] = g1;
        colorsBufferL[colorsBufferPosL + 2] = b1;
        colorsBufferL[colorsBufferPosL + 3] = a1;
        colorsBufferL[colorsBufferPosL + 4] = r2;
        colorsBufferL[colorsBufferPosL + 5] = g2;
        colorsBufferL[colorsBufferPosL + 6] = b2;
        colorsBufferL[colorsBufferPosL + 7] = a2;
        colorsBufferL[colorsBufferPosL + 8] = r3;
        colorsBufferL[colorsBufferPosL + 9] = g3;
        colorsBufferL[colorsBufferPosL + 10] = b3;
        colorsBufferL[colorsBufferPosL + 11] = a3;
        colorsBufferL[colorsBufferPosL + 12] = r4;
        colorsBufferL[colorsBufferPosL + 13] = g4;
        colorsBufferL[colorsBufferPosL + 14] = b4;
        colorsBufferL[colorsBufferPosL + 15] = a4;

        textureBufferL[textureBufferPosL] = u1;
        textureBufferL[textureBufferPosL + 1] = v1;
        textureBufferL[textureBufferPosL + 2] = u2;
        textureBufferL[textureBufferPosL + 3] = v2;
        textureBufferL[textureBufferPosL + 4] = u3;
        textureBufferL[textureBufferPosL + 5] = v3;
        textureBufferL[textureBufferPosL + 6] = u4;
        textureBufferL[textureBufferPosL + 7] = v4;

        indicesBufferL[indicesBufferPosL] = vertexCountL;
        indicesBufferL[indicesBufferPosL + 1] = (short)(vertexCountL + 2);
        indicesBufferL[indicesBufferPosL + 2] = (short)(vertexCountL + 1);
        indicesBufferL[indicesBufferPosL + 3] = vertexCountL;
        indicesBufferL[indicesBufferPosL + 4] = (short)(vertexCountL + 3);
        indicesBufferL[indicesBufferPosL + 5] = (short)(vertexCountL + 2);

        vertexBufferPos = vertexBufferPosL + 12;
        colorsBufferPos = colorsBufferPosL + 16;
        textureBufferPos = textureBufferPosL + 8;
        indicesBufferPos = indicesBufferPosL + 6;
        vertexCount = (short)(vertexCountL + 4);
    }

    public void batchTexQuad(int texNum) {
        u3 = u4 = (u1 = u2 = (texNum % 15) * TEX_CELL_X + TEX_1PX_X) + TEX_SIZE_X;
        v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y;
        batchQuad();
    }

    public void batchTexQuadExtruded(int texNum) {
        u3 = u4 = (u1 = u2 = (texNum % 15) * TEX_CELL_X + TEX_EXTR_X_FROM) + TEX_EXTR_X_SIZE;
        v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y;
        batchQuad();
    }

    public void batchTexQuadPieceTL(int texNum) {
        u3 = u4 = (u1 = u2 = (texNum % 15) * TEX_CELL_X + TEX_1PX_X) + TEX_SIZE_X_D2;
        v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y_D2;
        batchQuad();
    }

    public void batchTexQuadPieceTR(int texNum) {
        u3 = u4 = (u1 = u2 = (texNum % 15) * TEX_CELL_X + TEX_SIZE_X_D2_1PX) + TEX_SIZE_X_D2;
        v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y_D2;
        batchQuad();
    }

    public void batchTexQuadPieceBL(int texNum) {
        u3 = u4 = (u1 = u2 = (texNum % 15) * TEX_CELL_X + TEX_1PX_X) + TEX_SIZE_X_D2;
        v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_SIZE_Y_D2_1PX) + TEX_SIZE_Y_D2;
        batchQuad();
    }

    public void batchTexQuadPieceBR(int texNum) {
        u3 = u4 = (u1 = u2 = (texNum % 15) * TEX_CELL_X + TEX_SIZE_X_D2_1PX) + TEX_SIZE_X_D2;
        v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_SIZE_Y_D2_1PX) + TEX_SIZE_Y_D2;
        batchQuad();
    }

    public void batchTexQuadFlipped(int texNum) {
        u1 = u2 = (u3 = u4 = (texNum % 15) * TEX_CELL_X + TEX_1PX_X) + TEX_SIZE_X;
        v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y;
        batchQuad();
    }

    public void batchTexQuadFlippedPartTL(int texNum) {
        u1 = u2 = (u3 = u4 = (texNum % 15) * TEX_CELL_X + TEX_1PX_X) + TEX_SIZE_X_D2;
        v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y_D2;
        batchQuad();
    }

    public void batchTexQuadFlippedPartTR(int texNum) {
        u1 = u2 = (u3 = u4 = (texNum % 15) * TEX_CELL_X + TEX_SIZE_X_D2_1PX) + TEX_SIZE_X_D2;
        v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y_D2;
        batchQuad();
    }

    public void batchTexQuadFlippedPartBL(int texNum) {
        u1 = u2 = (u3 = u4 = (texNum % 15) * TEX_CELL_X + TEX_1PX_X) + TEX_SIZE_X_D2;
        v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_SIZE_Y_D2_1PX) + TEX_SIZE_Y_D2;
        batchQuad();
    }

    public void batchTexQuadFlippedPartBR(int texNum) {
        u1 = u2 = (u3 = u4 = (texNum % 15) * TEX_CELL_X + TEX_SIZE_X_D2_1PX) + TEX_SIZE_X_D2;
        v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_SIZE_Y_D2_1PX) + TEX_SIZE_Y_D2;
        batchQuad();
    }

    public void batchTexQuad2x(int texNum) {
        u3 = u4 = (u1 = u2 = (texNum % 15) * TEX_CELL_X + TEX_1PX_X) + TEX_SIZE_X_X2;
        v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y_X2;
        batchQuad();
    }

    // public void batchTexQuad4x1x(int texNum) {
    //     u3 = u4 = (u1 = u2 = (texNum % 15) * TEX_CELL_X + TEX_1PX_X) + TEX_SIZE_X_X4;
    //     v1 = v4 = (v2 = v3 = (texNum / 15) * TEX_CELL_Y + TEX_1PX_Y) + TEX_SIZE_Y;
    //     batchQuad();
    // }

    public void batchTexQuadMon(int texNum) {
        u3 = u4 = (u1 = u2 = (texNum % 7) * TEX_MON_CELL_X + TEX_1PX_X) + TEX_MON_SIZE_X;
        v1 = v4 = (v2 = v3 = (texNum / 7) * TEX_MON_CELL_Y + TEX_1PX_Y) + TEX_MON_SIZE_Y;
        batchQuad();
    }

    // public void batchLine() {
    //     int lineVertexBufferPosL = lineVertexBufferPos;
    //     int lineColorsBufferPosL = lineColorsBufferPos;
    //     float[] lineVertexBufferL = lineVertexBuffer;
    //     float[] lineColorsBufferL = lineColorsBuffer;
    //
    //     lineVertexBufferL[lineVertexBufferPosL] = x1;
    //     lineVertexBufferL[lineVertexBufferPosL + 1] = y1;
    //     lineVertexBufferL[lineVertexBufferPosL + 2] = x2;
    //     lineVertexBufferL[lineVertexBufferPosL + 3] = y2;
    //
    //     lineColorsBufferL[lineColorsBufferPosL] = r1;
    //     lineColorsBufferL[lineColorsBufferPosL + 1] = g1;
    //     lineColorsBufferL[lineColorsBufferPosL + 2] = b1;
    //     lineColorsBufferL[lineColorsBufferPosL + 3] = a1;
    //     lineColorsBufferL[lineColorsBufferPosL + 4] = r2;
    //     lineColorsBufferL[lineColorsBufferPosL + 5] = g2;
    //     lineColorsBufferL[lineColorsBufferPosL + 6] = b2;
    //     lineColorsBufferL[lineColorsBufferPosL + 7] = a2;
    //
    //     lineVertexBufferPos = lineVertexBufferPosL + 4;
    //     lineColorsBufferPos = lineColorsBufferPosL + 8;
    //     lineVertexCount += 2;
    // }

    public void batchLine(float sx, float sy, float ex, float ey) {
        int lineVertexBufferPosL = lineVertexBufferPos;
        int lineColorsBufferPosL = lineColorsBufferPos;
        float[] lineVertexBufferL = lineVertexBuffer;
        float[] lineColorsBufferL = lineColorsBuffer;

        lineVertexBufferL[lineVertexBufferPosL] = sx;
        lineVertexBufferL[lineVertexBufferPosL + 1] = sy;
        lineVertexBufferL[lineVertexBufferPosL + 2] = ex;
        lineVertexBufferL[lineVertexBufferPosL + 3] = ey;

        lineColorsBufferL[lineColorsBufferPosL] = r1;
        lineColorsBufferL[lineColorsBufferPosL + 1] = g1;
        lineColorsBufferL[lineColorsBufferPosL + 2] = b1;
        lineColorsBufferL[lineColorsBufferPosL + 3] = a1;
        lineColorsBufferL[lineColorsBufferPosL + 4] = r2;
        lineColorsBufferL[lineColorsBufferPosL + 5] = g2;
        lineColorsBufferL[lineColorsBufferPosL + 6] = b2;
        lineColorsBufferL[lineColorsBufferPosL + 7] = a2;

        lineVertexBufferPos = lineVertexBufferPosL + 4;
        lineColorsBufferPos = lineColorsBufferPosL + 8;
        lineVertexCount += 2;
    }

    public void setColorLineA(float a) {
        a1 = a;
        a2 = a;
    }

    public void setColorLineRGB(float r, float g, float b) {
        r1 = r;
        g1 = g;
        b1 = b;

        r2 = r;
        g2 = g;
        b2 = b;
    }

    public void setColorLineRGBA(float r, float g, float b, float a) {
        r1 = r;
        g1 = g;
        b1 = b;
        a1 = a;

        r2 = r;
        g2 = g;
        b2 = b;
        a2 = a;
    }

    public void setColorQuadA(float a) {
        a1 = a;
        a2 = a;
        a3 = a;
        a4 = a;
    }

    public void setColorQuadRGB(float r, float g, float b) {
        r1 = r;
        g1 = g;
        b1 = b;

        r2 = r;
        g2 = g;
        b2 = b;

        r3 = r;
        g3 = g;
        b3 = b;

        r4 = r;
        g4 = g;
        b4 = b;
    }

    public void setColorQuadRGBA(float r, float g, float b, float a) {
        r1 = r;
        g1 = g;
        b1 = b;
        a1 = a;

        r2 = r;
        g2 = g;
        b2 = b;
        a2 = a;

        r3 = r;
        g3 = g;
        b3 = b;
        a3 = a;

        r4 = r;
        g4 = g;
        b4 = b;
        a4 = a;
    }

    public void setColorQuadLight(float l) {
        r1 = l;
        g1 = l;
        b1 = l;

        r2 = l;
        g2 = l;
        b2 = l;

        r3 = l;
        g3 = l;
        b3 = l;

        r4 = l;
        g4 = l;
        b4 = l;
    }

    public void setColorQuadLight(float lf, float lt) {
        r1 = lf;
        g1 = lf;
        b1 = lf;

        r2 = lf;
        g2 = lf;
        b2 = lf;

        r3 = lt;
        g3 = lt;
        b3 = lt;

        r4 = lt;
        g4 = lt;
        b4 = lt;
    }

    public void setColorQuadLight(float lft, float lff, float ltf, float ltt) {
        r1 = lft;
        g1 = lft;
        b1 = lft;

        r2 = lff;
        g2 = lff;
        b2 = lff;

        r3 = ltf;
        g3 = ltf;
        b3 = ltf;

        r4 = ltt;
        g4 = ltt;
        b4 = ltt;
    }

    public void setCoordsQuadZ(float z) {
        z1 = z;
        z2 = z;
        z3 = z;
        z4 = z;
    }

    // In-game (sx=fx, sy=ty, ex=tx, ey=fy):
    //
    //      ^
    //  1 S | 4 t
    // -----+----->
    //  2 f | 3 E
    //
    // Ortho (sx=fx, sy=fy, ex=tx, ey=ty):
    //
    //      ^
    //   2  | 3tE
    // -----+----->
    //  1fS |  4
    //
    public void setCoordsQuadRect(float sx, float sy, float ex, float ey) {
        // X Y - -
        x1 = sx;
        y1 = sy;

        // X - - Y
        x2 = sx;
        y2 = ey;

        // - - X Y
        x3 = ex;
        y3 = ey;

        // - Y X -
        x4 = ex;
        y4 = sy;
    }

    // In-game (sx=fx, sy=ty, ex=tx, ey=fy):
    //
    //      ^
    //  1 S | 2 t
    // -----+----->
    //  4 f | 3 E
    //
    public void setCoordsQuadRectFlip(float sx, float sy, float ex, float ey) {
        // X Y - -
        x1 = sx;
        y1 = sy;

        // - Y X -
        x2 = ex;
        y2 = sy;

        // - - X Y
        x3 = ex;
        y3 = ey;

        // X - - Y
        x4 = sx;
        y4 = ey;
    }

    // Ortho (sx=fx, sy=fy, ex=tx, ey=ty):
    //
    //      ^
    //   2  | 3tE
    // -----+----->
    //  1fS |  4
    //
    public void setCoordsQuadRectFlat(float sx, float sy, float ex, float ey) {
        x1 = sx;
        y1 = sy;
        z1 = 0.0f;

        x2 = sx;
        y2 = ey;
        z2 = 0.0f;

        x3 = ex;
        y3 = ey;
        z3 = 0.0f;

        x4 = ex;
        y4 = sy;
        z4 = 0.0f;
    }

    public void setCoordsQuadBillboardZ(float sz, float ez) {
        z1 = sz;
        z2 = ez;
        z3 = ez;
        z4 = sz;
    }

    public void setCoordsQuadBillboard(float sx, float sy, float tx, float ty) {
        x1 = sx;
        y1 = -sy;

        x2 = sx;
        y2 = -sy;

        x3 = tx;
        y3 = -ty;

        x4 = tx;
        y4 = -ty;
    }

    public void setCoordsQuadBillboard(float sx, float sy, float tx, float ty, float sz, float ez) {
        x1 = sx;
        y1 = -sy;
        z1 = sz;

        x2 = sx;
        y2 = -sy;
        z2 = ez;

        x3 = tx;
        y3 = -ty;
        z3 = ez;

        x4 = tx;
        y4 = -ty;
        z4 = sz;
    }

    public void setTexRect(int sx, int sy, int ex, int ey) {
        u1 = sx;
        v1 = sy;

        u2 = sx;
        v2 = ey;

        u3 = ex;
        v3 = ey;

        u4 = ex;
        v4 = sy;
    }
}
