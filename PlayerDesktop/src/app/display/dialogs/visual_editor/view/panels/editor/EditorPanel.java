package app.display.dialogs.visual_editor.view.panels.editor;


import app.display.dialogs.visual_editor.LayoutManagement.LayoutManager.LayoutHandler;
import app.display.dialogs.visual_editor.handler.Handler;
import app.display.dialogs.visual_editor.model.DescriptionGraph;
import app.display.dialogs.visual_editor.model.LudemeNode;
import app.display.dialogs.visual_editor.view.components.AddLudemeWindow;
import app.display.dialogs.visual_editor.view.DesignPalette;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeConnection;
import app.display.dialogs.visual_editor.view.components.ludemenodecomponent.LudemeNodeComponent;
import app.display.dialogs.visual_editor.model.InputInformation;
import app.display.dialogs.visual_editor.view.panels.IGraphPanel;
import app.display.dialogs.visual_editor.view.panels.editor.selections.SelectionBox;
import grammar.Grammar;
import main.grammar.Symbol;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static app.display.dialogs.visual_editor.handler.Handler.mainPanel;

public class EditorPanel extends JPanel implements IGraphPanel
{

    private DescriptionGraph graph = new DescriptionGraph();
    private final List<LudemeNodeComponent> nodeComponents = new ArrayList<>();
    private final List<LudemeConnection> edges = new ArrayList<>();
    private Point mousePosition;

    private final LayoutHandler lm;
    private ConnectionHandler ch;

    private double zoomFactor = 1.0;
    private double zoomFactor0 = 1.0;
    private boolean zoomed = false;


    List<Symbol> symbols = Grammar.grammar().symbols();

    // flag to check if select button is active
    private boolean SELECTION_MODE = false;
    // flag to check if user performs selection
    private boolean SELECTING = false;
    // flag to check if selection was performed
    private boolean SELECTED = false;
    // list of selected nodes
    private List<LudemeNodeComponent> selectedLnc = new ArrayList<>();

    // node autoplacement
    private boolean autoplacement = false;

    // window to add a new ludeme out of all possible ones
    private final AddLudemeWindow addLudemeWindow = new AddLudemeWindow(symbols, this, false);
    // window to add a new ludeme as an input
    private final AddLudemeWindow connectLudemeWindow = new AddLudemeWindow(symbols, this, true);

    private static final boolean DEBUG = true;

    public EditorPanel(int width, int height)
    {
        setLayout(null);
        setPreferredSize(new Dimension(width, height));
        setBackground(DesignPalette.BACKGROUND_EDITOR);

        addMouseListener(clickListener);
        addMouseMotionListener(motionListener);
        addMouseWheelListener(wheelListener);
        //addMouseWheelListener(wheelListener2);

        add(addLudemeWindow);
        add(connectLudemeWindow);


        /*
        graph.setRoot(addNode(gameLudeme, 30, 30, false));*/
        Handler.gameDescriptionGraph = graph;
        Handler.editorPanel = this;

        LudemeNode gameLudemeNode = createLudemeNode(Grammar.grammar().symbolsByName("Game").get(0), 30, 30);
        graph.setRoot(gameLudemeNode);
        addLudemeNodeComponent(gameLudemeNode, false);

        lm = new LayoutHandler(graph, graph.getRoot().getId());
        ch = new ConnectionHandler(edges);
    }

