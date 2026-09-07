package net.minecraft.client.render;

import org.lwjgl.opengl.ARBDebugOutputCallback.Handler;

public class GLLoggingHandler implements Handler {
    /**
     * Severity levels.
     */
    private static final int
        GL_DEBUG_SEVERITY_HIGH_ARB = 0x9146,
        GL_DEBUG_SEVERITY_MEDIUM_ARB = 0x9147,
        GL_DEBUG_SEVERITY_LOW_ARB = 0x9148;

    /**
     * Sources.
     */
    private static final int
        GL_DEBUG_SOURCE_API_ARB = 0x8246,
        GL_DEBUG_SOURCE_WINDOW_SYSTEM_ARB = 0x8247,
        GL_DEBUG_SOURCE_SHADER_COMPILER_ARB = 0x8248,
        GL_DEBUG_SOURCE_THIRD_PARTY_ARB = 0x8249,
        GL_DEBUG_SOURCE_APPLICATION_ARB = 0x824A,
        GL_DEBUG_SOURCE_OTHER_ARB = 0x824B;

    /**
     * Types.
     */
    private static final int
        GL_DEBUG_TYPE_ERROR_ARB = 0x824C,
        GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR_ARB = 0x824D,
        GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR_ARB = 0x824E,
        GL_DEBUG_TYPE_PORTABILITY_ARB = 0x824F,
        GL_DEBUG_TYPE_PERFORMANCE_ARB = 0x8250,
        GL_DEBUG_TYPE_OTHER_ARB = 0x8251;

    @Override
    public void handleMessage(int source, int type, int id, int severity, String message) {
        if (id == 131185) {
            // Just a log about buffers
            return;
        }

        System.err.println("[LWJGL] ARB_debug_output message");
        System.err.println("\tID: " + id);

        String description;
        switch (source) {
            case GL_DEBUG_SOURCE_API_ARB:
                description = "API";
                break;
            case GL_DEBUG_SOURCE_WINDOW_SYSTEM_ARB:
                description = "WINDOW SYSTEM";
                break;
            case GL_DEBUG_SOURCE_SHADER_COMPILER_ARB:
                description = "SHADER COMPILER";
                break;
            case GL_DEBUG_SOURCE_THIRD_PARTY_ARB:
                description = "THIRD PARTY";
                break;
            case GL_DEBUG_SOURCE_APPLICATION_ARB:
                description = "APPLICATION";
                break;
            case GL_DEBUG_SOURCE_OTHER_ARB:
                description = "OTHER";
                break;
            default:
                description = printUnknownToken(source);
        }
        System.err.println("\tSource: " + description);

        switch (type) {
            case GL_DEBUG_TYPE_ERROR_ARB:
                description = "ERROR";
                break;
            case GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR_ARB:
                description = "DEPRECATED BEHAVIOR";
                break;
            case GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR_ARB:
                description = "UNDEFINED BEHAVIOR";
                break;
            case GL_DEBUG_TYPE_PORTABILITY_ARB:
                description = "PORTABILITY";
                break;
            case GL_DEBUG_TYPE_PERFORMANCE_ARB:
                description = "PERFORMANCE";
                break;
            case GL_DEBUG_TYPE_OTHER_ARB:
                description = "OTHER";
                break;
            default:
                description = printUnknownToken(type);
        }
        System.err.println("\tType: " + description);

        switch (severity) {
            case GL_DEBUG_SEVERITY_HIGH_ARB:
                description = "HIGH";
                break;
            case GL_DEBUG_SEVERITY_MEDIUM_ARB:
                description = "MEDIUM";
                break;
            case GL_DEBUG_SEVERITY_LOW_ARB:
                description = "LOW";
                break;
            default:
                description = printUnknownToken(severity);
        }
        System.err.println("\tSeverity: " + description);

        System.err.println("\tMessage: " + message);
    }

    private String printUnknownToken(final int token) {
        return "Unknown (0x" + Integer.toHexString(token).toUpperCase() + ")";
    }
}
