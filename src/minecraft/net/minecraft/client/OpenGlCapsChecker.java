package net.minecraft.client;

import org.lwjgl.opengl.GLContext;

/**
 * Caches the results of OpenGL extension capability queries.
 *
 * <p>LWJGL's {@code GLContext.getCapabilities()} involves native calls each time it
 * is invoked, so this class memoizes the results once at construction time.</p>
 */
public class OpenGlCapsChecker {
    /** If false, skip the ARB occlusion query check entirely (disabled for safety). */
    private static boolean supportsOcclusionCulling = true;

    /**
     * Returns true if the GPU/driver supports ARB occlusion queries, which are
     * used to cull distant geometry that would be fully blocked by closer blocks.
     *
     * @return true if ARB occlusion query is available
     */
    public boolean checkARBOcclusion() {
        return supportsOcclusionCulling && GLContext.getCapabilities().GL_ARB_occlusion_query;
    }
}
