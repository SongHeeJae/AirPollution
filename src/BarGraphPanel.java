import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class BarGraphPanel extends GraphPanel {

	//List<Graph<Double>> datas;
	
	public BarGraphPanel(String value) {
		super(value);
		datas = new ArrayList<>();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		for (int i=0; i<datas.size(); i++) {
			int y = (int)(590 - 550/(max/datas.get(i)));
			g.drawRect((i+1)*100, y, 50, 590-y);
			g.drawString(places.get(i), (i+1)*100, 620);
		}
	}
	
	public void addGraph(String place, double value) {
		places.add(place);
		datas.add(value);
		
		if(max <= value) max = value*2; // 현재 간격보다 최대치면 기준바꿔줌
		
		setResize();
	}
	
	public void setResize() {
		if(datas.size() * 125 + 50 > getPreferredSize().getWidth())
			setPreferredSize(new Dimension((int)getPreferredSize().getWidth() + 125, 650));
		reload();
	}
	
	
	public void removeGraph(int pos) {
		datas.remove(pos);
		setResize();
	}
	

}
