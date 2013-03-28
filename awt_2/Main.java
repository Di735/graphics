import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.sin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;




public class Main {
	public static void main(String[] args) {
		new Thread(null, new Runnable() {
			public void run() {
				try {
					new Main().run();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, "kek", 1 << 23).start();
	}

	void run() throws IOException {
		Locale.setDefault(Locale.US);
		MyFrame h = new MyFrame();
	}
	
	int offset = 100;

	public class MyFrame extends JFrame {
		static final double rotationSpeed = 0.01;
		static final double scaleSpeedBackward = 0.1;
		static final double scaleSpeedFroward = (scaleSpeedBackward) / (1 + scaleSpeedBackward);
		
		private static final long serialVersionUID = 484957602817494458L;
		JFormattedTextField jTFposX, jTFposY, jTFposZ;
		JLabel jLposX, jLposY, jLposZ;
		MyCanvas myCanvas;
		JPanel comboPanel;
		Point prevCursorPos = null;
		JButton jBMove = null;
		MyFrame() {
			super();
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			this.setSize(1200, 800);
			this.setLocation(100, 100);
			this.add(myCanvas = new MyCanvas(), BorderLayout.CENTER);
			myCanvas.addMouseMotionListener(new MouseMotionListener() {
				public void mouseMoved(MouseEvent e) {
					prevCursorPos = myCanvas.getMousePosition();
				}
				@Override
				public void mouseDragged(MouseEvent e) {
					int mask = e.getModifiersEx();
					if((MouseEvent.BUTTON3_DOWN_MASK & mask) == MouseEvent.BUTTON3_DOWN_MASK){
						//rotate
						if(prevCursorPos != null){
							Point cur = myCanvas.getMousePosition();
							int dx = cur.x - prevCursorPos.x;
							int dy = cur.y - prevCursorPos.y;
							myCanvas.figure.translate(dx, dy, 0);
							myCanvas.repaint();
						}
					}
					if((MouseEvent.BUTTON1_DOWN_MASK & mask) == MouseEvent.BUTTON1_DOWN_MASK){
						if(prevCursorPos != null){
							Point cur = myCanvas.getMousePosition();
							
							myCanvas.figure.rotateBetween(prevCursorPos, cur);
							myCanvas.repaint();
						}
					}
					prevCursorPos = myCanvas.getMousePosition();
				}
			});
			myCanvas.addMouseWheelListener(new MouseWheelListener() {
				
				@Override
				public void mouseWheelMoved(MouseWheelEvent e) {
					double w = e.getWheelRotation();
					if(w > 0)
						myCanvas.figure.changeScale(w * scaleSpeedFroward);
					if(w < 0)
						myCanvas.figure.changeScale(w * scaleSpeedBackward);
					myCanvas.repaint();
				}
			});

			comboPanel = new JPanel();
			this.add(comboPanel, BorderLayout.EAST);
			comboPanel.setPreferredSize(new Dimension(340, 600));
			comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.LINE_AXIS));
			comboPanel.setBorder(BorderFactory.createEtchedBorder());
			
		    Box bv = Box.createVerticalBox();
		    bv.add(new JLabel("������������ �������"));
		    bv.add(Box.createVerticalStrut(10));
		    {
			    Box bh = Box.createHorizontalBox();
			    bh.add(Box.createHorizontalGlue());
			    bh.add(new JLabel("delta X"));
			    bh.add(Box.createHorizontalGlue());
			    bh.add(new JLabel("delta Y"));
			    bh.add(Box.createHorizontalGlue());
			    bh.add(new JLabel("delta Z"));
			    bh.add(Box.createHorizontalGlue());
			    bv.add(bh);
		    }
		    bv.add(Box.createVerticalStrut(10));
		    {
				JPanel comboPanel2 = new JPanel();
				comboPanel2.setPreferredSize(new Dimension(340, 200));
				comboPanel2.setLayout(new BoxLayout(comboPanel2, BoxLayout.LINE_AXIS));
				comboPanel2.setBorder(BorderFactory.createEtchedBorder());
				
			    Box bh = Box.createHorizontalBox();
			    bh.add(Box.createHorizontalGlue());
			    bh.add(jTFposX = getJFormattedTextField());
			    bh.add(Box.createHorizontalGlue());
			    bh.add(jTFposY = getJFormattedTextField());
			    bh.add(Box.createHorizontalGlue());
			    bh.add(jTFposZ = getJFormattedTextField());
			    bh.add(Box.createHorizontalGlue());
			    comboPanel2.add(bh);
			    bv.add(comboPanel2);
		    }
		    bv.add(Box.createVerticalStrut(10));
		    {
			    Box bh = Box.createHorizontalBox();
			    bh.add(Box.createHorizontalGlue());
			    bh.add(jBMove = getJButtonMove());
			    bh.add(Box.createHorizontalGlue());
			    bh.add(jBMove = getJButtonClear());
			    bh.add(Box.createHorizontalGlue());
			    bv.add(bh);
		    }

		    bv.add(Box.createVerticalStrut(1000));
		    comboPanel.add(bv);
//			comboPanel.add(box, BorderLayout.SOUTH);
			
			
			comboPanel.repaint();
			this.setTitle("lab work �1");
			setVisible(true);
			this.update(getGraphics());
			this.repaint();
		}
		
		
		
		private javax.swing.JLabel getJLabel(String name, int sx, int sy, int delta) {
			JLabel jLabel = new javax.swing.JLabel();
			jLabel.setBounds(40 + delta, sy + 25, 60, 18);
			jLabel.setText(name);
			return jLabel;
		}

		private JFormattedTextField getJFormattedTextField() {
			NumberFormat fmt = NumberFormat.getInstance(Locale.US);
			fmt.setMaximumFractionDigits(3);
			JFormattedTextField cur = new JFormattedTextField(fmt);
			cur.setValue(0);
			cur.addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					JFormattedTextField cur = (JFormattedTextField) e.getComponent();
					String s = cur.getText();
					if(s == null || s.length() == 0)
						cur.setText("0");
					
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					JFormattedTextField cur = (JFormattedTextField) e.getComponent();
					cur.setText("");
				}
			});
			
