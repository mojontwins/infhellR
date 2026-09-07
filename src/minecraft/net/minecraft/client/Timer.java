package net.minecraft.client;

/**
 * Fixed-rate game clock. Runs at a target tick rate (default 20 TPS) and provides
 * both discrete tick counts and a continuous {@link #renderPartialTicks} value for
 * smooth entity/interpolation rendering.
 *
 * <p>The timer uses two clocks to remain accurate over long sessions:</p>
 * <ul>
 *   <li>A system clock (milliseconds) as the wall-clock reference.</li>
 *   <li>A high-resolution clock (nanoseconds) to measure frame deltas precisely.</li>
 * </ul>
 *
 * <p>If the two clocks diverge significantly (e.g. the system clock jumps due to
 * NTP sync), the high-resolution clock takes over to prevent jitter or fast-forward.</p>
 */
public class Timer {
    /** Target ticks per second (typically 20). */
    public float ticksPerSecond;
    /** Accumulated high-resolution time since the last sync. */
    private double lastHRTime;
    /** Number of discrete game ticks that have elapsed in the current frame. */
    public int elapsedTicks;
    /**
     * Fractional tick value (0..1) used to interpolate entity positions and animations
     * between the previous and current tick for smooth rendering.
     */
    public float renderPartialTicks;
    /** Speed multiplier — 1.0 is normal speed, higher values speed up the game clock. */
    public float timerSpeed = 1.0F;
    /** Accumulated fractional ticks (before extracting the integer part into {@link #elapsedTicks}). */
    public float elapsedPartialTicks = 0.0F;
    /** System clock value at the last sync point (milliseconds). */
    private long lastSyncSysClock;
    /** High-resolution clock value at the last sync point (milliseconds). */
    private long lastSyncHRClock;
    /** Accumulated system-time delta between syncs (used to detect clock drift). */
    private long runningTimeDelta;
    /** Smoothing factor applied to the system/high-res clock ratio (clamped to [0, 1]). */
    private double timeSyncAdjustment = 1.0D;

    public Timer(float ticksPerSecond) {
        this.ticksPerSecond = ticksPerSecond;
        this.lastSyncSysClock = System.currentTimeMillis();
        this.lastSyncHRClock = System.nanoTime() / 1000000L;
    }

    /**
     * Advances the timer by one frame. Computes the elapsed ticks and fractional
     * render value based on wall-clock time and the high-resolution clock.
     */
    public void updateTimer() {
        long nowSys = System.currentTimeMillis();
        long sysDelta = nowSys - this.lastSyncSysClock;
        long nowHR = System.nanoTime() / 1000000L;
        double nowSec = (double) nowHR / 1000.0D;

        // System clock jump detected (NTP sync, sleep, etc.) — reset the sync.
        if (sysDelta > 1000L || sysDelta < 0L) {
            this.lastHRTime = nowSec;
        } else {
            this.runningTimeDelta += sysDelta;
            // More than one second accumulated — recompute the sync ratio.
            if (this.runningTimeDelta > 1000L) {
                long hrDelta = nowHR - this.lastSyncHRClock;
                double ratio = (double) this.runningTimeDelta / (double) hrDelta;
                // Smooth the ratio change so the timer doesn't jump.
                this.timeSyncAdjustment += (ratio - this.timeSyncAdjustment) * 0.2D;
                this.lastSyncHRClock = nowHR;
                this.runningTimeDelta = 0L;
            }
            if (this.runningTimeDelta < 0L) {
                this.lastSyncHRClock = nowHR;
            }
        }

        this.lastSyncSysClock = nowSys;
        double frameDelta = (nowSec - this.lastHRTime) * this.timeSyncAdjustment;
        this.lastHRTime = nowSec;

        // Clamp to prevent fast-forward or rewind.
        if (frameDelta < 0.0D) frameDelta = 0.0D;
        if (frameDelta > 1.0D) frameDelta = 1.0D;

        // Accumulate fractional ticks, then split into integer + fractional parts.
        this.elapsedPartialTicks = (float) (this.elapsedPartialTicks
                + frameDelta * (double) this.timerSpeed * (double) this.ticksPerSecond);
        this.elapsedTicks = (int) this.elapsedPartialTicks;
        this.elapsedPartialTicks -= (float) this.elapsedTicks;

        // Cap at 10 ticks per frame to prevent spiral-of-death.
        if (this.elapsedTicks > 10) {
            this.elapsedTicks = 10;
        }
        this.renderPartialTicks = this.elapsedPartialTicks;
    }
}
