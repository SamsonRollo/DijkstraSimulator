package UI;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import Graph.DijkstraGraphController;
import Graph.Vertex;
import Graph.Edge;

public class VisualPanel extends JPanel{
	
	private DijkstraGraphController controller;
	private JPanel algoPanel, varPanel, executionPanel;
	private double[] distance;
	private ArrayList<Vertex> vertices;

	public VisualPanel(DijkstraGraphController controller, Dimension size){
		this.controller = controller;
		setLayout(new BorderLayout());
		add(loadTools(size), BorderLayout.NORTH);

		JPanel mainPanel = new JPanel(null);
		add(mainPanel);
		mainPanel.add(loadVVisual(size));
		mainPanel.add(loadVExecution(size));
		setSize((int)size.getWidth(), (int)size.getHeight());
	}

	private JPanel loadVVisual(Dimension d){
		JPanel algoVarPanel= new JPanel(null);
		algoPanel = new JPanel(null){
			@Override
			public void paintComponent(Graphics g){
				try{ //algorithm visual
					BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("src/DijkstraAlgo.text")));
					String line;
					int y = 10;
					while((line=br.readLine())!=null){
						if(currentAlgo!=null && line.contains(currentAlgo))
							g.setColor(Color.red);
						else 
							g.setColor(Color.black);
						g.drawString(line, 15, y+=12);
					}
					br.close();
				}catch(IOException ioe){System.out.println("Unable to read DijkstraAlgo.text");};
			}
		};
		algoPanel.setPreferredSize(new Dimension((int)d.getWidth()+70,(int)d.getHeight()/2-30));
		distance = new double[0];
		vertices = new ArrayList<Vertex>();
		varPanel = new JPanel(null){
			@Override
			public void paintComponent(Graphics g){
				int minW = 48;
				int y = 15;
				int x = 15;
				for(Vertex v : controller.getVertices()){
					if(v.getVertexName().length()*8>minW)
						minW = v.getVertexName().length()*8;
				}

				g.drawString("Vertex", x, y);
				g.drawString("Distance",x+minW+4, y);
				y+=24;

				int i = 0;
				for(Vertex v : controller.getVertices()){
					g.drawString(v.getVertexName(),x,y);
					String dist = "";
					try{
						dist = String.format("%.2f",distance[i]);
					}catch(Exception e){dist="";};
					g.drawString(dist,x+minW+10,y);
					y+=16;
					i++;
				}
				varPanel.setPreferredSize(new Dimension(x+minW+96,y+15));
				try{
					updateUI();
				}catch(Exception e){};
			}
		};
		varPanel.setPreferredSize(new Dimension((int)d.getWidth()/3+30,(int)d.getHeight()/2));

		JScrollPane jspAlgo = new JScrollPane(algoPanel);
		JScrollPane jspVar = new JScrollPane(varPanel);

		jspAlgo.getViewport().addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e){
				//algoPanel.repaint(); //try removing this 
				repaint();
			}
		});

		jspVar.getViewport().addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e){
				//varPanel.repaint(); //try removing this 
				repaint();
			}
		});

		algoVarPanel.add(jspAlgo);
		algoVarPanel.add(jspVar);

		addComponentListener(new ComponentAdapter(){ //listen to resizing of frame
			public void componentResized(ComponentEvent e){
				algoVarPanel.setSize(e.getComponent().getWidth()/3, e.getComponent().getHeight()); //resize drawing panel based on its frame
				jspAlgo.setBounds(1,0,e.getComponent().getWidth()/3, e.getComponent().getHeight()/2-55); //resize drawing panel based on its frame
				jspVar.setBounds(1,e.getComponent().getHeight()/2-55,e.getComponent().getWidth()/3, e.getComponent().getHeight()/2-72);
				algoVarPanel.updateUI();
			}
		});
		return algoVarPanel;
	}

	public void loadVertices(ArrayList<Vertex> vertices){
		updateVariables(vertices, new double[0]);
	}

	private JScrollPane loadVExecution(Dimension d){
		executionPanel = new JPanel(null){
			@Override
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				int farthestX = 300, farthestY = 347;
				for(Edge e: controller.getEdges()){
					if(e.getActivity().equals("active"))
						g.setColor(Color.red);
					else if(e.getActivity().equals("done"))
						g.setColor(Color.blue);
					else
						g.setColor(Color.black);

					//make an arrow if directed
					if(e.getv1().equals(e.getv2())){
						g.drawArc(e.getStartX()-10, e.getStartY()-25, 20,30, 0, 180);
						g.drawString(String.valueOf((int)e.getWeight()), e.getStartX()-10, e.getStartY()-27);
					}
					else{
						g.drawLine(e.getStartX(), e.getStartY(), e.getEndX(), e.getEndY());
						if(controller.isDirected()){
								g.fillPolygon(new int[] {(int)e.getArrow().get(0).getX(), (int)e.getArrow().get(1).getX(), (int)e.getArrow().get(2).getX()},
												new int[] {(int)e.getArrow().get(0).getY(), (int)e.getArrow().get(1).getY(), (int)e.getArrow().get(2).getY()},3);
							}
						g.drawString(String.valueOf((int)e.getWeight()), Math.abs((e.getStartX()+e.getEndX())/2), Math.abs((e.getStartY()+e.getEndY())/2));
					}
				}
				//normal vertex
				BufferedImage img = null;

				for(Vertex v: controller.getVertices()){
					if(v.getEndX()>farthestX)
						farthestX = v.getEndX();
					if(v.getEndY()>farthestY)
						farthestY = v.getEndY();

					try{
						if(v.getActivity().equals("active"))
							img = ImageIO.read(this.getClass().getClassLoader().getResource("src/vertexEval.png"));
						else if(v.getActivity().equals("done"))
							img = ImageIO.read(this.getClass().getClassLoader().getResource("src/vertexEvalDone.png"));
						else
							img = ImageIO.read(this.getClass().getClassLoader().getResource("src/vertex.png"));//source of nrmal vertex
					}catch(IOException ioe){System.out.println("Unable to load some images");};

					if(img!=null)
						g.drawImage(img,v.getStartX()-15, v.getStartY()-15, null);
					else{
						g.setColor(Color.magenta);
						g.fillOval(v.getStartX()-15, v.getStartY()-15,30,30);
					}

					g.setColor(Color.black);
					g.drawString(v.getVertexName(),v.getStartX()+10, v.getStartY()-10);
				}
				executionPanel.setPreferredSize(new Dimension(farthestX+15, farthestY+15));
				try{
					updateUI();
				}catch(Exception e){};
			}
		};

		JScrollPane jspExec = new JScrollPane(executionPanel);
		executionPanel.setBackground(Color.white);

		addComponentListener(new ComponentAdapter(){ //listen to resizing of frame
			public void componentResized(ComponentEvent e){
				jspExec.setBounds(e.getComponent().getWidth()/3, 0,(int)e.getComponent().getWidth()/3*2+3, (int)e.getComponent().getHeight()-128);
			}
		});
		return jspExec;
	}

	private JPanel loadTools(Dimension frameSize){ 
		JPanel drawTools = new JPanel();
		JPanel drawTools2 = new JPanel();
		JSlider speed = new JSlider(0,2000,500);
		ButtonGroup bg = new ButtonGroup();

		allV = new JRadioButton("S-S S-P");
		allV.setActionCommand("A");
		eToE = new JRadioButton("S-P S-P");
		eToE.setActionCommand("E");
		startField = new JTextField(5);
		endField = new JTextField("Disabled", 5);
		startB = new JButton("Start");
		stop = new JButton("Stop");
		resetB = new JButton("Reset");
		allV.setSelected(true);
		endField.setEnabled(false);

		beforeExec();
		
		startB.addActionListener(new ActionListener(){
		 	public void actionPerformed(ActionEvent e){
		 		String start = startField.getText();
		 		String end = endField.getText();
		 		
		 		if(controller.numberOfVertices()>0 && !start.equals("") 
		 			&& ((eToE.isSelected() && !end.equals("")) || allV.isSelected())){
			 		startB.setEnabled(false);
			 		stop.setEnabled(true);
					resetB.setEnabled(true);
					controller.runDijkstra(getVisualPanel(), start, end, bg.getSelection().getActionCommand());
		 		}
		 	}
		});
		stop.addActionListener(new ActionListener(){ //force stop esecution
			public void actionPerformed(ActionEvent e){
				controller.stopExec();
			}
		});
		resetB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				controller.resetExec();
			}
		});
		speed.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e){
				sleepTime = ((JSlider)e.getSource()).getValue();
			}
		});

		bg.add(allV);
		bg.add(eToE);

		allV.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				endField.setEnabled(false);
				endField.setText("Disabled");
			}
		});

		eToE.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				endField.setEnabled(true);
				endField.setText("");
			}
		});

		drawTools2.add(allV);
		drawTools2.add(eToE);
		drawTools2.add(new JLabel("Input Start"));
		drawTools2.add(startField);
		drawTools2.add(new JLabel("End"));
		drawTools2.add(endField);
		drawTools.add(startB);
		drawTools.add(stop);
		drawTools.add(resetB);
		drawTools.add(new JLabel("Speed"));
		drawTools.add(speed);

		JPanel jtb = new JPanel(new GridLayout(0,1));
		jtb.add(drawTools);
		jtb.add(drawTools2);

		return jtb;
	}

	public void reportResult(String[][] outputs, String start){
		String[] col = {"Vertex","Distance from "+start, "Path"};
		JTable table = new JTable(outputs, col){
			@Override
			public boolean isCellEditable(int x, int y){
				return false;
			}
		};
		JScrollPane sp = new JScrollPane(table);
		sp.setPreferredSize(new Dimension(500,300));
		JOptionPane.showMessageDialog(null,sp, "Results", JOptionPane.PLAIN_MESSAGE);
	}

	@SuppressWarnings("deprecation")
	public void stopExecution(){
		try{
			controller.getDijkstra().stop();
		}catch(Exception e){};
		controller.resetActivity();
	}

	public void reset(){
		stopExecution();
		beforeExec();
		refresh();
		updateVariables(controller.getVertices(), new double[0]);
		enableBottom();
		startField.setText("");
		endField.setText("Disabled");
		endField.setEnabled(false);
		allV.setSelected(true);
	}

	public void refresh(){
		currentAlgo = null;
		algoPanel.repaint();
		executionPanel.repaint();
	}

	public void refreshAlgoScreen(String currentAlgo, double[] distance){
		this.currentAlgo = currentAlgo;
		try{
			algoPanel.repaint();
		}catch(Exception e){};
		updateVariables(controller.getVertices(), distance);
	}

	public void updateVariables(ArrayList<Vertex> vertices, double[] distance){
		this.distance = distance;
		this.vertices = vertices;
		try{
			varPanel.repaint();
			repaint();
		}catch(Exception e){};
	}

	public JPanel getPanel(){
		return this;
	}

	public VisualPanel getVisualPanel(){
		return this;
	}

	public int getSleepTime(){
		return sleepTime;
	}

	public void doneExec(){
		startB.setEnabled(false);	
		stop.setEnabled(false);
		resetB.setEnabled(true);
	}

	public void beforeExec(){
		startB.setEnabled(true);	
		stop.setEnabled(false);
		resetB.setEnabled(false);	
	}

	public void disableBottom(){
		eToE.setEnabled(false);
		allV.setEnabled(false);
		startField.setEnabled(false);
		endField.setEnabled(false);
	}

	public void enableBottom(){
		eToE.setEnabled(true);
		allV.setEnabled(true);
		startField.setEnabled(true);
		if(eToE.isSelected())
			endField.setEnabled(true);
		else
			endField.setEnabled(false);
	}

	private GridLayout gl;
	private int sleepTime = 500;
	private JButton startB, stop, resetB;
	private String currentAlgo= null;
	private JTextField startField, endField;
	private JRadioButton allV, eToE;
}
