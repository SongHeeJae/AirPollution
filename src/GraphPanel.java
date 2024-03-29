import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;


public abstract class GraphPanel extends JPanel implements MouseMotionListener{
	
	private int pol;
	protected int x, y;
	protected double max; // 세로축 최대치
	protected String start, end; // 시작 종료 기간
	protected List<String> places;
	protected List<Double> datas;
	
	public GraphPanel(String start, String end, int pol) {
		setPreferredSize(new Dimension(950, 650));
		addMouseMotionListener(this);

		this.pol = pol;
		places = new ArrayList<>();
		datas = new ArrayList<>();
		max = 0;
		this.start = start;
		this.end = end;
	}
	
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);

		setBackground(Color.WHITE);
		g.drawLine(50, 40, 50, 590);
		g.drawLine(50, 590, (int)getPreferredSize().getWidth(), 590);
		
		if(pol < 2) g.drawString("(㎍/㎥)", 5, 20);
		else g.drawString("(ppm)", 5, 20); // 단위 그려줌
		
		if(!places.isEmpty()) // 세로축 단위별로 10칸 나눠줌
			for(int i=0; i<11; i++)
				g.drawString(String.format("%.3f", max/10*(10-i)), 5, 50+(55*i)); // 라인 높이 550
		
	}
	
	public void addBarGraph(String place, double value) {
		places.add(place);
		datas.add(value);
		if(max <= value) max = value*2; // 현재 간격보다 최대치면 기준바꿔줌
		setResize();
	}
	
	public void orderGraph(String order) {
		int j;
		for(int i=1; i<datas.size(); i++) {
			double k = datas.get(i);
			String kk = places.get(i);
			if(order.equals("+")) {
				for(j=i-1; j>=0 && datas.get(j) > k; j--) {
					datas.set(j+1, datas.get(j));
					places.set(j+1, places.get(j));
				}
			} else {
				for(j=i-1; j>=0 && datas.get(j) < k; j--) {
					datas.set(j+1, datas.get(j));
					places.set(j+1, places.get(j));
				}
			}
			datas.set(j+1, k);
			places.set(j+1, kk);
		}
		reload();
	}
	
	public void addLineGraph(String place, List<Double> value, double m) {
		places.add(place);
		datas.addAll(value);
		max = m > max ? m : max; // 현재 간격보다 최대치면 기준바꿔줌
		setResize();
	}	

	public abstract void setResize();
	
	public abstract void removeGraph(String place);
	
	public void reload() {
		revalidate();
		repaint();
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
