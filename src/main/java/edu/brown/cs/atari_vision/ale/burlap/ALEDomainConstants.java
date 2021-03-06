package edu.brown.cs.atari_vision.ale.burlap;

/**
 * Created by MelRod on 3/12/16.
 */
public class ALEDomainConstants {

    // Attribute Names
    public static final String				XATTNAME = "xAtt"; // x-coordinate attribute
    public static final String				YATTNAME = "yAtt"; // y-coordinate attribute

    public static final String				AGENT_CENT_XATTNAME = "acxAtt"; // agent-centered x-coordinate attribute
    public static final String				AGENT_CENT_YATTNAME = "acyAtt"; // agent-centered y-coordinate attribute

    public static final String				VXATTNAME = "vxAtt"; // x velocity attribute
    public static final String				VYATTNAME = "vyAtt"; // y velocity attribute



    // Object Class Names
    public static final String CLASSAGENT = "agent";
    public static final String CLASSALIEN = "alien";

    public static final String CLASS_BOMB_UNKNOWN = "bomb_unknown";
    public static final String CLASS_BOMB_AGENT = "bomb_agent";
    public static final String CLASS_BOMB_ALIEN = "bomb_alien";


    // Propositional Function names
    public static final String PFVertAlign = "pfVertAlign";


    // Constants
    public static final int ALEScreenWidth = 160;
    public static final int ALEScreenHeight = 210;
}
