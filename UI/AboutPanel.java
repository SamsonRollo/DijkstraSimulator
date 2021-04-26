package UI;

import javax.swing.*;
import java.awt.event.*;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;

public class AboutPanel extends JPanel{

	JPanel panel;
	
	public AboutPanel(){
		setLayout(new BorderLayout());
		JPanel outer = new JPanel(null);
		panel = new JPanel(null){
			@Override
			public void paintComponent(Graphics g){
				int x = 20, y = 20;
				try{
					BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("src/about.txt")));
					String line;
					while((line=br.readLine())!=null){
						if(line.startsWith("//"))
							continue;
						g.drawString(line, x, y+=15);
					}
					br.close();
				}catch(Exception e){System.out.println("Cannot read about file");};

				panel.setPreferredSize(new Dimension(x+600, y+40));
				updateUI();
			}
		};

		JScrollPane jspA = new JScrollPane(panel);
		addComponentListener(new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				jspA.setBounds(0,0,(int)e.getComponent().getWidth(),(int)e.getComponent().getHeight()-57);
				setSize(e.getComponent().getWidth(),e.getComponent().getHeight());
			}
		});
		outer.add(jspA);
		add(outer);
		repaint();
	}
}
