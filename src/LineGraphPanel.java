import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class LineGraphPanel extends GraphPanel{

	private List<Graph<List<Double>>> datas;
	public LineGraphPanel(String value) {
		super(value);
		datas = new ArrayList<>();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Color[] color = {Color.BLACK, Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE, Color.ORANGE};
		
		((Graphics2D)g).setStroke(new BasicStroke(2));
		for (int i=0; i<datas.size(); i++) {
			g.setColor(color[i]);
			int y = (int)(590 - 550/(max/datas.get(i).getValue().get(0))), yy;
			g.fillRect(100, y-2, 4, 4);
			for (int j=1; j<datas.get(i).getValue().size(); j++) {
				yy = y;
				y = (int)(590 - 550/(max/datas.get(i).getValue().get(j)));
				g.fillRect((j+1)*100-2, y-2, 4, 4);
				g.drawLine((j)*100, yy, (j+1)*100, y);
			}
		}
		
		
		if(datas.size() != 0) {
			g.setColor(Color.BLACK);
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			try {
				cal.setTime(sdf.parse(start));
				for(int i=1; i<=datas.get(0).getValue().size(); i++) {
					g.drawString(sdf.format(cal.getTime()), i*100-20, 620);
					cal.add(Calendar.DATE, 1);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setResize() {
		if(!datas.isEmpty() && datas.get(0).getValue().size() * 100 + 50 > getPreferredSize().getWidth())
			setPreferredSize(new Dimension(datas.get(0).getValue().size() * 100 + 50, 650));
	}
	
	public void removeGraph(int pos) {
		datas.remove(pos);
		setResize();
		reload();
	}
	
	public void addGraph(String place, List<Double> value) {
		datas.add(new Graph<List<Double>>(place, value));
		double m = value.stream().max(Double::compare).orElse(0.0);
		max = m > max ? m : max; // 현재 간격보다 최대치면 기준바꿔줌
		setResize();
		reload();
	}
	
	public void clear() {
		super.clear();
		datas.clear();
		reload();
	}
	
}
