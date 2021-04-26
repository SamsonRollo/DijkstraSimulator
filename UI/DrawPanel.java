package UI;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;
import Graph.*;
import Exceptions.*;

public class DrawPanel extends JPanel{

	public int currentTool = 0;
	private MainClass dPage;
	private JPanel drawPanel;
	private String selectedElement; //general purpose

	public DrawPanel(MainClass dPage){
		this.dPage = dPage;
		setLayout(new BorderLayout());
		add(loadTools(dPage.getSize()), BorderLayout.NORTH);
		add(loadDrawingPanel(dPage.getSize()));
		setSize((int)dPage.getSize().getWidth(), (int)dPage.getSize().getHeight());
	}

	private JPanel loadDrawingPanel(Dimension frameSize){
		JPanel outerDrawPanel = new JPanel(null);
		drawPanel = new JPanel(null){
			@Override
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				g.setColor(Color.black);
				int farthestX = 469, farthestY = 380;
				for(Edge e: dPage.getGraph().getEdges()){
					//make an arrow if directed
					if(e.getv1().equals(e.getv2())){
						g.drawArc(e.getStartX()-10, e.getStartY()-25, 20,30, 0, 180);

						if(dPage.getGraph().isWeighted())
							g.drawString(String.valueOf((int)e.getWeight()), e.getStartX()-10, e.getStartY()-27);
					}
					else{
						if(!isCarrying  || (isCarrying && e.getStartX()!=(int)initPoint.getX() && e.getStartY()!=(int)initPoint.getY())){
							g.drawLine(e.getStartX(), e.getStartY(), e.getEndX(), e.getEndY());
							if(dPage.getGraph().isDirected()){
								g.fillPolygon(new int[] {(int)e.getArrow().get(0).getX(), (int)e.getArrow().get(1).getX(), (int)e.getArrow().get(2).getX()},
												new int[] {(int)e.getArrow().get(0).getY(), (int)e.getArrow().get(1).getY(), (int)e.getArrow().get(2).getY()},3);
							}
						}
						
						if(dPage.getGraph().isWeighted())
							g.drawString(String.valueOf((int)e.getWeight()), Math.abs((e.getStartX()+e.getEndX())/2), Math.abs((e.getStartY()+e.getEndY())/2));
					}
				}
				//normal vertex
				boolean usedIMG = true;
				BufferedImage img = null;
				try{
					img = ImageIO.read(this.getClass().getClassLoader().getResource("src/vertex.png"));//source of nrmal vertex
				}catch(IOException ioe){
					usedIMG = false;
					System.out.println("Unable to load some images");
				};

				for(Vertex v: dPage.getGraph().getVertices()){
					if(v.getEndX()>farthestX)
						farthestX = v.getEndX();
					if(v.getEndY()>farthestY)
						farthestY = v.getEndY();
					if(usedIMG)
						g.drawImage(img,v.getStartX()-15, v.getStartY()-15, null);
					else{
						g.setColor(Color.magenta);
						g.fillOval(v.getStartX()-15, v.getStartY()-15,30,30);
					}

					g.setColor(Color.black);
					g.drawString(v.getVertexName(),v.getStartX()+15, v.getStartY()-5);
				}
				drawPanel.setPreferredSize(new Dimension(farthestX+30, farthestY+30));
				updateUI();
			}
		};
		drawPanel.setBackground(Color.white);

