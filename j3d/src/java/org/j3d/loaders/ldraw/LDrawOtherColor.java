/*****************************************************************************
 *                            (c) j3d.org 2002-2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 ****************************************************************************/

package org.j3d.loaders.ldraw;

// External imports
// None

// Local parser
// None

/**
 * Enumerated list of the official colour charts.
 * <p>
 * Colours are named by number in the Lego view of the world, however we can't
 * name stuff as a number here so it is prefixed by the colour type (core,
 * transparent etc)
 * <p>
 *
 * The definition of the file format can be found at:
 * <a href="http://www.ldraw.org/Article93.html">
 *  http://www.ldraw.org/Article93.html</a>
 *
 * @author  Justin Couch
 * @version $Revision: 1.3 $
 */
public class LDrawOtherColor extends LDrawColor
{
    public static final LDrawOtherColor CHROME_60 = new LDrawOtherColor(60, ColorType.CHROME, 0x64_5A4C, 0x28_1E10, 0);
    public static final LDrawOtherColor CHROME_61 = new LDrawOtherColor(61, ColorType.CHROME, 0x6C_96BF, 0x20_2A68, 0);
    public static final LDrawOtherColor CHROME_62 = new LDrawOtherColor(62, ColorType.CHROME, 0x3C_B371, 0x00_7735, 0);
    public static final LDrawOtherColor CHROME_63 = new LDrawOtherColor(63, ColorType.CHROME, 0xAA_4D8E, 0x6E_1152, 0);
    public static final LDrawOtherColor CHROME_64 = new LDrawOtherColor(64, ColorType.CHROME, 0x1B_2A34, 0x00_0000, 0);
    public static final LDrawOtherColor CHROME_334 = new LDrawOtherColor(334, ColorType.CHROME, 0xBB_A53D, 0xA4_C374, 0);
    public static final LDrawOtherColor CHROME_383 = new LDrawOtherColor(383, ColorType.CHROME, 0xE0_E0E0, 0xA4_A4A4, 0);

    // 494, 495 defined as internal colours
    public static final LDrawOtherColor PEARL_134 = new LDrawOtherColor(134, ColorType.PEARL, 0xAB_6038, 0x33_3333, 0);
    public static final LDrawOtherColor PEARL_135 = new LDrawOtherColor(135, ColorType.PEARL, 0x9C_A3A8, 0x33_3333, 0);
    public static final LDrawOtherColor PEARL_137 = new LDrawOtherColor(137, ColorType.PEARL, 0x56_77BA, 0x33_3333, 0);
    public static final LDrawOtherColor PEARL_142 = new LDrawOtherColor(142, ColorType.PEARL, 0xDC_BE61, 0x33_3333, 0);
    public static final LDrawOtherColor PEARL_148 = new LDrawOtherColor(148, ColorType.PEARL, 0x57_5857, 0x33_3333, 0);
    public static final LDrawOtherColor PEARL_150 = new LDrawOtherColor(150, ColorType.PEARL, 0xBB_BDBC, 0x33_3333, 0);
    public static final LDrawOtherColor PEARL_178 = new LDrawOtherColor(178, ColorType.PEARL, 0xB4_883E, 0x33_3333, 0);
    public static final LDrawOtherColor PEARL_179 = new LDrawOtherColor(179, ColorType.PEARL, 0x89_8788, 0x33_3333, 0);
    public static final LDrawOtherColor PEARL_183 = new LDrawOtherColor(183, ColorType.PEARL, 0xF2_F3F2, 0x33_3333, 0);
    public static final LDrawOtherColor PEARL_297 = new LDrawOtherColor(297, ColorType.PEARL, 0xCC_9C2B, 0x33_3333, 0);


    public static final LDrawOtherColor METALLIC_80 = new LDrawOtherColor(80, ColorType.METALLIC, 0xA5_A9B4, 0x33_3333, 250);
    public static final LDrawOtherColor METALLIC_81 = new LDrawOtherColor(81, ColorType.METALLIC, 0x89_9B5F, 0x33_3333, 250);
    public static final LDrawOtherColor METALLIC_82 = new LDrawOtherColor(82, ColorType.METALLIC, 0xDB_AC34, 0x33_3333, 250);
    public static final LDrawOtherColor METALLIC_83 = new LDrawOtherColor(83, ColorType.METALLIC, 0x1A_2831, 0x00_0000, 250);
    public static final LDrawOtherColor METALLIC_87 = new LDrawOtherColor(87, ColorType.METALLIC, 0x6D_6E5C, 0x5D_5B53, 250);

    public static final LDrawOtherColor RUBBER_65 = new LDrawOtherColor(65, ColorType.RUBBER, 0xF5_CD2F, 0x33_3333, 0);
    public static final LDrawOtherColor RUBBER_66 = new LDrawOtherColor(66, ColorType.RUBBER, 0xCA_B000, 0x8E_7400, 128);
    public static final LDrawOtherColor RUBBER_67 = new LDrawOtherColor(67, ColorType.RUBBER, 0xFF_FFFF, 0xC3_C3C3, 128);
    public static final LDrawOtherColor RUBBER_256 = new LDrawOtherColor(256, ColorType.RUBBER, 0x21_2121, 0x59_5959, 0);
    public static final LDrawOtherColor RUBBER_273 = new LDrawOtherColor(273, ColorType.RUBBER, 0x00_33B2, 0x33_3333, 0);
    public static final LDrawOtherColor RUBBER_324 = new LDrawOtherColor(324, ColorType.RUBBER, 0xC4_0026, 0x33_3333, 0);
    public static final LDrawOtherColor RUBBER_375 = new LDrawOtherColor(375, ColorType.RUBBER, 0xC1_C2C1, 0x33_3333, 0);
    public static final LDrawOtherColor RUBBER_406 = new LDrawOtherColor(406, ColorType.RUBBER, 0x00_1D68, 0x33_3333, 0);
    public static final LDrawOtherColor RUBBER_449 = new LDrawOtherColor(449, ColorType.RUBBER, 0x81_007B, 0x33_3333, 0);
    public static final LDrawOtherColor RUBBER_490 = new LDrawOtherColor(490, ColorType.RUBBER, 0xD7_F000, 0x33_3333, 0);
    public static final LDrawOtherColor RUBBER_496 = new LDrawOtherColor(496, ColorType.RUBBER, 0xA3_A2A4, 0x33_3333, 0);
    public static final LDrawOtherColor RUBBER_504 = new LDrawOtherColor(504, ColorType.RUBBER, 0x89_8788, 0x33_3333, 0);
    public static final LDrawOtherColor RUBBER_511 = new LDrawOtherColor(511, ColorType.RUBBER, 0xFA_FAFA, 0x33_3333, 0);


    private LDrawOtherColor(int idx,
                            ColorType ct,
                            int hexColour,
                            int hexComplement,
                            int a)
    {
        super(idx, ct, hexColour, hexComplement, a);
    }
}
