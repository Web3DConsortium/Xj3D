package org.xj3d.impl.core.eventmodel;

// Standard imports
import java.util.ArrayList;
import java.util.List;

import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.util.DefaultErrorReporter;
import org.j3d.util.ErrorReporter;

// Local imports
import org.web3d.vrml.lang.CADKernelType;
import org.web3d.vrml.lang.ComponentInfo;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.xj3d.core.eventmodel.NodeManager;

public class DefaultBrepManager implements NodeManager {

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** List of managed node types */
    private static final int[] MANAGED_NODE_TYPES = {
        TypeConstants.BREPNodeType,
        TypeConstants.ParametricGeometryNodeType
    };

    private CADKernelType cadRenderer=null;

    /**
     * Stack used to notify the manager that a Face tesselation has been successfully computed and is ready to be included in the scene graph
     */
    List<VRMLNodeType> readyFace=new ArrayList<>();

    static DefaultBrepManager instance=null;


    /**
     * Singleton pattern to expose the Manager to the Renderer
     * @return DefaultBrepManager
     */
    static public DefaultBrepManager getInstance()
    {
    	return instance;

    }

    public void setCadRenderer(CADKernelType renderer)
    {
    	this.cadRenderer=renderer;
    }

    /**
     * Notifies the manager that a face has been successfully tesselated and is ready to be rendered
     * (Note : the tesselation is no carried here, the flow is the following :
     * 1-Face tells CadRendered that it's been parsed and need a tesselation
     * 2-CadRenderer starts a thread to compute face tesselation
     * 3-CadRenderer tells BrepManager that a the face tesselation has been computed
     * 4-Manager waits for a safe time to update sceneGraph, then tells the Face it needs to update its geometry thru aviatrix updateBounds callback.
     * 5-Face queries the CadRenderer for the tesselation the "ready to render" tesselation.
     * @param n
     */
    synchronized public void addReadyFace(VRMLNodeType n)
    {
    	readyFace.add(n);
    }

    public DefaultBrepManager() {
            instance=this;
            errorReporter = DefaultErrorReporter.getDefaultReporter();
            errorReporter.messageReport("Instantiate BREP Manager");
    }

    @Override
    public void addManagedNode(VRMLNodeType node) {
    }

    @Override
    public void executePreEventModel(long time) {

    }

    @Override
    public void executePostEventModel(long time) {

    	//goes over list of faces notified to be rendered
    	List<VRMLNodeType> l=(List<VRMLNodeType>) ((ArrayList<VRMLNodeType>)readyFace).clone();
        for(Object d : l)//d is a face
    	{
            NodeUpdateListener f = (NodeUpdateListener) d;
            cadRenderer.updateGeometry(f);
            readyFace.remove((VRMLNodeType)f);
    	}
    }

    @Override
    public ComponentInfo[] getSupportedComponents() {
        return new ComponentInfo[] {
            new ComponentInfo("xj3d_BREP", 1)
        };
    }

    @Override
    public boolean initialize() {
        return true;
    }

    @Override
    public void removeManagedNode(VRMLNodeType node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null) {
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void setVRMLClock(VRMLClock clk) {
    }

    @Override
    public void resetTimeZero() {
    }

    @Override
    public int[] getManagedNodeTypes() {
        return MANAGED_NODE_TYPES;
    }

    @Override
    public boolean evaluatePreEventModel() {
        return true;
    }

    @Override
    public boolean evaluatePostEventModel() {
        return true;
    }

    @Override
    public void clear() {
    }

}
