package net.minecraft.network;

/**
 * Tracks how many packets of a given type have been observed and the cumulative
 * number of bytes they have consumed on the wire.
 *
 * <p>Instances are stored per packet ID in {@code Packet.packetStats} and updated
 * by {@link Packet#addPacket(int)} / {@link Packet#addTrafficStat(int, int)} each
 * time a packet of the matching ID is dispatched. The collected totals are
 * surfaced through {@link Packet#getPacketStats()} for use by the in-game
 * network statistics screen.</p>
 *
 * <p>This class is purely a debugging/accounting aid and has no effect on
 * packet processing itself.</p>
 */
public class PacketCounter {
	/** Total number of packets counted so far for the associated packet ID. */
	private int totalPackets;

	/** Cumulative payload (in bytes) of every packet counted so far for the associated packet ID. */
	private long totalBytes;

	/**
	 * Constructs an empty counter. Both {@link #totalPackets} and
	 * {@link #totalBytes} start at zero.
	 */
	public PacketCounter() {
	}

	/**
	 * Records that a single packet of the given size has been processed.
	 *
	 * <p>Increments {@link #totalPackets} by one and adds the supplied byte
	 * count to {@link #totalBytes}.</p>
	 *
	 * @param packetSizeBytes the size of the packet that was just observed, in
	 *                        bytes. Must be non-negative; values larger than
	 *                        {@link Integer#MAX_VALUE} will be sign-extended
	 *                        into the long accumulator.
	 */
	public void addPacket(int packetSizeBytes) {
		// Bump the per-type packet count and accumulate payload bytes in one go.
		++this.totalPackets;
		this.totalBytes += (long)packetSizeBytes;
	}

	/**
	 * @return the running total of packets counted for the associated packet ID.
	 */
	public int getTotalPackets() {
		return totalPackets;
	}

	/**
	 * Overrides the running packet count for the associated packet ID. Used by
	 * the network statistics screen when assembling aggregate values.
	 *
	 * @param totalPackets the new packet count to store.
	 */
	public void setTotalPackets(int totalPackets) {
		this.totalPackets = totalPackets;
	}

	/**
	 * @return the cumulative payload in bytes observed for the associated
	 *         packet ID.
	 */
	public long getTotalBytes() {
		return totalBytes;
	}

	/**
	 * Overrides the running byte count for the associated packet ID. Used by
	 * the network statistics screen when assembling aggregate values.
	 *
	 * @param totalBytes the new byte total to store.
	 */
	public void setTotalBytes(long totalBytes) {
		this.totalBytes = totalBytes;
	}
}
