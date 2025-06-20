/*
 * **************************************************************************
 *                        Copyright j3d.org (c) 2000 - ${year}
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read docs/lgpl.txt for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 * **************************************************************************
 */

package org.j3d.aviatrix3d.output.graphics;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawable;

import org.j3d.util.ErrorReporter;
import org.j3d.util.I18nManager;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.j3d.aviatrix3d.test.AV3DMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import org.j3d.aviatrix3d.pipeline.RenderOp;
import org.j3d.aviatrix3d.pipeline.graphics.*;
import org.j3d.aviatrix3d.rendering.ObjectRenderable;
import org.j3d.aviatrix3d.rendering.ViewEnvironmentCullable;
import org.j3d.aviatrix3d.test.MockGL2;

/**
 * Unit tests for the 2D rendering processor
 *
 * @author justin
 */
public class StandardRenderingProcessorTest
{
    @Mock
    private GL mockGL;

    @Mock
    private GLContext mockContext;

    @Mock
    private GraphicsOutputDevice mockOutputDevice;

    @Mock
    private GLDrawable mockDrawable;

    @Mock
    private ErrorReporter mockReporter;

    private MockGL2 mockGL2;

    @BeforeMethod(groups = "unit")
    public void setupTests() throws Exception
    {
        MockitoAnnotations.openMocks(this);

        I18nManager intl_mgr = I18nManager.getManager();
        intl_mgr.setApplication("StandardRenderingProcessorTest", "config.i18n.av3dResources");

        mockGL2 = new MockGL2(mockContext);
        when(mockGL.getGL2()).thenReturn(mockGL2);
        when(mockContext.getGL()).thenReturn(mockGL);
        when(mockContext.getGLDrawable()).thenReturn(mockDrawable);
    }

    @Test(groups = "unit")
    public void testBasicConstruction() throws Exception
    {
        StandardRenderingProcessor class_under_test = new StandardRenderingProcessor(mockOutputDevice);

        assertFalse(class_under_test.isTwoPassTransparentEnabled(), "2-pass transparency rendering should be disabled");
        assertEquals(class_under_test.getAlphaTestCutoff(), 1.0f, 0.001f, "Alpha test cutoff incorrect");
    }

    @Test(groups = "unit", expectedExceptions = IllegalArgumentException.class)
    public void testOwnerBufferAsNull() throws Exception
    {
        StandardRenderingProcessor class_under_test = new StandardRenderingProcessor(mockOutputDevice);
        class_under_test.setOwnerBuffer(null);
    }

    @Test(groups = "unit")
    public void testPrepareDataNormal() throws Exception
    {
        MainCanvasDescriptor test_descriptor = new MainCanvasDescriptor();

        StandardRenderingProcessor class_under_test = new StandardRenderingProcessor(mockOutputDevice);
        class_under_test.setOwnerBuffer(test_descriptor);

        class_under_test.prepareData(mockContext);
    }

    @Test(groups = "unit")
    public void testPrepareDataAlreadyTerminated() throws Exception
    {
        MainCanvasDescriptor test_descriptor = new MainCanvasDescriptor();

        StandardRenderingProcessor class_under_test = new StandardRenderingProcessor(mockOutputDevice);
        class_under_test.setOwnerBuffer(test_descriptor);
        class_under_test.halt();

        class_under_test.prepareData(mockContext);
    }

    @Test(groups = "unit")
    public void testReinitialise() throws Exception
    {
        MainCanvasDescriptor test_descriptor = new MainCanvasDescriptor();

        StandardRenderingProcessor class_under_test = new StandardRenderingProcessor(mockOutputDevice);
        class_under_test.setOwnerBuffer(test_descriptor);

        class_under_test.prepareData(mockContext);

        mockGL2.resetCallCount();

        class_under_test.reinitialize(mockContext);

        assertTrue(mockGL2.getCallCount() > 0, "Should have had some GL calls during reinit");
    }

    @Test(groups = "unit")
    public void testDispose() throws Exception
    {
        MainCanvasDescriptor test_descriptor = new MainCanvasDescriptor();

        StandardRenderingProcessor class_under_test = new StandardRenderingProcessor(mockOutputDevice);
        class_under_test.setOwnerBuffer(test_descriptor);
        class_under_test.prepareData(mockContext);

        // Don't release if we are multithreaded
        verify(mockContext, never()).release();
    }

    @Test(groups = "unit", dependsOnMethods = "testPrepareDataNormal")
    public void testRenderNoContent() throws Exception
    {
        MainCanvasDescriptor test_descriptor = new MainCanvasDescriptor();

        GraphicsProfilingData test_profile_data = new GraphicsProfilingData();
        StandardRenderingProcessor class_under_test = new StandardRenderingProcessor(mockOutputDevice);
        class_under_test.setOwnerBuffer(test_descriptor);
        class_under_test.prepareData(mockContext);
        class_under_test.render(mockContext, test_profile_data);

        verify(mockGL, atLeast(1)).getGL2();
        verifyNoMoreInteractions(mockGL);

        assertEquals(test_profile_data.numRenderables, 0, "Should not have registered renderables");
        assertEquals(test_profile_data.numTriangles, 0, "Should not have registered triangles");
    }

    @Test(groups = "unit", dependsOnMethods = "testRenderNoContent")
    public void testBasicRenderingLoop() throws Exception
    {
        // Basic renderable item that we want to have it render.
        ObjectRenderable test_renderable = mock(ObjectRenderable.class);

        // Empty class for this test. Nothing to do here.
        GraphicsRequestData test_request_data = new GraphicsRequestData();

        GraphicsDetails test_renderable_details = new GraphicsDetails();
        test_renderable_details.renderable = test_renderable;
        test_renderable_details.transform = new float[16];

        // Double up because we have start and stop render ops next.
        GraphicsDetails[] test_nodes = { test_renderable_details, test_renderable_details };

        RenderOp[] test_ops = { RenderOp.START_RENDER, RenderOp.STOP_RENDER };

        GraphicsEnvironmentData test_env_data = new GraphicsEnvironmentData();
        test_env_data.viewProjectionType = ViewEnvironmentCullable.PERSPECTIVE_PROJECTION;
        GraphicsEnvironmentData[] test_env_data_list = { test_env_data };

        MainCanvasDescriptor test_descriptor = new MainCanvasDescriptor();

        when(mockContext.makeCurrent()).thenReturn(GLContext.CONTEXT_CURRENT);

        GraphicsProfilingData test_profile_data = new GraphicsProfilingData();
        StandardRenderingProcessor class_under_test = new StandardRenderingProcessor(mockOutputDevice);
        class_under_test.setOwnerBuffer(test_descriptor);
        class_under_test.prepareData(mockContext);
        class_under_test.setDrawableObjects(test_request_data, test_nodes, test_ops, test_ops.length, test_env_data_list);

        class_under_test.render(mockContext, test_profile_data);

        verify(test_renderable, times(1)).render(mockGL2);
        verify(test_renderable, times(1)).postRender(mockGL2);

        mockGL2.verifyCall("glPushMatrix");
        mockGL2.verifyCall("glPopMatrix");
        mockGL2.verifyCall("glMultMatrixf", avAny(float[].class), 0);

        assertEquals(test_profile_data.numRenderables, 2, "Should have registered renderables");
    }
}