    public EditorPanel()
    {
        setLayout(null);
        //setPreferredSize(DesignPalette.DEFAULT_FRAME_SIZE);
        setBackground(DesignPalette.BACKGROUND_EDITOR);

        addMouseListener(clickListener);
        addMouseMotionListener(motionListener);
        addMouseWheelListener(wheelListener);
        addMouseWheelListener(wheelListener2);

        add(addLudemeWindow);
        add(connectLudemeWindow);


        /*
        graph.setRoot(addNode(gameLudeme, 30, 30, false));*/
        Handler.gameDescriptionGraph = graph;

        LudemeNode gameLudemeNode = createLudemeNode(Grammar.grammar().symbolsByName("Game").get(0), 30, 30);
        graph.setRoot(gameLudemeNode);
        addLudemeNodeComponent(gameLudemeNode, false);

        lm = new LayoutHandler(graph, graph.getRoot().getId());
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // TODO: fix zooming
        // Scaling works but not visible
        if (zoomed) {
            AffineTransform at = new AffineTransform();
            at.scale(zoomFactor, zoomFactor);
            zoomFactor0 = zoomFactor;
            g2.transform(at);
        }

        boolean showBackgroundDots = true;
        if(showBackgroundDots)
        {
            // draw background points
            // every 50 pixel a circle
            int paddingHorizontal = 35;
            int paddingvertical = 15;
            int frequency = DesignPalette.BACKGROUND_DOT_PADDING;
            int diameter = DesignPalette.BACKGROUND_DOT_DIAMETER;

            // to improve performance, only draw points that are in the visible area
            Rectangle viewRect = mainPanel.getPanel().getViewport().getViewRect();
            for (int i = paddingHorizontal; i < getWidth() - paddingHorizontal; i += frequency)
            {
                for (int j = paddingvertical; j < getHeight() - paddingvertical; j += frequency)
                {
                    if(i < viewRect.x || i > viewRect.x + viewRect.width || j < viewRect.y || j > viewRect.y + viewRect.height) continue; // TODO this can be optimized by a lot by adding this offset to i and j
                    g2.setColor(DesignPalette.BACKGROUND_VISUAL_HELPER);
                    g2.fillOval(i, j, diameter, diameter);
                }
            }
        }

        // set color for edges
        g2.setColor(DesignPalette.LUDEME_CONNECTION_EDGE);
        // set stroke for edges
        g2.setStroke(DesignPalette.LUDEME_EDGE_STROKE);

        // draw new connection
        ch.drawNewConnection(g2, mousePosition);

        // draw existing connections
        ch.paintConnections(g2);

        // paint ludeme nodes somewhere here...


        // Draw selection area
        if (SELECTION_MODE && !SELECTING) SelectionBox.drawSelectionModeIdle(mousePosition, g2);
        if (SELECTION_MODE && SELECTING) SelectionBox.drawSelectionArea(mousePosition, mousePosition, g2);
    }

    public void showCurrentlyAvailableLudemes(int x, int y)
    {
        if(DEBUG) System.out.println("[EP] Show list of connectable ludemes");
        // get game description up to current point
        int upUntilIndex = ch.getSelectedConnectionComponent().getInputField().getInputInformations().get(0).getIndex();
        for(InputInformation ii : ch.getSelectedConnectionComponent().getInputField().getInputInformations())
        {
            if(ii.getIndex() < upUntilIndex) upUntilIndex = ii.getIndex();
        }

        connectLudemeWindow.updateList(ch.getSelectedConnectionComponent().getRequiredSymbols());
        connectLudemeWindow.setVisible(true);
        connectLudemeWindow.setLocation(mousePosition);
        connectLudemeWindow.searchField.requestFocus();
        revalidate();
        repaint();
    }

    public LudemeNode createLudemeNode(Symbol symbol, int x, int y)
    {
        LudemeNode node = new LudemeNode(symbol, x, y);
        Handler.addNode(graph, node);
        return node;
    }

    private void addLudemeNodeComponent(LudemeNode node, boolean connect)
    {
        LudemeNodeComponent lc = new LudemeNodeComponent(node, this);
        addLudemeWindow.setVisible(false);
        connectLudemeWindow.setVisible(false);
        nodeComponents.add(lc);
        add(lc);
        lc.updatePositions();

        if(connect){ch.finishNewConnection(lc);}

        // expand editor
        //expandEditorPanelSize(lc);

        Handler.centerViewport(lc.getX()+lc.getWidth()/2, lc.getY()+lc.getHeight()/2);
    }


    public ConnectionHandler getCh()
    {
        return ch;
    }

    // # Methods to handle selection #

    /**
     * Enter selection mode
     */
    public void enterSelectionMode()
    {
        this.SELECTION_MODE = true;
    }

    /**
     * Exit selection mode
     * @return boundary box of the selection area
     */
    public Rectangle exitSelectionMode()
    {
        SELECTING = false;
        SELECTION_MODE = false;
        Handler.turnOffSelectionBtn();
        repaint();
        revalidate();
        return SelectionBox.endSelection();
    }

