package edu.brown.cs.atari_vision.caffe.vfa;

import burlap.mdp.core.state.State;
import org.bytedeco.javacpp.FloatPointer;

import static org.bytedeco.javacpp.caffe.*;

/**
 * Created by MelRod on 5/27/16.
 */
public interface NNStateConverter {

    void getStateInput(State state, FloatPointer input);

    void saveMemoryState(String filePrefix);
    void loadMemoryState(String filePrefix);
}
