import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.JPanel;

class Graph<T> {
	private String place;
	private T value;
	Graph (String place, T value) {
		this.place = place;
		this.value = value;
	}
	public String getPlace() {
		return place;
	}
	public T getValue() {
		return value;
	}
}

public abstract class GraphPanel extends JPanel implements MouseMotionListener{
	
	private String pol; // 오염물질 종류
	private int x, y;
	protected double max; // 세로축 최대치
	protected String start, end; // 시작 종료 기간
	// List<Graph> datas;
	public GraphPanel() {
	}
	
	public GraphPanel(String pol) {
		setPreferredSize(new Dimension(950, 650));
		addMouseMotionListener(this);
		this.pol = pol;
		max = 0;
		start = "99999999";
		end = "00000000";
	}
	
	public void setDuration(String start, String end) {
		this.start = Integer.parseInt(start) < Integer.parseInt(this.start) ? start : this.start;
		this.end = Integer.parseInt(end) > Integer.parseInt(this.end) ? end : this.end;
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		setBackground(Color.WHITE);
		g.drawLine(50, 40, 50, 590);
		g.drawLine(50, 590, (int)getPreferredSize().getWidth(), 590);
		
		if(pol.equals("미세먼지(㎍/㎥)") || pol.equals("초미세먼지(㎍/㎥)")) g.drawString("(㎍/㎥)", 5, 20);
		else g.drawString("(ppm)", 5, 20);
		
		if(max != 0) {
			for(int i=0; i<11; i++)
				g.drawString(String.format("%.3f", max/10*(10-i)), 5, 50+(55*i)); // 라인 높이 550
			if(50 <= x && x <= (int)getPreferredSize().getWidth() && 40 <= y && y <= 590) {
				g.drawLine(x,40,x, 590);
				g.drawLine(50,y,(int)getPreferredSize().getWidth(), y);
				g.drawString(String.format("%.3f", (max/550)*(590-y)), x+5, y-20);
			}
		}
	}
	
	public void addGraph(String place, double value) {}
	
	public void addGraph(String place, List<Double> value) {}
	
	public abstract void setResize();
	
	public abstract void removeGraph(int pos);
	
	public void reload() {
		revalidate();
		repaint();
	}
	
	public void clear() {
		setPreferredSize(new Dimension(950, 650));
		max = 0;
	}

	
	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		x = arg0.getX();
		y = arg0.getY();
		repaint();	
	}
	
}