			return cur;
		}

		private javax.swing.JButton getJButtonMove () {
			JButton jButton = new javax.swing.JButton();
			jButton.setText("�����������");
			jButton.setPreferredSize(new Dimension(120, 27));
			jButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(jTFposX.getValue() == null) jTFposX.setValue(0);
					if(jTFposY.getValue() == null) jTFposY.setValue(0);
					if(jTFposZ.getValue() == null) jTFposZ.setValue(0);
					
					if(jTFposX.getValue() == null || jTFposY.getValue() == null || jTFposZ.getValue() == null){
						final JFrame jFDI = new JFrame();
						Point cPos = MouseInfo.getPointerInfo().getLocation();
						jFDI.setSize(240, 200);
						jFDI.setLocation(new Point(max(0, cPos.x - 120), max(0, cPos.y - 140)));
						jFDI.setResizable(false);
						JPanel jDI = new JPanel();
						jFDI.add(jDI, BorderLayout.CENTER);
						jDI.setLayout(new BoxLayout(jDI, BoxLayout.LINE_AXIS));
						jDI.setBorder(BorderFactory.createEtchedBorder());
						
						Box bv = Box.createVerticalBox();
						bv.add(Box.createVerticalGlue());
						{
							Box bh = Box.createHorizontalBox();
							bh.add(Box.createHorizontalGlue());
							JLabel jla = new JLabel("���������� ������ ��� ����������");
							jla.setVisible(true);
							bh.add(jla);
							bh.add(Box.createHorizontalGlue());
							bv.add(bh);
						}
						bv.add(Box.createVerticalGlue());
						{
							Box bh = Box.createHorizontalBox();
							bh.add(Box.createHorizontalGlue());
							JButton jbt = new JButton();
							jbt.setText("OK");
							jbt.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									jFDI.dispose();
								}
							});
							bh.add(jbt);
							bh.add(Box.createHorizontalGlue());
							bv.add(bh);
						}
						bv.add(Box.createVerticalGlue());
						jDI.add(bv);
						jDI.setVisible(true);
						jFDI.setVisible(true);
					} else {
						double dx, dy, dz;
						try {
							dx = Double.parseDouble(jTFposX.getValue().toString());
							dy = Double.parseDouble(jTFposY.getValue().toString());
							dz = Double.parseDouble(jTFposZ.getValue().toString());
						} catch(NumberFormatException exc){
							System.err.println(exc);
							return;
						}
//						System.err.println(jTFposX.getValue().toString() + " " + jTFposY.getValue() + " " + jTFposZ.getValue());
						myCanvas.figure.translate(dx, dy, dz);
						myCanvas.repaint();
						
					}
				}
			});
			return jButton;
		}
		
		private javax.swing.JButton getJButtonClear () {
			JButton jButton = new javax.swing.JButton();
			jButton.setText("��������");
			jButton.setPreferredSize(new Dimension(120, 27));
			jButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					jTFposX.setValue(0);
					jTFposY.setValue(0);
					jTFposZ.setValue(0);
				}
			});
			return jButton;
		}
	}
	
	public class MyCanvas extends JPanel{
		private static final long serialVersionUID = 3133363063873958726L;
		MyPolyhedron figure, coords;
		public MyCanvas() {
			super();
			this.setBorder(BorderFactory.createEtchedBorder());
			this.setPreferredSize(new Dimension(100, 50));
			
			figure = new MyPolyhedron();
			for(int i = 0; i < 6; i++){
				double x = 50 * cos(i * PI / 3);
				double y = 50 * sin(i * PI / 3);
				figure.addPoint(i, x, y, -100);
				figure.addPoint(i + 6, x, y, 100);
			}
			for(int i = 0; i < 6; i++)
				figure.addConvexFlat(i, i, (i + 5) % 6, 6 + (i + 5) % 6, 6 + i);
			
//			figure.addConvexFlat(0, 0, 1, 7, 6);
			
			for(int i = 0; i < 3; i++){
				double x = 20 * cos(2 * i * PI / 3);
				double y = 20 * sin(2 * i * PI / 3);
				figure.addPoint(i + 12, x, y, 60);
			}
			for(int i = 0; i < 3; i++){
				figure.addConvexFlat(6 + i * 3, 12 + (i + 1) % 3, 12 + i, 6 + (i * 2 + 1) % 6);
				figure.addConvexFlat(6 + i * 3 + 1,12 + (i + 1) % 3, 6 + (i * 2 + 1) % 6, 6 + (i * 2 + 2) % 6);
				figure.addConvexFlat(6 + i * 3 + 2,12 + i, 6 + (i * 2) % 6, 6 + (i * 2 + 1) % 6);
				
			}
//			figure.addConvexFlat(15, 12, 13, 14);
			for(int i = 0; i < 6; i++){
				figure.addEdge(i, (i + 1) % 6);			//top
				figure.addEdge(6 + i, 6 + (i + 1) % 6);	//bottom
				figure.addEdge(i, 6 + i);	//vertical
				if(i % 2 == 0){
					figure.addEdge(6 + i, 12 + i / 2);
					figure.addEdge(6 + (i + 1) % 6, 12 + i / 2);
					figure.addEdge(6 + (i + 5) % 6, 12 + i / 2);
					figure.addEdge(12 + i / 2, 12 + ((i + 2) / 2) % 3);
				}
			}
//			coords = new MyPolyhedron();
//			coords.addPoint(0, 10, 0, 0);
			figure.addPoint(120, 0, 0, -MyPolyhedron.controlShellRange);
			figure.addPoint(121, 0, 1, -MyPolyhedron.controlShellRange);
			figure.addEdge(120, 121);
			
			
		}
		
		public void paint(Graphics gg){
			super.paint(gg);
			Graphics2D g = (Graphics2D) gg;
			int sx = 60, sy = 60, size = 40;
			g.setColor(Color.black);
			g.drawLine(sx, sy, sx + size, sy);
			g.drawString("X", sx + size, sy - 2);

			g.drawLine(sx, sy, sx, sy + size);
			g.drawString("Y", sx + 2, sy + size);
			

			g.drawLine(sx, sy, (int)(sx + size * cos(PI / 6)), (int)(sy - size * sin(PI / 6)));
			g.drawString("Z", (int)(sx + size * cos(PI / 6) - 6), (int)(sy - size * sin(PI / 6) - 2));
			
			
			figure.paint(g);
		}
	}
}
