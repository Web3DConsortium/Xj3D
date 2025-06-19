/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.chefx3d.model;

// External Imports
import java.util.*;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.*;
import org.jivesoftware.smack.filter.*;
import org.jivesoftware.smackx.muc.*;

// Internal Imports
import org.chefx3d.tool.Tool;
import org.chefx3d.tool.ToolGroup;
import org.chefx3d.util.DefaultErrorReporter;
import org.chefx3d.util.ErrorReporter;

/**
 * A world model that will use XMPP chatrooms to coordinate world models.
 *
 *
 * TODO: Stop making this public when we figure out factory design issues.
 * Should this class connect to server/chatroom for the user?
 *
 * @author Alan Hudson
 * @version $Revision: 1.21 $
 */
public class NetworkedXMPPWorldModel extends BaseWorldModel implements
        PacketListener {
    private static final int COMMAND_AddAssociationCommand = 0;

    private static final int COMMAND_AddEntityCommand = 1;

    private static final int COMMAND_AddPropertyCommand = 2;

    private static final int COMMAND_AddVertexCommand = 3;

    private static final int COMMAND_ChangeMasterCommand = 4;

    private static final int COMMAND_ChangePropertyCommand = 5;

    private static final int COMMAND_ChangeViewCommand = 6;

    private static final int COMMAND_ChangeViewTransientCommand = 7;

    private static final int COMMAND_ClearModelCommand = 8;

    private static final int COMMAND_MoveEntityCommand = 9;

    private static final int COMMAND_MoveEntityTransientCommand = 10;

    private static final int COMMAND_RemoveAssociationCommand = 11;

    private static final int COMMAND_RemoveEntityCommand = 12;

    private static final int COMMAND_RotateEntityCommand = 13;

    private static final int COMMAND_RotateEntityTransientCommand = 14;

    /** The list of all tools */
    private ArrayList<ToolGroup> allTools;

    /** Has allTools been changed into a HashMap */
    private boolean toolsProcessed;

    /** A mapping of tool names to Tool */
    private HashMap<String, Tool> toolMap;

    /** The server to connect to */
    private String server;

    /** The username to use */
    private String username;

    /** The password to use */
    private String password;

    /** Connection to the XMPP server */
    private XMPPConnection connection;

    /** Multiuser chat room */
    private MultiUserChat chatroom;

    /** The muc jid */
    private String mucJid;

    /** The muc room on the muc server we joining */
    private String mucRoom;

    /** The TransientProcessor for handling transient commands */
    private XMPPTransientProcessor transientProcessor;

    /** The controller that manages commands */
    private CommandController controller;

    /** Mapping of Commands to tokens for a switch statement */
    private Map<String, Integer> commandMap;

    /** The ErrorReporter for messages */
    private ErrorReporter errorReporter;

    /**
     * Constructor.
     *
     * @param controller The controller that manages commands
     * @param server The server to use
     * @param room
     * @param username The user name to use
     * @param passwd The passwd to use
     */
    public NetworkedXMPPWorldModel(CommandController controller,
            String server, String room, String username,String passwd) {
        toolsProcessed = false;
        this.server = server;
        this.username = username;
        this.password = passwd;
        mucRoom = room;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        this.controller = controller;

        initCommandMapping();

        login();
    }

    // ----------------------------------------------------------
    // Methods required by the PacketListener interface
    // ----------------------------------------------------------

    @Override
    public void processPacket(Packet packet) {
        if (!(packet instanceof Message))
            return;

        Message msg = (Message) packet;
        String xmlString = msg.getBody();

        // sniff first element name to determine which command it is
        int idx = xmlString.indexOf(" ");

        if (idx < 0)
            return;

        String cmdString = xmlString.substring(1, idx);

        Integer val = commandMap.get(cmdString);

        boolean found = false;
        Command cmd = null;

        if (val != null) {
            switch (val) {
            case COMMAND_AddAssociationCommand:
                cmd = new AddAssociationCommand(this);
                found = true;
                break;
            case COMMAND_AddEntityCommand:
                cmd = new AddEntityCommand(this);
                found = true;
                break;
            case COMMAND_AddPropertyCommand:
                cmd = new AddPropertyCommand(this);
                found = true;
                break;
            case COMMAND_AddVertexCommand:
                cmd = new AddVertexCommand(this);
                found = true;
                break;
            case COMMAND_ChangeMasterCommand:
                cmd = new ChangeMasterCommand(this);
                found = true;
                break;
            case COMMAND_ChangePropertyCommand:
                //cmd = new ChangePropertyCommand(this);
                found = true;
                break;
            case COMMAND_ChangeViewCommand:
                cmd = new ChangeViewCommand(this);
                found = true;
                break;
            case COMMAND_ChangeViewTransientCommand:
                cmd = new ChangeViewTransientCommand(this);
                found = true;
                break;
            case COMMAND_ClearModelCommand:
                cmd = new ClearModelCommand(this);
                found = true;
                break;
            case COMMAND_MoveEntityCommand:
                cmd = new MoveEntityCommand(this);
                found = true;
                break;
            case COMMAND_MoveEntityTransientCommand:
                cmd = new MoveEntityTransientCommand(this);
                found = true;
                break;
            case COMMAND_RemoveEntityCommand:
                cmd = new RemoveEntityCommand(this);
                found = true;
                break;
            case COMMAND_RotateEntityCommand:
                cmd = new RotateEntityCommand(this);
                found = true;
                break;
            case COMMAND_RotateEntityTransientCommand:
                cmd = new RotateEntityTransientCommand(this);
                found = true;
                break;
            }

            cmd.setErrorReporter(errorReporter);

            if (found) {
                cmd.deserialize(xmlString);

                if (cmd.isTransient()) {
                    transientProcessor.commandArrived(cmd);
                } else {
                    cmd.execute();
                }
            }
        }

        if (!found) {

            try {
                Class cl = Class.forName("org.chefx3d.model." + cmdString);
                Constructor constructor = cl
                        .getConstructor(new Class[] { WorldModel.class });

                Object obj = constructor.newInstance(new Object[] { this });

                Method method = cl.getMethod("deserialize",
                        new Class[] { String.class });

                method.invoke(obj, new Object[] { xmlString });

                cmd = (Command) obj;
                cmd.setErrorReporter(errorReporter);

                //errorReporter.messageReport("Command not in quicklist, should optmize");
                if (cmd.isTransient()) {
                    transientProcessor.commandArrived(cmd);
                } else {
                    cmd.execute();
                }
            } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                errorReporter.errorReport("Create Command failed!", e);
            }
        }

        // TODO: THink we need this but it breaks things
        if (!cmd.isTransient()) {
            //errorReporter.messageReport("Completed Command: " + cmd.getClass()
            //        + " tid: " + cmd.getTransactionID());
            int tID = cmd.getTransactionID();

            if (tID != 0) {
                transientProcessor.closeTransaction(tID);
            }
        }
    }

    // ----------------------------------------------------------
    // Methods required by the WorldModel interface
    // ----------------------------------------------------------

    /**
     * Apply a command against the model.
     *
     * @param command The command
     */
    @Override
    public void applyCommand(Command command) {
        // TODO: Should we execute before we send or wait till it comes back?
        controller.execute(command);

        // TODO: Need to clip to some maximum, say 25?

        // Close local transient commands so the reflected values don't cause
        // jitter

        if (command.isLocal() && !command.isTransient()) {
            // System.out.println("Close local Command: " + command.getClass() +
            // " tid: " + command.getTransactionID());
            int tID = command.getTransactionID();

            if (tID != 0) {
                transientProcessor.closeTransaction(tID);
            }
        }

        // TODO: Moved to packetArrived but not certain its right
        /*
         * if (!command.isTransient()) { System.out.println("Completed Command: " +
         * command.getClass() + " tid: " + command.getTransactionID()); int tID =
         * command.getTransactionID();
         *
         * if (tID != 0) { transientProcessor.closeTransaction(tID); } }
         */

        if (command.isTransient()) {
            transientProcessor.processCommand(command);

            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        command.serialize(Command.METHOD_XML, baos);

        String st = baos.toString();

        try {
            chatroom.sendMessage(st);
        } catch (XMPPException e) {
            errorReporter.errorReport("Apply Command Failed", e);
        }
    }

    /**
     * Undo the last change.
     */
    @Override
    public void undo() {
        controller.undo();
    }

    /**
     * Flush the undo history.
     */
    @Override
    public void clearHistory() {
        controller.clear();
    }

    /**
     * Returns true if there are any <code>Command</code>s to undo
     * @return 
     */
    @Override
    public boolean canUndo() {
        return controller.canUndo();
    }

    /**
     * Return the description of the <code>Command</code> to be executed if
     * <code>undo()</code> is called.
     * @return 
     */
    @Override
    public String getUndoDescription() {
        return controller.getUndoDescription();
    }

    /**
     * Redo the last change.
     */
    @Override
    public void redo() {
        controller.redo();
    }

    /**
     * Returns true if there are any <code>Command</code>s to redo
     * @return 
     */
    @Override
    public boolean canRedo() {
        return controller.canRedo();
    }

    /**
     * Return the description of the <code>Command</code> to be executed if
     * <code>redo()</code> is called.
     * @return 
     */
    @Override
    public String getRedoDescription() {
        return controller.getRedoDescription();
    }

    public void checkForDuplicateIDS() {
        // ignore
    }

    // ----------------------------------------------------------
    // Local Methods
    // ----------------------------------------------------------

    /**
     * Set the connection information.
     *
     * @param conn The connection
     * @param chat The chat room to use
     */
    public void setConnection(XMPPConnection conn, MultiUserChat chat) {
        connection = conn;
        chatroom = chat;
    }

    /**
     * Login to the XMPP server
     */
    private void login() {
        try {
            // Authenticate to our local XMPP server
            errorReporter.messageReport("logging in to " + server);
            connection = new XMPPConnection(server);
            errorReporter.messageReport("Using user: " + username + " + password: "
                    + password);

            connection.login(username, password);
            errorReporter.messageReport("logged in to " + server);

            errorReporter.messageReport(">>> Getting conn ID: "
                    + connection.getConnectionID());
            errorReporter.messageReport(">>> Getting host: " + connection.getHost());
            errorReporter.messageReport(">>> Getting user: " + connection.getUser());
            errorReporter.messageReport(">>> Getting port: " + connection.getPort());
            errorReporter.messageReport(">>> Getting service name: "
                    + connection.getServiceName());

            // Establish a connection to the MUC room
            mucJid = mucRoom + "@" + "conference." + server;

            if (chatroom == null) {
                chatroom = new MultiUserChat(connection, mucJid);
            }

            errorReporter.messageReport("Joining as: " + username);
            DiscussionHistory dh = new DiscussionHistory();
            dh.setMaxStanzas(0);

            chatroom.join(username, password, dh, 10_000);

            transientProcessor = new XMPPTransientProcessor(chatroom, this,
                    false, 0.5f, 0.5f);
            transientProcessor.start();

            // set up a packet filter to listen for only the things we want
            PacketFilter filter = new AndFilter(new PacketTypeFilter(
                    Message.class), new FromContainsFilter(mucJid));

            // Register the listener.
            connection.addPacketListener(this, filter);
        } catch (XMPPException e) {
            errorReporter.errorReport("Login Faliure!", e);
        }
    }

    /**
     * Initialize the command mapping table.
     */
    private void initCommandMapping() {
        commandMap = new HashMap();
        commandMap.put("AddAssociationCommand", COMMAND_AddAssociationCommand);

        commandMap.put("AddEntityCommand", COMMAND_AddEntityCommand);

        commandMap.put("AddPropertyCommand", COMMAND_AddPropertyCommand);

        commandMap.put("AddVertexCommand", COMMAND_AddVertexCommand);

        commandMap.put("ChangeMasterCommand", COMMAND_ChangeMasterCommand);

        commandMap.put("ChangePropertyCommand", COMMAND_ChangePropertyCommand);

        commandMap.put("ChangeViewCommand", COMMAND_ChangeViewCommand);

        commandMap.put("ChangeViewTransientCommand", COMMAND_ChangeViewTransientCommand);

        commandMap.put("ClearModelCommand", COMMAND_ClearModelCommand);

        commandMap.put("MoveEntityCommand", COMMAND_MoveEntityCommand);

        commandMap.put("MoveEntityTransientCommand", COMMAND_MoveEntityTransientCommand);

        commandMap.put("RemoveAssociationCommand", COMMAND_RemoveAssociationCommand);

        commandMap.put("RemoveEntityCommand", COMMAND_RemoveEntityCommand);

        commandMap.put("RotateEntityCommand", COMMAND_RotateEntityCommand);

        commandMap.put("RotateEntityTransientCommand", COMMAND_RotateEntityTransientCommand);
    }

    /**
     * Register an error reporter with the command instance
     * so that any errors generated can be reported in a nice manner.
     *
     * @param reporter The new ErrorReporter to use.
     */
    @Override
    public void setErrorReporter(ErrorReporter reporter) {
        super.setErrorReporter(reporter);

        errorReporter = reporter;

        if(errorReporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

}
