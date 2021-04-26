package UI;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import Graph.*;

public class MainClass extends JFrame implements ActionListener, MenuListener{

	private JPanel activePanel;
	private DijkstraGraphController graph;
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
		loadScreen("U","U");
	}

	private void loadScreen(String direct, String weight){
		graph = new DijkstraGraphController(direct, weight);
		dPanel = new DrawPanel(this);
		vPanel = new VisualPanel(this);
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

		fileMenu.add(newFile);
		fileMenu.add(loadFile);
		fileMenu.add(saveFile);
		fileMenu.add(exit);
		menu.add(fileMenu);
		menu.add(drawMenu);
		menu.add(runMenu);
		menu.add(aboutMenu);

		newFile.addActionListener(this);
		loadFile.addActionListener(this);
		saveFile.addActionListener(this);
		exit.addActionListener(this);
		drawMenu.addMenuListener(this);
		runMenu.addMenuListener(this);
		aboutMenu.addMenuListener(this);

		return menu;
	}

	@Override
	public void actionPerformed(ActionEvent e){
		JFileChooser jf = new JFileChooser();
		jf.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter fnef = new FileNameExtensionFilter("DijkstraGraph Files", "dijg");
		jf.addChoosableFileFilter(fnef);
	
		if(e.getActionCommand()=="Save File"){
			if(jf.showSaveDialog(null)==JFileChooser.APPROVE_OPTION){
					graph.saveGraph(jf.getSelectedFile()+".dijg");
				}
		}
		else if(e.getActionCommand()=="Load File"){
			if(jf.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
				graph.readGraph(jf.getSelectedFile());				
				vPanel.reset();
				vPanel.loadVertices(graph.getVertices());
				activePanel.repaint();
			}
		}
		else if(e.getActionCommand()=="New"){ //transfer later 
			JPanel newGraph = new JPanel(new GridLayout(4,2,2,2));
			JRadioButton unDirected = new JRadioButton("Undirected");
			unDirected.setActionCommand("U");
			unDirected.setSelected(true);
			JRadioButton directed = new JRadioButton("Directed");
			directed.setActionCommand("D");
			JRadioButton unWeighted = new JRadioButton("Unweighted");
			unWeighted.setActionCommand("U");
			unWeighted.setSelected(true);
			JRadioButton weighted = new JRadioButton("Weighted");
			weighted.setActionCommand("W");
			ButtonGroup weight = new ButtonGroup();
			weight.add(unWeighted);
			weight.add(weighted);
			ButtonGroup direct = new ButtonGroup();
			direct.add(unDirected);
			direct.add(directed);

			newGraph.add(new JLabel("Movement"));
			newGraph.add(new JLabel(""));
			newGraph.add(unDirected);
			newGraph.add(directed);
			newGraph.add(new JLabel("Weight"));
			newGraph.add(new JLabel());
			newGraph.add(unWeighted);
			newGraph.add(weighted);

			int i = JOptionPane.showConfirmDialog(null, newGraph, "New Dijkstra Graph",
				 		JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

			if(i==0){
				graph = new DijkstraGraphController(direct.getSelection().getActionCommand(), weight.getSelection().getActionCommand());
				vPanel.reset();
				dPanel = new DrawPanel(this);
				vPanel = new VisualPanel(this);
			}
		}
		else if(e.getActionCommand()=="Exit"){
			System.exit(0);
		}
	}

	public void menuSelected(MenuEvent e){
		if(e.getSource()==runMenu){
			vPanel.loadVertices(graph.getVertices());
			card.show(activePanel, "vPanel");
		}
		else if(e.getSource()==drawMenu){
			vPanel.reset();
		 	card.show(activePanel, "dPanel");
		}
		else if(e.getSource()==aboutMenu){
			vPanel.reset();
			card.show(activePanel, "aPanel");
		}
	}

	public void menuDeselected(MenuEvent e){}

	public void menuCanceled(MenuEvent e){}

	public DijkstraGraphController getGraph(){
		return graph;
	}
	
	public static void main(String[] args){
		EventQueue.invokeLater(new Runnable(){
			public void run(){
				new MainClass().setVisible(true);
			}
		});
	}

	private JMenu aboutMenu, runMenu, drawMenu;
	private CardLayout card;
}
