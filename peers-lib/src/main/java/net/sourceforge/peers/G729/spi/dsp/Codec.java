package net.sourceforge.peers.g729.spi.dsp;


import net.sourceforge.peers.g729.spi.format.Format;
import net.sourceforge.peers.g729.spi.memory.Frame;

import java.io.Serializable;

public interface Codec extends Serializable {
    Format getSupportedInputFormat();

    Format getSupportedOutputFormat();

    Frame process(Frame var1);
}