		drawPanel.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e){
				if(currentTool==1){ //add vertex
					if(vertexIsNear(e.getX(),e.getY()))
						return;
					selectedElement = null;
					selectedElement = JOptionPane.showInputDialog("Enter vertex name");
					if(selectedElement!=null){
						addVertex(e.getX(),e.getY(), selectedElement, 0);
						selectedElement = null;
					}
				}
				else if(currentTool==2){ //add edge
					if(selectedElement==null){ //get first vertex
						selectedElement = overVertex(e.getX(), e.getY());
					}else{ //if first vertex is okay, proceed here
						String eV = overVertex(e.getX(), e.getY());
						if(eV!=null){
							String weight = "";

							if(dPage.getGraph().isWeighted())
								weight = JOptionPane.showInputDialog("Enter weight or leave blank to automatically compute weight");
							if(weight!=null){
								try{
									if(!weight.equals(""))
										Double.parseDouble(weight);
									addEdge(selectedElement, eV, weight);
								}catch(NumberFormatException ne){new ErrorReport("Invalid Weight", "Add Error");};
							}
						}
						selectedElement = null; //after adding edge, either success or fail, it ill reset initial vertex
					}
				}
				else if(currentTool==3){ //delete function
					selectedElement = null;
					selectedElement = overVertex(e.getX(), e.getY());
					if(selectedElement!=null){ //if vertex
						deleteVertex(selectedElement);
						selectedElement = null;
						return;
					}
					selectedElement = overEdge(e.getX(), e.getY());
					if(selectedElement!=null){ //if edge
						deleteEdge(selectedElement);
						selectedElement = null;
						return;
					}
				}
			}
			public void mouseRelease(MouseEvent e){}
			public void mouseDragged(MouseEvent e){}

			public void mouseClicked(MouseEvent e){
				if(e.getClickCount()==2 && currentTool==0){//editing
					selectedElement = null; //reset selectedElement
					selectedElement = overVertex(e.getX(), e.getY());
					
					if(selectedElement!=null){
						String newName = JOptionPane.showInputDialog("Enter new vertex name");
						if(newName!=null)
							renameVertex(selectedElement, newName);
					}
					else{
						selectedElement = overEdge(e.getX(), e.getY());

						if(!dPage.getGraph().isWeighted() || selectedElement==null)
							return;
						
						String weight = JOptionPane.showInputDialog("Enter new weight");
						if(selectedElement!=null && (weight!=null || !weight.equals(""))){
							changeWeight(selectedElement, weight);
						}
					}
					selectedElement = null;
				}
				else if(currentTool==4){//moving
					if(!isCarrying){
						selectedElement = null;
						selectedElement = overVertex(e.getX(), e.getY());
					}

					if(selectedElement!=null && !isCarrying){ //pick up
						moveVertex(selectedElement);
					}
					else if(selectedElement!=null && isCarrying){ //put down
						dropVertex();
						selectedElement = null;
					}
				}
			}
		});

		drawPanel.addMouseMotionListener(new MouseAdapter(){
			public void mouseMoved(MouseEvent e){
				if(currentTool==4 && isCarrying){
					currentPoint = new Point(e.getX(),e.getY());
					dPage.getGraph().setArbitraryPosition(selectedElement, currentPoint);
				}
			}
		});

		JScrollPane jspDraw = new JScrollPane(drawPanel);

		addComponentListener(new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				jspDraw.setBounds(0,0,(int)e.getComponent().getWidth(), (int)e.getComponent().getHeight()-98);
			}
		});
		outerDrawPanel.add(jspDraw);
		return outerDrawPanel;
	}

	private JPanel loadTools(Dimension frameSize){ // change to null and find out why no show on north borderlayout
		JPanel drawTools = new JPanel();
		JButton move = new JButton();
		JButton select = new JButton();
		JButton vertex = new JButton();
		JButton edges = new JButton();
		JButton delete = new JButton();
		try{
			move.setIcon(new ImageIcon(ImageIO.read(this.getClass().getClassLoader().getResource("src/moveIcon.png"))));
		}catch(Exception e){
			move.setText("M");
			System.out.println("Unable to load moveIcon.png");
		};
		try{
			select.setIcon(new ImageIcon(ImageIO.read(this.getClass().getClassLoader().getResource("src/selectIcon.png"))));
		}catch(Exception e){
			select.setText("S");
			System.out.println("Unable to load selectIcon.png");
		};
		try{
			vertex.setIcon(new ImageIcon(ImageIO.read(this.getClass().getClassLoader().getResource("src/vertexIcon.png"))));
		}catch(Exception e){
			vertex.setText("V");
			System.out.println("Unable to load vertexIcon.png");
		};
		try{
			edges.setIcon(new ImageIcon(ImageIO.read(this.getClass().getClassLoader().getResource("src/edgeIcon.png"))));
		}catch(Exception e){
			edges.setText("E");
			System.out.println("Unable to load edgeIcon.png");
		};
		try{
			delete.setIcon(new ImageIcon(ImageIO.read(this.getClass().getClassLoader().getResource("src/deleteIcon.png"))));
		}catch(Exception e){
			delete.setText("D");
			System.out.println("Unable to load deleteIcon.png");
		};

		select.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				currentTool = 0;
			}
		});
		vertex.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				currentTool = 1;
			}
		});
		edges.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				currentTool = 2;
			}
		});
		delete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				currentTool = 3;
			}
		});
		move.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				currentTool = 4;
			}
		});
		drawTools.add(select);
		drawTools.add(move);
		drawTools.add(vertex);
		drawTools.add(edges);
		drawTools.add(delete);

		return drawTools;
	}

	public void addVertex(int x, int y, String name, double time){
		dPage.getGraph().addVertex(name, time, x, y);
		refresh();
	}

	public void addEdge(String v1, String v2, String weight){
		dPage.getGraph().addEdge(v1, v2, weight);
		refresh();
	}

	public void deleteVertex(String name){
		dPage.getGraph().removeVertex(name);
		refresh();
	}

	public void deleteEdge(String name){
		dPage.getGraph().removeEdge(name);
		refresh();
	}

	public void renameVertex(String oldName, String newName){
		if(dPage.getGraph().renameVertex(oldName, newName))
			refresh();
	}

	public void changeWeight(String edge, String weight){
		if(dPage.getGraph().changeWeight(edge, weight));
			refresh();
	}

	public void moveVertex(String verteName){
		int idx = dPage.getGraph().indexRetriever(verteName);
		initPoint = dPage.getGraph().getVertices().get(idx).getPoint();
		isCarrying = true;
	}

	public void dropVertex(){
		if(vertexIsNear((int)currentPoint.getX(),(int)currentPoint.getY()))
			dPage.getGraph().setArbitraryPosition(selectedElement, initPoint);
		else
			dPage.getGraph().setArbitraryPosition(selectedElement, currentPoint);
		isCarrying = false;
		//refresh();
	}

	private String overVertex(int x, int y){ //-15 for the offset
		for(Vertex v: dPage.getGraph().getVertices()){
			Rectangle checkV = new Rectangle(v.getStartX()-15,v.getStartY()-15,30,30);
			if(checkV.contains(new Point(x,y)))
				return v.getVertexName();
		}
		return null;
	}

	private String overEdge(int x, int y){ //use square later for wide range
		for(Edge e : dPage.getGraph().getEdges()){
			if(e.getv2().equals(e.getv1())){ //self loop edge
				if(e.getRectangle().contains(x,y))
					return e.getUniqueID();
			}
			else{
				for(Point p : e.getLine()){
					if((int)p.getX()<= x+2 && (int)p.getX()>= x-2 && (int)p.getY()<=y+2 && (int)p.getY()>=y-2)
						return e.getUniqueID();
				}
			}
		}
		return null;
	}

	private boolean vertexIsNear(int x, int y){
		if(x-20<0 || y-30<0) //beyond top and left
			return true;

		Rectangle newV = new Rectangle(x-15,y-15,30,30);
		for(Vertex v: dPage.getGraph().getVertices()){
			if(isCarrying && v.getVertexName().equals(selectedElement))
				continue;

			Rectangle checkV = new Rectangle(v.getStartX()-15,v.getStartY()-15,30,30);
			if(checkV.intersects(newV))
				return true;
		}
		return false;
	}

	public void refresh(){
		drawPanel.revalidate();
		drawPanel.repaint();
	}

	public JPanel getPanel(){
		return this;
	}

	private boolean isCarrying = false;
	private Point initPoint = null, currentPoint = null;
}