    /**
     * Clear selection list and deselects all nodes
     */
    private void deselectEverything()
    {
        graph.getNodes().forEach(n -> {
            LudemeNodeComponent lnc = getNodeComponent(n);
            lnc.setSELECTED(false);
        });
        selectedLnc = new ArrayList<>();
        SELECTED = false;
        repaint();
        revalidate();
    }

    /**
     * Select node and add it into selection list
     * @param lnc ludeme node component
     */
    public void addNodeToSelections(LudemeNodeComponent lnc)
    {
        SELECTED = true;
        lnc.setSELECTED(true);
        selectedLnc.add(lnc);
    }

    public boolean isSELECTION_MODE()
    {
        return SELECTION_MODE;
    }

    public List<LudemeNodeComponent> getSelectedLnc()
    {
        return selectedLnc;
    }

    public void setAutoplacement(boolean autoplacement)
    {
        this.autoplacement = autoplacement;
    }



    // # Implementation of IGraphPanel interface methods #

    @Override
    public void drawGraph(DescriptionGraph graph)
    {
        if(DEBUG) System.out.println("\n[EP] Redrawing graph\n");
        this.graph = graph;
        removeAll();
        nodeComponents.clear();
        edges.clear();
        ch.setSelectedConnectionComponent(null);
        List<LudemeNode> nodes = graph.getNodes();
        for(LudemeNode node : nodes)
        {
            //add(new LudemeBlock(node, null, 300));
            LudemeNodeComponent lc = new LudemeNodeComponent(node, this);
            nodeComponents.add(lc);
            add(lc);
            lc.revalidate();
        }
        for(LudemeNodeComponent lc : nodeComponents)
        {
            lc.updateProvidedInputs();
            lc.updatePositions();
        }

        add(addLudemeWindow);
        add(connectLudemeWindow);

        revalidate();
        repaint();
    }

    @Override
    public void updateGraph()
    {
        if(DEBUG) System.out.println("\n[EP] Updating graph\n");
        for (LudemeNodeComponent lc : nodeComponents)
        {
            lc.revalidate();
            lc.updateProvidedInputs();
            lc.updateLudemePosition();
        }
        revalidate();
        repaint();
    }

    @Override
    public DescriptionGraph getGraph() {
        return graph;
    }

    @Override
    public LudemeNodeComponent getNodeComponent(LudemeNode node)
    {
        for(LudemeNodeComponent lc : nodeComponents)
        {
            if(lc.node().equals(node))
            {
                return lc;
            }
        }
        return null;
    }

    @Override
    public LudemeNode addNode(Symbol symbol, int x, int y, boolean connect)
    {
        LudemeNode node = new LudemeNode(symbol, x, y);
        Handler.addNode(graph, node);
        addLudemeNodeComponent(node, connect);
        repaint();
        if(DEBUG) System.out.println("[EP] Added node: " + node.symbol().name());
        return node;
    }

    @Override
    public void showAllAvailableLudemes(int x, int y)
    {
        addLudemeWindow.setVisible(true);
        addLudemeWindow.setLocation(mousePosition);
        addLudemeWindow.searchField.requestFocus();
        revalidate();
        repaint();
    }

    @Override
    public void clickedOnNode(LudemeNodeComponent lnc)
    {
        LudemeNode node = lnc.node();
        if(ch.getSelectedConnectionComponent() != null)
        {
            if(ch.getSelectedConnectionComponent().getRequiredSymbols().contains(node.symbol()) && !lnc.getIngoingConnectionComponent().isFilled())
            {
                ch.finishNewConnection(lnc);
            }
        }
    }

    @Override
    public void removeNode(LudemeNode node)
    {
        if(DEBUG) System.out.println("[EP] Removing node " + node.symbol().name());
        LudemeNodeComponent lc = getNodeComponent(node);
        nodeComponents.remove(lc);
        ch.removeAllConnections(node, false);
        Handler.removeNode(graph, node);
        remove(lc);
        repaint();
        if (autoplacement) LayoutHandler.applyOnPanel(EditorPanel.this);
    }

    @Override
    public LayoutHandler getLayoutHandler()
    {
        return lm;
    }



    // # Mouse listeners #

