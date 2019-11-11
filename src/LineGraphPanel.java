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
import java.util.Map;

public class LineGraphPanel extends GraphPanel{

	public LineGraphPanel(String value) {
		super(value);
		datas = new ArrayList<>();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Color[] color = {Color.BLACK, Color.RED, Color.GREEN, Color.YELLOW, Color.BLUE, Color.ORANGE};
		
		((Graphics2D)g).setStroke(new BasicStroke(2));
		for(int i=0; i<places.size(); i++) {
			g.setColor(color[i]);
			int y = (int)(590 - 550/(max/datas.get(i * datas.size()/places.size()))), yy;
			g.fillRect(100, y-2, 4, 4);
			int k = 1;
			for (int j=i * datas.size()/places.size() + 1; j<(i+1) * datas.size() / places.size(); j++) {
				yy = y;
				y = (int)(590 - 550/(max/datas.get(j)));
				g.fillRect((k+1)*100-2, y-2, 4, 4);
				g.drawLine((k)*100, yy, (1+k++)*100, y);
			}
		}
		
		
		if(datas.size() != 0) {
			g.setColor(Color.BLACK);
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			try {
				cal.setTime(sdf.parse(start));
				for(int i=1; i<=datas.size()/places.size(); i++) {
					g.drawString(sdf.format(cal.getTime()), i*100-20, 620);
					cal.add(Calendar.DATE, 1);
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setResize() {
		if(!datas.isEmpty() && datas.size()/places.size() * 100 + 50 > getPreferredSize().getWidth())
			setPreferredSize(new Dimension(datas.size()/places.size() * 100 + 50, 650));
		reload();
	}
	
	public void removeGraph(int pos) {
		int i = pos * datas.size()/places.size(), j = datas.size()/places.size();
		while(j-- > 0) datas.remove(i);
		places.remove(pos);
		setResize();
	}
	
	public void addGraph(String place, List<Double> value) {
		places.add(place);
		datas.addAll(value);
		double m = value.stream().max(Double::compare).orElse(0.0);
		max = m > max ? m : max; // 현재 간격보다 최대치면 기준바꿔줌
		setResize();
	}

	
}
