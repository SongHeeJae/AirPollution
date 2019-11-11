import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class BarGraphPanel extends GraphPanel {

	List<Graph<Double>> datas;
	
	public BarGraphPanel(String value) {
		super(value);
		datas = new ArrayList<>();
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		for (int i=0; i<datas.size(); i++) {
			int y = (int)(590 - 550/(max/datas.get(i).getValue()));
			g.drawRect((i+1)*100, y, 50, 590-y);
			g.drawString(datas.get(i).getPlace(), (i+1)*100, 620);
		}
	}
	
	public void addGraph(String place, double value) {
		datas.add(new Graph<Double>(place, value));
		
		if(max <= value) max = value*2; // ���� ���ݺ��� �ִ�ġ�� ���عٲ���
		
		setResize();
		reload();
	}
	
	public void setResize() {
		if(datas.size() * 125 + 50 > getPreferredSize().getWidth())
			setPreferredSize(new Dimension((int)getPreferredSize().getWidth() + 125, 650));
	}
	
	
	public void removeGraph(int pos) {
		datas.remove(pos);
		setResize();
		reload();
	}
	
	public void clear() {
		super.clear();
		datas.clear();
		reload();
	}
}
