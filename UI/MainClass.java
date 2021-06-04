package UI;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import Graph.DijkstraGraphController;

public class MainClass extends JFrame implements ActionListener, MenuListener{

	private JPanel activePanel;
	private DijkstraGraphController controller;
	private DrawPanel dPanel;
	private VisualPanel vPanel;
	private AboutPanel aPanel;

	public MainClass(){
		setSize(500,500);
		setTitle("Dijkstra Algorithm Visualization");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setMinimumSize(new Dimension(500, 500));
		getContentPane().setLayout(null);
		setJMenuBar(loadMenu()); //create a jmenubar
		loadScreen();
	}

	private void loadScreen(){
		controller = new DijkstraGraphController(this);
		dPanel = new DrawPanel(controller, new Dimension(this.getWidth(), this.getHeight()));
		vPanel = new VisualPanel(controller, new Dimension(this.getWidth(), this.getHeight()));
		aPanel = new AboutPanel();
		card = new CardLayout();
		activePanel = new JPanel(card);
		activePanel.add(dPanel, "dPanel");
		activePanel.add(vPanel, "vPanel");
		activePanel.add(aPanel, "aPanel");
		getContentPane().add(activePanel);

		addComponentListener(new ComponentAdapter(){ //listen to resizing of frame
			public void componentResized(ComponentEvent e){
				activePanel.setSize(getWidth(), getHeight()); //resize drawing panel based on its frame
				activePanel.updateUI();
			}
		});
	}

	private JMenuBar loadMenu(){
		JMenuBar menu = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		aboutMenu = new JMenu("About");
		drawMenu = new JMenu("Draw");
		runMenu = new JMenu("Run");
		JMenuItem newFile = new JMenuItem("New");
		JMenuItem loadFile = new JMenuItem("Load File");
		JMenuItem saveFile = new JMenuItem("Save File");
		JMenuItem exit = new JMenuItem("Exit");
		JMenuItem generate = new JMenuItem("Generate random graph");
		transform = new JMenuItem("Transform to digraph");

		fileMenu.add(newFile);
		fileMenu.add(loadFile);
		fileMenu.add(saveFile);
		fileMenu.add(transform);
		fileMenu.add(generate);
		fileMenu.add(exit);
		menu.add(fileMenu);
		menu.add(drawMenu);
		menu.add(runMenu);
		menu.add(aboutMenu);

		newFile.addActionListener(this);
		loadFile.addActionListener(this);
		saveFile.addActionListener(this);
		transform.addActionListener(this);
		generate.addActionListener(this);
		exit.addActionListener(this);
		drawMenu.addMenuListener(this);
		runMenu.addMenuListener(this);
		aboutMenu.addMenuListener(this);

		return menu;
	}

	@Override
	public void actionPerformed(ActionEvent e){
		String action = e.getActionCommand();
		JFileChooser jf = new JFileChooser();
	
		if(action.equals("Save File")){
			if(jf.showSaveDialog(null)==JFileChooser.APPROVE_OPTION)
				controller.saveGraph(jf.getSelectedFile()+".txt");
		}else if(action.equals("Load File")){
			if(jf.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
				controller.loadGraph(jf.getSelectedFile());
		}else if(action.equals("Transform to digraph")){
			transform("Transform to ugraph", true);
		}else if(action.equals("Transform to ugraph")){
			transform("Transform to digraph", false);
		}else if(action.equals("New")){ //transfer later 
			int i = JOptionPane.showConfirmDialog(null, "Create new graph?", "New Dijkstra Graph",
				 		JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

			if(i==0){
				resetSetting("Transform to digraph");	
				controller = new DijkstraGraphController(this);			
				refreshVisual();
				dPanel = new DrawPanel(controller, new Dimension(this.getWidth(), this.getHeight()));
				vPanel = new VisualPanel(controller, new Dimension(this.getWidth(), this.getHeight()));
			}
		}
		else if(action.equals("Exit")){
			System.exit(0);
		}else if(action.equals("Generate random graph")){
			JPanel generation = new JPanel(new GridLayout(2,2));
			JTextField vSize = new JTextField("max 50");
			JTextField eSize = new JTextField();

			generation.add(new JLabel("Vertex count: "));
			generation.add(vSize);
			generation.add(new JLabel("Edge count: "));
			generation.add(eSize);

			int i = JOptionPane.showConfirmDialog(null, generation, "Random Graph",
				 		JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

			if(i==0){
				controller.generateGraph(vSize.getText(), eSize.getText());
			}
		}
	}

	public void menuSelected(MenuEvent e){
		if(e.getSource()==runMenu){
			controller.reloadVertices();
			card.show(activePanel, "vPanel");
		}
		else if(e.getSource()==drawMenu){
			refreshVisual();
		 	card.show(activePanel, "dPanel");
		}
		else if(e.getSource()==aboutMenu){
			refreshVisual();
			card.show(activePanel, "aPanel");
		}
	}

	public void menuDeselected(MenuEvent e){}

	public void menuCanceled(MenuEvent e){}

	public DijkstraGraphController getcontroller(){
		return controller;
	}

	public void refreshDraw(){
		dPanel.refresh();
	}

	public void refreshVisual(){
		vPanel.reset();
	}

	public void reloadVertices(java.util.ArrayList<Graph.Vertex> vertices){
		vPanel.loadVertices(vertices);
	}

	public void resetSetting(String text){
		transform.setText(text);
	}

	public void updateController(DijkstraGraphController controller){
		this.controller = controller;
	}

	public void transform(String text, boolean direct){
		resetSetting(text);
		controller.setDirected(direct);
		refreshDraw();
	}

	public void repaintActive(){
		activePanel.repaint();
	}

	public DrawPanel getDrawPanel(){
		return dPanel;
	}

	public VisualPanel getVisualPanel(){
		return vPanel;
	}
	
	public static void main(String[] args){
		EventQueue.invokeLater(new Runnable(){
			public void run(){
				new MainClass().setVisible(true);
			}
		});
	}

	private JMenu aboutMenu, runMenu, drawMenu;
	private JMenuItem transform;
	private CardLayout card;
}