    MouseListener clickListener = new MouseAdapter()
    {
        private void openPopupMenu(MouseEvent e)
        {
            JPopupMenu popupMenu = new EditorPopupMenu(EditorPanel.this);
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }

        @Override
        public void mouseClicked(MouseEvent e)
        {
            super.mouseClicked(e);

            System.out.println(Arrays.toString(((LudemeNode) graph.getRoot()).providedInputs()));

            if(connectLudemeWindow.isVisible())
            {
                ch.cancelNewConnection();
            }
            addLudemeWindow.setVisible(false);
            connectLudemeWindow.setVisible(false);
            if(e.getButton() == MouseEvent.BUTTON1)
            {
                // user is drawing a new connection
                if(ch.getSelectedConnectionComponent() != null)
                {
                    // if user has no chocie for next ludeme -> automatically add required ludeme
                    if(ch.getSelectedConnectionComponent().getRequiredSymbols().size() == 1)
                    {
                        addNode(ch.getSelectedConnectionComponent().getRequiredSymbols().get(0), e.getX(), e.getY(), true);
                    }
                    else if(!connectLudemeWindow.isVisible() && ch.getSelectedConnectionComponent().getRequiredSymbols().size() > 1)
                    {
                        showCurrentlyAvailableLudemes(e.getX(), e.getY());
                    }
                    if (autoplacement) LayoutHandler.applyOnPanel(EditorPanel.this);
                }
            }
            else
            {
                // user is selecting a connection -> cancel new connection
                if(ch.getSelectedConnectionComponent() != null)
                {
                    ch.cancelNewConnection();
                }
            }

            // When selection was performed user can clear it out by clicking on blank area
            if (SELECTED) deselectEverything();

            repaint();
            revalidate();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            super.mouseDragged(e);
        }

        public void mousePressed(MouseEvent e)
        {
            if(e.getButton() == MouseEvent.BUTTON3)
            {
                ch.cancelNewConnection();
                openPopupMenu(e);
            }

            if (SELECTION_MODE && e.getButton() == MouseEvent.BUTTON1)
            {
                SELECTING = true;
                repaint();
                revalidate();
            }
        }

        /**
         * - Open pop up menu right click on empty space
         * - Select nodes that fall within selection area
         * @param e mouse event
         */
        public void mouseReleased(MouseEvent e)
        {
            if(e.getButton() == MouseEvent.BUTTON3)
            {
                openPopupMenu(e);
            }

            if (SELECTING && e.getButton() == MouseEvent.BUTTON1)
            {
                Rectangle region = exitSelectionMode();
                if (region != null)
                {
                    graph.getNodes().forEach(n -> {
                        LudemeNodeComponent lnc = getNodeComponent(n);
                        if (region.intersects(lnc.getBounds()))
                        {
                            addNodeToSelections(lnc);
                            SELECTED = true;
                        }
                    });
                }
            }
        }

    };

    MouseMotionListener motionListener = new MouseAdapter()
    {
        @Override
        public void mouseMoved(MouseEvent e)
        {
            super.mouseMoved(e);
            mousePosition = e.getPoint();
            if (SELECTION_MODE) repaint();
            if(ch.getSelectedConnectionComponent() != null) repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e)
        {
            super.mouseDragged(e);
            mousePosition = e.getPoint();
            if (SELECTING) repaint();
        }
    };



    // # Mouse wheel listeners #

    MouseWheelListener wheelListener = new MouseAdapter()
    {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            double amount = Math.pow(1.01, e.getScrollAmount());
            if(e.getWheelRotation() > 0)
            {
                DesignPalette.scale((float) (Math.max(DesignPalette.SCALAR / amount, DesignPalette.MIN_SCALAR)));
            }
            else
            {
                DesignPalette.scale((float) (Math.min(DesignPalette.SCALAR * amount, DesignPalette.MAX_SCALAR)));
            }
            repaint();
        }
    };

    MouseWheelListener wheelListener2 = new MouseAdapter() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            zoomed = true;
            //Zoom in
            if (e.getWheelRotation() < 0) {
                zoomFactor *= 1.1;
                repaint();
                revalidate();
            }
            //Zoom out
            if (e.getWheelRotation() > 0) {
                zoomFactor /= 1.1;
                repaint();
                revalidate();
            }
        }
    };


}